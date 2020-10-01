package Server.Interface;

import Server.Common.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Vector;

public class TCPResourceManager implements Runnable {
	private Socket middlewareSock;
	private PrintWriter out;
	private BufferedReader in;
	private IResourceManager resourceManager;

	public TCPResourceManager(Socket middlewareSock) throws IOException {
		this.middlewareSock = middlewareSock;
		this.in = new BufferedReader(new InputStreamReader(middlewareSock.getInputStream()));
		this.out = new PrintWriter(middlewareSock.getOutputStream(), true);
		this.resourceManager = new ResourceManager("Resource Server");
	}

	public static void main(String[] args) {
		try (ServerSocket resourceManager = new ServerSocket(10025)) {
		  while (true) {
		  	new Thread(new TCPResourceManager(resourceManager.accept())).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//parsing later on
	@Override
  public void run() {
		String[] splited;
		try {
			splited = in.readLine().split(",");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		//parsing
		if (splited[0].toLowerCase().contains("add")) {
			if (splited[0].equals("AddFlight"))
				out.println(addFlight(Integer.parseInt(splited[1]), Integer.parseInt(splited[2]), Integer.parseInt(splited[3]), Integer
																																																													.parseInt(splited[4])));
			if (splited[0].equals("AddCars"))
				out.println(addCars(Integer.parseInt(splited[1]), splited[2], Integer.parseInt(splited[3]), Integer.parseInt(splited[4])));
			if (splited[0].equals("AddRooms"))
				out.println(addRooms(Integer.parseInt(splited[1]), splited[2], Integer.parseInt(splited[3]), Integer.parseInt(splited[4])));
		}
		if (splited[0].toLowerCase().contains("new")) {
			if (splited[0].equals("newCustomer")) {
				if (!splited[2].equals("0"))
					out.println(newCustomer(Integer.parseInt(splited[1]), Integer.parseInt(splited[2])));
				else out.println(newCustomer(Integer.parseInt(splited[1])));
			}
		}
		if (splited[0].toLowerCase().contains("delete")) {
			if (splited[0].equals("deleteFlight"))
				out.println(deleteFlight(Integer.parseInt(splited[1]), Integer.parseInt(splited[2])));
			if (splited[0].equals("deleteCars")) out.println(deleteCars(Integer.parseInt(splited[1]), splited[2]));
			if (splited[0].equals("deleteRooms")) out.println(deleteRooms(Integer.parseInt(splited[1]), splited[2]));
			if (splited[0].equals("deleteCustomer"))
				out.println(deleteCustomer(Integer.parseInt(splited[1]), Integer.parseInt(splited[2])));
		}
		if (splited[0].toLowerCase().contains("query")) {
			if (splited[0].equals("queryFlight"))
				out.println(queryFlight(Integer.parseInt(splited[1]), Integer.parseInt(splited[2])));
			if (splited[0].equals("queryCars")) out.println(queryCars(Integer.parseInt(splited[1]), splited[2]));
			if (splited[0].equals("queryRooms")) out.println(queryRooms(Integer.parseInt(splited[1]), splited[2]));
			if (splited[0].equals("queryCustomerInfo"))
				out.println(queryCustomerInfo(Integer.parseInt(splited[1]), Integer.parseInt(splited[2])));
			if (splited[0].equals("queryFlightPrice"))
				out.println(queryFlightPrice(Integer.parseInt(splited[1]), Integer.parseInt(splited[2])));
			if (splited[0].equals("queryCarsPrice")) out.println(queryCarsPrice(Integer.parseInt(splited[1]), splited[2]));
			if (splited[0].equals("queryRoomsPrice")) out.println(queryRoomsPrice(Integer.parseInt(splited[1]), splited[2]));
		}
		if (splited[0].toLowerCase().contains("reserve")) {
			if (splited[0].equals("reserveFlight"))
				out.println(reserveFlight(Integer.parseInt(splited[1]), Integer.parseInt(splited[2]), Integer.parseInt(splited[3])));
			if (splited[0].equals("reserveRoom"))
				out.println(reserveRoom(Integer.parseInt(splited[1]), Integer.parseInt(splited[2]), splited[3]));
			if (splited[0].equals("reserveCar"))
				out.println(reserveCar(Integer.parseInt(splited[1]), Integer.parseInt(splited[2]), splited[3]));
		}
	}

	public String addFlight(int id, int flightNum, int flightSeats, int flightPrice) {
		try {
			return resourceManager.addFlight(id, flightNum, flightSeats, flightPrice) ? "1" : "0";
		} catch (RemoteException e) {
			e.printStackTrace();
			return "0";
		}
	}

	public boolean addCars(int id, String location, int numCars, int price){
		try {
			return resourceManager.addFlight(id, flightNum, flightSeats, flightPrice) ? "1" : "0";
		} catch (RemoteException e) {
			e.printStackTrace();

		}
		return false;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price){
		try {
			aOutToServer.println("AddRooms,"+id+","+location+","+numRooms+","+price);
			String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
			return response.equals("1");
		} catch (Exception e) {
			// TODO
			System.err.println("TCPResourceManager Exception in addRooms(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}

		return false;
	}

	public int newCustomer(int id){
		try {
			return resourceManager.newCustomer(id);
		} catch (RemoteException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public String newCustomer(int id, int cid){
		try {
			return resourceManager.newCustomer(id, cid) ? "1" : "0";
		} catch (Exception e) {
			System.err.println("TCPResourceManager Exception in newCustomer(int, int): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return "0";
	}

	public String deleteFlight(int id, int flightNum){
		try {
			return resourceManager.deleteFlight(id, flightNum) ? "1":"0";
		} catch (Exception e) {
			System.err.println("TCPResourceManager Exception in deleteFlight(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return "0";
	}


	public String deleteCars(int id, String location){
		try {
			return resourceManager.deleteCars(id, location) ? "1": "0";
		} catch (Exception e) {
			System.err.println("TCPResourceManager Exception in deleteCars(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return "0";
	}

	public String deleteRooms(int id, String location){
		try {
			return resourceManager.deleteRooms(id, location) ? "1" : "0";
		} catch (Exception e) {
			System.err.println("TCPResourceManager Exception in deleteRooms(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return "0";
	}

	public String deleteCustomer(int id, int customerID){
		try {
			return resourceManager.deleleCustomer(id,customerID) ? "1" : "0";
		} catch (Exception e) {

			System.err.println("TCPResourceManager Exception in deleteCustomer(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
		return "0";
	}

	@Override
	public int queryFlight(int id, int flightNumber){
		try {
			aOutToServer.println("QueryFlight,"+id+","+flightNumber);
			String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
			return Integer.getInteger(response, -1); // Get value from response, -1 is default
		} catch (Exception e) {
			// TODO
			System.err.println("TCPResourceManager Exception in queryFlight(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}

		return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
	}

	@Override
	public int queryCars(int id, String location){
		try {
			aOutToServer.println("QueryCars,"+id+","+location);
			String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
			return Integer.getInteger(response, -1); // Get value from response, -1 is default
		} catch (Exception e) {
			// TODO
			System.err.println("TCPResourceManager Exception in queryCars(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}

		return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
	}

	@Override
	public int queryRooms(int id, String location){
		try {
			aOutToServer.println("QueryRooms,"+id+","+location);
			String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
			return Integer.getInteger(response, -1); // Get value from response, -1 is default
		} catch (Exception e) {
			// TODO
			System.err.println("TCPResourceManager Exception in queryRooms(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}

		return -1; // This is my error code. I imagine it would be clear to anyone reading, but an Exception might be a better idea
	}

	@Override
	public String queryCustomerInfo(int id, int customerID){
		try {
			aOutToServer.println("QueryCustomer,"+id+","+customerID);
			String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
			return response; // Get value from response
		} catch (Exception e) {
			// TODO
			System.err.println("TCPResourceManager Exception in queryCustomerInfo(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	public int queryFlightPrice(int id, int flightNumber){
		try {
			return resourceManager.queryFlightPrice(id, flightNumber)
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int queryCarsPrice(int id, String location){
		try {
			return resourceManager.queryCarsPrice(id, location)
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int queryRoomsPrice(int id, String location){
		try {
			return resourceManager.queryRoomsPrice(id, location)
		} catch (RemoteException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public String reserveFlight(int id, int customerID, int flightNumber){
		try {
			return resourceManager.reserveFlight(id, customerID, flightNumber) ? "1" : "0";
		} catch (RemoteException e) {
			e.printStackTrace();
			return "0";
		}
	}

	public String reserveCar(int id, int customerID, String location){
		try {
			return resourceManager.reserveCar(id, customerID, location) ? "1" : "0";
		} catch (RemoteException e) {
			e.printStackTrace();
			return "0";
		}
	}

	public String reserveRoom(int id, int customerID, String location){
		try {
			return resourceManager.reserveRoom(id, customerID, location) ? "1" : "0";
		} catch (RemoteException e) {
			e.printStackTrace();
			return "0";
		}
	}

	@Override
	public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car,
			boolean room){
		try {
			String messageStart = "Bundle,"+id+","+customerID;
			String messageMiddle = "";
			for(String flightNumber : flightNumbers){
				messageMiddle = messageMiddle + "," + flightNumber;
			}
			String messageEnd = ","+location+","+ (car ? "1" : "0") + "," + (room ? "1" : "0");

			aOutToServer.println(messageStart + messageMiddle + messageEnd);
			String response = aInFromServer.readLine(); // I assume this is blocking, otherwise this is definitely incorrect
			return response.equals("1");
		} catch (Exception e) {
			// TODO
			System.err.println("TCPResourceManager Exception in bundle(...): " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}

		return false;
	}

	@Override
	public String getName(){
		return "<TCPResourceManager::getName()>"; // Not sure what this function is for, so I have this tag here to identify it if it ever comes up
	}

}
