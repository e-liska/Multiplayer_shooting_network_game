package game2015;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

	public static final int size = 20;
	public static final int scene_height = size * 20 + 100;
	public static final int scene_width = size * 20 + 200;

	public static Image image_floor;
	public static Image image_wall;
	public static Image hero_right, hero_left, hero_up, hero_down;

	public static Player me;
	public static List<Player> players = new ArrayList<Player>();

	private static Label[][] fields;
	private static TextArea scoreList;
	private static Button btnClient;

	private static State myState = State.IDLE;
	private static int logicalTime = 0;
	private static List<Player> queueCTM = new ArrayList<Player>();
	private static int receivedCTM = 0;
	private static int requestTime = 0;
	private static KeyCode keyPressed;

	public void setKeyPressed(KeyCode k) {
		keyPressed = k;
	}

	private static String[] board = { // 20x20
		"wwwwwwwwwwwwwwwwwwww", "w        ww        w", "w w  w  www w  w  ww",
			"w w  w   ww w  w  ww", "w  w               w",
			"w w w w w w w  w  ww", "w w     www w  w  ww",
			"w w     w w w  w  ww", "w   w w  w  w  w   w",
			"w     w  w  w  w   w", "w ww ww        w  ww",
			"w  w w    w    w  ww", "w        ww w  w  ww",
			"w         w w  w  ww", "w        w     w  ww",
			"w  w              ww", "w  w www  w w  ww ww",
			"w w      ww w     ww", "w   w   ww  w      w",
	"wwwwwwwwwwwwwwwwwwww" };

	// -------------------------------------------
	// | Maze: (0,0) | Score: (1,0) |
	// |-----------------------------------------|
	// | boardGrid (0,1) | scorelist |
	// | | (1,1) |
	// -------------------------------------------

	@Override
	public void start(Stage primaryStage) {
		try {
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(0, 10, 0, 10));

			Text mazeLabel = new Text("Maze:");
			mazeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

			Text scoreLabel = new Text("Score:");
			scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

			scoreList = new TextArea();

			GridPane boardGrid = new GridPane();

			image_wall = new Image(this.getClass().getResourceAsStream(
					"Image/wall4.png"), size, size, false, false);
			image_floor = new Image(this.getClass().getResourceAsStream(
					"Image/floor1.png"), size, size, false, false);

			hero_right = new Image(this.getClass().getResourceAsStream(
					"Image/heroRight.png"), size, size, false, false);
			hero_left = new Image(this.getClass().getResourceAsStream(
					"Image/heroLeft.png"), size, size, false, false);
			hero_up = new Image(this.getClass().getResourceAsStream(
					"Image/heroUp.png"), size, size, false, false);
			hero_down = new Image(this.getClass().getResourceAsStream(
					"Image/heroDown.png"), size, size, false, false);

			fields = new Label[20][20];
			for (int j = 0; j < 20; j++) {
				for (int i = 0; i < 20; i++) {
					switch (board[j].charAt(i)) {
					case 'w':
						fields[i][j] = new Label("", new ImageView(image_wall));
						break;
					case ' ':
						fields[i][j] = new Label("", new ImageView(image_floor));
						break;
					default:
						throw new Exception("Illegal field value: "
								+ board[j].charAt(i));
					}
					boardGrid.add(fields[i][j], i, j);
				}
			}
			scoreList.setEditable(false);

			btnClient = new Button("We are all ready!");
			btnClient.setOnAction(event -> this.clientStartAction());

			grid.add(mazeLabel, 0, 0);
			grid.add(scoreLabel, 1, 0);
			grid.add(boardGrid, 0, 1);
			grid.add(scoreList, 1, 1);
			grid.add(btnClient, 1, 2);

			Scene scene = new Scene(grid, scene_width, scene_height);
			primaryStage.setScene(scene);
			primaryStage.show();

			scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
				if (event.getCode().equals(KeyCode.UP)
						|| event.getCode().equals(KeyCode.DOWN)
						|| event.getCode().equals(KeyCode.LEFT)
						|| event.getCode().equals(KeyCode.RIGHT)) {
					if (myState == State.IDLE) {
						this.setKeyPressed(event.getCode());
						this.sendRequest();
					}
				}
			});

			// Setting up standard players

			me = new Player(CurrentPlayerInfo.myName, CurrentPlayerInfo.myPosX,
					CurrentPlayerInfo.myPosY, CurrentPlayerInfo.myDirection);
			me.setIpAddress(InetAddress.getLocalHost());
			players.add(me);
			fields[me.getXpos()][me.getYpos()].setGraphic(new ImageView(Main
					.getDirectionImage(me.getDirection())));

			scoreList.setText(Main.getScoreList());

			for (String ip : CurrentPlayerInfo.players) {
				try {
					Player p = new Player(ip);
					players.add(p);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}

			Thread serverSocket = new ServerSocket();
			serverSocket.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void processKeyStroke() {
		switch (keyPressed) {
		case UP:
			Main.playerMoved(0, -1, "up");
			break;
		case DOWN:
			Main.playerMoved(0, +1, "down");
			break;
		case LEFT:
			Main.playerMoved(-1, 0, "left");
			break;
		case RIGHT:
			Main.playerMoved(+1, 0, "right");
			break;
		default:
			break;
		}
	}

	public static void playerMoved(int delta_x, int delta_y, String direction) {
		me.setDirection(direction);
		final int x = me.getXpos(), y = me.getYpos();

		if (board[y + delta_y].charAt(x + delta_x) == 'w') {
			synchronized (me) {
				me.addPoints(-1);
			}
		} else {
			Player p = Main.getPlayerAt(x + delta_x, y + delta_y);
			if (p != null) {
				synchronized (me) {
					me.addPoints(10);
				}
				synchronized (p) {
					p.addPoints(-10);
				}
				Main.tellOthers("POINT " + p.getName() + " " + p.getPoint());
			} else {
				synchronized (me) {
					me.addPoints(1);
				}
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						fields[x][y].setGraphic(new ImageView(image_floor));
						fields[x + delta_x][y + delta_y]
								.setGraphic(new ImageView(Main
										.getDirectionImage(direction)));
					}
				});
				me.setXpos(x + delta_x);
				me.setYpos(y + delta_y);
			}
		}

		Main.updateScore();
		Main.tellOthers("MOVE " + me.getXpos() + " " + me.getYpos() + " "
				+ me.getDirection());
		Main.tellOthers("POINT " + me.getName() + " " + me.getPoint());
	}

	public void sendRequest() {
		synchronized (myState) {
			Main.tellOthers("RTM " + (++logicalTime));
			myState = State.WAITING;
			requestTime = logicalTime;
		}
	}

	public static String getScoreList() {
		StringBuffer b = new StringBuffer(100);
		for (Player p : players) {
			b.append(p + "\r\n");
		}
		return b.toString();
	}

	public static Player getPlayerAt(int x, int y) {
		for (Player p : players) {
			if (p.getXpos() == x && p.getYpos() == y)
				return p;
		}
		return null;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	private void clientStartAction() {
		for (Player p : players) {
			if (p != me) {
				Thread thread = new ClientThread(p);
				thread.start();
			}
		}
	}

	public static Player getPlayerByIp(InetAddress ip) {
		for (Player p : players) {
			if (p.getIpAddress().equals(ip))
				return p;
		}
		return null;
	}

	public static Player getPlayerByName(String name) {
		for (Player p : players) {
			if (p.getName().equals(name))
				return p;
		}
		return null;
	}

	private static void tellOthers(String message) {
		// to cause concurrency problems uncomment this:
		/*
		 * try { Thread.sleep(200); } catch (InterruptedException e) {
		 * e.printStackTrace(); }
		 */
		System.out.println("me> " + message);
		for (Player p : players) {
			if (p != me) {
				p.getOut().println(message);
			}
		}
	}

	private static Image getDirectionImage(String direction) {
		if (direction.equals("right"))
			return hero_right;

		if (direction.equals("left"))
			return hero_left;

		if (direction.equals("up"))
			return hero_up;

		if (direction.equals("down"))
			return hero_down;

		return null;
	}

	public static void updatePlayer(Player player, int newX, int newY,
			String direction) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				synchronized (player) {
					fields[player.getXpos()][player.getYpos()]
							.setGraphic(new ImageView(image_floor));
					player.setXpos(newX);
					player.setYpos(newY);
					player.setDirection(direction);
					fields[player.getXpos()][player.getYpos()]
							.setGraphic(new ImageView(Main
									.getDirectionImage(player.getDirection())));
				}
			}
		});
	}

	public static void updateScore() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				scoreList.setText(Main.getScoreList());
			}
		});
	}

	public static void request(Player player, int time) {
		synchronized (myState) {
			logicalTime = Math.max(logicalTime, time) + 1;
			System.out.println("my time: " + requestTime + "; your: " + time
					+ "; " + myState);
			if (myState == State.IDLE) {
				player.getOut().println("CTM");
			} else if (myState == State.WAITING) {
				if (requestTime < time) {
					synchronized (queueCTM) {
						queueCTM.add(player);
					}
				} else if (requestTime == time) {
					System.out.println("časy jsou stejné: "
							+ me.getName().compareTo(player.getName()));
					if (me.getName().compareTo(player.getName()) < 0) {
						synchronized (queueCTM) {
							queueCTM.add(player);
						}
					} else {
						player.getOut().println("CTM");
					}
				} else {
					player.getOut().println("CTM");
				}
			} else if (myState == State.HOLD) {
				synchronized (queueCTM) {
					queueCTM.add(player);
				}
			}
		}
	}

	public static void incCTMs() {
		receivedCTM++;
		if (receivedCTM >= players.size() - 1) {
			myState = State.HOLD;
			receivedCTM = 0;
			Main.processKeyStroke();
			myState = State.IDLE;
			synchronized (queueCTM) {
				for (Player p : queueCTM) {
					p.getOut().println("CTM");
				}
				queueCTM.clear();
			}
		}
	}
}
