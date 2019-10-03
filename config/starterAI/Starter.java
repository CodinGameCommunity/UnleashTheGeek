import static java.lang.Math.*;

import java.io.*;
import java.nio.*;
import java.util.*;


class Coord {
	final int x;
	final int y;

	Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}

	Coord(Scanner in) {
		this(in.nextInt(), in.nextInt());
	}

	Coord add(Coord other) {
		return new Coord(x + other.x, y + other.y);
	}

	// Manhattan distance (for 4 directions maps)
	// see: https://en.wikipedia.org/wiki/Taxicab_geometry
	int distance(Coord other) {
		return abs(x - other.x) + abs(y - other.y);
	}
	
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + x;
		result = PRIME * result + y;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Coord other = (Coord) obj;
		return (x == other.x) && (y == other.y);
	}

	public String toString() {
		return x + " " + y;
	}
}


class Cell {
	boolean known;
	int ore;
	boolean hole;
	
	Cell(boolean known, int ore, boolean hole) {
		this.known = known;
		this.ore = ore;
		this.hole = hole;
	}

	Cell(Scanner in) {
		String oreStr = in.next();
		if (oreStr.charAt(0) == '?') {
			known = false;
			ore = 0;
		} else {
			known = true;
			ore = Integer.parseInt(oreStr);
		}
		String holeStr = in.next();
		hole = (holeStr.charAt(0) != '0');
	}
}


class Action {
	final String command;
	final Coord pos;
	final EntityType item;
	String message;

	private Action(String command, Coord pos, EntityType item) {
		this.command = command;
		this.pos = pos;
		this.item = item;
	}

	static Action none() {
		return new Action("WAIT", null, null);
	}

	static Action move(Coord pos) {
		return new Action("MOVE", pos, null);
	}

	static Action dig(Coord pos) {
		return new Action("DIG", pos, null);
	}

	static Action request(EntityType item) {
		return new Action("REQUEST", null, item);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(command);
		if (pos != null) {
			builder.append(' ').append(pos);
		}
		if (item != null) {
			builder.append(' ').append(item);
		}
		if (message != null) {
			builder.append(' ').append(message);
		}
		return builder.toString();
	}
}


enum EntityType {
	NOTHING, ALLY_ROBOT, ENEMY_ROBOT, RADAR, TRAP, AMADEUSIUM;

	static EntityType valueOf(int id) {
		return values()[id + 1];
	}
}


class Entity {
	private static final Coord DEAD_POS = new Coord(-1, -1);

	// Updated every turn
	final int id;
	final EntityType type;
	final Coord pos;
    final EntityType item;
    
	// Computed for my robots
	Action action;

	Entity(Scanner in) {
		id = in.nextInt();
		type = EntityType.valueOf(in.nextInt());
		pos = new Coord(in);
		item = EntityType.valueOf(in.nextInt());
	}

	boolean isAlive() {
		return !DEAD_POS.equals(pos);
	}
}


class Team {
	int score;
	Collection<Entity> robots;

	void readScore(Scanner in) {
		score = in.nextInt();
		robots = new ArrayList<>();
	}
}


class Board {
	// Given at startup
	final int width;
	final int height;

	// Updated each turn
	final Team myTeam = new Team();
	final Team opponentTeam = new Team();
	private Cell[][] cells;
	int myRadarCooldown;
	int myTrapCooldown;
	Map<Integer, Entity> entitiesById;
	Collection<Coord> myRadarPos;
	Collection<Coord> myTrapPos;

	Board(Scanner in) {
		width = in.nextInt();
		height = in.nextInt();
	}

	void update(Scanner in) {
		// Read new data
		myTeam.readScore(in);
		opponentTeam.readScore(in);
		cells = new Cell[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				cells[y][x] = new Cell(in);
			}
		}
		int entityCount = in.nextInt();
		myRadarCooldown = in.nextInt();
		myTrapCooldown = in.nextInt();
		entitiesById = new HashMap<>();
		myRadarPos = new ArrayList<>();
		myTrapPos = new ArrayList<>();
		for (int i = 0; i < entityCount; i++) {
			Entity entity = new Entity(in);
			entitiesById.put(entity.id, entity);
			if (entity.type == EntityType.ALLY_ROBOT) {
				myTeam.robots.add(entity);
			} else if (entity.type == EntityType.ENEMY_ROBOT) {
				opponentTeam.robots.add(entity);
			} else if (entity.type == EntityType.RADAR) {
				myRadarPos.add(entity.pos);
			} else if (entity.type == EntityType.TRAP) {
				myTrapPos.add(entity.pos);
			}
		}
	}

	boolean cellExist(Coord pos) {
		return (pos.x >= 0) && (pos.y >= 0) && (pos.x < width) && (pos.y < height);
	}

	Cell getCell(Coord pos) {
		return cells[pos.y][pos.x];
	}
}


class Player {

	public static void main(String args[]) {
		new Player().run();
	}

	final Scanner in = new Scanner(System.in);

	void run() {
		// Parse initial conditions
		Board board = new Board(in);

		while (true) {
			// Parse current state of the game
			board.update(in);

			// Insert your strategy here
			for (Entity robot : board.myTeam.robots) {
				robot.action = Action.none();
				robot.action.message = "Java Starter";
			}

			// Send your actions for this turn
			for (Entity robot : board.myTeam.robots) {
				System.out.println(robot.action);
			}
		}
	}
}