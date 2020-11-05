package Server.RMI;

import Server.Interface.IResourceManager;
import Server.Transaction.InvalidTransaction;
import Server.Transaction.TransactionAborted;
import Server.Transaction.TransactionManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

public class RMIMiddleware implements IResourceManager {
  public static final String SERVER_NAME = "Resources";
  private static final String RMI_PREFIX = "group_25_";
  private static IResourceManager flightResourceManager;
  private static IResourceManager carResourceManager;
  private static IResourceManager roomResourceManager;
  private final TransactionManager transactionManager;

  public static void main(String[] args) {
    String flightServer = args[0];
    String carsServer = args[1];
    String roomsServer = args[2];

    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }

    try {
      Registry flightRegistry = LocateRegistry.getRegistry(flightServer, 1099);
      Registry carRegistry = LocateRegistry.getRegistry(carsServer, 1099);
      Registry roomRegistry = LocateRegistry.getRegistry(roomsServer, 1099);
      flightResourceManager = (IResourceManager) flightRegistry.lookup(RMI_PREFIX + "Flights");
      System.out.println("Connected to Flights resource manager.");
      carResourceManager = (IResourceManager) carRegistry.lookup(RMI_PREFIX + "Cars");
      System.out.println("Connected to Cars resource manager.");
      roomResourceManager = (IResourceManager) roomRegistry.lookup(RMI_PREFIX + "Rooms");
      System.out.println("Connected to Rooms resource manager.");
    } catch (RemoteException | NotBoundException e) {
      e.printStackTrace();
      System.exit(1);
    }

