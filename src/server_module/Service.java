package server_module;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.lang.model.type.PrimitiveType;
import javax.xml.stream.events.StartDocument;

import com.sun.javafx.runtime.VersionInfo;

public class Service {
	private boolean live;
	private Socket connectionSocket;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	private Server server_parent;
	private Connection connectionToDatabase;

	private String inputFromClient;

	public Service(Socket connection, Server server_obj) throws Exception {
		live = true;
		connectionSocket = connection;
		inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		outToClient = new DataOutputStream(connectionSocket.getOutputStream());
		server_parent = server_obj;
		connectToDatabase();

		System.out.println("A sevice was create....");
	}

	private void stop_service() {
		live = false;
	}

	public void start_service() throws Exception {

		while (live) {
			System.out.println("Read line from client");
			while (false == inFromClient.ready()) {
				if (connectionSocket.isClosed()) {
					stop_service();
					return;
				}

			}
			inputFromClient = inFromClient.readLine();

			if (inputFromClient.equals("turn off server")) {
				turn_off_server();
			}
			if (inputFromClient.equals("close socket")) {
				close_socket();
			}
			if (inputFromClient.equals("LOGIN")) {
				login_service();
			}
			if (inputFromClient.equals("REGISTER-REVIEW")) {
				register_review();
			}
			if (inputFromClient.equals("REGISTER")) {
				register_new_account_service();
			}
		}
	}

	private void turn_off_server() throws Exception {
		System.out.println("Stop service and turn off the server!");
		stop_service();
		server_parent.turnoff();

		System.out.println("Reply client...");
		outToClient.writeBytes(inputFromClient.toUpperCase() + '\n');
	}

	private void close_socket() throws Exception {
		stop_service();
		System.out.println("Closing socket");
		connectionSocket.close();
	}

	private void connectToDatabase() {
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql://localhost/Car_Racing";
			connectionToDatabase = DriverManager.getConnection(url, "daoduynhan", "123456hml");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	private boolean login_info_is_true(String acc, String pass) {
		try {
			String querry = "select \"Acc\", \"Pass\" from \"Account\" where \"Acc\" = '" + acc + "';";
			Statement st = connectionToDatabase.createStatement();
			ResultSet rs = st.executeQuery(querry);
			while (rs.next()) {
				String pass_result = rs.getString("Pass");
				if (true == pass_result.equals(pass)) {
					rs.close();
					st.close();

					return true;
				}
			}
			rs.close();
			st.close();
		} catch (SQLException se) {
			System.err.println(se.getMessage());
		}
		return false;
	}

	private void login_service() throws Exception {

		String user_name = inFromClient.readLine();
		String user_pass = inFromClient.readLine();
		System.out.println("Login infomation from client: " + "'" + user_name + "' and " + "'" + user_pass + "'");

		if (login_info_is_true(user_name, user_pass)) {
			System.out.println("New user login success");
			outToClient.writeBytes("login success" + '\n');
		} else {
			System.out.println("New user login failse");
			outToClient.writeBytes("login false" + '\n');
		}
	}

	private void register_review() {
		try {
			String user_name = inFromClient.readLine();
			String querry = "select \"Acc\", \"Pass\" from \"Account\" where \"Acc\" = '" + user_name + "';";
			Statement st = connectionToDatabase.createStatement();
			ResultSet rs = st.executeQuery(querry);
			System.out.println("Info of register account  " + user_name);
			while (rs.next()) {
				String user_result = rs.getString("Acc");
				if (true == user_result.equals(user_name)) {
					rs.close();
					st.close();
					System.out.println("User was exist!");
					outToClient.writeBytes("user exist" + '\n');
				}
			}
			rs.close();
			st.close();
			outToClient.writeBytes("user not exist" + '\n');
		} catch (Exception e) {

		}

	}

	private void register_new_account_service() {
		try {
			String user_name = inFromClient.readLine();
			String user_pass = inFromClient.readLine();
			System.out
					.println("Register infomation from client: " + "'" + user_name + "' and " + "'" + user_pass + "'");
			String querry = "INSERT INTO \"Account\" ( \"Acc\", \"Pass\") values ('" + user_name + "', '" + user_pass
					+ "'); ";
			Statement st = connectionToDatabase.createStatement();
			ResultSet rs = st.executeQuery(querry);
		} catch (Exception e) {

		}
	}
}
