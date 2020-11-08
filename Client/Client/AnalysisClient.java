package Client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Server.Transaction.InvalidTransaction;
import Server.Transaction.TransactionAborted;

public class AnalysisClient extends RMIClient {
	int aNumClients;
	int aMillisecondsBetweenTransactions;
	
	public static void main(String args[]) // Literally just copied and pasted from RMIClient. I think I changed all the references to RMIClient to AnalysisClient, so it should all be fine
	{
		int numClients = 0;
		int millisecondsBetweenTransactions = 0;
		
		if (args.length > 0)
		{
			if(args[0].equals("-h")) {
				System.out.println("First arg is: s_serverHost");
				System.out.println("Second arg is: s_serverName");
				System.out.println("Third arg is: clients");
				System.out.println("Second arg is: millisecondsBetweenTransactions");
				System.exit(0);
			}
			s_serverHost = args[0];
		}
		if (args.length > 1)
		{
			s_serverName = args[1];
		}
		if (args.length > 2)
		{
			numClients = Integer.parseInt(args[2]);
		}
		if (args.length > 3)
		{
			millisecondsBetweenTransactions = Integer.parseInt(args[3]);
		}
		if (args.length > 4)
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
			AnalysisClient client = new AnalysisClient(numClients, millisecondsBetweenTransactions);
			client.connectServer();
			client.start();
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public AnalysisClient(int pNumClients, int pMillisecondsBetweenTransactions)
	{
		super();
		aNumClients = pNumClients;
		aMillisecondsBetweenTransactions = pMillisecondsBetweenTransactions;
	}

	@Override
	public void start()
	{
//		runSingleResourceAnalysis();
//   	runMultiResourceAnalysis();
    runSingleResourceMultipleClientAnalysis(aNumClients, aMillisecondsBetweenTransactions);
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

	private void runSingleResourceMultipleClientAnalysis(int nClients, int milliBetweenTransactions) {
		List<Long> totalDurations = new ArrayList<>(Collections.nCopies(nClients, (long)0));
		final int runs = 20;
		List<Thread> clients = new ArrayList<>(nClients);
		for (int thread = 0; thread < nClients; thread++) {
			final int threadId = thread;
			clients.add(new Thread(() -> {
				for (int run = 0; run < runs; run++) {
					Transaction transaction = new Transaction(Arrays.asList(
						xid -> m_resourceManager.addFlight(xid, 1, 10, 50),
						xid -> m_resourceManager.addCars(xid, "A", 10, 50),
						xid -> m_resourceManager.addRooms(xid, "A", 10, 50)
					));
					long txStart = System.currentTimeMillis();
					try {
						transaction.execute();
					} catch (RemoteException | TransactionAborted | InvalidTransaction e) {
						System.out.println(e);
					} finally {
						final long duration = System.currentTimeMillis() - txStart;
						totalDurations.set(threadId, totalDurations.get(threadId) + duration);
						System.out.println("full transaction duration: " + duration + " ms");
					}
					try {
						Thread.sleep(milliBetweenTransactions);
					} catch (InterruptedException ignore) {
					}
				}
			}));
		}
		clients.forEach(Thread::start);
		for (Thread client : clients) {
			try {
				client.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long avgDuration = totalDurations.stream().reduce((long)0, Long::sum) / (nClients * runs);
		System.out.println("Average transaction duration: " + avgDuration + " ms");
	}

	private interface Call { // Supposed to be a functional interface
		void execute(int xid) throws RemoteException, TransactionAborted, InvalidTransaction;
	}

	private class Transaction{ // Supposed to be the 'parameterized transaction type' from the Assignment Specs, change the name if you like
		private final List<Call> aCalls;

		public Transaction(List<Call> pCalls) {
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
