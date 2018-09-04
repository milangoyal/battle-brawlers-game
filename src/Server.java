import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
/**
 * Threaded server that creates multiple {@link ServerThread.class} to handle incoming client requests.
 * Holds vectors of all active ServerThreads and Games. 
 * @author Milan Goyal
 *
 */
public class Server {

	private Vector<ServerThread> serverThreads;
	private Vector<Game> gameList;
	private Characters brawlerData;
	
	/**
	 * Creates a ServerSocket at given port and uses list of {@link Brawler} from inputed data as
	 * changeable game characters. Character data is generally parsed from a valid JSON file before being passed
	 * into Server constructor.
	 * @param port
	 * @param data
	 * @throws IOException
	 */
	public Server(int port, Characters data) throws IOException {
		ServerSocket ss = new ServerSocket(port);
		serverThreads = new Vector<ServerThread>();
		gameList = new Vector<Game>();
		brawlerData = data;
		System.out.println("Success!");
		while(true) {
			Socket s = ss.accept(); // blocking
			System.out.println("Connection from: " + s.getInetAddress());
			ServerThread st = new ServerThread(s, this);
			serverThreads.add(st);
		}
	}
	
	/*
	 * The following functions are called by {@link ServerThread} in response to client requests.
	 * Examples including creating, joining, and initiating moves within games. 
	 */
	
