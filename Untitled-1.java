class ClientThread extends Thread {


	private Socket socket;
	private int id;
	private char[] combination;

	private Writer out;
	private BufferedReader in;


	public ClientThread(Socket socket, int id, char[] combination) {
		this.socket = socket;
		this.id = id;
		this.combination = combination;
	}

	public void run() {
		try {
			manageRequest();
		} catch(UnknownHostException e) {
			System.out.println("Host desconocido");
		} catch(IOException e) {

			System.out.println("Juagador desconectado " + id);
		}
	}

	private void manageRequest() throws UnknownHostException, IOException {


		System.out.println("Tengo que adivinar: " + String.valueOf(combination));


		OutputStream outputStream = socket.getOutputStream();
		InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());

		
		out = new OutputStreamWriter(outputStream, "ASCII");
		in = new BufferedReader(inputStreamReader);


		if (!in.readLine().equals("JOIN_GAME")) {
			socket.close();
		}


		for (int i = 0; i < 15; i++) {

			// Espero turno
			while (Server.turn != id) {
				try {
					sleep(2000);
				} catch (InterruptedException e) {}
			}

			if (Server.someoneWon >= 0) {
				String msg = "LOSE\n" + Server.someoneWon + "\n";
				out.write(msg);
				out.flush();
				break;
			}
			
			String msg = "MOVE\nnull\n";
			out.write(msg);
			out.flush();


			String[] message = readClientRequest();
			chooseAction(message);


			Server.updateTurn();
		}
	}

	private String[] readClientRequest() {

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

	private void chooseAction(String[] message) {
		String action = message[0];
		String args = message[1];

		for (int i = 0; i < Server.actions.length; i++) {
			if (Server.actions[i].equals(action)) {

				switch (i) {
					
					case 0: // C_MOVES
						processClientMove(args);
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


	private void processClientMove(String move) {
		
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
			send("WIN\nGANASTE!!\n");
			Server.someoneWon = id;

		} else { // CLIENTE NO GANA
			String result = "B" + black + "W" + white;
			send("MOVE_RESPONSE\n" + result + "\n");
		}
	}

	private void send(String message) {
		try {
			out.write(message);
			out.flush();
		} catch (IOException e) {
			System.out.println("Error al enviar");
		}
	}

	
}