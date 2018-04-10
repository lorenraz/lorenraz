package main.java.server;
import java.io.IOException;

public class Player {
	String nick;
	String roomName;
	ProtocolCallback handler;
	int points = 0;
	
	
	public Player(String nick, ProtocolCallback handler){
		this.nick= nick;
		this.handler = handler;
	}
	
	public String getNick(){
		return nick;
	}
	
	public void setRoom(String roomName){
		this.roomName = roomName;
		
	}

	public String getRoomName() {
		return roomName;
	}
	
	public void setPoints(int pointsToAdd){
		this.points = this.points + pointsToAdd;
	}
	
	public int getPoints(){
		return this.points;
	}

	public void sendMSG(String toSend){
		try {
			handler.sendMessage(toSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}		
}
