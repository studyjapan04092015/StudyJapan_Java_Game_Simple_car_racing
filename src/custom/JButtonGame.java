package custom;

import javax.swing.JButton;

public class JButtonGame extends JButton{
	public Integer id_game;

	public String nameOfPrimarryPlayer;
	public String addressOfPrimarryPlayer;

	public Integer number_of_player_on_game;
	public Integer max_player;
	public boolean game_is_ready;
	
	public String map_name;
	
	public JButtonGame( String nameButton) {
		super(nameButton);
	}
}
