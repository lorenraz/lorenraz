package main.java.server;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import main.java.tokenizer.StringMessage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;


public class BlufferGame extends Game{

	ConcurrentHashMap<Player, String> mapOfAnswares = new ConcurrentHashMap<Player, String>();	//map of the players's answares so it will be shuffeled
	ConcurrentHashMap<String, Player> mapByAnswers = new ConcurrentHashMap<String, Player>();	//string is the answer
	ConcurrentHashMap<Player, Integer> mapOfPoints = new ConcurrentHashMap<Player, Integer>();		//map that sums the points before sending it to the player
	ConcurrentHashMap<Player, Integer> mapOfPointsPerRound = new ConcurrentHashMap<Player, Integer>();
	int leftPlayersNeedToAnswar;
	int numOfPlayers;
	int indexOfRealAnswer;
	int leftPlayersToChooseAns;
	String[] arrayOfAnswers;
	String realAnswer; 
	String question;
	int[] indexsCantChoose = new int[3];		//Indexes of the question already been asked
	int i = 0;	//make sure we don't choose the same question more the once
	questions[] questionsArray;
	Player fictiviPlayer = new Player("fictiviPlayer", null);	
	int roundNumber = 0;

	public BlufferGame(Room room){	//Constructor
		super();
		this.room = room;
		//jason...
		Gson gson = new Gson();
		BufferedReader reader = null;

		try{
			reader = new BufferedReader(new FileReader ("serverTest.json"));
		}
		catch(FileNotFoundException e){
			e.printStackTrace();
		}
		AppData appData = gson.fromJson(reader, AppData.class);
		this.questionsArray = appData.getQuestions();		//assuming valid input (at least 3 questions) 
		indexsCantChoose = new int[questionsArray.length];
		//end of jason..
		Iterator<Player> it= room.playerIt();
		while (it.hasNext()){
			mapOfPoints.put(it.next(), new Integer (0));
		}
		for(int i = 0; i<3; i++){
			indexsCantChoose[i] = -1;
		}
		askQuestion();
	}

	public void setRealAnswer(String realAnswer){
		this.realAnswer = realAnswer;
	}

	public void setQuestion(String question){
		this.question = question;
	}

	public void startGame(int numOfPlayers){
		System.out.println("start in bluffergame");
		this.leftPlayersNeedToAnswar = numOfPlayers;
		this.numOfPlayers = numOfPlayers;
		arrayOfAnswers = new String[this.numOfPlayers + 1];
		this.leftPlayersToChooseAns = numOfPlayers;
	}

	public void askQuestion() {	//ask all players a question using jason
		this.roundNumber++;
		int indexOfQuestion = (int) (Math.random()*questionsArray.length);
		for(int j = 0; j < 3; j++){		//make sure we don't choose the same question more the once
			if (indexOfQuestion == indexsCantChoose[j]){
				indexOfQuestion = (int) (Math.random()*questionsArray.length);
				System.out.println("indexOfQuestion in the for " + indexOfQuestion);
				j = -1;
			}
		}
		indexsCantChoose[i] = indexOfQuestion;
		i++;
		this.question = questionsArray[indexOfQuestion].getQuestionText();		//the question to ask
		this.realAnswer = questionsArray[indexOfQuestion].getRealAnswer();		//the real answer	
		Iterator<Player> itr= room.playerIt();	//set all points of players for this round to be 0
		while (itr.hasNext()){
			mapOfPointsPerRound.put(itr.next(), new Integer (0));
		}
		mapOfAnswares.put(fictiviPlayer, realAnswer);	//add the real answer to the map
		String QuestionToAsk = "ASKTXT " + this.question +"\n"; 
		room.sendMsg("server",QuestionToAsk);
	}

	public void playerResponse(Player p, String answer){
		answer = answer.toLowerCase();
		if(answer.equals(realAnswer)){
			p.sendMSG("SYSMSG TXTRESP REJECTED\n");
		}
		else{
			mapOfAnswares.put(p, answer);	//add player's answer to the map
			mapByAnswers.put(answer, p);
			leftPlayersNeedToAnswar--;
			p.sendMSG("SYSMSG TXTRESP ACCEPTED\n");
			if(leftPlayersNeedToAnswar == 0){	//all the players gave their answer
				String optionalAnswers = "";
				int i = 0; 				//index of answer
				for (String value : mapOfAnswares.values()) {	//iterate the answers in mapOfAnswares
					arrayOfAnswers[i] = value;
					optionalAnswers = optionalAnswers +  i + "." + value + " ";
					i++;
				}
				room.sendMsg("server", "ASKCHOICES " +  optionalAnswers + "\n");
			}
		}
	}

	public void dealSelectedAnswer(Player p, int indexOfAns) {
		this.leftPlayersToChooseAns--;
		if(indexOfAns > arrayOfAnswers.length || arrayOfAnswers[indexOfAns].equals(null)){	//bad answer input
			p.sendMSG("SYSMSG SELECTRESP UNIDENTIFIED\n");
		}	
		else{	//the answer the player choose exist
			p.sendMSG("SYSMSG SELECTRESP ACCEPTED\n");
			mapOfAnswares.put(p, arrayOfAnswers[indexOfAns]);	//set the player's answer in the map
			if(arrayOfAnswers[indexOfAns].equals(this.realAnswer)){	//the player choose the real answer
				mapOfPoints.put(p, mapOfPoints.get(p) + 10);
				mapOfPointsPerRound.put(p, mapOfPointsPerRound.get(p) + 10);	//set player's points for this round
			}
			else{
				Player chosenPlayer = mapByAnswers.get(arrayOfAnswers[indexOfAns]);			//this player choose other player's answer
				mapOfPoints.put(chosenPlayer, mapOfPoints.get(chosenPlayer) + 5);
				mapOfPointsPerRound.put(chosenPlayer, mapOfPointsPerRound.get(chosenPlayer) + 5);	//set player's points for this round
			}
		}
		if(this.leftPlayersToChooseAns == 0){
			room.sendMsg("server", "GAMEMSG The correct answer is: " + realAnswer + "\n");
			for (Player key : mapOfPointsPerRound.keySet()) {				//iterate the map of points and send each player the points message
				//	key.setPoints(mapOfPoints.get(key));				//add points to the Player
				if(mapOfAnswares.get(key).equals(realAnswer))
					key.sendMSG("GAMEMSG correct! +" + mapOfPointsPerRound.get(key) + "pts\n");
				else
					key.sendMSG("GAMEMSG wrong! +" + mapOfPointsPerRound.get(key) + "pts\n");
			}
			if(this.roundNumber < 3){
				this.leftPlayersNeedToAnswar = this.numOfPlayers;
				this.leftPlayersToChooseAns = this.numOfPlayers;
				this.mapOfAnswares = new ConcurrentHashMap<Player, String>();
				this.mapByAnswers = new ConcurrentHashMap<String, Player>();
				askQuestion();
			}
			else{	
				String summary = "";
				for (Player key : mapOfPoints.keySet()){		//a summary of all players and their points
					summary = summary + key.getNick() + ": " + mapOfPoints.get(key) + "pts, ";
				}
				room.sendMsg("server", "GAMEMSG Summary: "+ summary + "\n");
				super.canGameQuit = true;		//the game finished after 3 rounds
				room.setIsGameFinished();
				room.terminateGame();
			}
		}
	}
	
}
