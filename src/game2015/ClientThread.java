package game2015;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {

	private Player p;
	private PrintWriter out;

	public ClientThread(Player p) {
		this.p = p;
	}

	@Override
	public void run() {
		try {
			this.out = new PrintWriter(new Socket(p.getIpAddress(),
					CurrentPlayerInfo.port).getOutputStream(), true);

			out.println("NAME " + CurrentPlayerInfo.myName + " "
					+ CurrentPlayerInfo.myPosX + " " + CurrentPlayerInfo.myPosY
					+ " " + CurrentPlayerInfo.myDirection);
			p.setOut(out);
		} catch (IOException e) {
			System.out.println("Error creating stream");
		}
	}
}
