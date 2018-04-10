package main.java.server;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Room {
	String name;
	ConcurrentLinkedQueue<Player> ClientsInTheRoom = new ConcurrentLinkedQueue<Player>();
	boolean isGameStarted = false;
	boolean isGameFinished = false;
	Game game;
	int numOfPlayers = 0;

	public Room (String name){	//Constructor
		this.name = name;
	}

	public String getName(){
		return this.name;
	}

	public boolean getisGameStarted(){
		return isGameStarted;
	}

	public boolean getIsGameFinished(){
		return this.isGameFinished;
	}

	public void setIsGameFinished(){
		this.isGameFinished = true;
	}

	public int getNumOfPlayers(){
		return this.numOfPlayers;
	}

	public void setNumOfPlayers(int num){
		numOfPlayers = num;
	}

	public void gameStart(){
		this.isGameStarted = true;
	}

	public void gameOver(){
		this.isGameStarted = false;
	}

	public void setGame(Game game){
		this.game = game;
	}

	public void addPlayer(){
		this.numOfPlayers++;
	}
	public Iterator<Player> playerIt(){
		return ClientsInTheRoom.iterator();
	}

	public void deletPlayers(Player p){		//delete specific player
		for (Player _player : ClientsInTheRoom) {
			if(p.equals(_player)){
				ClientsInTheRoom.remove(p);
			}
		}
	}

	public ServerResult addPlayer(Player player) {
		ServerResult result = ServerResult.REJECTED;
		if (!isGameStarted && ClientsInTheRoom.add(player)){
			result = ServerResult.ACCEPTED;
		}
		return result;
	}

	public void sendMsg(String senderName, String toSend)  {		//send a message to all the clients in this room
		for (Player p : ClientsInTheRoom) {
			if(!p.getNick().equals(senderName)){
				p.sendMSG(toSend);
			}
		}
	}

	public void startGame(String gameName, int numOfPlayers){
		if(gameName.equals("BLUFFER")){
			game = new BlufferGame(this);
		}
		game.startGame(numOfPlayers);
	}

	public void playerResponse(Player p, String ansOfPlayer){
		game.playerResponse(p, ansOfPlayer);
	}

	public void dealSelectedAnswer(Player player, int answer) {
		game.dealSelectedAnswer(player, answer);

	}

	public void closeConnection(Player player) {
		ClientsInTheRoom.remove(player);
	}

	public void terminateGame() {
		game = null;
	}
}



