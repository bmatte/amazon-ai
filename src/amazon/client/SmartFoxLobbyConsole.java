package amazon.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class SmartFoxLobbyConsole implements SmartFoxLobby {
	
	SmartFoxClient client;
	
	public SmartFoxLobbyConsole(SmartFoxClient client) {
		this.client = client; 
	}
		
	@Override
	public void receiveGameList(ArrayList<String> list) {
		
		if(list.isEmpty()){ // Empty check
			System.out.println("no rooms found.");
			return;
		}
		
		//list room names
		System.out.println("List of Game rooms:");
		int roomIndexID = 0;
		for (String roomName : list) { 
			System.out.println("ID#: " + roomIndexID++ + " name" + roomName);
		}
		System.out.print("input room id:  ");
		Scanner reader = new Scanner(System.in);  
		
		boolean error = true;
		do{ //loop if wrong room number is chosen
			int roomID = 0;
			while(error){ // check for errors and invalid input
				try {
					System.out.println("Enter a number: ");
					roomID = reader.nextInt();
					if(roomID < 0 || roomID > roomIndexID){ //valid range check
						System.out.println("not a room number");
					}else{
						error = false;
					}
				} catch (Exception e) {
					System.out.println("not a valid input");
				}
			}
			
			String roomName = list.get(roomID);
			
			System.out.println("join room "+ roomName  + "?  y/n"); //Confirmation 
			String yn = "";
			while(yn != "y" && yn != "n" ){
				yn = reader.next();
				yn = yn.toLowerCase();
			}
			
			if(yn == "y"){
				client.joinRoom(roomID);
			}else{
				error = true;
			}
		}while(error);
		
		
		
		reader.close();
	}

}
