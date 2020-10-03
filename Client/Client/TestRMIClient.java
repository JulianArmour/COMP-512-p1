package Client;

import Server.Interface.IResourceManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class TestRMIClient extends Client {
  private static String s_serverHost;
  private static int s_serverPort = 1099;
  private static String s_serverName;
  private static String s_rmiPrefix = "group_25_";

  public static void main(String[] args) {
    s_serverHost = args[0];
    s_serverName = args[1];

    // Set the security policy
    if (System.getSecurityManager() == null) {
      System.setSecurityManager(new SecurityManager());
    }

    try {
      TestRMIClient client = new TestRMIClient();
      client.connectServer();
      client.start();
    } catch (Exception e) {
      System.err.println((char) 27 + "[31;1mClient exception: " + (char) 27 + "[0mUncaught exception");
      e.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  public void start() {
    testQueryNoFlights();
    testAddFlight();
    testQueryExistingFlights();
    testAddCars();
    testAddRooms();
    testNewCustomerWithID();
  }

  private void testNewCustomerWithID() {
    String name = "testNewCustomerWithID";
    boolean pass = false;
    try {
      pass = m_resourceManager.newCustomer(1, 999);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    System.out.println((pass ? "Success" : "Failure") + " in " + name);
  }

  private void testQueryNoFlights() {
    String name = "testQueryNoFlights";
    boolean pass = false;
    try {
      pass = 0 == m_resourceManager.queryFlight(1, 999);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    System.out.println((pass ? "Success" : "Failure") + " in " + name);
  }

  private void testQueryExistingFlights() {
    String name = "testQueryExistingFlights";
    boolean pass = false;
    try {
      pass = 100 == m_resourceManager.queryFlight(1, 123);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    System.out.println((pass ? "Success" : "Failure") + " in " + name);
  }

  private void testAddFlight() {
    boolean pass = false;
    try {
      pass = m_resourceManager.addFlight(1, 123, 100, 1000);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    System.out.println((pass ? "Success" : "Failure") + " in testAddFlight");
  }

  private void testAddCars() {
    boolean pass = false;
    try {
      pass = m_resourceManager.addCars(1, "carPlace", 100, 1000);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    System.out.println((pass ? "Success" : "Failure") + " in testAddCars");
  }

  private void testAddRooms() {
    boolean pass = false;
    try {
      pass = m_resourceManager.addRooms(1, "roomPlace", 100, 1000);
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    System.out.println((pass ? "Success" : "Failure") + " in testAddFlight");
  }


  @Override
  public void connectServer() {
    connectServer(s_serverHost, s_serverPort, s_serverName);
  }

  public void connectServer(String server, int port, String name) {
    try {
      boolean first = true;
      while (true) {
        try {
          Registry registry = LocateRegistry.getRegistry(server, port);
          m_resourceManager = (IResourceManager) registry.lookup(s_rmiPrefix + name);
          System.out.println(
            "Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
          break;
        } catch (NotBoundException | RemoteException e) {
          if (first) {
            System.out.println(
              "Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
            first = false;
          }
        }
        Thread.sleep(500);
      }
    } catch (Exception e) {
      System.err.println((char) 27 + "[31;1mServer exception: " + (char) 27 + "[0mUncaught exception");
      e.printStackTrace();
      System.exit(1);
    }
  }

}
