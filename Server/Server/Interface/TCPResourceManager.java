package Server.Interface;

import java.util.Vector;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.io.IOException;

public class TCPResourceManager implements IResourceManager {
	Socket aSocket;
    PrintWriter aOutToServer;
    BufferedReader aInFromServer;
	
	
	public TCPResourceManager(String pServer, int pPort) throws UnknownHostException, IOException{
		aSocket = new Socket(pServer, pPort);
		aOutToServer= new PrintWriter(aSocket.getOutputStream(),true);
		aInFromServer = new BufferedReader(new InputStreamReader(aSocket.getInputStream()));
	}

	@Override
	public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice){
		aOutToServer.println("AddFlight,"+id+","+flightNum+","+flightSeats+","+flightPrice);
		try {
			String response = aInFromServer.readLine();
		} catch (IOException e) {
			// TODO
			System.out.println("TCPResourceManager line ~31 IOException");
			e.printStackTrace();
			System.exit(1);
		}
		
		return false;
	}

	@Override
	public boolean addCars(int id, String location, int numCars, int price){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addRooms(int id, String location, int numRooms, int price){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int newCustomer(int id){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean newCustomer(int id, int cid){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteFlight(int id, int flightNum){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCars(int id, String location){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteRooms(int id, String location){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteCustomer(int id, int customerID){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int queryFlight(int id, int flightNumber){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCars(int id, String location){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRooms(int id, String location){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String queryCustomerInfo(int id, int customerID){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int queryFlightPrice(int id, int flightNumber){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryCarsPrice(int id, String location){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int queryRoomsPrice(int id, String location){
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean reserveFlight(int id, int customerID, int flightNumber){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveCar(int id, int customerID, String location){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reserveRoom(int id, int customerID, String location){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean bundle(int id, int customerID, Vector<String> flightNumbers, String location, boolean car,
			boolean room){
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName(){
		// TODO Auto-generated method stub
		return null;
	}

}
