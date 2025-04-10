import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Run extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int initialX, initialY;
	private static final String settingsPath = "settings.txt";
	private static String bgImagePath;
	private static int locationX, locationY;
	private static int sleepTime;
	private static int moveSpeed;
	private static boolean isSleepEnabled;
	public Run() {
		loadSettings();
		addController();
	}

	private void loadSettings() {
		loadDefaultSettings();
		// Load settings from file
		// check settings file exists
		java.io.File settingsFile = new java.io.File(settingsPath);
		if (settingsFile.exists()) {
			try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(settingsPath))) {
				String line;
				while ((line = br.readLine()) != null) {
					String[] parts = line.split("=");
					if (parts.length == 2) {
						String key = parts[0].trim();
						String value = parts[1].trim();
						switch (key) {
							case "x":
								locationX = Integer.parseInt(value);
								break;
							case "y":
								locationY = Integer.parseInt(value);
								break;
							case "bgImage":
								bgImagePath = value;
								break;
							case "sleepTime":
								sleepTime = Integer.parseInt(value);
								break;
							case "moveSpeed":
								moveSpeed = Integer.parseInt(value);
								break;
							case "isSleepEnabled":
								isSleepEnabled = Boolean.parseBoolean(value);
								break;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			loadDefaultSettings();
		}
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setType(Type.UTILITY);
		setBounds(locationX, locationY, 150, 150);
		setUndecorated(true);
		setVisible(true);
		setImageBackground(bgImagePath);
	}
	
	private void loadDefaultSettings() {
		bgImagePath = "bg.png"; // default background image
		locationX = 1000; // default x location
		locationY = 500; // default y location
		sleepTime = 60; // default sleep time
		moveSpeed = 1; // default step move
		isSleepEnabled = false; // default sleep disabled
	}
	
	private void saveSettings() {
		// create settings file if it doesn't exist
		try {
			java.io.File settingsFile = new java.io.File(settingsPath);
			if (!settingsFile.exists()) {
				settingsFile.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(settingsPath))) {
			bw.write("x=" + getLocation().x);
			bw.newLine();
			bw.write("y=" + getLocation().y);
			bw.newLine();
			bw.write("bgImage=" + bgImagePath);
			bw.newLine();
			bw.write("sleepTime=" + sleepTime);
			bw.newLine();
			bw.write("moveSpeed=" + moveSpeed);
			bw.newLine();
			bw.write("isSleepEnabled=" + isSleepEnabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void setImageBackground(String imagePath) {
		// check if image exists
		java.io.File imageFile = new java.io.File(imagePath);
		if (!imageFile.exists()) {
			JOptionPane.showMessageDialog(this, "Image file not found: " + imagePath, "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		JPanel panel = new JPanel() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon icon = new ImageIcon(imagePath);
				g.drawImage(icon.getImage(), 0, 0, null);
				setSize(icon.getIconWidth(), icon.getIconHeight());
			}
		};
		panel.setLayout(new BorderLayout());
		setContentPane(panel);
		setBackground(new Color(0, 0, 0, 0));
	}

	private void addController() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem closeItem = new JMenuItem("Close");
					closeItem.addActionListener(f -> {
						saveSettings();
						dispose();
						System.exit(0);
					});
					popup.add(closeItem);
					
					JMenuItem disableSleepItem = new JMenuItem();
					if (!isSleepEnabled) {
						disableSleepItem.setText("Enable Sleep");
					} else {
						disableSleepItem.setText("Disable Sleep");
					}
					disableSleepItem.addActionListener(f -> {
						isSleepEnabled = !isSleepEnabled;
					});
					popup.add(disableSleepItem);
					
					// add a menu item to change the background image
					JMenuItem changeBgItem = new JMenuItem("Change Background Image");
					changeBgItem.addActionListener(f -> {
						// open a file chooser to select a new background image
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setDialogTitle("Select Background Image");
						fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
						// set file filter to only show image files
						fileChooser.setAcceptAllFileFilterUsed(false);
						fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "jpg", "png", "gif"));
						int result = fileChooser.showOpenDialog(null);
						if (result == JFileChooser.APPROVE_OPTION) {
							java.io.File selectedFile = fileChooser.getSelectedFile();
							if (selectedFile == null || !selectedFile.exists()) {
								JOptionPane.showMessageDialog(null, "Invalid file selected", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							if (bgImagePath.equals(selectedFile.getAbsolutePath())) {
								JOptionPane.showMessageDialog(null, "Selected image is already set as background", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							bgImagePath = selectedFile.getAbsolutePath();
							setImageBackground(bgImagePath);
							saveSettings();
						}
					});
					popup.add(changeBgItem);
					
					popup.show(getContentPane(), e.getX(), e.getY());
				} else {
					initialX = e.getX();
					initialY = e.getY();
				}
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e))
					return;
				int deltaX = e.getX() - initialX;
				int deltaY = e.getY() - initialY;
				setLocation(getLocation().x + deltaX, getLocation().y + deltaY);
			}
		});
	}

	private static void disableSleep() throws AWTException {
		Robot robot = new Robot();
		while (true) {
			robot.delay(1000*sleepTime); // 1 minute delay
			if (isSleepEnabled) {
				continue;
			}
			PointerInfo a = MouseInfo.getPointerInfo();
			Point b = a.getLocation();
			int x = (int) b.getX();
			int y = (int) b.getY();
			robot.mouseMove(x + moveSpeed, y + moveSpeed);
			moveSpeed = -moveSpeed;
			System.out.println("Mouse moved to: " + x + ", " + y);
		}
	}

	public static void main(String[] args) {
		new Run();
		try {
			disableSleep();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
}
