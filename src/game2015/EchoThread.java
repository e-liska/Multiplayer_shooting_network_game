package game2015;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class EchoThread extends Thread {

	private Socket socket;
	private Player player;

	public EchoThread(Socket socket, Player player) {
		this.socket = socket;
		this.player = player;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String line;
			while ((line = in.readLine()) != null) {
				String[] commandInfo = line.split(" ");
				System.out.println(line);
				if (commandInfo[0].toUpperCase().equals("NAME")) {
					player.setName(commandInfo[1]);
					player.setXpos(Integer.parseInt(commandInfo[2]));
					player.setYpos(Integer.parseInt(commandInfo[3]));
					player.setDirection(commandInfo[4]);
				} else if (commandInfo[0].toUpperCase().equals("MOVE")) {
					Main.updatePlayer(player, Integer.parseInt(commandInfo[1]),
							Integer.parseInt(commandInfo[2]), commandInfo[3]);
				} else if (commandInfo[0].toUpperCase().equals("POINT")) {
					synchronized (player) {
						Main.getPlayerByName(commandInfo[1]).setPoint(
								Integer.parseInt(commandInfo[2]));
						Main.updateScore();
					}
				} else if (commandInfo[0].toUpperCase().equals("RTM")) {
					Main.request(player, Integer.parseInt(commandInfo[1]));
				} else if (commandInfo[0].toUpperCase().equals("CTM")) {
					Main.incCTMs();
				} else {
					System.out.println("Unknown message!");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
