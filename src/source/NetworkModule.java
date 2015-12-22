package source;
 
import java.net.*;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class NetworkModule {

	public static class Client {
		public Socket 			socket = null;
		public DataInputStream  in 	   = null;
		public DataOutputStream out    = null;
		public int udpPort = -1;
		public String address = null;
	}

	private Vector clients = new Vector();


	private int nextID = 1;
	private ServerSocket serverSocket = null;

	private synchronized int getNextID() {
		int id = nextID;
		nextID++;
		return id;
	}

	private boolean isHost = true;

	private int assignedID = 0;

	private RacingGame racingGame = null;

	public NetworkModule( int port ) {
		try {
		
			System.out.println( "making server socket" );
			int attempts = 0;
			while ( serverSocket == null ) {

				try {
					serverSocket = new ServerSocket( port );
					System.out.println( "made server socket" );
				}
				catch( Exception e ) {
					port++;
					attempts++;
					if ( attempts > 50 ) break;
					System.out.println( "attempting to rebind port" );
				}
			}
			

			System.out.println( "making datagrams" );
			receiverSocket = new DatagramSocket();
			receiverPacket = new DatagramPacket( new byte[ PACKET_SIZE ], PACKET_SIZE );

			senderSocket = new DatagramSocket();
			System.out.println( "made datagrams" );

			java.awt.Frame frame = new java.awt.Frame( InetAddress.getLocalHost().toString() + ":" + receiverSocket.getLocalPort() );
			frame.add( textArea );
			frame.setSize( 200, 200 );
			
			
			WindowAdapter close = new WindowAdapter() {
			
				public void windowClosing( WindowEvent we ) {
					Object source = we.getSource();
					((Frame)source).dispose();
				}

				public void windowClosed( WindowEvent we ) {
					System.exit( 0 );
				}
			
			};
			
			frame.addWindowListener( close );
			
			frame.show();

		}
		catch( IOException ioe ) {
			System.err.println( ioe );
		}

	}
	
	public void setRacingGame( RacingGame racingGame ) {
		this.racingGame = racingGame;
	}

	private long timeAtReset = -1;

	public void reset() {
		System.out.println( "resetting" );
		status = 0;
		for ( int i = 0; i < receivedDataFrom.length; i++ ) {
			receivedDataFrom[ i ] = false;
			receivedStatuses[ i ] = -1;
			State state = vehicleStates[ i ];
			
			synchronized( state ) {
				state.x = -100;
				state.y = -100;
				state.angle = 0;
				state.vx = 0;
				state.vy = 0;
				state.angularVelocity = 0;
				state.updated = false;
			}
		}
		
		if ( isHost ) {
			sendCommand( "TRACK NAME" );
			sendCommand( trackName );
			sendCommand( "RESET" );
		}
		
	
		timeAtReset = System.currentTimeMillis();
	}
	
	private volatile Thread receiveThread = null; 
	
	public void start() {
		receiveThread = new Thread() {
			public void run() {
				while( Thread.currentThread() == receiveThread ) {
					receiveState();
					Thread.yield();
				}
			}
		};
		receiveThread.setPriority( Thread.MIN_PRIORITY );
		if ( serverSocket != null )
			receiveThread.start();
	}
	
	public void stop() {
		receiveThread = null;
	}

	private java.awt.TextArea textArea = new java.awt.TextArea();
	
	
	public int getAssignedID() {
		return assignedID;
	}

	public synchronized void addClient( InetAddress address, int tcpPort, int udpPort ) {
		try {
			textArea.append( "adding client " + address.getHostAddress() + "\n" );
			System.out.println( "adding client" );
			Socket socket = new Socket( address, tcpPort );
			socket.setSoTimeout( 10000 ); // 5 seconds
			System.out.println( "client socket bound" );
			DataInputStream  in  = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );
			DataOutputStream out = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );

			Client client = new Client();

			client.socket = socket;
			client.in  = in;
			client.out = out;
			client.udpPort = udpPort;
			client.address = address.getHostAddress();

			textArea.append( "sending server details\n" );

			int id = getNextID();

			client.out.writeUTF( "ID" );
			client.out.writeInt( id );

			client.out.writeUTF( "TRACK NAME" );
			client.out.writeUTF( trackName );

			client.out.writeUTF( "NEW CONNECTION" );
			client.out.writeUTF( InetAddress.getLocalHost().getHostAddress() );
			client.out.writeInt( receiverSocket.getLocalPort() );

			client.out.flush();

			textArea.append( "sent server details\n" );

			for ( int i = 0; i < clients.size(); i++ ) {
				Client existingClient = (Client)clients.elementAt( i );

				client.out.writeUTF( "NEW CONNECTION" );
				client.out.writeUTF( existingClient.address );
				client.out.writeInt( existingClient.udpPort );

				client.out.flush();
			}

			textArea.append( "sent details for " + clients.size() + " other client(s)\n" );
			
			clients.addElement( client );


		}
		catch( IOException ioe ) {
			System.err.println( ioe );
		}
	}

	public synchronized void sendNewClientDetails( String address, int port ) {

		for ( int i = 0; i < clients.size(); i++ ) {
			Client client = (Client)clients.elementAt( i );
			try {
				client.out.writeUTF( "NEW CONNECTION" );
				client.out.writeUTF( address );
				client.out.writeInt( port );
				client.out.flush();
			}
			catch( IOException ioe ) {
				System.err.println( ioe );
			}
		}

	}

	public synchronized void sendCommand( String command ) {
		System.out.println( "sending " + command );
		for ( int i = 0; i < clients.size(); i++ ) {
			Client client = (Client)clients.elementAt( i );
			try {
				client.out.writeUTF( command );
				client.out.flush();
			}
			catch( IOException ioe ) {
				System.err.println( ioe );
			}
		}

	}
	
	private void initTrackChoice( Choice choice ) {
		FilenameFilter trackFilter = new FilenameFilter() {
			public boolean accept( File dir, String name ) {
				return name.endsWith( ".track" );
			}
		};
		
		String currentDir = System.getProperty( "user.dir" );
		
		File dir = new File( currentDir );
		String[] tracks = dir.list( trackFilter );
		
		if ( tracks.length == 0 )
			throw new RuntimeException( "could not find any .track files" );
		
		for ( int i = 0; i < tracks.length; i++ ) {
			choice.add( tracks[ i ] );
		}
		
		this.tracks = tracks;
	}

	private String trackName = null;

	public String getTrackName() {
		return trackName;
	}

	private String[] tracks = null;
	private int currentTrack = -1;

	public void nextTrack() {
		currentTrack++;
		currentTrack = currentTrack % tracks.length;
		trackName = tracks[ currentTrack ];
	}

	public void stopWaiting() {
		try {					
			serverSocket.close();
		}
		catch( Exception e ) {
			textArea.append( e.toString() );
		}
	}
	
	public String listenAsServer() {
		Dialog doneDialog = new Dialog( new Frame(), "Done" );
		
		try {
			
			final Dialog dialog = new Dialog( new Frame(), "Server" );
			dialog.setLayout( new FlowLayout() );
			Choice choice = new Choice();
			choice.add( "1" );
			if ( serverSocket != null ) {	
				choice.add( "2" );
				choice.add( "3" );
				choice.add( "4" );
				choice.add( "5" );
				choice.add( "6" );
				choice.add( "7" );
				choice.add( "8" );
			}
			
			dialog.add( new Label( "Players" ) );
			dialog.add( choice );
			
			Choice trackChoice = new Choice();
			initTrackChoice( trackChoice );
			dialog.add( new Label( "Track" ) );
			dialog.add( trackChoice );
			
			Button ok = new Button( "Ok" );
			ok.setActionCommand( "ok" );
			Button cancel = new Button( "Cancel" );
			cancel.setActionCommand( "cancel" );

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					String command = ae.getActionCommand();
					if ( command.equals( "ok" ) ) {
						dialog.dispose();
					}
					else if ( command.equals( "cancel" ) ) {
						System.exit( 0 );
					}
					else if ( command.equals( "done" ) ) {
						stopWaiting();
					}
				}
			};

			ok.addActionListener( actionListener );
			cancel.addActionListener( actionListener );
			dialog.add( ok );
			dialog.add( cancel );

			dialog.setModal( true );
			dialog.pack();
			dialog.show();

			int numPlayers = choice.getSelectedIndex();

			trackName = trackChoice.getSelectedItem();
			currentTrack = trackChoice.getSelectedIndex();
			
			if ( numPlayers == 0 ) {
				textArea.append( "Done\n" );
				return "DONE";
			}
			
			
			Button done = new Button( "Stop Waiting" );
			done.setActionCommand( "done" );
			done.addActionListener( actionListener );
			doneDialog.setLayout( new GridLayout( 3, 1 ) );
			doneDialog.add( new Label( "press to stop waiting for everyone else" ) );
			doneDialog.add( new Label( "otherwise wait until all players have joined" ) );
			doneDialog.add( done );
			doneDialog.pack();
			doneDialog.show();
			
			while( true ) {

				int playersLeft = (numPlayers-clients.size());
				
				//System.out.println( "listening as server for " + playersLeft + " more player" + (playersLeft > 1? "s" : "") );
				textArea.append(  "listening as server for " + playersLeft + " more player" + (playersLeft > 1? "s" : "") + "\n" );
				Socket socket = serverSocket.accept();
				//System.out.println( "got a client" );

				DataInputStream in = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );

				System.out.println( "got a client" );

				String addressString = in.readUTF();
				
				System.out.println( addressString );

				int tcpPort = in.readInt();

				System.out.println( "tcp: " + tcpPort );

				int udpPort = in.readInt();

				System.out.println( "udp: " + udpPort );
				socket.close();

				System.out.println( "getting address" );
				InetAddress address = socket.getInetAddress();
				System.out.println( "got address" );

				addSender( addressString, address, udpPort );

				sendNewClientDetails( address.getHostAddress(), udpPort );

				addClient( address, tcpPort, udpPort );
				
				//thread.start();

				if ( clients.size() == (numPlayers) ) {
					textArea.append( "Done\n" );
					sendCommand( "DONE" );
					//socket.close();
					serverSocket.close();
					
					doneDialog.dispose();
					return "DONE";
				}
			}
		}
		catch( IOException ioe ) {
			//System.err.println( ioe );
			//return "IOException";
			textArea.append( "Done\n" );
			sendCommand( "DONE" );
			doneDialog.dispose();		
			return "DONE/via IOException";
		}

		//return "CONNECTED";
	}

	public String connectToServer( String address, int port ) {

		isHost = false;
		
		try {
			System.out.println( "connecting to server" );
			Socket socket = new Socket( address, port );
			textArea.append( "connected to host\n" );
			System.out.println( "connected to server" );

			DataOutputStream out = new DataOutputStream( new BufferedOutputStream( socket.getOutputStream() ) );

			out.writeUTF( socket.getLocalAddress().getHostAddress() );

			out.writeInt( serverSocket.getLocalPort() );

			out.writeInt( receiverSocket.getLocalPort() );

			out.flush();

			socket.close();
			
			System.out.println( "sent data to server" );
			
			return listenAsClient();
		}
		catch( IOException ioe ) {
			System.err.println( ioe );
		}

		return "ERROR";
	}

	public String listenForCommand() {
	
		try {
			String command = connectionToHost.readUTF();

			if ( command.equals( "ID" ) ) {
				assignedID = connectionToHost.readInt();
				textArea.append( "ID " + assignedID + "\n" );
			}
			else if ( command.equals( "TRACK NAME" ) ) {
				trackName = connectionToHost.readUTF();
				System.out.println( trackName );
				textArea.append( "Track " + trackName + "\n" );
			}
			else if ( command.equals( "NEW CONNECTION" ) ) {

				String address = connectionToHost.readUTF();
				int udpPort    = connectionToHost.readInt();

				System.out.println( "client read: " + command + " " + address + " " + udpPort );

				addSender( address, InetAddress.getByName( address ), udpPort );
			}
			else if ( command.equals( "RESET" ) ) {
				textArea.append( "Reset\n" );	
				racingGame.reset();
				racingGame.start();
			}
			else if ( command.equals( "DONE" ) ) {
				textArea.append( "Done\n" );	
			}
			System.out.println( "received " + command );
			return command;
		}
		catch( IOException ioe ) {
			//System.err.println( ioe );
			return "IOException";
		}
	}

	private DataInputStream connectionToHost = null;

	public String listenAsClient() {

		try {

			System.out.println( "listening as client" );
			Socket socket = serverSocket.accept();
			socket.setSoTimeout( 10000 ); // 10 seconds
			System.out.println( "client got a server" );

			connectionToHost = new DataInputStream( new BufferedInputStream( socket.getInputStream() ) );

			while ( true ) {
				String command = listenForCommand();
				
				if ( command.equals( "DONE" ) ) {
					Thread thread = new Thread() {
						public void run() {
							while( true ) {
								String cmnd = listenForCommand();
								if ( !cmnd.equals( "IOException" ) )
									textArea.append( cmnd + "\n" );
								Thread.yield();
							}
						}
					};
					thread.start();
					return command;
				}
			}
		}
		catch( IOException ioe ) {
			System.err.println( ioe );
			return "IOException";
		}

		//return "CONNECTED";
	}

	public static class State {
		public float x;
		public float vx;
		public float y;
		public float vy;
		public float angle;
		public float angularVelocity;
		public boolean updated = false;
	}

	private DatagramSocket receiverSocket = null;
	private DatagramPacket receiverPacket = null;
	private State[] vehicleStates = { new State() };
	private boolean[] receivedDataFrom = { true };
	private byte[] receivedStatuses = { (byte)-1 };
	private long[] receivedTimes = { -1 };
	
	private DatagramSocket senderSocket = null;
	private DatagramPacket[] senderPackets = new DatagramPacket[ 0 ];
	private final int PACKET_SIZE = 26;
	private final byte[] senderData = new byte[ PACKET_SIZE ];




	//private java.awt.TextArea textArea = new java.awt.TextArea();


	/** Add someone who wants to listen to us. **/

	public synchronized void addSender( String addressString, InetAddress address, int port ) {

		//System.out.println( "sender " + address + " " + port  );

		textArea.append( addressString + ":" + port + "\n" );

		System.out.println( "adding sender" );
		
		byte[] data = new byte[ PACKET_SIZE ];
		DatagramPacket packet = new DatagramPacket( data, PACKET_SIZE, address, port );

		if ( senderPackets == null )
			senderPackets = new DatagramPacket[ 0 ];

		DatagramPacket[] oldsenders = senderPackets;

		DatagramPacket[] newsenders = new DatagramPacket[ oldsenders.length +1 ];

		System.arraycopy( oldsenders, 0, newsenders, 0, oldsenders.length );

		newsenders[ oldsenders.length ] = packet;

		senderPackets = newsenders;

		vehicleStates = new State[ senderPackets.length +1 ]; // for us AND others
		receivedDataFrom = new boolean[ vehicleStates.length ];
		receivedStatuses = new byte[ vehicleStates.length ];
		receivedTimes = new long[ vehicleStates.length ];
		for ( int i = 0; i < vehicleStates.length; i++ ) {
			vehicleStates[ i ] = new State();
			receivedDataFrom[ i ] = false;
			receivedStatuses[ i ] = (byte)-1;
			receivedTimes[ i ] = -1;
		}

		System.out.println( "sender added"  );
	}


	public State[] getVehicleStates() {
		return vehicleStates;
	}

	private byte status = 0;
	
	/** Broadcast the state of a vehicle to all of the other players. **/

	public void broadcastState( Vehicle vehicle, Body.State state ) {

		int x = Float.floatToIntBits( state.x );
		int y = Float.floatToIntBits( state.y );
		int angle = Float.floatToIntBits( state.angle );
		int vx = Float.floatToIntBits( state.vx );
		int vy = Float.floatToIntBits( state.vy );
		int angularVelocity = Float.floatToIntBits( state.angularVelocity );

		int pos = 0;
		senderData[ pos ] = status;
		pos++;
		senderData[ pos ] = vehicle.getID();
		pos++;
		pos = encodeInt( x, senderData, pos );
		pos = encodeInt( y, senderData, pos );
		pos = encodeInt( angle, senderData, pos );
		pos = encodeInt( vx, senderData, pos );
		pos = encodeInt( vy, senderData, pos );
		pos = encodeInt( angularVelocity, senderData, pos );

		for ( int i = 0; i < senderPackets.length; i++ ) {
			DatagramPacket packet = senderPackets[ i ];
			packet.setData( senderData );
			try {
				senderSocket.send( packet );
			}
			catch( IOException ioe ){
				System.err.println( ioe );
			}
		}
	}

	public boolean ready() {
		byte minStatus = (byte)3;
		receivedStatuses[ assignedID ] = status;
		for ( int i = 0; i < receivedDataFrom.length; i++ ) {
			byte receivedStatus = receivedStatuses[ i ];
			
			// the game carries on if someone drops out
			// i.e. we have not heard from them in a
			// second or two
			if ( (receivedTimes[ i ] - receivedTimes[ assignedID ]) < 5000 )
				minStatus = receivedStatus < minStatus? receivedStatus : minStatus;
		}

		if ( minStatus >= status )
			status++;
		
		return status >= 3;
	}
	
	public byte getStatus() {
		return status;
	}

	public void receiveState() {

		receivedDataFrom[ assignedID ] = true;
		receivedStatuses[ assignedID ] = status;
		receivedTimes[ assignedID ] = System.currentTimeMillis();
		
		if ( receiveThread == null )
			return;
		
		try {
			receiverSocket.setSoTimeout( 4000 );
			receiverSocket.receive( receiverPacket );
		}
		catch( IOException ioe ) {
			// timed out
			//System.err.println( ioe );
		}
		byte[] receivedData = receiverPacket.getData();

		int pos = 0;
		
		byte receivedstatus = receivedData[ pos ];
		pos++;
		byte id = receivedData[ pos ];
		pos++;
		int x = decodeInt( receivedData, pos );
		int y = decodeInt( receivedData, pos +4 );
		int angle = decodeInt( receivedData, pos +8 );
		int vx = decodeInt( receivedData, pos +12);
		int vy = decodeInt( receivedData, pos +16 );
		int angularVelocity = decodeInt( receivedData, pos +20 );

		State state = vehicleStates[ id ];

		synchronized( state ) {
			state.x = Float.intBitsToFloat( x );
			state.y = Float.intBitsToFloat( y );
			state.angle = Float.intBitsToFloat( angle );
			state.vx = Float.intBitsToFloat( vx );
			state.vy = Float.intBitsToFloat( vy );
			state.angularVelocity = Float.intBitsToFloat( angularVelocity );
			receivedDataFrom[ id ] = true;
			receivedStatuses[ id ] = receivedstatus;
			receivedTimes[ id ] = System.currentTimeMillis();
			state.updated = true;
		}
	}

	private int decodeInt( byte[] data, int pos ) {
		int b1 = 0xFF & data[ pos    ];
		int b2 = 0xFF & data[ pos +1 ];
		int b3 = 0xFF & data[ pos +2 ];
		int b4 = 0xFF & data[ pos +3 ];

		int anInt = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;

		return anInt;
	}

	private int encodeInt( int anInt, byte[] data, int pos ) {
		data[ pos    ] = (byte)((0xFF000000 & anInt) >> 24);
		data[ pos +1 ] = (byte)((0x00FF0000 & anInt) >> 16);
		data[ pos +2 ] = (byte)((0x0000FF00 & anInt) >> 8);
		data[ pos +3 ] = (byte)((0x000000FF & anInt));
		return pos + 4;
	}


	public static void main( String[] args ) {

		if ( args.length == 0 ) {

			final NetworkModule host = new NetworkModule( 1234 );

			Thread hostThread = new Thread() {
				public void run() {
					String result = host.listenAsServer();
					System.out.println( result );
				}
			};

			hostThread.start();

			return;

		}

		final String host = args[ 0 ];

		final NetworkModule client = new NetworkModule( 1235 );

		Thread clientThread = new Thread() {
			public void run() {
				String result = client.connectToServer( host, 1234 );
				System.out.println( result );
			}
		};

		clientThread.start();

		final NetworkModule client2 = new NetworkModule( 1236 );

		Thread clientThread2 = new Thread() {
			public void run() {
				String result = client2.connectToServer( host, 1234 );
				System.out.println( result );
			}
		};

		clientThread2.start();
	}
}