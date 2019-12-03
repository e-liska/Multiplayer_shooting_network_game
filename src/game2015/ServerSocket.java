package game2015;

import java.io.IOException;
import java.net.Socket;

public class ServerSocket extends Thread {

	@Override
	public void run() {

		java.net.ServerSocket serverSocket;

		try {
			serverSocket = new java.net.ServerSocket(1414);

			while (true) {
				System.out.println("Waiting");
				Socket socket = serverSocket.accept();
				System.out.println("Connection from: "
						+ socket.getInetAddress());

				Thread thread = new EchoThread(socket,
						Main.getPlayerByIp(socket.getInetAddress()));
				thread.start();

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
