package net.mirwaldt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TestServer {
	public static void main(String[] args) throws UnknownHostException,
			IOException, InterruptedException {
		Socket socket = new Socket("10.201.77.102", 1234);
		OutputStreamWriter writer = new OutputStreamWriter(
				new BufferedOutputStream(socket.getOutputStream()));
		writer.write("SIZE");
		writer.flush();

		DataInputStream dataInputStream = new DataInputStream(
				socket.getInputStream());
		String[] sizeReply = dataInputStream.readLine().split(" ");
		int width = Integer.parseInt(sizeReply[1]);
		int height = Integer.parseInt(sizeReply[2]);

		int myWidth = width / 3;
		int myHeight = height / 3;

		int startX = 2 * myWidth;
		int startY = 1 * myHeight;

		int endX = startX + myWidth;
		int endY = startY + myHeight;

		int windowWidth = myWidth / 2;
		int windowHeight = myHeight / 2;

		int leftTopStartX = startX;
		int leftTopStartY = startY;

		int leftTopEndX = leftTopStartX + windowWidth;
		int leftTopEndY = leftTopStartY + windowHeight;

//		int[] rgb = new int[3];
//		for (int x = leftTopStartX; x < leftTopEndX; x++) {
//			StringBuilder stringBuilder = new StringBuilder();
//			float c = (float) (x - leftTopStartX) / (float) windowWidth;
//			System.out.println(c);
//			rgb(c, rgb);
//			for (int y = leftTopStartY; y < leftTopEndY; y++) {
//				String command = "PX " + x + " " + y + " "
//						+ String.format("%02x%02x%02x", rgb[0], rgb[1], rgb[2])
//						+ "\n";
//				stringBuilder.append(command);
//			}
//			System.out.println(String.format("#%02x%02x%02x", rgb[0], rgb[1],
//					rgb[2]));
//			writer.write(stringBuilder.toString());
//			writer.flush();
//		}

		// int leftBottomStartX = startX;
		// int leftBottomStartY = startY + windowHeight;
		//
		// int leftBottomEndX = leftBottomStartX + windowWidth;
		// int leftBottomEndY = leftBottomStartY + windowHeight;
		//
		// for (int x = leftBottomStartX; x < leftBottomEndX; x++) {
		// StringBuilder stringBuilder = new StringBuilder();
		// for (int y = leftBottomStartY; y < leftBottomEndY; y++) {
		// String command = "PX " + x + " " + y + " "
		// + "00FF00" + "\n";
		// stringBuilder.append(command);
		// }
		// writer.write(stringBuilder.toString());
		// writer.flush();
		// }

		int sourceStartX = myWidth;
		int sourceStartY = myHeight;

		int sourceEndX = sourceStartX + myWidth;
		int sourceEndY = sourceStartY + myHeight;

		Color[][] colors = new Color[myWidth][myHeight];

		for (int x = sourceStartX; x < sourceEndX; x++) {
			StringBuilder stringBuilder = new StringBuilder();
			for (int y = sourceStartY; y < sourceEndY; y++) {
				String command = "PX " + x + " " + y + " " + "\n";
				stringBuilder.append(command);
			}
			writer.write(stringBuilder.toString());
			writer.flush();

			for (int y = sourceStartY; y < sourceEndY; y++) {
				String[] pixelReply = dataInputStream.readLine().split(" ");
				String color = pixelReply[3];
				colors[x - sourceStartX][y - sourceStartY] = new Color(
						Integer.valueOf(color.substring(0, 2), 16),
						Integer.valueOf(color.substring(2, 4), 16),
						Integer.valueOf(color.substring(4, 6), 16));
			}
		}

		JFrame frame = new JFrame();
		frame.getContentPane().add(new ImagePanel(myWidth, myHeight, colors));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(myWidth + 100, myHeight + 100);
		frame.setVisible(true);

		Thread.sleep(1000000);
	}

	public static void rgb(float c, int[] rgb) {
		if (c >= 0 && c <= (1 / 6.f)) {
			rgb[0] = 255;
			rgb[1] = (int) (1530 * c);
			rgb[2] = 0;
		} else if (c > (1 / 6.f) && c <= (1 / 3.f)) {
			rgb[0] = (int) (255 - (1530 * (c - 1 / 6f)));
			rgb[1] = 255;
			rgb[2] = 0;
		} else if (c > (1 / 3.f) && c <= (1 / 2.f)) {
			rgb[0] = 0;
			rgb[1] = 255;
			rgb[2] = (int) (1530 * (c - 1 / 3f));
		} else if (c > (1 / 2f) && c <= (2 / 3f)) {
			rgb[0] = 0;
			rgb[1] = (int) (255 - ((c - 0.5f) * 1530));
			rgb[2] = 255;
		} else if (c > (2 / 3f) && c <= (5 / 6f)) {
			rgb[0] = (int) ((c - (2 / 3f)) * 1530);
			rgb[1] = 0;
			rgb[2] = 255;
		} else if (c > (5 / 6f) && c <= 1) {
			rgb[0] = 255;
			rgb[1] = 0;
			rgb[2] = (int) (255 - ((c - (5 / 6f)) * 1530));
		}
	}

	static class ImagePanel extends JPanel {
		int width;
		int weight;
		Color[][] colors;

		public ImagePanel(int width, int weight, Color[][] colors) {
			super();
			this.width = width;
			this.weight = weight;
			this.colors = colors;
		}

		public void paint(Graphics g) {
			Image img = createImage();
			g.drawImage(img, 20, 20, (ImageObserver) this);

		}

		private Image createImage() {
			BufferedImage bufferedImage = new BufferedImage(width, weight,
					BufferedImage.TYPE_INT_RGB);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < weight; y++) {
					bufferedImage.setRGB(x, y, colors[x][y].getRGB());
				}
			}

			File outputfile = new File("D:\\saved2.png");
			try {
				ImageIO.write(bufferedImage, "png", outputfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bufferedImage;
		}
	}
}
