package Client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Server.Transaction.InvalidTransaction;
import Server.Transaction.TransactionAborted;

public class AnalysisClient extends RMIClient {
	public static void main(String args[]) // Literally just copied and pasted from RMIClient. I think I changed all the references to RMIClient to AnalysisClient, so it should all be fine
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
//		runSingleResourceAnalysis();
   	runMultiResourceAnalysis();
	}

	private void runMultiResourceAnalysis() {
		long totalDuration = 0;
		final int runs = 50;
		AtomicInteger resource = new AtomicInteger(0);
		for (int run = 0; run < runs; run++) {
			try {
				Transaction transaction = new Transaction(Arrays.asList(
					xid -> m_resourceManager.addCars(xid, String.valueOf(resource.getAndIncrement()), 10, 50),
					xid -> m_resourceManager.addFlight(xid, resource.getAndIncrement(), 10, 50),
					xid -> m_resourceManager.addRooms(xid, String.valueOf(resource.getAndIncrement()), 10, 50)
				));
				long txStart = System.currentTimeMillis();
				transaction.execute();
				final long duration = System.currentTimeMillis() - txStart;
				totalDuration += duration;
				System.out.println("full transaction duration: " + duration + " ms") ;
			} catch (RemoteException | TransactionAborted | InvalidTransaction e) {
				System.out.println(e);
			}
		}
		System.out.println("Average transaction duration: " + totalDuration / runs + " ms");
	}

	private void runSingleResourceAnalysis() {
		long totalDuration = 0;
		final int runs = 50;
		AtomicInteger resource = new AtomicInteger(0);
		for (int run = 0; run < runs; run++) {
			try {
				Transaction transaction = new Transaction(Arrays.asList(
					xid-> m_resourceManager.addCars(xid, String.valueOf(resource.getAndIncrement()), 10, 50),
					xid-> m_resourceManager.addCars(xid, String.valueOf(resource.getAndIncrement()), 10, 50),
					xid-> m_resourceManager.addCars(xid, String.valueOf(resource.getAndIncrement()), 10, 50)
				));
				long txStart = System.currentTimeMillis();
				transaction.execute();
				final long duration = System.currentTimeMillis() - txStart;
				totalDuration += duration;
				System.out.println("full transaction duration: " + duration + " ms") ;
			} catch (RemoteException | TransactionAborted | InvalidTransaction e) {
				System.out.println(e);
			}
		}
		System.out.println("Average transaction duration: " + totalDuration / runs + " ms");
	}

	private interface Call { // Supposed to be a functional interface
		void execute(int xid) throws RemoteException, TransactionAborted, InvalidTransaction;
	}

	private class Transaction{ // Supposed to be the 'parameterized transaction type' from the Assignment Specs, change the name if you like
		private final List<Call> aCalls;

		public Transaction(List<Call> pCalls) throws RemoteException {
			aCalls = pCalls;
		}

		public void execute() throws RemoteException, TransactionAborted, InvalidTransaction // No error handling here. Maybe we should add some? Or handle it in start(), not sure
		{
			// get new xid
			long start = System.currentTimeMillis();
			final int xid = m_resourceManager.start();
			System.out.println("new xid time: " + (System.currentTimeMillis() - start) + " ms") ;
			//perform commands
			for(Call call : aCalls) {
				long startCall = System.currentTimeMillis();
				call.execute(xid);
				System.out.println("Exec time: " + (System.currentTimeMillis() - startCall) + " ms") ;
			}
			//commit
			long commitStart = System.currentTimeMillis();
			m_resourceManager.commit(xid);
			System.out.println("Commit Exec time: " + (System.currentTimeMillis() - commitStart) + " ms");
		}
	}
}
