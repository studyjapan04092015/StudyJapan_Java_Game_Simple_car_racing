package server_module.game_module;

import java.util.Vector;

import com.sun.javafx.runtime.VersionInfo;

public class Game {
	class Player{
		String playerName;
		String playerAddress;
		
		public Player( String name, String addr){
			playerName = name;
			playerAddress = addr;
		}
	}
	public int id_game;
	private Player primarry_player;
	private Vector<Player> list_player;
	private Integer number_of_player_on_game;
	private Integer max_player;
	private boolean game_is_ready;
	
	private String map_name;
	
	public Game( String primarryPlayerName, String primarryPlayerAddress, String mapName, Integer maxPlayer) {
		primarry_player = new Player(primarryPlayerName, primarryPlayerAddress);
		
		list_player = new Vector();
		list_player.addElement(primarry_player);

		number_of_player_on_game = 1;
		max_player = maxPlayer;
		game_is_ready = false;
		
		map_name = mapName;
	}
	
	public boolean addPlayer( String player, String address) {
		if ( number_of_player_on_game < max_player ) {
			list_player.add( new Player(player, address) );
			number_of_player_on_game += 1;
			
			if( number_of_player_on_game == max_player ){
				game_is_ready = true;
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void quitPlayer( String typePlayer, String player, String addr) {
		int index;
		for( index = 0; index < list_player.size(); index++){
			if(list_player.elementAt(index).playerName == player){
				list_player.remove(index);
				break;
			}
		}
		number_of_player_on_game -= 1;
		if( typePlayer.equals("primary player") ){
			if( number_of_player_on_game > 0){
				primarry_player = list_player.firstElement();
			}
		}

		game_is_ready = false;
	}
	
	public boolean gameIsEmpty() {
		if( number_of_player_on_game == 0){
			return true;
		}
		
		return false;
	}
	
	public void changeMap( String map){
		map_name = map;
	}
	
	public int getIdGame() {
		return id_game;
	}
	
	public String getNameOfPrimarryPlayer() {
		return primarry_player.playerName;
	}
	public String getAddressOfPrimarryPlayer() {
		return primarry_player.playerAddress;
	}
	
	public String getMapName() {
		return map_name;
	}
	
	public int getNumberOfPlayerGame() {
		return number_of_player_on_game.intValue();
	}
	
	public int getMaxPlayerOfGame() {
		return max_player.intValue();
	}
}
