package custom;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Create_new_game extends JFrame {
	private String[] tracks = null;
	private Socket socketWithServer;
	private String acc;
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;

	public Create_new_game(String title, Socket socketConnectToServer, String account) {
		socketWithServer = socketConnectToServer;
		acc = account;

		setTitle(title);
		JPanel pnFlow = new JPanel();
		pnFlow.setLayout(new FlowLayout());
		pnFlow.setBackground(Color.PINK);

		Choice choice = new Choice();
		choice.add("1");
		choice.add("2");
		choice.add("3");
		choice.add("4");
		choice.add("5");
		choice.add("6");
		choice.add("7");
		choice.add("8");

		pnFlow.add(new Label("Players"));
		pnFlow.add(choice);

		Choice trackChoice = new Choice();
		initTrackChoice(trackChoice);
		pnFlow.add(new Label("Track"));
		pnFlow.add(trackChoice);
		Button ok = new Button("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				try {
					outToServer = new DataOutputStream(socketWithServer.getOutputStream());
					inFromServer = new BufferedReader(new InputStreamReader(socketWithServer.getInputStream()));
					outToServer.writeBytes("CREATE GAME" + '\n');
					outToServer.writeBytes( acc + '\n');
					outToServer.writeBytes( InetAddress.getLocalHost().getHostAddress() + '\n');
					outToServer.writeBytes( trackChoice.getSelectedItem() + '\n');
					outToServer.writeBytes( choice.getSelectedItem() + '\n');
					ViewRooms.main(socketWithServer, acc);
				} catch (Exception e) {
				}
			}
		});
		pnFlow.add(ok);

		Button cancel = new Button("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					dispose();
					ViewRooms.main(socketWithServer, acc);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		pnFlow.add(cancel);

		Container con = getContentPane();
		con.add(pnFlow);

	}

	private void initTrackChoice(Choice choice) {
		FilenameFilter trackFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".track");
			}
		};

		String currentDir = System.getProperty("user.dir");

		File dir = new File(currentDir);
		String[] tracks_tmp = dir.list(trackFilter);

		if (tracks_tmp.length == 0)
			throw new RuntimeException("could not find any .track files");

		for (int i = 0; i < tracks_tmp.length; i++) {
			choice.add(tracks_tmp[i]);
		}

		tracks = tracks_tmp;
	}

	public static void main(Socket server, String acc) {

		Create_new_game myUI = new Create_new_game("Demo FlowLayout", server, acc);

		myUI.setSize(550, 60);
		myUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myUI.setLocationRelativeTo(null);
		myUI.setVisible(true);

	}

}