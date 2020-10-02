package Server.Interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPMiddleware {
  private static final int SERVER_PORT = 10025;
  private static String flightServer;
  private static String carServer;
  private static String roomServer;

  public static void main(String[] args) {
    flightServer = args[0];
    carServer = args[1];
    roomServer = args[2];

    //Try-With_Resources block: WILL AUTO-CLOSE THE SERVER SOCKET
    try (ServerSocket middlewareSock = new ServerSocket(SERVER_PORT)) {
      //noinspection InfiniteLoopStatement
      while (true) {
        new Thread(new ClientRequestHandler(middlewareSock.accept())).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class ClientRequestHandler implements Runnable {
    private final Socket clientSocket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public ClientRequestHandler(Socket clientSocket) throws IOException {
      this.clientSocket = clientSocket;
      this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
      //try-with-resources, all these will be closed automatically by the jvm.
      try (clientSocket; reader; writer) {
        while (true) {
          String cmd = reader.readLine();
          if (cmd == null)
            return;
          writer.println(dispatchCommand(cmd));
        }
      } catch (IOException ignored) {
      } // client disconnected
    }

    private String dispatchCommand(String cmd) {
      final String cmdType = cmd.split(",")[0];
      if (cmdType.toLowerCase().contains("flight"))
        return dispatchToRM(cmd, flightServer);
      if (cmdType.toLowerCase().contains("car"))
        return dispatchToRM(cmd, carServer);
      if (cmdType.toLowerCase().contains("room"))
        return dispatchToRM(cmd, roomServer);
      if (cmdType.equalsIgnoreCase("QueryCustomer"))
        return dispatchQueryCustomer(cmd);
      if (cmdType.equalsIgnoreCase("AddCustomerID"))
        return dispatchAddCustomerWithID(cmd);
      if (cmdType.equalsIgnoreCase("AddCustomer"))
        return dispatchAddCustomerNoID(cmd);
      if (cmdType.toLowerCase().contains("bundle"))
        return dispatchBundle(cmd);

      System.out.println("unrecognized command: " + cmd);
      return "0";
    }

    private String dispatchQueryCustomer(String cmd) {
      return "Flight " + dispatchToRM(cmd, flightServer)
             + "Car " + dispatchToRM(cmd, carServer)
             + "Room " + dispatchToRM(cmd, roomServer);
    }

    private String dispatchAddCustomerNoID(String cmd) {
      int id = Integer.parseInt(dispatchToRM(cmd, flightServer));
      if (id == 0) return "0"; // 0 indicates failure
      var cmdWithId = cmd + "," + id;
      return dispatchToRM(cmdWithId, roomServer).equals("1")
             && dispatchToRM(cmdWithId, carServer).equals("1") ? Integer.toString(id) : "0";
    }

    private String dispatchAddCustomerWithID(String cmd) {
      return dispatchToRM(cmd, flightServer).equals("1")
             && dispatchToRM(cmd, roomServer).equals("1")
             && dispatchToRM(cmd, carServer).equals("1") ? "1" : "0";
    }

    private String dispatchToRM(String cmd, String server) {
      try (Socket rmSock = new Socket(server, SERVER_PORT);
           var out = new PrintWriter(rmSock.getOutputStream(), true);
           var in = new BufferedReader(new InputStreamReader(rmSock.getInputStream()))) {
        out.println(cmd);
        System.out.println("send " + cmd + " to RM server");
        final StringBuilder response = new StringBuilder();
        while (true) {
          String line = in.readLine();
          if (line == null) break;
          response.append(line).append("\n");
        }
        System.out.println("receive " + response + " from RM server");
        return response.toString();
      } catch (IOException e) {
        e.printStackTrace();
        return "0";
      }
    }

    private String dispatchBundle(String cmd) {
      String[] args = cmd.split(",");
      if (args.length < 7)
        return "0";
      String xid = args[1];
      String customerID = args[2];
      String location = args[args.length - 3];
      boolean bookRoom = args[args.length - 1].equals("1");
      boolean bookCar = args[args.length - 2].equals("1");

      boolean success = true;
      for (int i = 3; i < args.length - 3; i++) {
        success &= "1".equals(String.join(",", "ReserveFlight", xid, customerID, args[i]));
      }
      if (bookCar)
        success &= "1".equals(dispatchCommand(String.join(",", "ReserveCar", xid, customerID, location)));
      if (bookRoom)
        success &= "1".equals(dispatchCommand(String.join(",", "ReserveRoom", xid, customerID, location)));
      return success ? "1" : "0";
    }
  }
}
