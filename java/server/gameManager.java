package main.java.server;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class gameManager {

	ConcurrentLinkedDeque<String> listOfGame = new ConcurrentLinkedDeque<String>();
	ConcurrentHashMap<String, Room> mapRoomsByName = new ConcurrentHashMap<String, Room>();
	ConcurrentHashMap<String, Game> mapOfGames = new ConcurrentHashMap<String, Game>();
//	ConcurrentHashMap<Player, Game> gameByPlayer = new ConcurrentHashMap<Player, Game>();
	Game game;
	

	public gameManager(){
		listOfGame.addFirst("BLUFFER");
	//	mapOfGames.put("BLUFFER",new BlufferGame());
	}
	
	public ConcurrentHashMap<String, Room> getMapRoomsByName(){
		return this.mapRoomsByName;
	}
	
	public ConcurrentLinkedDeque<String> getListOfGame(){
		return this.listOfGame;
	}
	
	public void addGame(){
		listOfGame.addFirst("BLUFFER");
	}
}
