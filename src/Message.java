import java.io.Serializable;
import java.util.List;

/**
 * Primary method of communication between {@link ServerThread} and {@link Client}.
 * Depending on subclass, they will perform different actions once a Message has
 * been received. 
 * @author Milan Goyal
 *
 */
public class Message implements Serializable {
	public static final long serialVersionUID = 1;
	private String message;
	
	public Message(String message) {
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
}

class JoinOrStart extends Message {
	public static final long serialVersionUID = 1;
	public JoinOrStart() {
		super("Please make a choice:\n1) Start Game\n2) Join Game\n");
	}
}

class JoinOrStartResponse extends Message {
	public static final long serialVersionUID = 1;
	public JoinOrStartResponse(String message) {
		super(message);
	}
}

class NameGame extends Message {
	public static final long serialVersionUID = 1;
	public NameGame() {
		super("What will you name your game?\n");
	}	
}

class NameGameExists extends Message {
	public static final long serialVersionUID = 1;
	public NameGameExists() {
		super("This game already exists!\nWhat will you name your game?\n");
	}
}

class NameGameResponse extends Message {
	public static final long serialVersionUID = 1;
	public NameGameResponse(String message) {
		super(message);
	}
}

class JoinNameGame extends Message {
	public static final long serialVersionUID = 1;
	public JoinNameGame() {
		super("What is the name of the game you wish to join?\n");
	}	
}

class JoinNameGameResponse extends Message {
	public static final long serialVersionUID = 1;
	public JoinNameGameResponse(String message) {
		super(message);
	}
}

class JoinNameGameNotExist extends Message {
	public static final long serialVersionUID = 1;
	public JoinNameGameNotExist() {
		super("This game does not exist!\nWhat is the name of the game you wish to join?\n");
	}
}

class JoinNameGameFull extends Message {
	public static final long serialVersionUID = 1;
	public JoinNameGameFull() {
		super("This game is full!\nWhat is the name of the game you wish to join?\n");
	}
}

class NumberPlayers extends Message {
	public static final long serialVersionUID = 1;
	private String gameName;
	public NumberPlayers(String gameName) {
		super("How many players?\n1 or 2\n");
		this.gameName = gameName;
	}
	public String getGameName() {
		return gameName;
	}
}

class NumberPlayersResponse extends Message {
	public static final long serialVersionUID = 1;
	private String gameName;
	public NumberPlayersResponse(String message, String gameName) {
		super(message);
		this.gameName = gameName;
	}
	public String getGameName() {
		return gameName;
	}
}

class WaitingToConnect extends Message {
	public static final long serialVersionUID = 1;
	public WaitingToConnect() {
		super("Waiting for players to connect...\n");
	}
}

class StartingGameJoiner extends Message {
	public static final long serialVersionUID = 1;
	public StartingGameJoiner() {
		super("Starting game...\n");
	}
}

class StartingGameCreator extends Message {
	public static final long serialVersionUID = 1;
	public StartingGameCreator() {
		super("Player 2 connected!\nStarting game...\n");
	}
}

class ChooseBrawlers extends Message {
	public static final long serialVersionUID = 1;
	private int size;
	public ChooseBrawlers(String m, int size) {
		super(m);
		this.size = size;
	}
	public int getSize() {
		return size;
	}	
}

class ChooseBrawlersResponse extends Message {
	public static final long serialVersionUID = 1;
	private List<Integer> list;
	public ChooseBrawlersResponse(List<Integer> list) {
		super("");
		this.list = list;
	}
	public List<Integer> getList() {
		return list;
	}	
}

class GameMessage extends Message {
	public static final long serialVersionUID = 1;
	public GameMessage(String m) {
		super(m);
	}
}

class GameMessageResponse extends Message {
	public static final long serialVersionUID = 1;
	public GameMessageResponse(String message) {
		super(message);
	}
}

class GameOver extends Message {
	public static final long serialVersionUID = 1;
	public GameOver(String message) {
		super(message);
	}
}