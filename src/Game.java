import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents an active game between one or multiple {@link ServerThread}.
 * Holds and advances game state as inputs are received.
 * 
 * @author Milan Goyal
 *
 */
public class Game {
	private ServerThread gameCreator;
	private ServerThread gameJoiner = null;
	
	int p1_inputReceived = 0;
	int p2_inputReceived = 0;
	
	private Brawler[] p1_brawlers;
	private Brawler[] p2_brawlers;
	
	private boolean[] p1_alive;
	private boolean[] p2_alive;
	
	private String[] strongAgainst = {"water", "fire", "air", "earth", "lightning", "water"};
	private String[] weakAgainst = {"water", "lightning", "earth", "air", "fire", "water"};
	
	private String gameName;
	int players;
	
	boolean gameOver;
	
	public Game(String gameName, int players, ServerThread gameCreator) {
		this.gameName = gameName;
		this.players = players;
		this.gameCreator = gameCreator;
		gameOver = false;
	}
	
	public String getGameName() {
		return this.gameName;
	}
	
	public int getNumberPlayers() {
		return players;
	}
	
	public ServerThread getGameCreator() {
		return this.gameCreator;
	}
	
	public ServerThread getGameJoiner() {
		return this.gameJoiner;
	}
	
	public boolean addPlayer(ServerThread st) {
		if (players == 2 && gameJoiner == null) {
			this.gameJoiner = st;
			return true;
		}
		return false;
	}
	
	public void setP1Brawlers(Brawler b1, Brawler b2, Brawler b3) {
		p1_brawlers = new Brawler[3];
		p1_brawlers[0] = b1;
		p1_brawlers[1] = b2;
		p1_brawlers[2] = b3;
		p1_alive = new boolean[3];
		Arrays.fill(p1_alive, true);
		p1_inputReceived = 1;
	}
	
	public void setP2Brawlers(Brawler b1, Brawler b2, Brawler b3) {
		p2_brawlers = new Brawler[3];
		p2_brawlers[0] = b1;
		p2_brawlers[1] = b2;
		p2_brawlers[2] = b3;
		p2_alive = new boolean[3];
		Arrays.fill(p2_alive, true);	
		p2_inputReceived = 1;
	}
	
	public Brawler[] getP1Brawlers() {
		return p1_brawlers;
	}
	
	public Brawler[] getP2Brawlers() {
		return p2_brawlers;
	}
	
