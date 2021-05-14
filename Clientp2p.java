import java.net.*;
import java.util.*;
import java.io.*;

// java Clientp2p MY_PORT OTHER_IP OTHER_PORT

public class Clientp2p {

	public static int MY_PORT;
	public static String OTHER_IP;
	public static int OTHER_PORT;

	static Boolean mConnected;
	static Boolean oConnected;



	public static void main(String[] args) {
		
		MY_PORT = Integer.parseInt(args[0]);
		OTHER_IP = args[1];
		OTHER_PORT = Integer.parseInt(args[2]);



		mConnected = tryConnect(OTHER_IP, OTHER_PORT);

		oConnected = waitForOther(MY_PORT);
		
	
		if (!mConnected) {
			wait(1000);
			mConnected = tryConnect(OTHER_IP, OTHER_PORT);
		}








	}


	private static Boolean tryConnect(String ip, int port) {

		try {
			Socket socket = new Socket();
			SocketAddress address = new InetSocketAddress (ip, port);

			socket.connect(address);

			System.out.println("mConn establecida\n");
			return true;

		} catch (IOException e) {
			System.out.print("mConn no establecida\n");
			return false;
		}
	}


	private static Boolean waitForOther(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(MY_PORT);

			Socket socket = serverSocket.accept();

			System.out.print("oConn establecida\n");

			return true;


		} catch (IOException e2) {
			System.out.print("No se puede conectar\n");
			return false;
		}
	}

	private static void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}

}


		// try {
		// 	Socket mSocket = new Socket();
		// 	SocketAddress address = new InetSocketAddress (OTHER_IP, OTHER_PORT);

		// 	mSocket.connect(address);

		// 	System.out.println("Conexión establecida");
		// 	mConnected = true;

		// 	try {
		// 		ServerSocket serverSocket = new ServerSocket(MY_PORT);

		// 		Socket oSocket = serverSocket.accept();

		// 		oConnected = true;

		// 		//TODOS CONECTADOS

		// 	} catch (IOException e2) {
		// 		System.out.print("No se puede crear server socket");
		// 	}


		// } catch(UnknownHostException e) {
		// 	System.out.println("Nombre del servidor desconocido");
		// } catch (IOException e) {
		// 	System.out.println("Conexión no establecida");
		// 	mConnected = false;

		// 	try {
		// 		ServerSocket serverSocket = new ServerSocket(MY_PORT);

		// 		Socket oSocket = serverSocket.accept();

		// 		oConnected = true;

		// 		try {
		// 			Thread.sleep(1000);
		// 		} catch (InterruptedException e3) {}

		// 		Socket mSocket = new Socket();
		// 		SocketAddress address = new InetSocketAddress (OTHER_IP, OTHER_PORT);

		// 		mSocket.connect(address);

		// 		System.out.println("Conexión establecida");
		// 		mConnected = true;

		// 		//TODOS CONECTADOS

		// 	} catch (IOException e2) {
		// 		System.out.print("No se puede conectar");
		// 	}

		// }