# battle-brawlers-game

BATTLE BRAWLERS GAME

This is a Pokemon style game that uses a threaded server to allow multiple clients to connect and battle each other.
Connecting clients can create or join games. Clients can create 1 player games against the computer or create 2 player
games which are joinable by other clients.

To try out the game:
1) Run src/Server.java and provide a valid host and port for the ServerSocket. It will also prompt you for an input file for the
brawler data. You can use the test.json in the lib folder or provide your own valid json to play with custom brawlers and abilities.

2) Run Client.java and provide the same host and port as the server to connect. You will be able to create or join existing games.
If you create a 2 player game, start another Client.java thread to join the game so that it can start.
