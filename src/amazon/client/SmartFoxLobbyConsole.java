package amazon.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class SmartFoxLobbyConsole implements SmartFoxLobby {

	SmartFoxClient client;

	public SmartFoxLobbyConsole() {
	}

	@Override
	public void receiveGameList(ArrayList<String> list) {

		// Check if empty.
		if (list.isEmpty()) {
			System.out.println("No rooms found!");
			return;
		}

		// List room names.
		System.out.println("List of game rooms:");
		int roomIndexID = 0;
		for (String roomName : list) {
			System.out.println(roomIndexID++ + ": " + roomName);
		}
		System.out.print("Enter room ID: ");
		Scanner reader = new Scanner(System.in);

		boolean error = true;
		do { // Loop if wrong room number is chosen.
			int roomID = 0;
			while (error) { // Check for errors and invalid input.
				try {
					roomID = reader.nextInt();
					if (roomID < 0 || roomID > roomIndexID) { // Valid range
																// check.
						System.err.println("Not a room number!");
					} else {
						error = false;
					}
				} catch (Exception e) {
					System.err.println("Not a valid input!");
				}
			}

			// String roomName = list.get(roomID);
			//
			// System.out.println("join room "+ roomName + "? y/n"); //Room
			// number confirmation.
			// String yn = "";
			// while(yn != "y" && yn != "n" ){
			// yn = reader.next();
			// yn = yn.toLowerCase();
			// }
			//
			// if(yn == "y"){
			client.joinRoom(roomID);
			// }else{
			// error = true;
			// }
		} while (error);

		reader.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see amazon.client.SmartFoxLobby#setClient(amazon.client.SmartFoxClient)
	 */
	@Override
	public void setClient(SmartFoxClient client) {
		this.client = client;
	}

}
