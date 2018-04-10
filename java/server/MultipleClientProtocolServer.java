package main.java.server;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;


interface ServerProtocolFactory {
	ServerProtocol create();
}

class ConnectionHandler implements Runnable, ProtocolCallback {

	private BufferedReader in;
	private PrintWriter out;
	Socket clientSocket;
	ServerProtocol protocol;
	boolean closed = false;
	boolean shouldClose = false;
	LinkedList<String> inQueue = new LinkedList<String>();
	ConcurrentLinkedQueue<String> outQueue = new ConcurrentLinkedQueue<String>();

	public ConnectionHandler(Socket acceptedSocket, ServerProtocol p)
	{
		in = null;
		out = null;
		clientSocket = acceptedSocket;
		protocol = p;
	}

	public void run()
	{
		try {
			initialize();
		}
		catch (IOException e) {
			System.out.println("Error in initializing I/O");
		}

		try {
			process();
		} 
		catch (IOException e) {
			System.out.println("Error in I/O");
		} 

		System.out.println("Connection closed - bye bye...");
		close();

	}

	public void process() throws IOException
	{
		String msg;

		while (!shouldClose){
			while ((msg = in.readLine()) != null){
				protocol.processMessage(msg, this);
				System.out.println("Recieved \"" + msg + "\" from client");
			}
		}
	}

	// Starts listening
	public void initialize() throws IOException
	{
		// Initialize I/O
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8"), true);
		System.out.println("I/O initialized");
	}

	// Closes the connection
	public void close()
	{
		try {
			if (in != null)
			{
				in.close();
			}
			if (out != null)
			{
				out.close();
			}

			clientSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("Exception in closing I/O");
		}
	}

	@Override
	public void sendMessage(Object msg) throws IOException {
		out.println(msg);
	}

}

class MultipleClientProtocolServer implements Runnable {
	private ServerSocket serverSocket;
	private int listenPort;
	private ServerProtocolFactory factory;
	private ServerProtocol protocol;


	public MultipleClientProtocolServer(int port, ServerProtocolFactory p)
	{
		serverSocket = null;
		listenPort = port;
		factory = p;
	}

	public MultipleClientProtocolServer(int port, ServerProtocol protocolTBGP) {
		serverSocket = null;
		listenPort = port;
		protocol  = protocolTBGP;
	}

	public void run()
	{
		try {
			serverSocket = new ServerSocket(listenPort);
			System.out.println("Listening...");
		}
		catch (IOException e) {
			System.out.println("Cannot listen on port " + listenPort);
		}

		while (true)
		{
			try {
				ConnectionHandler newConnection = new ConnectionHandler(serverSocket.accept(), protocol);
				new Thread(newConnection).start();
			}
			catch (IOException e)
			{
				System.out.println("Failed to accept on port " + listenPort);
			}
		}
	}


	// Closes the connection
	public void close() throws IOException
	{
		serverSocket.close();
	}

	public static void main(String[] args) throws IOException
	{
		// Get port
		int port = Integer.decode(args[0]).intValue();
		MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new protocolTBGP());
		Thread serverThread = new Thread(server);
		serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			System.out.println("Server stopped");
		}
	}
}