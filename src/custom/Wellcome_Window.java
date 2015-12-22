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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;

@SuppressWarnings("serial")

/**
 * @author daoduynhan
 *
 */
public class Wellcome_Window extends JDialog {
	private JTextField textField;
	private JPasswordField passwordField;
	static JLabel lblWelcome;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		final JFrame jframe = new JFrame("Simple Racing car");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Wellcome_Window frame = new Wellcome_Window();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Wellcome_Window() {
		// TODO Auto-generated constructor stub
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
				Connection conn;
				Boolean status = false;

				try {
					conn = connectToDatabase();

					if ( login_info_is_true(conn, (textField.getText()).toString(), (passwordField.getPassword()).toString()) ) {
						dispose();

						JOptionPane.showMessageDialog(null, "Logged successfully !", "Message",
								JOptionPane.PLAIN_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "Incorrect User/Password");
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, e.getMessage());
					e.printStackTrace();
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
					return;
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

	private Connection connectToDatabase() {
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost/Car_Racing";
			conn = DriverManager.getConnection(url, "daoduynhan", "123456hml");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(2);
		}
		return conn;
	}

	private boolean login_info_is_true(Connection conn, String acc, String pass) {
		try {
			String querry = "select \"Acc\", \"Pass\" from \"Account\" where \"Acc\" = '" + acc +"';" ;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(querry);
			System.out.println( "Info of login session: " + acc + pass);
			while( rs.next() ) {
				String pass_result = rs.getString("Pass");
				System.out.println("Info from server: " +pass_result);
				if ( !pass_result.equals(pass)){
					rs.close();
					st.close();
					
					return true;
				}
			}
			rs.close();
			st.close();
		} catch (SQLException se) {
			System.err.println("Threw a SQLException creating the list of blogs.");
			System.err.println(se.getMessage());
		}
		return false;
	}
}
