package source;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class RacingGame extends Applet implements Runnable {
	private Race race = null;
	private Vehicle vehicle = null;
	private Color[] colors = null;
	private Track track = null;
	private Image trackImage = null;
	private final int TRACK_IMAGE_SIZE = 48;
	private int[] trackImagePixels = null;
	private int[] trackPixels = null;
	private NetworkModule module = null;
	private Font font = new Font( "monospaced", Font.PLAIN, 15 );
	private Font bigfont = new Font( "monospaced", Font.BOLD, 20 );
	private boolean limit = true; // limit frame rate
	private int showLimitFor = 50;
	private boolean showfps = false; // show frames per second
	
	public RacingGame() {

		track = new Track();
		//track.sections = sections;
		setFont( font );
		setBackground( Color.white );
	}

	public RacingGame( byte id, Vehicle[] vehicles, NetworkModule module ) {
		this();
		this.module = module;
		module.setRacingGame( this );
		vehicle = vehicles[ id ];
		colors = new Color[ vehicles.length ];
		
		race = new Race( vehicle, track, vehicles, module );

		reset();

		
		initControl();
	}
	
	private Frame frame = null;
	
	public void reset() {
		
		//System.out.println( 
		stop();
		
		while( !threadFinished ) {
			try {
				Thread.sleep( 10 );
			}
			catch( Exception e ){};
		}
		
		Graphics g = getGraphics();
		if ( g != null ) {
			g.clearRect( 0, 0, size.width, size.height );
			g.drawString( "Resetting", 100, 100 );
			
		}
			
		System.out.println( "reseting racinggame" );
		
		if ( module != null ) {
	
			boolean firsttime = track.sections == null;
			try {
		
				java.io.Reader in = new java.io.FileReader( module.getTrackName() );

				track.load( in );

				in.close();
		
			}
			catch( java.io.IOException e ) {
				System.err.println( "error loading file" );
				System.err.println( e );
			}
			
			if ( !firsttime )
				module.reset();
				
			if ( frame != null )
				frame.setTitle( module.getTrackName() );
			
			//System.out.println( "sections " + track.sections.length );
		}
		
		race.reset();
		
		finalPosition = null;
		trackImage = null;
		
		vehicle.setMotorOn( false );
		vehicle.reverse( false );
		vehicle.steerLeft( false );
		vehicle.steerRight( false );
	}
	
	public String getAppletInfo() {
		return "RacingGame 1.0 (C) 2001 John S Montgomery";
	}
	
	private boolean green = false;
	
	public void initControl() {
		KeyAdapter keys = new KeyAdapter() {
			public void keyPressed( KeyEvent ke ) {
				switch( ke.getKeyCode() ) {
					case KeyEvent.VK_UP: vehicle.setMotorOn( true ); break;
					case KeyEvent.VK_DOWN: vehicle.reverse( true ); break;
					case KeyEvent.VK_LEFT: vehicle.steerLeft( true ); break;
					case KeyEvent.VK_RIGHT: vehicle.steerRight( true ); break;
				}
			}

			public void keyReleased( KeyEvent ke ) {
				switch( ke.getKeyCode() ) {
					case KeyEvent.VK_UP: vehicle.setMotorOn( false ); break;
					case KeyEvent.VK_DOWN: vehicle.reverse( false ); break;
					case KeyEvent.VK_LEFT: vehicle.steerLeft( false ); break;
					case KeyEvent.VK_RIGHT: vehicle.steerRight( false ); break;
					
					case KeyEvent.VK_G: 
						green = !green;
						if ( green ) {
							setBackground( new Color( 0, 128, 0 ) );	
							setForeground( new Color( 64, 192, 64 ) );	
						}
						else {
							setBackground( Color.white );	
							setForeground( Color.black );	
						}
						
						break;
					case KeyEvent.VK_F: 
						showfps = !showfps;		
						break;
					case KeyEvent.VK_L: 
						limit = !limit;
						showLimitFor = 50;
						break;
				}
			}

		};

		MouseAdapter mouse = new MouseAdapter() {
			public void mousePressed( MouseEvent me ) {
				requestFocus();
			}
			
		};

		addMouseListener( mouse );
		addKeyListener( keys );

		requestFocus();


		for ( int i = 0; i < colors.length; i++ ) {
			colors[ i ] = new Color( 255 - 255*(i%2), 127*(i%3), 255*(i%2) );
		}
		
	}

	public void init() {
		limit = true;
		try {
			java.net.URL trackURL = new java.net.URL( getCodeBase(), "donington.track" );
			java.io.Reader in = new java.io.InputStreamReader( trackURL.openConnection().getInputStream() );

			track.load( in );
			//System.out.println( "section " + track.sections );
			in.close();

		}
		catch( java.io.IOException e ) {
			System.err.println( "error loading file" );
			System.err.println( e );
		}

		vehicle = new Vehicle( 224, 128 );

		Vehicle[] vehicles = { vehicle, new Vehicle( 234, 135 ), new Vehicle( 244, 120 ) };
		colors = new Color[ vehicles.length ];
		race = new Race( track, vehicles );

		initControl();
	}

	private volatile boolean running = true;
	private volatile Thread thread = null;
	private volatile boolean threadFinished = true;
	
	public void start() {
		running = true;
		thread = new Thread( this );
		thread.setPriority( Thread.MIN_PRIORITY );
		thread.start();
		
		if ( module != null )
			module.start();
			
		requestFocus();
	}

	private Graphics offScrGr = null;
	private Image offScrImage = null;
	private Dimension size = null;
	private Point clipPoint = null;
	private Rectangle clipRectangle = null;
	
	private void scrollTo( int x, int y ) {
		if ( x < 0 ) x = 0;
		if ( y < 0 ) y = 0;
		
		int dx = clipPoint.x - x;
		int dy = clipPoint.y - y;
		
		if ( dx != 0 || dy != 0 ) {
			clipPoint.x = x;
			clipPoint.y = y;
			clipRectangle.x = clipPoint.x;
			clipRectangle.y = clipPoint.y;
		}
		offScrGr.translate( dx, dy );
	}
	
	
	public void run() {

		requestFocus();
		
		threadFinished = false;
		
		if ( offScrGr == null ) {
			size = getSize();
			offScrImage = createImage(size.width, size.height );
			offScrGr = offScrImage.getGraphics();
			clipPoint = new Point();
			clipRectangle = new Rectangle( 0, 0, size.width, size.height );
		}

		

		long start = System.currentTimeMillis();

		long originalstart = start;
		int frames = 0;
		double fps = 0;

		float dt = 0.05f;
		int sleepTime = 10;
		
		Graphics g = getGraphics();

		
		while( running && Thread.currentThread() == thread ) { // and this thread

			race.integrate( dt );
			Thread.yield();
			
			Body.State state = vehicle.currentState();
			//System.out.println( state.x, state.y );
			
			scrollTo( (int)(state.x -0.5*size.width), (int)(state.y - 0.5*size.height) );
			
			offScrGr.setColor( getBackground() );
			offScrGr.fillRect( clipPoint.x, clipPoint.y, size.width, size.height );
			paintTrack( offScrGr);
			offScrGr.setColor( getForeground() );
			
			if ( showfps )
			offScrGr.drawString( "fps "+fps, 
								clipPoint.x + 10,
								clipPoint.y + 40 );
			
			if ( showLimitFor > 0 ) {
				showLimitFor--;
				offScrGr.drawString( "limit " + (limit?"on" : "off"),
									 clipPoint.x + 10,
									 clipPoint.y + 60 );
			}
			
									 
			/*offScrGr.drawString( ""+dt, 
								clipPoint.x + size.width-70,
								clipPoint.y + 60 );	*/
			Thread.yield();
			
			if ( running ) // stop drawing if !running, causes errors on exit otherwise
				g.drawImage( offScrImage, 0, 0, this );
	
			/*try {
				Thread.sleep( 10 );
			}
			catch( Exception e ) {
			};*/
			
			long end = System.currentTimeMillis();

			frames++;

			//if ( frames > 3 )
			

			if ( end - originalstart > 100 ) { // 0.1 sec
				// average it out, so as to avoid sudden speed ups
				float new_dt = (0.0035f*(end - originalstart))/frames;
					
				// avoid sudden jumps in timestep size
				if ( (new_dt - dt) > 0.03f )
					new_dt = dt + 0.03f;
				dt = new_dt;
				
				fps = (1000.0*frames)/((end - originalstart));
				//long time = (end - originalstart)/frames;
				//sleepTime = (int)(15 - time);
				// don't want integration steps to
				// get too big, otherwise we'll
				// get outside the track
				if ( dt > 0.2f ) dt = 0.2f;
				frames = 0;
				originalstart = end;
			}

			long time = (end-start);

			Thread.yield();

			//int sleep = (int)( sleepTime - time );
			if ( limit && sleepTime > 0 ) {
				// limit the speed
				try {
					Thread.sleep( sleepTime );
				}
				catch( Exception e ){}
			}

			//start = System.currentTimeMillis();
		}
		
		threadFinished = true;

	}

	public void stop() {
		running = false;
		thread = null;
		if ( module != null )
			module.stop();
	}

	private int convert( int pixel ) {
		return 0x99FFFFFF & pixel;
	}
	
	private void blob( int pixel, float x, float y ) {
		int width = track.getWidth();
		int height = track.getHeight();
		int ix = (int)(TRACK_IMAGE_SIZE*x/width);
		int iy = (int)(TRACK_IMAGE_SIZE*y/height);
		
		pixel = convert( pixel );
		int rad = 2;
		for ( int i = Math.max( ix-rad, 0 ); i < Math.min( ix+rad, TRACK_IMAGE_SIZE); i++ ) {
			for ( int j = Math.max( iy-rad, 0 ); j < Math.min( iy+rad, TRACK_IMAGE_SIZE ); j++ ) {
				int index = TRACK_IMAGE_SIZE*j + i;
				trackImagePixels[ index ] = pixel;
			}
		}
		
	}
	
	
	private void plot( int pixel, float x, float y ) {
		int width = track.getWidth();
		int height = track.getHeight();
		int ix = (int)(TRACK_IMAGE_SIZE*x/width);
		int iy = (int)(TRACK_IMAGE_SIZE*y/height);
		if (   ix >= 0 && ix < TRACK_IMAGE_SIZE
			&& iy >= 0 && iy < TRACK_IMAGE_SIZE ) {
			int index = TRACK_IMAGE_SIZE*iy + ix;
			trackImagePixels[ index ] = convert( pixel );
		}
	
	}
	
	
	private String finalPosition = null;
	
	private long lastTrackPaint = 0;
	
	public void paintTrackImage( Graphics g ) {
		Track track = race.getTrack();
		int width = track.getWidth();
		int height = track.getHeight();

		if ( trackImage == null ) {
			
			trackImagePixels = new int[ TRACK_IMAGE_SIZE*TRACK_IMAGE_SIZE ];
			trackPixels = new int[ TRACK_IMAGE_SIZE*TRACK_IMAGE_SIZE ];
		
			for ( int i = 0; i < trackImagePixels.length; i++ ) trackImagePixels[ i ] = 0x00000000;
			java.awt.image.MemoryImageSource source = new java.awt.image.MemoryImageSource( TRACK_IMAGE_SIZE, TRACK_IMAGE_SIZE, trackImagePixels, 0, TRACK_IMAGE_SIZE );
			source.setAnimated( true );
			
			for ( int i = 0; i < track.sections.length; i++ ) {
				Track.Section section = track.sections[ i ];
				Rectangle bounds = section.getBounds();
				int pix = i == 0 ? 0xFF666666 : 0xFF999999;
				for ( int x = bounds.x; x < bounds.x + bounds.width; x++ ) {
					for ( int y = bounds.y; y < bounds.y + bounds.height; y++ ) {
						if ( section.type() == Track.Section.CORNER ) {
							CornerSection corner = (CornerSection)section;
							int cx = corner.cornerX();
							int cy = corner.cornerY();
							int dx = x - cx;
							int dy = y - cy;
							int distSq = dx*dx + dy*dy;
							if ( distSq <= bounds.width*bounds.width )
								plot( pix, x, y );
						}
						else
							plot( pix, x, y );
					}
				}
								
			}
			
			trackImage = createImage( source );
			System.arraycopy( trackImagePixels, 0, trackPixels, 0, trackPixels.length );
		}
		
		long time = System.currentTimeMillis();
		
		if ( time - lastTrackPaint > 200 ) {
			lastTrackPaint = time;
			
			System.arraycopy( trackPixels, 0, trackImagePixels, 0, trackPixels.length );
		
			Vehicle[] vehicles = race.getVehicles();
		
			for ( int i = 0; i < vehicles.length; i++ ) {

				Color color = colors[ i  ];
				int pixel = color.getRGB();
				Vehicle vehicle = vehicles[ i ];
				Vehicle.State state = vehicle.currentState();
				
				blob( pixel, state.x, state.y );
			}
		
			trackImage.flush();
		}
		
		int inset = 5;
		
		g.drawImage( trackImage, clipPoint.x + size.width - TRACK_IMAGE_SIZE - inset,
								 clipPoint.y + size.height - TRACK_IMAGE_SIZE - inset,
								 this );
		
		int[] trackCounts = race.getTrackCounts();
		int trackCount = trackCounts[ vehicle.getID() ];

		int textX = clipPoint.x + size.width - TRACK_IMAGE_SIZE - 70,
			textY = clipPoint.y + size.height - TRACK_IMAGE_SIZE - inset + 5;
			
		
		g.drawString( "lap " + trackCount, 
					 textX, textY );

		int[] ranks = race.getRanks();
		int rank = ranks[ vehicle.getID() ];

		String rankString = ""+rank;

		switch( rank ) {
			case 1: rankString += "st"; break;
			case 2: rankString += "nd"; break;
			case 3: rankString += "rd"; break;
			default:
				rankString += "th";
		}

		g.drawString( rankString, 
					 textX, textY + 20 );

		/*g.drawString( "" + (int)vehicle.currentState().speed(), 
					 textX, 
					 textY + 40 );*/
		
		g.drawString( "col: ", 
					 textX, 
					 textY + 40 );

		
				   
		g.setColor( colors[ vehicle.getID() ] );

		g.fillRect( textX + 40, 
				    textY + 30, 
				   10, 10 );
				   
		g.setColor( getForeground() );
		g.drawRect( textX + 40, 
					textY + 30, 
				   10, 10 );
		
		int numLaps = race.getNumLaps();
		
		if ( numLaps <= trackCount || finalPosition != null ) {
			if ( finalPosition == null )
				finalPosition = rankString;
			g.setColor( getForeground() );
			g.setFont( bigfont );
			g.drawString( "You came " + finalPosition, 
					 	  clipPoint.x + size.width/2 - 50, clipPoint.y + size.height/2 );
					 	  
			g.setFont( font );
		}
		
		if ( race.getCurrentLight() != 4 ) {
			int light = race.getCurrentLight();
			
			switch( light ) {
				case 1: g.setColor( Color.red ); break;
				case 2: g.setColor( Color.yellow ); break;
				case 3: g.setColor( Color.green ); break;
				default:
					g.setColor( Color.red );
			}
			
			g.fillOval( clipPoint.x + size.width/2 - 10, clipPoint.y + size.height/2 -10, 20, 20 );
			
		}
		
	}
	
	
	private Polygon poly = new Polygon( new int[ 4 ], new int[ 4 ], 4 );

	public void paintTrack( Graphics g ) {

		g.setColor( getForeground() );

		Track track = race.getTrack();

		for ( int i = 0; i < track.sections.length; i++ ) {
			Track.Section section = track.sections[ i ];
			Rectangle bounds = section.getBounds();
			
			if ( !clipRectangle.intersects( bounds ) )
				continue;
				
			g.setClip( bounds.x, bounds.y, bounds.width, bounds.height );
			
			if ( i == 0 ) {
				
				// draw direction arrow
				
				Track.Section next = section.getNextSection();
				Rectangle nextBounds = next.getBounds();
								
				double x1 = bounds.x + bounds.width*0.5;
				double y1 = bounds.y + bounds.height*0.5;
				
				double x2 = nextBounds.x + nextBounds.width*0.5;
				double y2 = nextBounds.y + nextBounds.height*0.5;
				
				double dx = x2 - x1;
				double dy = y2 - y1;
				
				double dist = Math.sqrt( dx*dx + dy*dy );
				
				dx /= dist;
				dy /= dist;
				
				g.drawLine( (int)( x1 + dy*bounds.width*0.25 ), (int)( y1 - dx*bounds.width*0.25 ),
							(int)( x1 + dx*bounds.width*0.25 ), (int)( y1 + dy*bounds.width*0.25 ) );

				g.drawLine( (int)( x1 - dy*bounds.width*0.25 ), (int)( y1 + dx*bounds.width*0.25 ),
							(int)( x1 + dx*bounds.width*0.25 ), (int)( y1 + dy*bounds.width*0.25 ) );
							
				g.drawLine( (int)( x1 - dy*bounds.width*0.25 ), (int)( y1 + dx*bounds.width*0.25 ),
							(int)( x1 + dy*bounds.width*0.25 ), (int)( y1 - dx*bounds.width*0.25 ) );
				
			}
			
			if ( section.type() == Track.Section.STRAIGHT ) {
				StraightSection straight = (StraightSection)section;
				if ( straight.isVertical() ) {
					g.drawLine( bounds.x, bounds.y, bounds.x, bounds.y + bounds.height );
					g.drawLine( bounds.x + bounds.width-1, bounds.y, bounds.x + bounds.width-1, bounds.y + bounds.height );
				}
				else {
					g.drawLine( bounds.x, bounds.y, bounds.x + bounds.width, bounds.y );
					g.drawLine( bounds.x, bounds.y + bounds.height-1, bounds.x + bounds.width, bounds.y + bounds.height-1 );
				}
				
			}
			else {
				CornerSection corner = (CornerSection)section;
				g.drawOval( corner.cornerX() - bounds.width,
						   corner.cornerY() - bounds.height,
						   2*bounds.width-1, 2*bounds.height-1 );
			}

			if ( i == 0 ) {
				Track.Section prev = section.getPreviousSection();
				Rectangle prevBounds = prev.getBounds();
				g.drawRect( prevBounds.x-1, prevBounds.y-1, prevBounds.width+2, prevBounds.height+2 );
			}
		}

		g.setClip( clipPoint.x, clipPoint.y, getSize().width, getSize().height );

		Vehicle[] vehicles = race.getVehicles();

		for ( int j = 0; j < vehicles.length; j++ ) {

			g.setColor( colors[ j ] );
			Vehicle vehicle = vehicles[ j ];
			Vehicle.State state = vehicle.currentState();
			float angle = state.angle;

			for ( int i = 0; i < state.numpoints; i++ ) {
				float x1 = state.xpoints[ i ];
				//float x2 = state.xpoints[ (i +1)%state.numpoints ];
				float y1 = state.ypoints[ i ];
				//float y2 = state.ypoints[ (i +1)%state.numpoints ];
				//g.drawLine( (int)x1, (int)y1, (int)x2, (int)y2 );
				poly.xpoints[ i ] = (int)x1;
				poly.ypoints[ i ] = (int)y1;
			}

			g.fillPolygon( poly );

			g.setColor( getForeground() );

			g.drawPolygon( poly );

			float steering_angle = vehicle.steering_angle;

			double dx = Math.sin( angle + steering_angle );
			double dy = Math.cos( angle + steering_angle );

			int len = 4;
			g.drawLine( (int)(state.x + vehicle.wx1), (int)(state.y + vehicle.wy1),
						(int)(state.x + vehicle.wx1 - dx*len), (int)(state.y + vehicle.wy1 - dy*len) );

			g.drawLine( (int)(state.x + vehicle.wx2), (int)(state.y + vehicle.wy2),
						(int)(state.x + vehicle.wx2 - dx*len), (int)(state.y + vehicle.wy2 - dy*len) );

			//g.drawString( ""+vehicle.getID(), (int)state.x, (int)state.y );

		}
		
		paintTrackImage( g );

	}

	public void update( Graphics g ) {
		paint( g );
	}

	
	public void paint( Graphics g ) {
		if ( race == null || offScrGr == null )
			return;

	}


	private static Dialog about = null;
	
	public static void showAbout( Frame parent ) {
	
		//System.out.println( "show") ;
		about = new Dialog( parent, "About" );
	
		about.setLayout( new GridLayout( 6, 1 ) );
	
		about.add( new Label( "RacingGame " ) );
		about.add( new Label( "This is our project for network programming." ) );
		about.add( new Label( "Our team:" ) );
		about.add( new Label( "Đào Duy Nhẫn - 20111939 " ) );
		about.add( new Label( "Lương Thị Thắm  - " ) );
		
		Button ok = new Button( "Ok" );
		ok.addActionListener( new ActionListener() {
								public void actionPerformed( ActionEvent ae ) {
									about.dispose();
								}
							  } );
							  
		about.add( ok );
		about.setModal( true );
		
		about.pack();
		about.show();
	}

	public static void main( String[] args ) {

		/*if ( args.length == 0 ) {
			VehicleTest test = new VehicleTest();
			return;
		}*/
		final Frame frame = new Frame();
		final Dialog dialog = new Dialog( frame, "RacingGame" );
		dialog.setLayout( new GridLayout( 4, 1 ) );

		Button host = new Button( "Host" );
		host.setActionCommand( "host" );
		dialog.add( host );

		Button join = new Button( "Join" );
		join.setActionCommand( "join" );
		TextField hostAddress = new TextField( "host address" );
		Panel panel = new Panel( new FlowLayout() );
		panel.add( join );
		panel.add( hostAddress );
		dialog.add( panel );

		Button about = new Button( "About" );
		about.setActionCommand( "about" );
		dialog.add( about );
		
		Button cancel = new Button( "Cancel" );
		cancel.setActionCommand( "cancel" );
		dialog.add( cancel );

		final StringBuffer dialogResult = new StringBuffer();

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed( ActionEvent ae ) {
				String command = ae.getActionCommand();
				if ( command.equals( "host" ) ) {
					dialogResult.append( "host" );
					dialog.dispose();
				}
				else if ( command.equals( "join" ) ) {
					dialogResult.append( "join" );
					dialog.dispose();
				}
				else if ( command.equals( "about" ) ) {
					showAbout( frame );
				}
				else if ( command.equals( "cancel" ) ) {
					System.exit( 0 );
				}
			}
		};

		host.addActionListener( actionListener );
		join.addActionListener( actionListener );
		about.addActionListener( actionListener );
		cancel.addActionListener( actionListener );

		dialog.setModal( true );
		dialog.pack();
		dialog.show();

		NetworkModule module = null;
		if ( dialogResult.toString().equals( "host" ) ) {

			module = new NetworkModule( 1234 );

			String result = module.listenAsServer();
			System.out.println( result );

		}
		else {

			module = new NetworkModule( 1235 );

			String result = module.connectToServer( hostAddress.getText(), 1234 );
			System.out.println( result );
		}

		NetworkModule.State[] states = module.getVehicleStates();
		byte id = (byte)module.getAssignedID();

		Vehicle[] vehicles = new Vehicle[ states.length ];

		for ( int i = 0; i < vehicles.length; i++ ) {
			vehicles[ i ] = new Vehicle( (byte)i, 224 + i*10, 128 );
		}

		final RacingGame vehicleTest = new RacingGame( id, vehicles, module );
		vehicleTest.setSize( 300, 300 );

		WindowAdapter close = new WindowAdapter() {

			public void windowClosing( WindowEvent we ) {
				Object source = we.getSource();
				vehicleTest.stop();
				((Frame)source).dispose();
			}

			public void windowClosed( WindowEvent we ) {
				System.exit( 0 );
			}

		};

		frame.setTitle( module.getTrackName() );
		frame.addWindowListener( close );
		frame.add( vehicleTest );

		vehicleTest.frame = frame;
		frame.setSize( 350, 350 );
		//frame.show();

		

		/*final NetworkModule net = module;

		Thread receiveThread = new Thread() {
			public void run() {
				while( vehicleTest.running ) {
					net.receiveState();
					
				}
			}
		};

		receiveThread.setPriority( Thread.MIN_PRIORITY );
		receiveThread.start();*/
		
		if ( dialogResult.toString().equals( "host" ) ) {
		
			final Runnable reseter = new Runnable() {
				public void run() {
					synchronized( vehicleTest ) {
						vehicleTest.reset();
						System.out.println( "reset done" );
						vehicleTest.start();
					}
				}
			};
		
			ActionListener resetListener = new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					String command = ae.getActionCommand();
					if ( command.equals( "reset" ) ) {
						(new Thread( reseter )).start();
					}
					else if ( command.equals( "next" ) ) {
						vehicleTest.module.nextTrack();
						
						System.out.println( "next button" );
						(new Thread( reseter )).start();
					}
				}
			};
			panel = new Panel();
			Panel sidePanel = new Panel( new GridLayout( 2, 1 ) );
			Button reset = new Button( "reset" );
			reset.setActionCommand( "reset" );
			reset.addActionListener( resetListener );
			sidePanel.add( reset );
			
			Button next = new Button( "next" );
			next.setActionCommand( "next" );
			next.addActionListener( resetListener );
			sidePanel.add( next );
			panel.add( sidePanel );
			frame.add( panel, BorderLayout.SOUTH );
		
			frame.show();
			
			frame.setSize( 350, 350 + sidePanel.getPreferredSize().height );
		
			/*long time = System.currentTimeMillis();
			while( System.currentTimeMillis() - time < 10000 ) {
				try{
					Thread.sleep( 1000 );
				}
				catch( Exception e ){}
			}

			System.out.println( "timed reset" );
			vehicleTest.reset();
			System.out.println( "reset done" );
			vehicleTest.start();*/
		}
		
		//frame.setSize( 350, 350 );
		frame.show();
		
		vehicleTest.start();
	}

}
