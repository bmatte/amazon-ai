package amazon.client;

import java.util.ArrayList;

/**
 * Interface lobby for selecting a Smart Fox server game room.
 */
public interface SmartFoxLobby {
	/**
	 * Called upon SmartFoxClient server login, to receive the list of game room
	 * names.
	 */
	public void receiveGameList(ArrayList<String> list);
	
	/**
	 * Set the client 
	 * @param client Client to set.
	 */
	public void setClient(SmartFoxClient client);
}
