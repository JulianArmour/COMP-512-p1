package Client;

import Server.Interface.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;

import java.util.*;
import java.io.*;

public class TCPClient extends Client
{
	private static String s_serverHost = "localhost";
	private static int s_serverPort = 10025;
	private static String s_serverName = "Server";

	private static String s_rmiPrefix = "group_25_";

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
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.TCPClient [server_hostname [server_tcpobject]]");
			System.exit(1);
		}

		// Set the security policy
//		if (System.getSecurityManager() == null)
//		{
//			System.setSecurityManager(new SecurityManager());
//		}

		// Get a reference to the RMIRegister
		try {
			TCPClient client = new TCPClient();
			client.connectServer();
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TCPClient()
	{
		super();
	}

	public void connectServer()
	{
		connectServer(s_serverHost, s_serverPort, s_serverName);
	}

	public void connectServer(String server, int port, String name)
	{
		try {
			boolean first = true;
			while (true) {
				try {
					Socket socket = new Socket(server, port);
					if(!socket.isConnected())
					{
						socket.close();
						throw new ConnectException();
					}
					m_resourceManager = new TCPClientResourceManager(socket);
					System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
					break;
				}
				catch (Exception e) {
					if (first) {
					  e.printStackTrace();
						System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
						first = false;
					}
				}
				Thread.sleep(500);
			}
		}
		catch (Exception e) {
			System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
