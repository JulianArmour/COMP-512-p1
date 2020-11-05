package Server.Transaction;

import Server.Common.Flight;
import Server.Interface.IResourceManager;
import Server.LockManager.DeadlockException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionManager {
  private final IResourceManager flightRM;
  private final IResourceManager carRM;
  private final IResourceManager roomRM;
  private final AtomicInteger transactionIdCounter;
  private final LockManager lockManager;
  private final ConcurrentMap<Integer, ConcurrentHashMap<Integer, FlightData>> flightDataCopies;
  private final ConcurrentMap<Integer, ConcurrentHashMap<String, CarData>> carDataCopies;
  private final ConcurrentMap<Integer, ConcurrentHashMap<String, RoomData>> roomDataCopies;
  private final Set<Integer> abortedTransactions;
  private final Set<Integer> activeTransactions;

  private static class FlightData {
    final int flightId;
    final int nSeats;
    final int price;

    public FlightData(int flightId, int nSeats, int price) {
      this.flightId = flightId;
      this.nSeats = nSeats;
      this.price = price;
    }
  }

  private static class CarData {
    final String location;
    final int count;
    final int price;

    public CarData(String location, int count, int price) {
      this.location = location;
      this.count = count;
      this.price = price;
    }
  }

  private static class RoomData {
    final String location;
    final int count;
    final int price;

    public RoomData(String location, int count, int price) {
      this.location = location;
      this.count = count;
      this.price = price;
    }
  }

  public TransactionManager(IResourceManager flightResourceManager, IResourceManager carResourceManager, IResourceManager roomResourceManager) {
    this.flightRM = flightResourceManager;
    this.carRM = carResourceManager;
    this.roomRM = roomResourceManager;
    this.transactionIdCounter = new AtomicInteger(0);
    this.lockManager = new LockManager();
    this.flightDataCopies = new ConcurrentHashMap<>();
    this.carDataCopies = new ConcurrentHashMap<>();
    this.roomDataCopies = new ConcurrentHashMap<>();
    this.abortedTransactions = Collections.synchronizedSet(new HashSet<>());
    this.activeTransactions = Collections.synchronizedSet(new HashSet<>());
  }

  public int startTransaction() {
    final int xid = transactionIdCounter.incrementAndGet();
    flightDataCopies.put(xid, new ConcurrentHashMap<>());
    carDataCopies.put(xid, new ConcurrentHashMap<>());
    roomDataCopies.put(xid, new ConcurrentHashMap<>());
    activeTransactions.add(xid);
    return xid;
  }

  public synchronized boolean commit(int transactionId) throws TransactionAborted, InvalidTransaction {
    if (abortedTransactions.contains(transactionId))
      throw new TransactionAborted(transactionId, "This transaction tried to commit but was previously aborted");
    if (!activeTransactions.contains(transactionId))
      throw new InvalidTransaction(transactionId, "This transaction was already committed");
    lockManager.UnlockAll(transactionId);
    return true;
  }

  public synchronized void abort(int transactionId) throws InvalidTransaction {
    if (!activeTransactions.contains(transactionId))
      throw new InvalidTransaction(transactionId, "Cannot abort an inactive transaction");
    activeTransactions.remove(transactionId);
    abortedTransactions.add(transactionId);

    ConcurrentMap<Integer, FlightData> flightCopies = flightDataCopies.remove(transactionId);
    ConcurrentMap<String, CarData> carCopies = carDataCopies.remove(transactionId);
    ConcurrentMap<String, RoomData> roomCopies = roomDataCopies.remove(transactionId);
    flightCopies.values().forEach(flightData -> restoreFlightData(transactionId, flightData));
    carCopies.values().forEach(carData -> restoreCarData(transactionId, carData));
    roomCopies.values().forEach(roomData -> restoreRoomData(transactionId, roomData));
  }

  private void restoreFlightData(int transactionId, FlightData flightData) {
    try {
      flightRM.setFlight(transactionId, flightData.flightId, flightData.nSeats, flightData.price);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void restoreCarData(int transactionId, CarData carData) {
    try {
      carRM.setCars(transactionId, carData.location, carData.count, carData.price);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  private void restoreRoomData(int transactionId, RoomData roomData) {
    try {
      roomRM.setRooms(transactionId, roomData.location, roomData.count, roomData.price);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
  }

  /**
   * @param xid       transaction id
   * @param flightNum flight number to lock on
   * @return true if transaction with id is permitted to write to the flight. False otherwise.
   */
  public boolean beginFlightWrite(int xid, int flightNum) throws TransactionAborted, InvalidTransaction {
    if (!activeTransactions.contains(xid))
      throw new InvalidTransaction(xid, "Transaction is not active");
    try {
      if (!lockManager.Lock(xid, "flight-" + flightNum, TransactionLockObject.LockType.LOCK_WRITE))
        return false;
      // get and store data state in case of abort
      int seats = flightRM.queryFlight(xid, flightNum);
      int price = flightRM.queryFlightPrice(xid, flightNum);
      flightDataCopies.get(xid).putIfAbsent(flightNum, new FlightData(flightNum, seats, price));
      return true;
    } catch (DeadlockException e) {
      System.out.println("TransactionManager:: Could not acquire flight " + flightNum + " lock on transaction " + xid);
      abort(xid);
      throw new TransactionAborted(xid, "Another transaction already has a write lock on flight " + flightNum);
    } catch (RemoteException e) {
      System.out.println("TransactionManager::beginFlightWrite:: Could not retrieve flight " + flightNum + " data on " +
                         "transaction" + xid);
    } catch (NullPointerException e) {// this is for flightDataCopies.get(xid)
      throw new TransactionAborted(xid, "Transaction was aborted");
    }
    return false;
  }

  public boolean beginFlightRead(int xid, int flightNum) throws TransactionAborted, InvalidTransaction {
    if (!activeTransactions.contains(xid))
      throw new InvalidTransaction(xid, "Transaction is not active");
    try {
      return lockManager.Lock(xid, "flight-" + flightNum, TransactionLockObject.LockType.LOCK_READ);
    } catch (DeadlockException e) {
      System.out.println("TransactionManager:: Could not acquire flight " + flightNum + " lock on transaction " + xid);
      abort(xid);
      throw new TransactionAborted(xid, "Another transaction already has a write lock on flight " + flightNum);
    }
  }

  public boolean beginCarWrite(int xid, String location) throws TransactionAborted, InvalidTransaction {
    if (!activeTransactions.contains(xid))
      throw new InvalidTransaction(xid, "Transaction is not active");
    try {
      if (!lockManager.Lock(xid, "car-" + location, TransactionLockObject.LockType.LOCK_WRITE))
        return false;
      // get and store data state in case of abort
      int seats = carRM.queryCars(xid, location);
      int price = carRM.queryCarsPrice(xid, location);
      carDataCopies.get(xid).putIfAbsent(location, new CarData(location, seats, price));
      return true;
    } catch (DeadlockException e) {
      System.out.println("TransactionManager:: Could not acquire cars " + location + " lock on transaction " + xid);
      abort(xid);
      throw new TransactionAborted(xid, "Another transaction already has a write lock on cars " + location);
    } catch (RemoteException e) {
      System.out.println("TransactionManager::beginCarWrite:: Could not retrieve cars " + location + " data on " +
                         "transaction" + xid);
    } catch (NullPointerException e) {// this is for carDataCopies.get(xid)
      throw new TransactionAborted(xid, "Transaction was aborted");
    }
    return false;
  }

  public boolean beginCarRead(int xid, String location) throws TransactionAborted, InvalidTransaction {
    if (!activeTransactions.contains(xid))
      throw new InvalidTransaction(xid, "Transaction is not active");
    try {
      return lockManager.Lock(xid, "car-" + location, TransactionLockObject.LockType.LOCK_READ);
    } catch (DeadlockException e) {
      System.out.println("TransactionManager:: Could not acquire car " + location + " lock on transaction " + xid);
      abort(xid);
      throw new TransactionAborted(xid, "Another transaction already has a write lock on car " + location);
    }
  }


  public boolean beginRoomWrite(int xid, String location) throws TransactionAborted, InvalidTransaction {
    if (!activeTransactions.contains(xid))
      throw new InvalidTransaction(xid, "Transaction is not active");
    try {
      if (!lockManager.Lock(xid, "room-" + location, TransactionLockObject.LockType.LOCK_WRITE))
        return false;
      // get and store data state in case of abort
      int count = roomRM.queryRooms(xid, location);
      int price = carRM.queryRoomsPrice(xid, location);
      roomDataCopies.get(xid).putIfAbsent(location, new RoomData(location, count, price));
      return true;
    } catch (DeadlockException e) {
      System.out.println("TransactionManager:: Could not acquire room at " + location + " lock on transaction " + xid);
      abort(xid);
      throw new TransactionAborted(xid, "Another transaction already has a write lock on rooms at " + location);
    } catch (RemoteException e) {
      System.out.println("TransactionManager::beginRoomWrite:: Could not retrieve rooms at " + location + " data on " +
              "transaction" + xid);
    } catch (NullPointerException e) {// this is for carDataCopies.get(xid)
      throw new TransactionAborted(xid, "Transaction was aborted");
    }
    return false;
  }

  public boolean beginRoomRead(int xid, String location) throws TransactionAborted, InvalidTransaction {
    if (!activeTransactions.contains(xid))
      throw new InvalidTransaction(xid, "Transaction is not active");
    try {
      return lockManager.Lock(xid, "room-" + location, TransactionLockObject.LockType.LOCK_READ);
    } catch (DeadlockException e) {
      System.out.println("TransactionManager:: Could not acquire room at " + location + " lock on transaction " + xid);
      abort(xid);
      throw new TransactionAborted(xid, "Another transaction already has a write lock on room " + location);
    }
  }
}
