package custom;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.sun.glass.ui.View;


public class ViewRooms extends JFrame {

	private Socket socketConnectWithServer;
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;
	Container conTainer;
	private String Account;
	private Timer timer;

	public ViewRooms(String title, Socket socketWithServer, String acc) {

		socketConnectWithServer = socketWithServer;
		Account = acc;

		try {
			outToServer = new DataOutputStream(socketConnectWithServer.getOutputStream());
			inFromServer = new BufferedReader(new InputStreamReader(socketConnectWithServer.getInputStream()));
			
		} catch (Exception e) {
		}
		conTainer = getContentPane();
		conTainer.setLayout(new GridLayout(2, 1));

		setTitle(title);
		JPanel pnHeader = new JPanel();
		pnHeader.setLayout(new GridLayout(1, 2));
		pnHeader.setBackground(Color.PINK);
		JButton btn1 = new JButton("Say bye and exit");
		btn1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				JOptionPane.showMessageDialog(null, "Bye!");
				System.exit(NORMAL);
			}
		});
		pnHeader.add(btn1);
		JButton createGame = new JButton("Create new game");
		createGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					timer.stop();
					dispose();
					Create_new_game.main(socketConnectWithServer, Account);
					/*
					outToServer.writeBytes("CREATE GAME" + '\n');
					outToServer.writeBytes( Account + '\n');
					outToServer.writeBytes( InetAddress.getLocalHost().getHostAddress() + '\n');
					outToServer.writeBytes( "Osscar" + '\n');
					outToServer.writeBytes( Integer.toString(10) + '\n');
					*/
				} catch (Exception e) {
				}
			}
		});
		pnHeader.add(createGame);
		conTainer.add(pnHeader);

		JPanel pnFlow = new JPanel();

		pnFlow.setLayout(new GridLayout(5, 1));
		pnFlow.setBackground(Color.PINK);
		conTainer.add(pnFlow);

		repaint();

		timer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				try {
					pnFlow.removeAll();
					outToServer.writeBytes("LIST GAMES" + '\n');
					String answerFromServer = inFromServer.readLine();
					while (false == answerFromServer.equals("END LIST GAMES")) {
						String idGame = inFromServer.readLine();
						System.out.println(idGame);
						String nameOfPrimarryPlayer = inFromServer.readLine();
						System.out.println(nameOfPrimarryPlayer);
						String addressOfPrimarryPlayer = inFromServer.readLine();
						System.out.println(addressOfPrimarryPlayer);
						String mapName = inFromServer.readLine();
						System.out.println(mapName);
						String numberOfPlayerGame = inFromServer.readLine();
						System.out.println(numberOfPlayerGame);
						String maxPlayer = inFromServer.readLine();
						System.out.println(maxPlayer);

						String nameButton ;
						nameButton = idGame + mapName + numberOfPlayerGame + maxPlayer + nameOfPrimarryPlayer;

						System.out.println( nameButton);
						JButtonGame buttonGame = new JButtonGame( idGame);
						buttonGame.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								JOptionPane.showMessageDialog(null, "Join game!");
							}
						});
						pnFlow.add(buttonGame);
						repaint();
						revalidate();
						answerFromServer = inFromServer.readLine();
					}
					repaint();
					revalidate();

				} catch (Exception e) {
				}
			}
		});
		timer.setRepeats(true);
		timer.start();

	}

	public static void main(Socket socketWithServer, String accInfo) {

		ViewRooms myUI = new ViewRooms("Demo FlowLayout", socketWithServer, accInfo);

		myUI.setSize(600, 200);
		myUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myUI.setLocationRelativeTo(null);
		myUI.setVisible(true);

	}
}