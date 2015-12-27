package server_module;

import server_module.Service;
import server_module.game_module.Game;

import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.lang.model.type.PrimitiveType;
import javax.swing.JFrame;

import sun.print.resources.serviceui;

public class Server extends JFrame {
	private boolean isLive;
	private Vector<Game> listGame;
	
	public Server() {
		System.out.println("A Server game was created...");
		isLive = true;
		
		listGame = new Vector();
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
	
	public void addGame( Game newGame) {
		newGame.id_game = listGame.size() + 1;
		listGame.add(newGame);
	}
	
	public Game getGame( int id_game) {
		int index_game = 0;
		for (; index_game < listGame.size(); index_game++){
			if( id_game == listGame.elementAt(index_game).id_game){
				break;
			}
		}
		
		return listGame.elementAt(index_game);
	}
	
	public void checkGameToRemove( Game gameCheck) {
		if( gameCheck.gameIsEmpty() ){
			listGame.remove(gameCheck);
		}
	}
	
	public int numberGames() {
		return listGame.size();
	}
	
	public Game getGameForDisplayInfoGame( int index ){
		return listGame.elementAt(index);
	}
	
	public static void main( String argv[]) throws Exception {	

		Server ServerGame = new Server();
		ServerGame.start();;
	}
}
