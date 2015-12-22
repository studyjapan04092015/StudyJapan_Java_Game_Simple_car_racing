package custom;

import javax.swing.JDialog;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class Register_new_account extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textField;
	private JPasswordField passwordField;
	private JPasswordField passwordField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Register_new_account dialog = new Register_new_account();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Register_new_account() {

		setTitle("New User Signup");

		setBackground(SystemColor.textHighlight);
		setForeground(SystemColor.textHighlight);
		setBounds(100, 100, 385, 225);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setForeground(SystemColor.textHighlight);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lblUser = new JLabel("User name");
		lblUser.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblUser.setBounds(93, 25, 35, 14);
		contentPanel.add(lblUser);

		JLabel lblPassword = new JLabel("Password");
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPassword.setBounds(68, 62, 67, 14);
		contentPanel.add(lblPassword);

		textField = new JTextField();
		textField.setBounds(154, 22, 102, 20);
		contentPanel.add(textField);
		textField.setColumns(10);

		passwordField = new JPasswordField(50);
		passwordField.setBounds(154, 59, 102, 20);
		contentPanel.add(passwordField);

		JLabel lblResetPassword = new JLabel("Repeat password");
		lblResetPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblResetPassword.setBounds(33, 92, 102, 14);
		contentPanel.add(lblResetPassword);

		passwordField_1 = new JPasswordField(50);
		passwordField_1.setBounds(154, 90, 102, 20);
		contentPanel.add(passwordField_1);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setForeground(SystemColor.textHighlight);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						Connection conn = null;
						try {
							conn = connectToDatabase();
							String username = textField.getText();
							String password = passwordField.getText();
							Boolean status = true;

							// Check if user is exist

							if ( true == user_is_exist(conn, username)) {
								 status = false;
							}

							if (status == false) {
								JOptionPane.showMessageDialog(null, "User available", "Error",
										JOptionPane.PLAIN_MESSAGE);
								Register_new_account NU = new Register_new_account();

							} else {
								if (username.equals("") || password.equals("")) {
									JOptionPane.showMessageDialog(null, "Name or password require", "Error",
											JOptionPane.ERROR_MESSAGE);
								} else if ( passwordField_1.getText().equals(passwordField.getText()) == false) {
									System.out.println(passwordField.getText());
									System.out.println(passwordField_1.getText());
									JOptionPane.showMessageDialog(null, "Password not match", "Error",
											JOptionPane.PLAIN_MESSAGE);
								} else {
									register_user(conn, username, password);

									String SMessage = "Signed up successfully";

									JOptionPane.showMessageDialog(null, SMessage, "Message", JOptionPane.PLAIN_MESSAGE);
									((java.sql.Connection) conn).close();
									dispose();
									Wellcome_Window Restart_Wellcome = new Wellcome_Window();
									Restart_Wellcome.setVisible(true);
									new Frame().add(Restart_Wellcome, BorderLayout.CENTER);
								}
							}
						} catch (SQLException se) {
							se.printStackTrace();
						} catch (Exception a) {
							a.printStackTrace();
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent arg0) {
						dispose();
						Wellcome_Window Restart_Wellcome = new Wellcome_Window();
						Restart_Wellcome.setVisible(true);
						new Frame().add(Restart_Wellcome, BorderLayout.CENTER);
					}

				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);

			}
		}
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

	private boolean user_is_exist(Connection conn, String acc) {
		try {
			String querry = "select \"Acc\", \"Pass\" from \"Account\" where \"Acc\" = '" + acc + "';";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(querry);
			System.out.println("Info of login session: " + acc);
			while (rs.next()) {
				String user_result = rs.getString("Acc");
				if ( true == user_result.equals(acc)) {
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
	private void register_user(Connection conn, String acc, String pass) {
		try {
			String querry = "INSERT INTO \"Account\" ( \"Acc\", \"Pass\") values ('" + acc + "', '" + pass + "'); ";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(querry);
		} catch (SQLException se) {
			System.err.println("Threw a SQLException creating the list of blogs.");
			System.err.println(se.getMessage());
		}
	}

}
