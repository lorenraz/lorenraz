package main.java.server;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

import main.java.tokenizer.StringMessage;


public class protocolTBGP<T> implements AsyncServerProtocol<T> {

	gameManager _gameManager = new gameManager();
	ConcurrentHashMap<String, ProtocolCallback<T>> mapByName = new ConcurrentHashMap<String, ProtocolCallback<T>>();
	ConcurrentHashMap<ProtocolCallback<T>, Player> mapByCallback= new ConcurrentHashMap<ProtocolCallback<T>, Player>();
	private boolean _shouldClose = false;
	private boolean _connectionTerminated = false;

	@Override
	public void processMessage(Object msg, ProtocolCallback callback) {


		String[] clientMessage = ((String) msg).split(" ");
		if(clientMessage[0].equals("NICK")) {
			//	System.out.println("success nick!");
			if(mapByName.containsKey(clientMessage[1])){		//if this nick name already exist
				try {
					callback.sendMessage(("SYSMSG "+msg+ " REJECTED name is teken, pick another\n"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{	//creat this name
				mapByName.put(clientMessage[1], callback);
				Player player = new Player((String) msg, callback);
				mapByCallback.put(callback, player);
				try {
					callback.sendMessage(("SYSMSG "+ msg + " ACCEPTED\n"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if(clientMessage[0].equals("JOIN")){
			String roomName = clientMessage[1];
			Room room = _gameManager.mapRoomsByName.get(roomName);
			Player player = mapByCallback.get(callback);
			String reply;
			if(room == null){
				room = new Room(roomName);
				room.addPlayer();
				room.addPlayer(player);
				player.setRoom(roomName);
				_gameManager.mapRoomsByName.put(roomName,room);
				mapByCallback.get(callback).sendMSG("SYSMSG JOIN ACCEPTED\n");
			}
			else if(!room.getisGameStarted()){	//if the game hasn't started yet
				ServerResult result;
				result = room.addPlayer(mapByCallback.get(callback));
				room.addPlayer();
				room.addPlayer(player);
				player.setRoom(roomName);
				try {
					callback.sendMessage("SYSMSG JOIN ACCEPTED\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (player.getRoomName() != null){		//if the player is already playing in other room
				if(!_gameManager.getMapRoomsByName().get(player.getRoomName()).getisGameStarted()){
					mapByCallback.get(callback).sendMSG("SYSMSG JOIN REJECTED");
				}
			}
			else{	//the game already started
				mapByCallback.get(callback).sendMSG("SYSMSG JOIN REJECTED");
			}
		}
		if (clientMessage[0].equals("MSG")){
			//find the room for of the player
			String roomName = mapByCallback.get(callback).getRoomName();
			String senderName = mapByCallback.get(callback).getNick();
			String toSend = "USRMSG " + senderName + ":" + ((String) msg).substring(4, ((String)msg).length());
			_gameManager.getMapRoomsByName().get(roomName).sendMsg(senderName, toSend);
		}

		if (clientMessage[0].equals("LISTGAMES")){
			String listGames = "";
			for (String gameName : _gameManager.getListOfGame()){
				listGames = listGames + gameName;
			}
			mapByCallback.get(callback).sendMSG("SYSMSG LISTGAMES ACCEPTED " + listGames + "\n");
		}

		if (clientMessage[0].equals("STARTGAME")){
			Player player = mapByCallback.get(callback);
			for (String gameName : _gameManager.getListOfGame()){
				if(clientMessage[1].equals(gameName)){
					String roomName = mapByCallback.get(callback).getRoomName();
					Room roomToStart = _gameManager.getMapRoomsByName().get(roomName);
					if(roomToStart.getisGameStarted()){		//if the game already started
						player.sendMSG("SYSMSG STARTGAME REJECTED\n");
					}
					else{
						roomToStart.gameStart();
						player.sendMSG("SYSMSG STARTGAME ACCEPTED\n");
						roomToStart.startGame(clientMessage[1], roomToStart.getNumOfPlayers());
					}
				}
			}
		}

		if (clientMessage[0].equals("TXTRESP")){
			String ansOfPlayer = "";
			String roomName = mapByCallback.get(callback).getRoomName();
			Room room = _gameManager.getMapRoomsByName().get(roomName);
			for(int i=1; i<clientMessage.length; i++){
				ansOfPlayer = ansOfPlayer + clientMessage[i];
			}
			room.playerResponse(mapByCallback.get(callback), ansOfPlayer);
		}

		if (clientMessage[0].equals("SELECTRESP")){
			int answer = Integer.parseInt(clientMessage[1]);
			int numOfPlayers = _gameManager.getMapRoomsByName().get(mapByCallback.get(callback).getRoomName()).getNumOfPlayers();
			boolean flag = false;
			String roomName = mapByCallback.get(callback).getRoomName();
			Room room = _gameManager.getMapRoomsByName().get(roomName);
			for(int i=0; i<=numOfPlayers&&!flag; i++){
				if(answer == i){
					flag = true;
					room.dealSelectedAnswer(mapByCallback.get(callback), answer);
				}
			}
			if(!flag)
				mapByCallback.get(callback).sendMSG("SYSMSG SELECTRESP UNIDENTIFIED\n");
		}

		if (clientMessage[0].equals("QUIT")){
			Player player = mapByCallback.get(callback);
			String playerName = player.getNick();
			String roomName = mapByCallback.get(callback).getRoomName();
			Room room = _gameManager.getMapRoomsByName().get(roomName);
			if(room.getIsGameFinished()){	
				player.sendMSG("SYSMSG QUIT ACCEPTED\n");
				room.closeConnection(player);
				mapByName.remove(playerName);
				mapByCallback.remove(callback);
			} else
				player.sendMSG("SYSMSG QUIT REJECTED\n");
		}
	}

	@Override
	public boolean isEnd(T msg) {
		if(msg instanceof String ){
			if(((String)msg).equals("QUIT"))
				return true;
		}
		return false;
	}

	@Override
	public boolean shouldClose() {	
		return _shouldClose;
	}

	@Override
	public void connectionTerminated() {
		_connectionTerminated = true;
	}
}
