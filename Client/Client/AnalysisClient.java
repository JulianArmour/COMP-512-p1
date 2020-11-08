package Client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Server.Transaction.InvalidTransaction;
import Server.Transaction.TransactionAborted;

public class AnalysisClient extends RMIClient {
	public static void main(String args[])
	{	
		if (args.length > 0)
		{
			s_serverHost = args[0];
		}
		if (args.length > 1)
		{
			s_serverName = args[1];
		}
		if (args.length > 2)
		{
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
			System.exit(1);
		}

		// Set the security policy
		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		// Get a reference to the RMIRegister
		try {
			AnalysisClient client = new AnalysisClient();
			client.connectServer();
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public AnalysisClient()
	{
		super();
	}
	
	@Override
	public void start() 
	{
		AtomicInteger customerId = new AtomicInteger(-1);
		try { // TODO: The assignment specs say that the Transaction called in each loop should be have different parameters. Does this mean outright different reservations and such, or just different xid? We may need to find a way to generate these Transactions dynamically if it's the former
		Transaction transaction = new Transaction(Arrays.asList( // This is just a demo of what we might use as a transaction
				(xid)-> {m_resourceManager.addCars(xid, "Montreal", 10, 50);},
				(xid)-> {customerId.set(m_resourceManager.newCustomer(xid));}, // Had to do some weird stuff with AtomicInteger in order to capture a modifiable int
				(xid)-> {m_resourceManager.reserveCar(xid, customerId.intValue(), "Montreal");}
				
				));
		
		transaction.execute();
		transaction.close();
		} catch (RemoteException | TransactionAborted | InvalidTransaction e) {
			System.out.println(e);
		}
		
	}
	
	private interface Call { // Supposed to be a functional interface
		void execute(int xid) throws RemoteException, TransactionAborted, InvalidTransaction;
	}
	
	private class Transaction{ // Supposed to be the 'parameterized transaction type' from the Assignment Specs, change the name if you like
		private int xid;
		private ArrayList<Call> aCalls; // Maybe we could put 
		
		public Transaction(List<Call> pCalls) throws RemoteException
		{
			xid = m_resourceManager.start();
			aCalls = new ArrayList<Call>(pCalls);
		}
		
		public void execute() throws RemoteException, TransactionAborted, InvalidTransaction
		{
			for(Call call : aCalls) {
				call.execute(xid);
			}
		}
		
		public void close() throws RemoteException, TransactionAborted, InvalidTransaction
		{
			m_resourceManager.commit(xid);
		}
	}
}
