import java.net.*;
import java.util.*;
import java.io.*;

// java Clientp2p MY_PORT OTHER_IP OTHER_PORT

public class Clientp2p {

	public static int MY_PORT;
	public static String OTHER_IP;
	public static int OTHER_PORT;

	private static final int combinationSize = 4;
	public static final char[] colors = {'R', 'Y', 'B', 'G'};

	static Boolean mConnected;
	static Boolean oConnected;

	public static final String[] actions = {"MOVE", "WIN", "MOVE_RESPONSE", "YOUR_TURN", "DRAW"};
	private static String[] log;
	private static Boolean win = false, lose = false;
	private static int totalTurns = 15;
	private static int turn = 0;
	private static char[] combination;

	private static Socket socket;
	private static PrintWriter out;
	private static BufferedReader in;

	//private ServerSocket serverSocket;



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

		try {
			OutputStream outputStream = socket.getOutputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());

			out = new PrintWriter(outputStream, true);
			in = new BufferedReader(inputStreamReader);

		} catch (IOException e) {}

		startGame();


	}


	private static Boolean tryConnect(String ip, int port) {

		try {
			socket = new Socket();
			SocketAddress address = new InetSocketAddress (ip, port);

			socket.connect(address);

			//System.out.println("mConn establecida\n");
			return true;

		} catch (IOException e) {
			//System.out.print("mConn no establecida\n");
			return false;
		}
	}


	private static Boolean waitForOther(int port) {

		System.out.print("Esperando al otro jugador.\n");

		try {
			ServerSocket serverSocket = new ServerSocket(MY_PORT);

			socket = serverSocket.accept();

			//System.out.print("oConn establecida\n");

			return true;


		} catch (IOException e2) {
			//System.out.print("No se puede conectar\n");
			return false;
		}
	}

	private static void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}



	// GAME LOGIC


	private static void startGame() {

		String[] message;
		log = new String[totalTurns + 1];

		combination = generateCombination();
		System.out.println("Combinación del oponente: " + String.valueOf(combination));


		if (MY_PORT < OTHER_PORT) {
			move();
		}

		while(turn < totalTurns && !win && !lose) {

			message = readClientRequest();
			chooseAction(message);

		}

		if (!win && !lose) {
			System.out.println("No te quedan turnos.");

			if (MY_PORT < OTHER_PORT) {
				message = readClientRequest(); // MOVE
				chooseAction(message);

				message = readClientRequest(); // MOVE_RESPONSE
				chooseAction(message);

				message = readClientRequest(); // DRAW
				chooseAction(message);
			} else {
				System.out.println("Empate.");
				send("DRAW\nnull\n");
			}
			
		
		}

		System.out.println("Fin de la partida");

		closeSocket();

	}


	private static String[] readClientRequest() {

		String action = null;
		String args = "null";

		try {
			
			while (action == null || action.length() < 1) {
				action = in.readLine();
			}
			args = in.readLine();
			
		} catch (IOException e) {
			System.out.println("No se ha podido leer");
		}		

		return new String[] {action, args};
	}



	private static void win() {
		clearScreen();
		System.out.println("Has ganado!!");
		win = true;
	}




	private static char[] generateCombination() {
		Random random = new Random();

		char[] combination = new char[4];

		for (int i = 0; i < combinationSize; i++) {
			combination[i] = colors[random.nextInt(combinationSize)];
		}
		
		return combination;
	}


	private static void chooseAction(String[] message) {
		String action = message[0];
		String args = message[1];

		for (int i = 0; i < actions.length; i++) {
			if (actions[i].equals(action)) {

				switch (i) {
					
					case 0: // MOVE
						processClientMove(args);
						break;
					case 1: // WIN
						win();
						break;
					case 2: // MOVE_RESPONSE
						showMoveResponse(args);
						break;
					case 3: // YOUR_TURN
						move();
						break;
					case 4: // DRAW
						System.out.println("Empate.");
						break;
					default:
						System.out.println("Invalid action");
						break;

				}
				return;
			}
		}
		System.out.println("Invalid action: " + action);
	}


	private static void processClientMove(String move) {
		
		int black = 0;
		int white = 0;		

		for (int i = 0; i < 4; i++) {
			if (move.charAt(i) == combination[i]) {
				black++;
			} else if (String.valueOf(combination).indexOf(move.charAt(i)) != -1) {
				white++;
			}
		}

		if (black == 4) { // CLIENTE GANA
			send("WIN\nnull\n");
			clearScreen();
			System.out.println("El oponente ha acertado, has perdido :(");
			lose = true;

		} else { // CLIENTE NO GANA
			String result = "B" + black + "W" + white;
			send("MOVE_RESPONSE\n" + result + "\n");
		}
	}

	private static void send(String message) {
		out.println(message);
	}


	private static void showMoveResponse(String response) {

		log[turn++] += " --> " + response;


		clearScreen();
		for (int i = 0; i < turn; i++) {
			System.out.println(log[i]);
		}


		System.out.println("\nResultado: " + log[turn - 1]);
		System.out.println("Esperando a los oponentes...\n");

		send("YOUR_TURN\n" + "null" + "\n");

		
	}

	private static void move() {

		if (turn > totalTurns - 1) return;

		System.out.print("Te toca, introduce tu jugada: ");

		String move;

		do {
			move = System.console().readLine();
			System.out.print("INTRODUCE 4 LETRAS:\n");
		} while (move.length() != 4);


		log[turn] = move;

		send("MOVE\n" + move + "\n");
	}

	private static void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		try {
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		} catch(IOException ex) {

		} 
		catch (InterruptedException ex) {}
	}

	private static void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {}
	}

}