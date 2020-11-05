package Server.Interface;

import Server.Common.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Arrays;

public class TCPResourceManager implements Runnable {
  private static final IResourceManager resourceManager = new ResourceManager("Resource Server");
  private final Socket middlewareSocket;
  private final PrintWriter out;
  private final BufferedReader in;

  public TCPResourceManager(Socket middlewareSock) throws IOException {
    this.middlewareSocket = middlewareSock;
    this.in = new BufferedReader(new InputStreamReader(middlewareSock.getInputStream()));
    this.out = new PrintWriter(middlewareSock.getOutputStream(), true);
  }

  public static void main(String[] args) {
    try (ServerSocket resourceManager = new ServerSocket(10025)) {
      //noinspection InfiniteLoopStatement
      while (true) {
        new Thread(new TCPResourceManager(resourceManager.accept())).start();
        System.out.println("Got connection from middleware");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  //parsing later on
  @Override
  public void run() {
    String[] args;
    try {
      args = in.readLine().split(",");
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    System.out.println(Arrays.toString(args));

    //parsing
    if (args[0].toLowerCase().contains("add")) {
      if (args[0].equalsIgnoreCase("AddFlight"))
        out.println(addFlight(Integer.parseInt(args[1]),
                              Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4])));
      else if (args[0].equalsIgnoreCase("AddCars"))
        out.println(addCars(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4])));
      else if (args[0].equalsIgnoreCase("AddRooms"))
        out.println(addRooms(Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4])));
      else if (args[0].equalsIgnoreCase("AddCustomer"))
        out.println(newCustomer(Integer.parseInt(args[1])));
      else if (args[0].equalsIgnoreCase("AddCustomerID"))
        out.println(newCustomer(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
    }
    else if (args[0].toLowerCase().contains("delete")) {
      if (args[0].equalsIgnoreCase("DeleteFlight"))
        out.println(deleteFlight(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
      else if (args[0].equalsIgnoreCase("DeleteCars"))
        out.println(deleteCars(Integer.parseInt(args[1]), args[2]));
      else if (args[0].equalsIgnoreCase("DeleteRooms"))
        out.println(deleteRooms(Integer.parseInt(args[1]), args[2]));
      else if (args[0].equalsIgnoreCase("DeleteCustomer"))
        out.println(deleteCustomer(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
    }
    else if (args[0].toLowerCase().contains("query")) {
      if (args[0].equalsIgnoreCase("QueryFlight"))
        out.println(queryFlight(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
      else if (args[0].equalsIgnoreCase("QueryCars"))
        out.println(queryCars(Integer.parseInt(args[1]), args[2]));
      else if (args[0].equalsIgnoreCase("QueryRooms"))
        out.println(queryRooms(Integer.parseInt(args[1]), args[2]));
      else if (args[0].equalsIgnoreCase("QueryCustomer"))
        out.println(queryCustomerInfo(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
      else if (args[0].equalsIgnoreCase("QueryFlightPrice"))
        out.println(queryFlightPrice(Integer.parseInt(args[1]), Integer.parseInt(args[2])));
      else if (args[0].equalsIgnoreCase("QueryCarsPrice"))
        out.println(queryCarsPrice(Integer.parseInt(args[1]), args[2]));
      else if (args[0].equalsIgnoreCase("QueryRoomsPrice"))
        out.println(queryRoomsPrice(Integer.parseInt(args[1]), args[2]));
    }
    else if (args[0].toLowerCase().contains("reserve")) {
      if (args[0].equalsIgnoreCase("ReserveFlight"))
        out.println(reserveFlight(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])));
      else if (args[0].equalsIgnoreCase("ReserveRoom"))
        out.println(reserveRoom(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]));
      else if (args[0].equalsIgnoreCase("ReserveCar"))
        out.println(reserveCar(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[3]));
    }

    try {
      middlewareSocket.close();
      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String addFlight(int id, int flightNum, int flightSeats, int flightPrice) {
    try {
      System.out.println(id + " " + flightNum + " " + flightSeats + " " + flightPrice);
      return resourceManager.addFlight(id, flightNum, flightSeats, flightPrice) ? "1" : "0";
    } catch (RemoteException e) {
      return "0";
    }
  }

  public String addCars(int id, String location, int numCars, int price) {
    try {
      return resourceManager.addCars(id, location, numCars, price) ? "1" : "0";
    } catch (RemoteException e) {
      return "0";
    }
  }

  public String addRooms(int id, String location, int numRooms, int price) {
    try {
      return resourceManager.addRooms(id, location, numRooms, price) ? "1" : "0";
    } catch (RemoteException e) {
      return "0";
    }
  }


  public int newCustomer(int id) {
    try {
      return resourceManager.newCustomer(id);
    } catch (RemoteException e) {
      e.printStackTrace();
      return 0;
    }
  }

  public String newCustomer(int id, int cid) {
    try {
      return resourceManager.newCustomer(id, cid) ? "1" : "0";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "0";
  }

  public String deleteFlight(int id, int flightNum) {
    try {
      return resourceManager.deleteFlight(id, flightNum) ? "1" : "0";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "0"; stampTable;
  }


  public String deleteCars(int id, String location) {
    try {
      return resourceManager.deleteCars(id, location) ? "1" : "0";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "0";
  }

  public String deleteRooms(int id, String location) {
    try {
      return resourceManager.deleteRooms(id, location) ? "1" : "0";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "0";
  }

  public String deleteCustomer(int id, int customerID) {
    try {
      return resourceManager.deleteCustomer(id, customerID) ? "1" : "0";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "0";
  }

  public int queryFlight(int id, int flightNumber) {
    try {
      return resourceManager.queryFlight(id, flightNumber);
    } catch (RemoteException e) {
      return 0;
    }
  }

  public int queryCars(int id, String location) {
    try {
      return resourceManager.queryCars(id, location);
    } catch (RemoteException e) {
      return 0;
    }
  }

  public int queryRooms(int id, String location) {
    try {
      return resourceManager.queryRooms(id, location);
    } catch (RemoteException e) {
      return 0;
    }
  }

  public String queryCustomerInfo(int id, int customerID) {
    try {
      return resourceManager.queryCustomerInfo(id, customerID);
    } catch (RemoteException e) {
      return "No info found";
    }
  }

  public int queryFlightPrice(int id, int flightNumber) {
    try {
      return resourceManager.queryFlightPrice(id, flightNumber);
    } catch (RemoteException e) {
      e.printStackTrace();
      return 0;
    }
  }

  public int queryCarsPrice(int id, String location) {
    try {
      return resourceManager.queryCarsPrice(id, location);
    } catch (RemoteException e) {
      e.printStackTrace();
      return 0;
    }
  }

  public int queryRoomsPrice(int id, String location) {
    try {
      return resourceManager.queryRoomsPrice(id, location);
    } catch (RemoteException e) {
      e.printStackTrace();
      return 0;
    }
  }

  public String reserveFlight(int id, int customerID, int flightNumber) {
    try {
      return resourceManager.reserveFlight(id, customerID, flightNumber) ? "1" : "0";
    } catch (RemoteException e) {
      e.printStackTrace();
      return "0";
    }
  }

  public String reserveCar(int id, int customerID, String location) {
    try {
      return resourceManager.reserveCar(id, customerID, location) ? "1" : "0";
    } catch (RemoteException e) {
      e.printStackTrace();
      return "0";
    }
  }

  public String reserveRoom(int id, int customerID, String location) {
    try {
      return resourceManager.reserveRoom(id, customerID, location) ? "1" : "0";
    } catch (RemoteException e) {
      e.printStackTrace();
      return "0";
    }
  }
}
