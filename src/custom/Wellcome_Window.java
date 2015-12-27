package custom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import custom.Register_new_account;
import source.RacingGame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

@SuppressWarnings("serial")

/**
 * @author daoduynhan
 *
 */
public class Wellcome_Window extends JFrame {
	private JTextField textField;
	private JPasswordField passwordField;
	private JLabel lblWelcome;
	private Socket connectionWithServerGame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Wellcome_Window frame = new Wellcome_Window("Simple Racing Car");
		frame.setVisible(true);
	}

	/**
	 * Create the frame.
	 */
	public Wellcome_Window( String nameFrame) {
		super( nameFrame );
		try {
			connectionWithServerGame = new Socket("localhost", 6789);
		} catch (Exception e) {
		}
		
		getContentPane().setForeground(new Color(0, 0, 0));
		getContentPane().setFont(new Font("Tahoma", Font.BOLD, 13));
		getContentPane().setBackground(new Color(34, 139, 34));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 346, 202);
		getContentPane().setLayout(null);

		JLabel lblUser = new JLabel("User");
		lblUser.setForeground(Color.BLACK);
		lblUser.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblUser.setBounds(49, 43, 46, 14);
		getContentPane().add(lblUser);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblPassword.setBounds(49, 68, 63, 14);
		getContentPane().add(lblPassword);

		textField = new JTextField();
		textField.setForeground(Color.BLUE);
		textField.setBounds(122, 41, 111, 20);
		getContentPane().add(textField);
		textField.setColumns(10);

		passwordField = new JPasswordField();
		passwordField.setBounds(122, 66, 111, 20);
		getContentPane().add(passwordField);

		JButton btnLogin = new JButton("Login");
		btnLogin.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnLogin.setForeground(Color.BLUE);
		btnLogin.setBounds(99, 96, 71, 23);
		getContentPane().add(btnLogin);
		SwingUtilities.getRootPane(btnLogin).setDefaultButton(btnLogin);
		btnLogin.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				Boolean status = false;

				try {
					if (login_info_is_true( textField.getText(), passwordField.getText())) {
						dispose();

						ViewRooms.main( connectionWithServerGame, textField.getText());
					} else {
						JOptionPane.showMessageDialog(null, "Incorrect User/Password");
					}
				} catch (Exception e) {
				}
			}
		});

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setForeground(Color.BLUE);
		btnCancel.setBounds(190, 95, 71, 22);
		getContentPane().add(btnCancel);
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose();
				System.exit(0);
			}
		});
		btnCancel.setFont(new Font("Tahoma", Font.BOLD, 11));

		JButton btnSignUp = new JButton("Sign up");
		btnSignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					dispose();
					Register_new_account reg_new_acc = new Register_new_account();
					reg_new_acc.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		btnSignUp.setFont(new Font("Tahoma", Font.BOLD, 11));
		btnSignUp.setForeground(Color.BLUE);
		btnSignUp.setBounds(231, 130, 89, 23);
		getContentPane().add(btnSignUp);
	}

	private boolean login_info_is_true( String acc, String pass) {
		String answerFromServer;

		try {
		DataOutputStream outToServer = new DataOutputStream(connectionWithServerGame.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(
				new InputStreamReader(connectionWithServerGame.getInputStream()));

		outToServer.writeBytes("LOGIN" + '\n' + acc + '\n' + pass + '\n');

		answerFromServer = inFromServer.readLine();


		if (answerFromServer.equals("LOGIN SUCCESS")) {
			return true;
		}
		} catch (Exception e) {
		}
		return false;
	}
}
