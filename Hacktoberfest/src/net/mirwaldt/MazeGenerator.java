package net.mirwaldt;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Arrays;

/*
 * from https://rosettacode.org/wiki/Maze_generation#Java
 * 
 * recursive backtracking algorithm
 * shamelessly borrowed from the ruby at
 * http://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking
 */
public class MazeGenerator {
	private final int offSetX = 16;
	private final int offSetY = 9;

	private final int size = 18;

	private final int x;
	private final int y;
	private final int[][] maze;
	private final BufferedImage bufferedImage;

	public MazeGenerator(int x, int y, BufferedImage bufferedImage) {
		this.x = x;
		this.y = y;
		this.bufferedImage = bufferedImage;
		maze = new int[this.x][this.y];
		generateMaze(0, 0);
	}

	public void display() {
		for (int i = 0; i < y; i++) {
			int pY = offSetY + i * size;
			for (int j = 0; j < x; j++) {
				int pX = offSetX + j * size;
				bufferedImage.setRGB(pX, pY, Color.WHITE.getRGB());
				if((maze[j][i] & 1) == 0) {
					for (int k = pX + 1; k < pX + size; k++) {
						bufferedImage.setRGB(k, pY, Color.WHITE.getRGB());
					}
				}
			}
			
			for (int j = 0; j < x; j++) {
				int pX = offSetX + j * size;
				bufferedImage.setRGB(pX, pY, Color.WHITE.getRGB());
				if((maze[j][i] & 8) == 0) {
					for (int k = pY; k < pY + size; k++) {
						bufferedImage.setRGB(pX, k, Color.WHITE.getRGB());
					}
				}
			}
			for (int k = pY; k < pY + size; k++) {
				bufferedImage.setRGB(offSetX + x * size, k, Color.WHITE.getRGB());
			}
		}

		for (int j = 0; j < x; j++) {
			int pX = offSetX + j * size;
			for (int k = pX; k < pX + size; k++) {
				bufferedImage.setRGB(k, offSetY + y * size, Color.WHITE.getRGB());
			}
		}
		bufferedImage.setRGB(offSetX + x * size, offSetY + y * size, Color.WHITE.getRGB());

		for (int i = 1; i < size; i++) {
			for (int j = 1; j < size; j++) {
				bufferedImage.setRGB(offSetX + i, offSetY + j, Color.CYAN.getRGB());
			}
		}
		
		for (int i = 1; i < size; i++) {
			for (int j = 1; j < size; j++) {
				bufferedImage.setRGB(offSetX + (x-1) * size + i, offSetY + (y-1) * size + j, Color.MAGENTA.getRGB());
			}
		}
	}

	private void generateMaze(int cx, int cy) {
		DIR[] dirs = DIR.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (DIR dir : dirs) {
			int nx = cx + dir.dx;
			int ny = cy + dir.dy;
			if (between(nx, x) && between(ny, y) && (maze[nx][ny] == 0)) {
				maze[cx][cy] |= dir.bit;
				maze[nx][ny] |= dir.opposite.bit;
				generateMaze(nx, ny);
			}
		}
	}

	private static boolean between(int v, int upper) {
		return (v >= 0) && (v < upper);
	}

	private enum DIR {
		N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private DIR opposite;

		// use the static initializer to resolve forward references
		static {
			N.opposite = S;
			S.opposite = N;
			E.opposite = W;
			W.opposite = E;
		}

		private DIR(int bit, int dx, int dy) {
			this.bit = bit;
			this.dx = dx;
			this.dy = dy;
		}
	};
}