	/**
	 * Returns the Brawler that is currently fighting for Player 1. This Brawler's health
	 * cannot be 0.
	 * @return
	 */
	public int getP1CurrentBrawler() {
		for (int i = 0; i < 3; i++) {
			if (p1_brawlers[i].getStats().getHealth() != 0) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the Brawler that is currently fighting for Player 2. This Brawler's health
	 * cannot be 0.
	 * @return
	 */
	public int getP2CurrentBrawler() {
		for (int i = 0; i < 3; i++) {
			if (p2_brawlers[i].getStats().getHealth() != 0) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns true if all players have selected their next moves and the game state
	 * can advance.
	 * @return
	 */
	public boolean bothInputsReceived() {
		if (p1_inputReceived != 0 && p2_inputReceived != 0) {
			return true;
		}
		return false;
	}
	
	public void setP1_inputReceived(int num) {
		p1_inputReceived = num;
	}
	
	public void setP2_inputReceived(int num) {
		p2_inputReceived = num;
	}
	
	public void resetInputs() {
		p1_inputReceived = 0;
		p2_inputReceived = 0;
	}
	
	public boolean isGameOver() {
		return gameOver;
	}
	
	/**
	 * Advances the game state based upon input from players. The Brawler with the higher speed attacks first.
	 * Calls {@link Game#attack(Brawler, Brawler, int)} and begins constructing a list of {@link Message} that
	 * contains information on the updated game state that will be sent back to {@link Client}.
	 * @return
	 */
	public List<Message> fight() {
		Brawler b1 = p1_brawlers[getP1CurrentBrawler()];
		Brawler b2 = p2_brawlers[getP2CurrentBrawler()];
		int b1_ability = p1_inputReceived-1;
		int b2_ability = p2_inputReceived-1;
		List<Message> cm = new ArrayList<Message>();
		String p1_message = "";
		String p2_message = "";
		int b1_speed = b1.getStats().getSpeed();
		int b2_speed = b2.getStats().getSpeed();
		if (b1_speed == b2_speed) {
			if (Math.random() < 0.5) {
				b1_speed += 1;
			}
			else {
				b2_speed += 1;
			}
		}
		//Determines which Brawler attacks first and then begins constructing Message class to relay updated
		//game state information for each player.
		if (b1_speed > b2_speed) {
			List<String> list = attack(b1, b2, b1_ability);
			p1_message += list.get(0);
			p2_message += list.get(1);
			if (b2.getStats().getHealth() != 0) {
				list = attack(b2, b1, b2_ability);
				p2_message += list.get(0);
				p1_message += list.get(1);
				//b2 must be alive, b1 could be dead
				p2_message += "\n" + b2.getName() + " has " + b2.getStats().getHealth() + " health\n";
				if (b1.getStats().getHealth() != 0) {
					p1_message += "\n" + b1.getName() + " has " + b1.getStats().getHealth() + " health\n";
				}
				else {
					p1_message += "\n" + b2.getName() + " has " + b2.getStats().getHealth() + " health\n";
					p1_message += "\n" + b1.getName() + " was defeated!\n";
					p2_message += "\n" + b1.getName() + " was defeated!\n";
					if (getP1CurrentBrawler() == -1) {
						gameOver = true;
					}
					else {
						Brawler new_b1 = p1_brawlers[getP1CurrentBrawler()];
						p1_message += "You sent out " + new_b1.getName() + "!\n";
						p2_message += "Your opponent sent out " + new_b1.getName() + "!\n";
					}
				}
			}
			else {
				//b1 must be alive, b2 must be dead
				p1_message += "\n" + b1.getName() + " has " + b1.getStats().getHealth() + " health\n";
				p2_message += "\n" + b1.getName() + " has " + b1.getStats().getHealth() + " health\n";
				p1_message += "\n" + b2.getName() + " was defeated!\n";
				p2_message += "\n" + b2.getName() + " was defeated!\n";
				if (getP2CurrentBrawler() == -1) {
					gameOver = true;
				}
				else {
					Brawler new_b2 = p2_brawlers[getP2CurrentBrawler()];
					p2_message += "You sent out " + new_b2.getName() + "!\n";
					p1_message += "Your opponent sent out " + new_b2.getName() + "!\n";
				}
			}
		}
		else {
			List<String> list = attack(b2, b1, b2_ability);
			p2_message += list.get(0);
			p1_message += list.get(1);
			if (b1.getStats().getHealth() != 0) {
				list = attack(b1, b2, b1_ability);
				p1_message += list.get(0);
				p2_message += list.get(1);
				//b1 must be alive, b2 could be dead
				p1_message += "\n" + b1.getName() + " has " + b1.getStats().getHealth() + " health\n";
				if (b2.getStats().getHealth() != 0) {
					p2_message += "\n" + b2.getName() + " has " + b2.getStats().getHealth() + " health\n";
				}
				else {
					p2_message += "\n" + b1.getName() + " has " + b1.getStats().getHealth() + " health\n";
					p2_message += "\n" + b2.getName() + " was defeated!\n";
					p1_message += "\n" + b2.getName() + " was defeated!\n";
					if (getP2CurrentBrawler() == -1) {
						gameOver = true;
					}
					else {
						Brawler new_b2 = p2_brawlers[getP2CurrentBrawler()];
						p2_message += "You sent out " + new_b2.getName() + "!\n";
						p1_message += "Your opponent sent out " + new_b2.getName() + "!\n";
					}
					
				}
			}
			else {
				//b2 must be alive, b1 must be dead
				p2_message += "\n" + b2.getName() + " has " + b2.getStats().getHealth() + " health\n";
				p1_message += "\n" + b2.getName() + " has " + b2.getStats().getHealth() + " health\n";
				p2_message += "\n" + b1.getName() + " was defeated!\n";
				p1_message += "\n" + b1.getName() + " was defeated!\n";
				if (getP1CurrentBrawler() == -1) {
					gameOver = true;
				}
				else {
					Brawler new_b1 = p1_brawlers[getP1CurrentBrawler()];
					p1_message += "You sent out " + new_b1.getName() + "!\n";
					p2_message += "Your opponent sent out " + new_b1.getName() + "!\n";
				}
			}
		}
		//Sends special Message stating game has ended if one player has no more Brawlers
		if (gameOver) {
			if (getP1CurrentBrawler() == -1) {
				p1_message += "\nYou are out of brawlers!\n\nYou Lose!\n";
				p2_message += "\nYour opponent is out of brawlers!\n\nYou Win!\n";
			}
			else {
				p2_message += "\nYou are out of brawlers!\n\nYou Lose!\n";
				p1_message += "\nYour opponent is out of brawlers!\n\nYou Win!\n";
			}
			Message p1_cm = new GameOver(p1_message);
			Message p2_cm = new GameOver(p2_message);
			cm.add(p1_cm);
			cm.add(p2_cm);
			resetInputs();
			return cm;
		}
		
		//Appending message that contains updated game state information with choices for next move
		Brawler curr_b1 = p1_brawlers[getP1CurrentBrawler()];
		Brawler curr_b2 = p2_brawlers[getP2CurrentBrawler()];
		p1_message += "\nChoose a move:\n";
		p2_message += "\nChoose a move:\n";
		for (int i = 0; i < curr_b1.getAbilities().size(); i++) {
			int j = i+1;
			p1_message += j + ") " + curr_b1.getAbilities().get(i).getName() + ", " + curr_b1.getAbilities().get(i).getType() + ", " + curr_b1.getAbilities().get(i).getDamage() + "\n";
		}
		for (int i = 0; i < curr_b2.getAbilities().size(); i++) {
			int j = i+1;
			p2_message += j + ") " + curr_b2.getAbilities().get(i).getName() + ", " + curr_b2.getAbilities().get(i).getType() + ", " + curr_b2.getAbilities().get(i).getDamage() + "\n";
		}
		Message p1_cm = new GameMessage(p1_message);
		Message p2_cm = new GameMessage(p2_message);
		cm.add(p1_cm);
		cm.add(p2_cm);
		resetInputs();
		return cm;
	}
	
	/**
	 * Calculates damage for each Brawler based on ability type, damage score, and defending Brawler's type.
	 * Returns List<String> containing information about how much damage done and remaining health of Brawlers
	 * for each player.
	 * @param b1
	 * @param b2
	 * @param ability
	 * @return
	 */
	public List<String> attack(Brawler b1, Brawler b2, int ability) {
		String attackerMessage = "";
		String defenderMessage = "";
		int damage = b1.getAbilities().get(ability).getDamage();
		String abilityType = b1.getAbilities().get(ability).getType();
		String opponentType = b2.getType();
		double attackStat = b1.getStats().getAttack();
		double defenseStat = b2.getStats().getDefense(); 
		double effectiveness = 1;
		
		
		for (int i = 0; i < strongAgainst.length; i++) {
			if (strongAgainst[i].equals(abilityType)) {
				if (strongAgainst[i+1].equals(opponentType)) {
					effectiveness = 2;
				}
				break;
			}
		}
		
		for (int i = 0; i < weakAgainst.length; i++) {
			if (weakAgainst[i].equals(abilityType)) {
				if (weakAgainst[i+1].equals(opponentType)) {
					effectiveness = 0.5;
				}
				break;
			}
		}
		
		damage = (int) (((attackStat*(damage/defenseStat))/5)*effectiveness);
		int health = b2.getStats().getHealth();
		if (damage >= health) {
			b2.getStats().setHealth(0);
			if (b2 == p1_brawlers[0] || b2 == p1_brawlers[1] || b2 == p1_brawlers[2]) {
				for (int i = 0; i < p1_alive.length; i++) {
					if (p1_alive[i] == true) {
						p1_alive[i] = false;
						break;
					}
				}
			}
			else {
				for (int i = 0; i < p2_alive.length; i++) {
					if (p2_alive[i] == true) {
						p2_alive[i] = false;
						break;
					}
				}
			}
		}
		else {
			health = health - damage;
			b2.getStats().setHealth(health);
		}
		
		attackerMessage += b1.getName() + " used " + b1.getAbilities().get(ability).getName() + "!\n";
		defenderMessage += b1.getName() + " used " + b1.getAbilities().get(ability).getName() + "!\n";
		if (effectiveness == 2) {
			attackerMessage += "It was super effective!\n";
			defenderMessage += "It was super effective!\n";
		}
		if (effectiveness == 0.5) {
			attackerMessage += "It was not very effective!\n";
			defenderMessage += "It was not very effective!\n";	
		}
		attackerMessage += "It did " + damage + " damage!\n";
		defenderMessage += "It did " + damage + " damage!\n";	
	
		List<String> list = new ArrayList<String>();
		list.add(attackerMessage);
		list.add(defenderMessage);
		return list;
		
	}

}
