import java.util.Properties;

import com.codingame.gameengine.runner.MultiplayerGameRunner;

public class UTG2019Main {
	public static void main(String[] args) {

		MultiplayerGameRunner gameRunner = new MultiplayerGameRunner();
		
		// Set seed here (leave empty for random)
//		gameRunner.setSeed(7659744061232896144l);

		// Select agents here
		gameRunner.addAgent("python3 config/Boss.py3", "Player 1");
		gameRunner.addAgent("python3 config/Boss.py3", "Player 2");		
		
		Properties params = new Properties();
		// Set params here
//		params.setProperty("state", "ROBOT0 0 0 TRAP; ROBOT1 5 1 TRAP; GOLD 1 0 10;GOLD 2 1 10");
		gameRunner.setGameParameters(params);
		

		gameRunner.start(8888);
	}
}