	public boolean gameExists(String name) {
		for (int i = 0; i < gameList.size(); i++ ) {
			if (name.equalsIgnoreCase(gameList.get(i).getGameName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean gameExistsCaseSensitive(String name) {
		for (int i = 0; i < gameList.size(); i++ ) {
			if (name.equals(gameList.get(i).getGameName())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a new {@link Game} and adds it to the Server's list of games.
	 * The game is associated with the ServerThread that created it and the ServerThread
	 * is associated with the Game that is now part of. 
	 * @param gameName
	 * @param players
	 * @param st
	 */
	public void createGame(String gameName, int players, ServerThread st) {
		Game new_game = new Game(gameName, players, st);
		gameList.add(new_game);
		st.setGame(new_game);
	}
	
	/**
	 * Adds ServerThread to Game if Game with given name exists and is not full.
	 * Returns true if join successful and false otherwise. 
	 * @param gameName
	 * @param st
	 * @return
	 */
	public boolean joinGame(String gameName, ServerThread st) {
		for (int i = 0; i < gameList.size(); i++) {
			if (gameList.get(i).getGameName().equals(gameName)) {
				boolean joined = gameList.get(i).addPlayer(st);
				if (joined) {
					Message cm = new StartingGameCreator();
					gameList.get(i).getGameCreator().sendMessage(cm);
					st.setGame(gameList.get(i));
					return true;
				}
				else {
					return false;
				}
			}
		}
		return false;
	}
	
	/**
	 * Sends a message to the ServerThreads within the input Game asking them 
	 * to select their Brawlers so that game may begin. 
	 * @param gameName
	 */
	public void queryBrawlers(String gameName) {
		String query = "Choose 3 Brawlers:\n";
		int size = brawlerData.getBrawlers().size();
		for (int i = 0; i < brawlerData.getBrawlers().size(); i++) {
			int j = i+1;
			query = query + j + ") " + brawlerData.getBrawlers().get(i).getName() + "\n";
		}
		for (int i = 0; i < gameList.size(); i++) {
			if (gameList.get(i).getGameName().equals(gameName)) {
				Message cm = new ChooseBrawlers(query, size);
				if (gameList.get(i).getNumberPlayers() == 1) {
					gameList.get(i).getGameCreator().sendMessage(cm);
				}
				else {
					gameList.get(i).getGameCreator().sendMessage(cm);
					gameList.get(i).getGameJoiner().sendMessage(cm);
				}
			}
		}
	}
	
	/**
	 * Sets the chosen Brawlers for a given ServerThread. If the Game is 1-player, Brawlers for
	 * the computer are randomly chosen. If both players have set their Brawlers, sends a message to 
	 * the ServerThreads announcing chosen Brawlers and commences battle. 
	 * @param list
	 * @param game
	 * @param st
	 */
	public void setBrawlers(List<Integer> list, Game game, ServerThread st) {
		Brawler b1 = new Brawler(brawlerData.getBrawlers().get(list.get(0)-1));
		Brawler b2 = new Brawler(brawlerData.getBrawlers().get(list.get(1)-1));
		Brawler b3 = new Brawler(brawlerData.getBrawlers().get(list.get(2)-1));
		
		if (game.getGameCreator() == st) {
			game.setP1Brawlers(b1, b2, b3);
		}
		else {
			game.setP2Brawlers(b1, b2, b3);
		}
		//Randomly setting Brawlers for Computer in case of 1-player games
		if (game.getNumberPlayers() == 1) {
			Brawler b1Random = new Brawler(brawlerData.getBrawlers().get(new Random().nextInt(list.size())));
			Brawler b2Random = new Brawler(brawlerData.getBrawlers().get(new Random().nextInt(list.size())));
			Brawler b3Random = new Brawler(brawlerData.getBrawlers().get(new Random().nextInt(list.size())));

			game.setP2Brawlers(b1Random, b2Random, b3Random);
			Brawler p1_b1 = game.getP1Brawlers()[game.getP1CurrentBrawler()];
			Brawler p2_b1 = game.getP2Brawlers()[game.getP2CurrentBrawler()];
			String cm1 = "\nExcellent!\n";
			cm1 += "You sent out " + p1_b1.getName() + "!\n";
			cm1 += "Your opponent sent out " + p2_b1.getName() + "!\n";
			cm1 += "\nChoose a move:\n";
			for (int i = 0; i < p1_b1.getAbilities().size(); i++) {
				int j = i+1;
				cm1 += j + ") " + p1_b1.getAbilities().get(i).getName() + ", " + p1_b1.getAbilities().get(i).getType() + ", " + p1_b1.getAbilities().get(i).getDamage() + "\n";
			}
			Message cm_1 = new GameMessage(cm1);
			game.resetInputs();
			game.getGameCreator().sendMessage(cm_1);

		}
		//Sends message to both ServerThreads if both players have selected their Brawlers, commencing battle.
		else if (game.bothInputsReceived()) {
			Brawler p1_b1 = game.getP1Brawlers()[game.getP1CurrentBrawler()];
			Brawler p2_b1 = game.getP2Brawlers()[game.getP2CurrentBrawler()];
			
			String cm1 = "\nExcellent!\n";
			String cm2 = "\nExcellent!\n";
			cm1 += "You sent out " + p1_b1.getName() + "!\n";
			cm2 += "You sent out " + p2_b1.getName() + "!\n";
			cm1 += "Your opponent sent out " + p2_b1.getName() + "!\n";
			cm2 += "Your opponent sent out " + p1_b1.getName() + "!\n";
			cm1 += "\nChoose a move:\n";
			cm2 += "\nChoose a move:\n";
			for (int i = 0; i < p1_b1.getAbilities().size(); i++) {
				int j = i+1;
				cm1 += j + ") " + p1_b1.getAbilities().get(i).getName() + ", " + p1_b1.getAbilities().get(i).getType() + ", " + p1_b1.getAbilities().get(i).getDamage() + "\n";
			}
			for (int i = 0; i < p2_b1.getAbilities().size(); i++) {
				int j = i+1;
				cm2 += j + ") " + p2_b1.getAbilities().get(i).getName() + ", " + p2_b1.getAbilities().get(i).getType() + ", " + p2_b1.getAbilities().get(i).getDamage() + "\n";
			}
			Message cm_1 = new GameMessage(cm1);
			Message cm_2 = new GameMessage(cm2);
			game.resetInputs();
			game.getGameCreator().sendMessage(cm_1);
			game.getGameJoiner().sendMessage(cm_2);
		}
	}
	
	/**
	 * Sets the attack choice for input ServerThread. If 1-player game, Computer's attack choice
	 * is randomly selected. If both players have set their attacks, the attacks are initiated
	 * and resulting messages are sent to all players. If game is over after last attack, removes the Game
	 * from Server's list of games. 
	 * @param ability
	 * @param game
	 * @param st
	 */
	public void attackReceived(int ability, Game game, ServerThread st) {
		if (game.getGameCreator() == st) {
			game.setP1_inputReceived(ability);
		}
		else if (game.getGameJoiner() == st) {
			game.setP2_inputReceived(ability);
		}
		if (game.getNumberPlayers() == 1) {
			int abilityRandom = new Random().nextInt(game.getP2Brawlers()[game.getP2CurrentBrawler()].getAbilities().size()) + 1;
			game.setP2_inputReceived(abilityRandom);
		}
		if (game.bothInputsReceived()) {
			List<Message> list = game.fight();
			game.getGameCreator().sendMessage(list.get(0));
			if (game.getNumberPlayers() == 2) {
				game.getGameJoiner().sendMessage(list.get(1));
			}
		}
		if (game.isGameOver()) {
			for(int i = 0; i < gameList.size(); i++) {
				if (gameList.get(i) == game) {
					gameList.remove(i);
				}
			}
		}
	}
	
	public static void main(String [] args) {
		Scanner scan = new Scanner(System.in);
		String input;
		Gson gson = new Gson();
		JsonReader reader = null;
		Characters data;
		while (true) {
			
			System.out.println("What is the name of the input file?");
			input = scan.nextLine();
				
			try{
				reader = new JsonReader( new FileReader(input));
				data = gson.fromJson(reader, Characters.class);
				break;
			}
			catch(FileNotFoundException fnfe) {
				System.out.println("That file could not be found");
				System.out.println();
			}
			catch(JsonParseException jspe) {
				System.out.println(jspe.getMessage());
				System.out.println();
			}

		}
		while (true) {
			System.out.println("Please enter a valid port:");
			input = scan.nextLine();
			int portNumber = Integer.parseInt(input);
			if (portNumber < 1024 || portNumber > 49151) {
				System.out.println("Invalid Port!");
				continue;
			}
			try {
				Server cr = new Server(portNumber, data);
				break;
			} catch (IOException ioe) {
				System.out.println("Invalid Port!");
			}
			
			
		}
	}
}