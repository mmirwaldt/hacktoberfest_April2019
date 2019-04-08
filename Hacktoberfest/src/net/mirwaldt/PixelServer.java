package net.mirwaldt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class PixelServer {
	private final static int MY_WIDTH = (int) (4096 / 6.0);
	private final static int MY_HEIGHT = (int) (2160 / 6.0);
	private final static int TOTAL_WIDTH = 2 * MY_WIDTH;
	private final static int TOTAL_HEIGHT = 3 * MY_HEIGHT;

	public static void main(String[] args) throws IOException {
		BufferedImage bufferedImage = new BufferedImage(TOTAL_WIDTH,
				TOTAL_HEIGHT, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < TOTAL_WIDTH; x++) {
			for (int y = 0; y < TOTAL_HEIGHT; y++) {
				bufferedImage.setRGB(x, y, Color.BLACK.getRGB());
			}
		}

		for (int x = 0; x < TOTAL_WIDTH; x++) {
			bufferedImage.setRGB(x, 2 * MY_HEIGHT, Color.WHITE.getRGB());
		}

		for (int y = 2 * MY_HEIGHT; y < TOTAL_HEIGHT; y++) {
			bufferedImage.setRGB(MY_WIDTH - 1, y, Color.WHITE.getRGB());
		}

		// use second monitor
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		JFrame frame = new JFrame(gs[0].getDefaultConfiguration());
		ImagePanel panel = new ImagePanel(bufferedImage);
		frame.getContentPane().add(panel);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				panel.refresh();
			}
		}, 0, 1000);

		MazeGenerator mazeGenerator = new MazeGenerator(74, 39, bufferedImage);
		mazeGenerator.display();

		Timer timer2 = new Timer();
		timer2.schedule(new TimerTask() {
			@Override
			public void run() {
				for (int x = 0; x < TOTAL_WIDTH; x++) {
					for (int y = 0; y < 2 * MY_HEIGHT; y++) {
						panel.setRgb(x, y, Color.BLACK.getRGB());
					}
				}

				new MazeGenerator(74, 39, bufferedImage).display();
			}
		}, 3 * 60000, 3 * 60000);

		ServerSocket serverSocket = new ServerSocket(1234);

		try {
			while (true) {
				Socket socket = serverSocket.accept();
				new Thread(new ConnectionHandler(socket, panel)).start();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

	static class ImagePanel extends JPanel {
		BufferedImage img;

		public ImagePanel(BufferedImage img) {
			super();
			this.img = img;
		}

		public void paint(Graphics g) {
			g.drawImage(img, 0, 0, (ImageObserver) this);
		}

		public int getRgb(int x, int y) {
			return img.getRGB(x, y);
		}

		public void setRgb(int x, int y, int rgb) {
			img.setRGB(x, y, rgb);
		}

		public void refresh() {
			SwingUtilities.invokeLater(() -> {
				ImagePanel.this.repaint();
			});
		}

	}

	static class ConnectionHandler implements Runnable {
		private final Socket socket;
		private final ImagePanel panel;

		public ConnectionHandler(Socket socket, ImagePanel panel) {
			super();
			this.socket = socket;
			this.panel = panel;
		}

		@Override
		public void run() {
			try {
				DataInputStream dataInputStream = new DataInputStream(
						new BufferedInputStream(socket.getInputStream()));
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
						new BufferedOutputStream(socket.getOutputStream()));

				while (true) {
					String command = dataInputStream.readLine().trim();
					// System.out.println(command);
					if ("SIZE".equals(command)) {
						outputStreamWriter.write("SIZE " + TOTAL_WIDTH + " "
								+ TOTAL_HEIGHT + "\n");
						outputStreamWriter.flush();
					} else if (command.startsWith("PX")) {
						String[] pxCommandTokens = command.split(" ", -1);
						if (pxCommandTokens.length == 3) {
							int x = Integer.valueOf(pxCommandTokens[1]);
							int y = Integer.valueOf(pxCommandTokens[2]);

							if (0 <= x && x < TOTAL_WIDTH && 0 <= y
									&& y < TOTAL_HEIGHT) {
								int rgb = panel.getRgb(x, y);
								Color color = new Color(rgb);
								String rgbAsHexString = String.format(
										"%02x%02x%02x", color.getRed(),
										color.getGreen(), color.getBlue());
								outputStreamWriter.write("PX " + x + " " + y
										+ " " + rgbAsHexString + "\n");
								outputStreamWriter.flush();
							}

						} else if (pxCommandTokens.length == 4) {
							int x = Integer.valueOf(pxCommandTokens[1]);
							int y = Integer.valueOf(pxCommandTokens[2]);
							String rgbAsHexString = pxCommandTokens[3];

							if (0 <= x && x < TOTAL_WIDTH && 0 <= y
									&& y < TOTAL_HEIGHT
									&& rgbAsHexString.length() == 6) {
								try {
									int red = Integer.valueOf(
											rgbAsHexString.substring(0, 2), 16);
									int green = Integer.valueOf(
											rgbAsHexString.substring(2, 4), 16);
									int blue = Integer.valueOf(
											rgbAsHexString.substring(4, 6), 16);
									panel.setRgb(x, y, new Color(red, green,
											blue).getRGB());
								} catch (NumberFormatException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

			} catch (Throwable e) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

	}
}
