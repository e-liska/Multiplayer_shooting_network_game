package game2015;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Player {
	private String name;
	private int xpos;
	private int ypos;
	private int point;
	private String direction;
	private InetAddress ipAddress;
	private PrintWriter out;

	public Player(String ipAddress) throws UnknownHostException {
		this.ipAddress = InetAddress.getByName(ipAddress);
	}

	public Player(String name, int xpos, int ypos, String direction) {
		this.name = name;
		this.xpos = xpos;
		this.ypos = ypos;
		this.direction = direction;
		this.point = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}

	public int getXpos() {
		return xpos;
	}

	public void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return ypos;
	}

	public void setYpos(int ypos) {
		this.ypos = ypos;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public void addPoints(int p) {
		point += p;
	}

	public int getPoint() {
		return this.point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public PrintWriter getOut() {
		return this.out;
	}

	public void setOut(PrintWriter out) {
		this.out = out;
	}

	public InetAddress getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public String toString() {
		return name + ":   " + point;
	}
}
