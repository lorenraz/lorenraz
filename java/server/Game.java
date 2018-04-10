package main.java.server;

public abstract class Game {
	
	Room room;
	boolean canGameQuit = false;	//if 3 rounds passed the gane can be done
	
	public abstract void dealSelectedAnswer(Player p, int indexOfAns);
	public abstract void askQuestion();
	public abstract void startGame(int numOfPlayers);
	public abstract void playerResponse(Player p, String answer);
	
	public void setRoom(Room r){
		this.room = r;
	}
	
	public boolean getCanGameQuit(){
		return this.canGameQuit;
	}
	
}