    try {
      IResourceManager middleware = (IResourceManager) UnicastRemoteObject.exportObject(new RMIMiddleware(), 0);
      Registry middlewareRegistry = LocateRegistry.getRegistry(1099);
      middlewareRegistry.rebind(RMI_PREFIX + SERVER_NAME, middleware);
      System.out.println("Registered middleware: " + RMI_PREFIX + SERVER_NAME);

      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          middlewareRegistry.unbind(RMI_PREFIX + SERVER_NAME);
          System.out.println("Unregistered middleware.");
        } catch (RemoteException | NotBoundException e) {
          e.printStackTrace();
        }
      }));
    } catch (RemoteException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public RMIMiddleware() {
    this.transactionManager = new TransactionManager(flightResourceManager, carResourceManager, roomResourceManager);
  }

  @Override
  public int start() throws RemoteException {
    return transactionManager.startTransaction();
  }

  @Override
  public boolean commit(int transactionId) throws RemoteException, TransactionAborted, InvalidTransaction {
    return transactionManager.commit(transactionId);
  }

  @Override
  public void abort(int transactionId) throws RemoteException, InvalidTransaction {
    transactionManager.abort(transactionId);
  }

  @Override
  public boolean shutdown() throws RemoteException {
    if (!flightResourceManager.shutdown())
      return false;
    if (!carResourceManager.shutdown())
      return false;
    if (!roomResourceManager.shutdown())
      return false;
    (new Thread(() -> {
      try {
        Thread.sleep(300);
      } catch (InterruptedException ignored) {
      }
      System.exit(0);
    })).start();
    return true;
  }

  @Override
  public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) throws RemoteException, TransactionAborted, InvalidTransaction {
    if (transactionManager.beginFlightWrite(id, flightNum)) {
      return flightResourceManager.addFlight(id, flightNum, flightSeats, flightPrice);
    }
    return false;
  }

  @Override
  public boolean addCars(int id, String location, int numCars, int price) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if (transactionManager.beginCarWrite(id, location))
	  {
		  return carResourceManager.addCars(id, location, numCars, price);
	  }
	  return false;
  }

  @Override
  public boolean addRooms(int id, String location, int numRooms, int price) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if (transactionManager.beginRoomWrite(id, location))
	  {
		  return roomResourceManager.addRooms(id, location, numRooms, price);
	  }
	  return false;
  }

  @Override
  public int newCustomer(int id) throws RemoteException, InvalidTransaction, TransactionAborted {
    int cid = flightResourceManager.newCustomer(id);
    carResourceManager.newCustomer(id, cid);
    roomResourceManager.newCustomer(id, cid);
    return cid;
  }

  @Override
  public boolean newCustomer(int id, int cid) throws RemoteException, InvalidTransaction, TransactionAborted {
    return flightResourceManager.newCustomer(id, cid)
           && carResourceManager.newCustomer(id, cid)
           && roomResourceManager.newCustomer(id, cid);
  }

  @Override
  public boolean deleteFlight(int id, int flightNum) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if (transactionManager.beginFlightWrite(id, flightNum)) 
	  {
		  return flightResourceManager.deleteFlight(id, flightNum);
	  }
	  return false;
  }

  @Override
  public boolean deleteCars(int id, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginCarWrite(id, location))
	  {
		  return carResourceManager.deleteCars(id, location);
	  }
	  return false;
  }

  @Override
  public boolean deleteRooms(int id, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if (transactionManager.beginRoomWrite(id, location))
	  {
		  return roomResourceManager.deleteRooms(id, location);
	  }
	  return false;
  }

  @Override
  public boolean deleteCustomer(int id, int customerID) throws RemoteException, InvalidTransaction, TransactionAborted {
    return flightResourceManager.deleteCustomer(id, customerID)
           && carResourceManager.deleteCustomer(id, customerID)
           && roomResourceManager.deleteCustomer(id, customerID);
  }

  @Override
  public int queryFlight(int id, int flightNumber) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginFlightRead(id, flightNumber))
	  {
		  return flightResourceManager.queryFlight(id, flightNumber);
	  }
	  return 0; // TODO: Just adding a todo here so that people see this. Should our error message be 0?
  }

  @Override
  public int queryCars(int id, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginCarRead(id, location))
	  {
		  return carResourceManager.queryCars(id, location);
	  }
	  return 0;// TODO: Just adding a todo here so that people see this. Should our error message be 0?
  }

  @Override // done
  public int queryRooms(int id, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginRoomRead(id, location))
	  {
		  return roomResourceManager.queryRooms(id, location);
	  }
	  return 0;// TODO: Just adding a todo here so that people see this. Should our error message be 0?
  }

  @Override
  public String queryCustomerInfo(int id, int customerID) throws InvalidTransaction, TransactionAborted {
    String flightBill;
    String carBill;
    String roomBill;
 // TODO: Just adding a todo here so that people see this. Don't image we need any locks here, do we?
    try {
      flightBill = flightResourceManager.queryCustomerInfo(id, customerID);
    } catch (RemoteException e) {
      e.printStackTrace();
      flightBill = "0";
    }
    try {
      carBill = carResourceManager.queryCustomerInfo(id, customerID);
    } catch (RemoteException e) {
      e.printStackTrace();
      carBill = "0";
    }
    try {
      roomBill = roomResourceManager.queryCustomerInfo(id, customerID);
    } catch (RemoteException e) {
      e.printStackTrace();
      roomBill = "0";
    }
    final String unavailable = "bill unavailable";
    return ("Flight " + (flightBill.equals("0") ? unavailable : flightBill)
            + "\nCar " + (carBill.equals("0") ? unavailable : carBill)
            + "\nRoom " + (roomBill.equals("0") ? unavailable : roomBill)
    ).replaceAll(" for customer \\d+", ":");
  }

  @Override
  public int queryFlightPrice(int id, int flightNumber) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginFlightRead(id, flightNumber))
	  {
		  return flightResourceManager.queryFlight(id, flightNumber);
	  }
	  return 0; // TODO: Just adding a todo here so that people see this. Should our error message be 0?
  }

  @Override
  public int queryCarsPrice(int id, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginCarRead(id,location))
	  {
		  return carResourceManager.queryCarsPrice(id, location);
	  }
	  return 0;// TODO: Just adding a todo here so that people see this. Should our error message be 0?
  }

  @Override
  public int queryRoomsPrice(int id, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginRoomRead(id, location))
	  {
		  return roomResourceManager.queryRoomsPrice(id, location);
	  }
	  return 0; // TODO: Just adding a todo here so that people see this. Should our error message be 0?
  }

  @Override
  public boolean reserveFlight(int id, int customerID, int flightNumber) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginFlightWrite(id, flightNumber))
	  {
		  return flightResourceManager.reserveFlight(id, customerID, flightNumber);
	  }
	  return false;
  }

  @Override
  public boolean reserveCar(int id, int customerID, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginCarWrite(id, location))
	  {
		  return carResourceManager.reserveCar(id, customerID, location);
	  }
	  return false;
  }

  @Override
  public boolean reserveRoom(int id, int customerID, String location) throws RemoteException, InvalidTransaction, TransactionAborted {
	  if(transactionManager.beginRoomWrite(id, location))
	  {
		  return roomResourceManager.reserveRoom(id, customerID, location);
	  }
	  return false;
  }

  @Override
  public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException, NumberFormatException, InvalidTransaction, TransactionAborted {
	// TODO: Add transactionManager calls plus query calls
    boolean result = true;
    for (String flightNum : flightNumbers) {
      result &= reserveFlight(id, customerID, Integer.parseInt(flightNum));
    }
    if (car) {
      result &= reserveCar(id, customerID, location);
    }
    if (room) {
      result &= reserveRoom(id, customerID, location);
    }
    return result;
  }

  @Override
  public String getName() {
    return SERVER_NAME;
  }
}
