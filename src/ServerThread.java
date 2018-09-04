import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

/**
 * Each instantiation of ServerThread handles an individual {@link Client} 
 * and communicates via a Socket using various {@link Message} subclasses.
 * Each ServerThread has access to the Game its Client is part of and can make
 * calls to the Server. 
 * @author Milan Goyal
 *
 */
public class ServerThread extends Thread {


	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private Server sr;
	private Game game;
	public ServerThread(Socket s, Server sr) {
		try {
			this.sr = sr;
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
			this.start();
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread constructor: " + ioe.getMessage());
		}
	}


	/**
	 * Sends a message back to Client via the socket output stream.
	 * @param m
	 */
	public void sendMessage(Message m) {
		try {
			oos.writeObject(m);
			oos.flush();
		} catch (IOException ioe) {
			System.out.println("ioe: " + ioe.getMessage());
		}
	}
	
	public void setGame(Game game) {
		this.game = game;
	}
	
	/**
	 * Continuously reads incoming {@link Message} from Client via socket and depending on Message subclass,
	 * performs a specified action.
	 */
	public void run() {
		try {
			Message initial =  new JoinOrStart();
			sendMessage(initial);
			while(true) {
				Message m = (Message)ois.readObject();
				if (m instanceof JoinOrStartResponse) {
					String responseText = m.getMessage();
					int response = Integer.parseInt(responseText);
					if (response == 1) {
						Message cm = new NameGame();
						sendMessage(cm);
					}
					else if (response == 2) {
						Message cm = new JoinNameGame();
						sendMessage(cm);
					}
				}
				if (m instanceof NameGameResponse) {
					String responseText = m.getMessage();
					if (sr.gameExists(responseText)) {
						Message cm = new NameGameExists();
						sendMessage(cm);
					}
					else {
						Message cm = new NumberPlayers(responseText);
						sendMessage(cm);
					}
				}
				if (m instanceof NumberPlayersResponse) {
					NumberPlayersResponse m_2 = (NumberPlayersResponse) m;
					String gameName = m_2.getGameName();
					String responseText = m_2.getMessage();
					int players = Integer.parseInt(responseText);
					sr.createGame(gameName, players, this);
					if (players == 1) {
						Message cm = new StartingGameJoiner();
						sendMessage(cm);
						sr.queryBrawlers(gameName);
					}
					else {
						Message cm = new WaitingToConnect();
						sendMessage(cm);
					}
				}
				if (m instanceof JoinNameGameResponse) {
					String responseText = m.getMessage();
					if (!sr.gameExistsCaseSensitive(responseText)) {
						Message cm = new JoinNameGameNotExist();
						sendMessage(cm);
					}
					else {
						boolean joined = sr.joinGame(responseText, this);
						if (!joined) {
							Message cm = new JoinNameGameFull();
							sendMessage(cm);
						}
						else {
							Message cm = new StartingGameJoiner();
							sendMessage(cm);
							sr.queryBrawlers(responseText);
						}
					}
				}
				if (m instanceof ChooseBrawlersResponse) {
					ChooseBrawlersResponse m_2 = (ChooseBrawlersResponse) m;
					List<Integer> brawlers = m_2.getList();
					sr.setBrawlers(brawlers, game, this);
				}
				if (m instanceof GameMessageResponse) {
					String responseText = m.getMessage();
					int response;
					Brawler currBrawler;
					if (game.getGameCreator() == this) {
						currBrawler = game.getP1Brawlers()[game.getP1CurrentBrawler()];
					}
					else {
						currBrawler = game.getP1Brawlers()[game.getP1CurrentBrawler()];
					}
					try {
						response = Integer.parseInt(responseText);
						int size = currBrawler.getAbilities().size();
						if (response < 1 || response > size + 1) {
							String line = "Invalid!";
							line += "\nChoose a move:\n";
							for (int i = 0; i < currBrawler.getAbilities().size(); i++) {
								int j = i+1;
								line += j + ") " + currBrawler.getAbilities().get(i).getName() + ", " + currBrawler.getAbilities().get(i).getType() + ", " + currBrawler.getAbilities().get(i).getDamage() + "\n";
							}
							Message cm = new GameMessage(line);
							sendMessage(cm);
							continue;
						}
						sr.attackReceived(response, game, this);
					}
					catch (Exception e) {
						String line = "Invalid!";
						line += "\nChoose a move:\n";
						for (int i = 0; i < currBrawler.getAbilities().size(); i++) {
							int j = i+1;
							line += j + ") " + currBrawler.getAbilities().get(i).getName() + ", " + currBrawler.getAbilities().get(i).getType() + ", " + currBrawler.getAbilities().get(i).getDamage() + "\n";
						}
						Message cm = new GameMessage(line);
						sendMessage(cm);
					}
					
				}
			}
		} catch (IOException ioe) {
			System.out.println("ioe in ServerThread.run(): " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
		}
	}
}