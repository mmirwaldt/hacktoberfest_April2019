package net.mirwaldt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SolveLabyrinth {

	static enum Field {
		FREE, WALL, END
	}

	static class Point {
		private final int x;
		private final int y;

		public Point(int x, int y) {
			super();
			this.x = x;
			this.y = y;
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + ")";
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

	static enum Direction {
		UP(0, -1), DOWN(0, +1), LEFT(-1, 0), RIGHT(+1, 0);

		int stepX;
		int stepY;

		private Direction(int stepX, int stepY) {
			this.stepX = stepX;
			this.stepY = stepY;
		}

		public int getStepX() {
			return stepX;
		}

		public int getStepY() {
			return stepY;
		}

		public Direction opposite() {
			if (this == UP) {
				return DOWN;
			} else if (this == DOWN) {
				return UP;
			} else if (this == LEFT) {
				return RIGHT;
			} else { // RIGHT
				return LEFT;
			}
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		BufferedImage img = ImageIO.read(SolveLabyrinth.class
				.getResourceAsStream("saved.png"));

		int widthInPositions = 74;
		int heightInPositions = 39;

		int fieldWidth = 2 * widthInPositions + 1;
		int fieldHeight = 2 * heightInPositions + 1;

		Field[][] map = new Field[fieldHeight][fieldWidth];

		createBorder(fieldWidth, fieldHeight, map);

		setCentralFreeFields(fieldWidth, fieldHeight, map);

		setCentralWalls(fieldWidth, fieldHeight, map);

		setTarget(fieldWidth, fieldHeight, map);

		Point leftTopCorner = findLeftTopCorner(img);
		if (leftTopCorner.getX() == -1) {
			throw new RuntimeException("Left top point not found!");
		}

		System.out.println("leftTop=" + leftTopCorner);

		readNonCentralWalls(img, widthInPositions, heightInPositions, map,
				leftTopCorner);
		System.out.println(Arrays.deepToString(map).replace("],", "],\n"));

		List<Point> pathToEnd = findPath(map, new Point(1, 1),
				new ArrayList<>(),
				Arrays.asList(Direction.DOWN, Direction.RIGHT));
		System.out.println(pathToEnd);

		drawPath(img, leftTopCorner, pathToEnd);

		JFrame frame = new JFrame();
		frame.getContentPane().add(new ImagePanel(img));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(img.getWidth() + 100, img.getHeight() + 100);
		frame.setVisible(true);

		Thread.sleep(1000000);

	}

	private static Point findLeftTopCorner(BufferedImage img) {
		for (int y = 0; y < 100; y++) {
			for (int x = 0; x < 100; x++) {
				if ((new Color(img.getRGB(x, y)).equals(Color.WHITE))) {
					return new Point(x, y);
				}

			}
		}
		return new Point(-1, -1);
	}

	private static void drawPath(BufferedImage img, Point leftTopCorner,
			List<Point> pathToEnd) {
		Graphics g = img.getGraphics();
		g.setColor(Color.YELLOW);

		Point previousPoint = pathToEnd.get(0);
		for (Point point : pathToEnd.subList(1, pathToEnd.size())) {
			int startPositionX = leftTopCorner.getX()
					+ (previousPoint.getX() / 2 + 1) * 18 - 8;
			int startPositionY = leftTopCorner.getY()
					+ (previousPoint.getY() / 2 + 1) * 18 - 8;
			int endPositionX = leftTopCorner.getX() + (point.getX() / 2 + 1) * 18 - 8;
			int endPositionY = leftTopCorner.getY() + (point.getY() / 2 + 1) * 18 - 8;

			g.drawLine(startPositionX, startPositionY, endPositionX,
					endPositionY);

			previousPoint = point;
		}
	}

	private static void readNonCentralWalls(BufferedImage img,
			int widthInPositions, int heightInPositions, Field[][] map,
			Point leftTopCorner) {
		for (int wallPositionX = 1; wallPositionX <= widthInPositions; wallPositionX++) {
			for (int wallPositionY = 1; wallPositionY <= heightInPositions; wallPositionY++) {
				int pixelPositionX = leftTopCorner.getX() + wallPositionX * 18;
				int pixelPositionY = leftTopCorner.getY() + wallPositionY * 18;

				int leftFieldX = 2 * wallPositionX - 1;
				int leftFieldY = 2 * wallPositionY;
				if ((img.getRGB(pixelPositionX - 5, pixelPositionY) & 0xFFFFFF00) == (Color.WHITE
						.getRGB() & 0xFFFFFF00)) {
					map[leftFieldY][leftFieldX] = Field.WALL;
				} else {
					map[leftFieldY][leftFieldX] = Field.FREE;
				}

				int topFieldX = 2 * wallPositionX;
				int topFieldY = 2 * wallPositionY - 1;
				if ((img.getRGB(pixelPositionX, pixelPositionY - 5) & 0xFFFFFF00) == (Color.WHITE
						.getRGB() & 0xFFFFFF00)) {
					map[topFieldY][topFieldX] = Field.WALL;
				} else {
					map[topFieldY][topFieldX] = Field.FREE;
				}
			}
		}
	}

	private static void setTarget(int fieldWidth, int fieldHeight, Field[][] map) {
		map[fieldHeight - 2][fieldWidth - 2] = Field.END;
	}

	private static void setCentralWalls(int fieldWidth, int fieldHeight,
			Field[][] map) {
		for (int fieldX = 2; fieldX < fieldWidth; fieldX += 2) {
			for (int fieldY = 2; fieldY < fieldHeight; fieldY += 2) {
				map[fieldY][fieldX] = Field.WALL;
			}
		}
	}

	private static void setCentralFreeFields(int fieldWidth, int fieldHeight,
			Field[][] map) {
		for (int fieldX = 1; fieldX < fieldWidth; fieldX += 2) {
			for (int fieldY = 1; fieldY < fieldHeight; fieldY += 2) {
				map[fieldY][fieldX] = Field.FREE;
			}
		}
	}

	private static void createBorder(int fieldWidth, int fieldHeight,
			Field[][] map) {
		for (int fieldX = 0; fieldX < fieldWidth; fieldX++) {
			map[0][fieldX] = Field.WALL;
			map[fieldHeight - 1][fieldX] = Field.WALL;
		}

		for (int fieldY = 0; fieldY < fieldHeight; fieldY++) {
			map[fieldY][0] = Field.WALL;
			map[fieldY][fieldWidth - 1] = Field.WALL;
		}
	}

	private static List<Point> findPath(Field[][] map, Point currentPosition,
			List<Point> positions, List<Direction> directions) {
		List<Point> newPosition = new ArrayList<>(positions);
		if (2 <= newPosition.size()) {
			Point morePrevíous = newPosition.get(newPosition.size() - 2);
			Point previous = newPosition.get(newPosition.size() - 1);
			if (isPreviousTransitive(currentPosition, morePrevíous, previous)) {
				newPosition.remove(newPosition.size() - 1);
			}
		}
		newPosition.add(currentPosition);

		for (Direction direction : directions) {
			Point nextPosition = new Point(currentPosition.getX()
					+ direction.getStepX(), currentPosition.getY()
					+ direction.getStepY());
			Field nextField = map[nextPosition.getY()][nextPosition.getX()];
			if (nextField == Field.END) {
				return newPosition;
			} else if (nextField == Field.FREE) {
				final List<Direction> newDirections = new ArrayList<Direction>(
						Arrays.asList(Direction.values()));
				newDirections.remove(direction.opposite()); // going backwards
															// is forbidden
				List<Point> path = findPath(map, nextPosition, newPosition,
						newDirections);
				if (path != Collections.<Point> emptyList()) {
					return path;
				}
			}
		}
		return Collections.emptyList();
	}

	private static boolean isPreviousTransitive(Point currentPosition,
			Point first, Point previous) {
		return (first.getX() == previous.getX() && previous.getX() == currentPosition
				.getX())
				|| (first.getY() == previous.getY() && previous.getY() == currentPosition
						.getY());
	}

	static class ImagePanel extends JPanel {
		BufferedImage img;

		public ImagePanel(BufferedImage img) {
			super();
			this.img = img;
		}

		public void paint(Graphics g) {
			g.drawImage(img, 10, 10, (ImageObserver) this);

		}
	}
}
