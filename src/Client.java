import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Each Client thread communicates with a specific {@link ServerThread} via a socket. The ServerThread
 * is then able to access the Server and Game object associated with the Client.
 * @author Milan Goyal
 *
 */
public class Client extends Thread {

	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	public Client(String hostname, int port) throws UnknownHostException, IOException {
		System.out.println("Trying to connect to " + hostname + ":" + port);
		Socket s = new Socket(hostname, port);
		System.out.println("Connected to " + hostname + ":" + port);
		ois = new ObjectInputStream(s.getInputStream());
		oos = new ObjectOutputStream(s.getOutputStream());
		this.start();			
	}
	
	/**
	 * Continually reads incoming {@link Message} from {@link ServerThread} via socket communication. Depending
	 * on Message subclass, performs a certain action and sends back a response to ServerThread.
	 */
	public void run() {
		try {
			while(true) {
				Scanner scan = new Scanner(System.in);
				Message cm = (Message)ois.readObject();
				System.out.print(cm.getMessage());
				if (cm instanceof JoinOrStart) {
					boolean badInput = true;
					String line = "";
					while (badInput) {
						line = scan.nextLine();
						try {
							int input = Integer.parseInt(line);
							if (input == 1 || input == 2) {
								badInput = false;
							}
							else {
								System.out.println("Invalid!");
								System.out.println(cm.getMessage());
							}
						}
						catch (Exception e) {
							System.out.println("Invalid!");
							System.out.println(cm.getMessage());
						}
					}
					Message m = new JoinOrStartResponse(line);
					oos.writeObject(m);
					oos.flush();
				}
				if (cm instanceof NameGame || cm instanceof NameGameExists) {
					String line = scan.nextLine();
					Message m = new NameGameResponse(line);
					oos.writeObject(m);
					oos.flush();
				}
				if (cm instanceof NumberPlayers) {
					boolean badInput = true;
					String line = "";
					while (badInput) {
						line = scan.nextLine();
						try {
							int players = Integer.parseInt(line);
							if (players == 1 || players == 2) {
								badInput = false;
							}
							else {
								System.out.println("Invalid!");
								System.out.println(cm.getMessage());
							}
						}
						catch (Exception e) {
							System.out.println("Invalid!");
							System.out.println(cm.getMessage());
						}
					}
					NumberPlayers cm_2 = (NumberPlayers) cm;
					String gameName = cm_2.getGameName();
					Message m = new NumberPlayersResponse(line, gameName);
					oos.writeObject(m);
					oos.flush();
				}
				if (cm instanceof JoinNameGame || cm instanceof JoinNameGameNotExist || cm instanceof JoinNameGameFull) {
					String line = scan.nextLine();
					Message m = new JoinNameGameResponse(line);
					oos.writeObject(m);
					oos.flush();
				}
				if (cm instanceof ChooseBrawlers) {
					ChooseBrawlers cm_2 = (ChooseBrawlers) cm;
					int size = cm_2.getSize();
					boolean badInput = true;
					while(badInput) {
						String line = scan.nextLine();
						List<Integer> list = new ArrayList<Integer>();
						for (String s : line.split(",")) {
							int choice;
							try {
								choice = Integer.parseInt(s);
								if (choice < 1 || choice > size) {
									System.out.println("Invalid!");
									System.out.println(cm.getMessage());
									break;
								}
								list.add(Integer.parseInt(s));
								if (list.size() > 3) {
									System.out.println("Invalid!");
									System.out.println(cm.getMessage());
									break;	
								}
							}
							catch (Exception e) {
								System.out.println("Invalid!");
								System.out.println(cm.getMessage());
								break;
							}							
						}	
						if (list.size() == 3) {
							badInput = false;
							Message m = new ChooseBrawlersResponse(list);
							oos.writeObject(m);
							oos.flush();
						}
					}
				}
				if (cm instanceof GameMessage) {
					String line = scan.nextLine();
					Message m = new GameMessageResponse(line);
					oos.writeObject(m);
					oos.flush();	
				}
				if (cm instanceof GameOver) {
					
				}
			}
		} catch (IOException ioe) {
			System.out.println("ioe in Client.run(): " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			System.out.println("cnfe: " + cnfe.getMessage());
		}
	}
	public static void main(String [] args) {
		Scanner scan = new Scanner(System.in);
		String ip;
		String port;
		int portNumber;
		while (true) {
			System.out.println("Please enter an IP address:");
			ip = scan.nextLine();
			while (true) {
				System.out.println("Please enter a port:");
				port = scan.nextLine();
				portNumber = Integer.parseInt(port);
				if (portNumber < 1024 || portNumber > 49151) {
					System.out.println("Invalid port!");
					continue;
				}
				break;
			}
			try {
				Client cc = new Client(ip, portNumber);
				break;
			} catch(Exception e) {
				System.out.println("Unable to connect!");
				continue;
			}
		}
	}
}