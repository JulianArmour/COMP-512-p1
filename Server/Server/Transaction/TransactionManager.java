package Server.Transaction;

import Server.Interface.IResourceManager;
import Server.LockManager.DeadlockException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionManager {
  private final IResourceManager flightRM;
  private final IResourceManager carRM;
  private final IResourceManager roomRM;
  private final AtomicInteger transactionIdCounter;
  private final LockManager lockManager;
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
    this.abortedTransactions = Collections.synchronizedSet(new HashSet<>());
    this.activeTransactions = Collections.synchronizedSet(new HashSet<>());
  }

  public int startTransaction() {
    final int xid = transactionIdCounter.incrementAndGet();
    activeTransactions.add(xid);
    return xid;
  }

  public synchronized boolean commit(int transactionId) throws TransactionAborted, InvalidTransaction, RemoteException {
    if (abortedTransactions.contains(transactionId))
      throw new TransactionAborted(transactionId, "This transaction tried to commit but was previously aborted");
    if (!activeTransactions.contains(transactionId))
      throw new InvalidTransaction(transactionId, "This transaction was already committed");
    activeTransactions.remove(transactionId);
    flightRM.commit(transactionId);
    carRM.commit(transactionId);
    roomRM.commit(transactionId);
    lockManager.UnlockAll(transactionId);
    return true;
  }

  public synchronized void abort(int transactionId) throws InvalidTransaction, RemoteException {
    if (!activeTransactions.contains(transactionId))
      throw new InvalidTransaction(transactionId, "Cannot abort an inactive transaction");
    activeTransactions.remove(transactionId);
    abortedTransactions.add(transactionId);
    flightRM.abort(transactionId);
    carRM.abort(transactionId);
    roomRM.abort(transactionId);
    lockManager.UnlockAll(transactionId);
  }


  /**
   * @param transactionId the transaction id
   * @param key flight id, car location, or room location
   * @param resourceType "flight", "car", or "room"
   * @param lockType read or write lock
   * @return true on success
   * @throws TransactionAborted
   * @throws InvalidTransaction
   * @throws RemoteException
   */
  public boolean beginLock(int transactionId, String key, String resourceType,
                           TransactionLockObject.LockType lockType) throws TransactionAborted, InvalidTransaction, RemoteException {
    if (!activeTransactions.contains(transactionId))
      throw new InvalidTransaction(transactionId, "Transaction is not active");
    try {
      return lockManager.Lock(transactionId, resourceType + "-" + key, lockType);
    } catch (DeadlockException deadlockException) {
      System.out.println("TransactionManager:: could not acquire " + resourceType + " " + key + " lock on transaction" +
                         " " + transactionId);
      abort(transactionId);
      throw new TransactionAborted(transactionId, "Another transaction already has a " + lockType.toString() + " on"
                                                  + resourceType + " " + key);
    }
  }

}
