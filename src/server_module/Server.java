package server_module;

import server_module.Service;

import java.io.*;
import java.net.*;

import javax.lang.model.type.PrimitiveType;
import javax.swing.JFrame;

import sun.print.resources.serviceui;

public class Server extends JFrame {
	private boolean isLive;
	
	public Server() {
		System.out.println("A Server game was created...");
		isLive = true;
	}
	
	public void start() throws Exception{
		System.out.println("Server is serviceing...");
		ServerSocket watting_socket = new ServerSocket(6789);
		while ( isLive ){
			Socket connectionSocket = watting_socket.accept();
			
			Service Server_service = new Service(connectionSocket, this);
			Server_service.start_service();
		}
		watting_socket.close();
		
	}
	
	public void turnoff(){
		this.isLive = false;
	}
	
	public static void main( String argv[]) throws Exception {	

		Server ServerGame = new Server();
		ServerGame.start();;
	}
}
