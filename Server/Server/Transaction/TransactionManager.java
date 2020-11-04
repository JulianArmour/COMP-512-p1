package Server.Transaction;

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
  private final ConcurrentMap<Integer, ConcurrentHashMap<Integer, CarData>> carDataCopies;
  private final ConcurrentMap<Integer, ConcurrentHashMap<Integer, RoomData>> roomDataCopies;
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
    //TODO: add code to abort a transaction.
    activeTransactions.remove(transactionId);
    abortedTransactions.add(transactionId);
  }

  /**
   * @param xid       transaction id
   * @param flightNum flight number to lock on
   * @return true if transaction with id is permitted to write to the flight. False otherwise.
   */
  public boolean beginFlightWrite(int xid, int flightNum) {
    boolean success = false;
    try {
      success = lockManager.Lock(xid, "flight-" + flightNum, TransactionLockObject.LockType.LOCK_WRITE);
      // get and store data state in case of abort
      int seats = flightRM.queryFlight(xid, flightNum);
      int price = flightRM.queryFlightPrice(xid, flightNum);
      flightDataCopies.get(xid).putIfAbsent(flightNum, new FlightData(flightNum, seats, price));
    } catch (DeadlockException e) {
      System.out.println("TransactionManager:: Could not acquire flight " + flightNum + " lock on transaction " + xid);
      try {
        abort(xid);
      } catch (InvalidTransaction invalidTransaction) {
        System.out.println("TransactionManager::beginFlightWrite:: Could not abort transaction: invalid transaction " +
                           "id");
      }
      return false;
    } catch (RemoteException e) {
      System.out.println("TransactionManager::beginFLightWrite:: Could not retrieve flight " + flightNum + " data on " +
                         "transaction" + xid);
    }
    return success;
  }

  public boolean beginFlightRead(int xid, int flightNum) {
    //TODO
  }

}