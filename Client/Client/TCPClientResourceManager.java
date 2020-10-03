package Client;

import Server.Interface.IResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class TCPClientResourceManager implements IResourceManager {
  Socket aSocket;
  PrintWriter aOutToServer;
  BufferedReader aInFromServer;


  public TCPClientResourceManager(Socket serverSocket) throws IOException {
    aSocket = serverSocket;
    aOutToServer = new PrintWriter(aSocket.getOutputStream(), true);
    aInFromServer = new BufferedReader(new InputStreamReader(aSocket.getInputStream()));
  }

  @Override
  public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice) {
    try {
      aOutToServer.println("AddFlight," + id + "," + flightNum + "," + flightSeats + "," + flightPrice);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in addFlight(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean addCars(int id, String location, int numCars, int price) {
    try {
      aOutToServer.println("AddCars," + id + "," + location + "," + numCars + "," + price);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in addCars(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean addRooms(int id, String location, int numRooms, int price) {
    try {
      aOutToServer.println("AddRooms," + id + "," + location + "," + numRooms + "," + price);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in addRooms(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public int newCustomer(int id) {
    try {
      aOutToServer.println("AddCustomer," + id);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return Integer.parseInt(response); // Get value from response
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in newCustomer(int): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return -1; // I hope we can use this as an error code, if not, maybe I'll throw an exception instead
  }

  @Override
  public boolean newCustomer(int id, int cid) {
    try {
      aOutToServer.println("AddCustomerID," + id + "," + cid);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in newCustomer(int, int): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean deleteFlight(int id, int flightNum) {
    try {
      aOutToServer.println("DeleteFlight," + id + "," + flightNum);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in deleteFlight(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean deleteCars(int id, String location) {
    try {
      aOutToServer.println("DeleteCars," + id + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in deleteCars(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean deleteRooms(int id, String location) {
    try {
      aOutToServer.println("DeleteRooms," + id + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in deleteRooms(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean deleteCustomer(int id, int customerID) {
    try {
      aOutToServer.println("DeleteCustomer," + id + "," + customerID);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in deleteCustomer(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public int queryFlight(int id, int flightNumber) {
    try {
      aOutToServer.println("QueryFlight," + id + "," + flightNumber);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return Integer.parseInt(response); // Get value from response
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in queryFlight(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
  }

  @Override
  public int queryCars(int id, String location) {
    try {
      aOutToServer.println("QueryCars," + id + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return Integer.parseInt(response); // Get value from response
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in queryCars(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
  }

  @Override
  public int queryRooms(int id, String location) {
    try {
      aOutToServer.println("QueryRooms," + id + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return Integer.parseInt(response); // Get value from response
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in queryRooms(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
  }

  @Override
  public String queryCustomerInfo(int id, int customerID) {
    try {
      aOutToServer.println("QueryCustomer," + id + "," + customerID);
      return aInFromServer.readLine().replace("\\n", "\n");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in queryCustomerInfo(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return null;
  }

  @Override
  public int queryFlightPrice(int id, int flightNumber) {
    try {
      aOutToServer.println("QueryFlightPrice," + id + "," + flightNumber);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return Integer.parseInt(response); // Get value from response
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in queryFlightPrice(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
  }

  @Override
  public int queryCarsPrice(int id, String location) {
    try {
      aOutToServer.println("QueryCarsPrice," + id + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return Integer.parseInt(response); // Get value from response
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in queryCarsPrice(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
  }

  @Override
  public int queryRoomsPrice(int id, String location) {
    try {
      aOutToServer.println("QueryRoomsPrice," + id + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return Integer.parseInt(response); // Get value from response
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in queryRoomsPrice(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
  }

  @Override
  public boolean reserveFlight(int id, int customerID, int flightNumber) {
    try {
      aOutToServer.println("ReserveFlight," + id + "," + customerID + "," + flightNumber);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in reserveFlight(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean reserveCar(int id, int customerID, String location) {
    try {
      aOutToServer.println("ReserveCar," + id + "," + customerID + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in reserveCars(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean reserveRoom(int id, int customerID, String location) {
    try {
      aOutToServer.println("ReserveRoom," + id + "," + customerID + "," + location);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in reserveRoom(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car,
                        boolean room) {
    try {
      String messageStart = "Bundle," + id + "," + customerID;
      StringBuilder messageMiddle = new StringBuilder();
      for (String flightNumber : flightNumbers) {
        messageMiddle.append(",").append(flightNumber);
      }
      String messageEnd = "," + location + "," + (car ? "1" : "0") + "," + (room ? "1" : "0");

      aOutToServer.println(messageStart + messageMiddle + messageEnd);
      String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
      return response.equals("1");
    } catch (Exception e) {
      System.err.println("TCPClientResourceManager Exception in bundle(...): " + e.toString());
      e.printStackTrace();
      System.exit(1);
    }

    return false;
  }

  @Override
  public String getName() {
    return "<TCPClientResourceManager::getName()>"; // Not sure what this function is for, so I have this tag here to identify it if it ever comes up
  }

}
