package pck;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Transmisor extends JFrame implements ActionListener {
	private static final long serialVersionUID = -6056260699202978657L;

	private final JTextField uriField;
	private final JButton connect;
	private final JButton close;
	private final JTextArea taConexiones;
	private final JTextArea taMensajes;
	
	private final JTextField chatField;
	
	private WebSocketClient cc;

	public Transmisor( String defaultlocation ) {
		super( "WebSocket Client" );
		Container c = getContentPane();
		GridLayout layout = new GridLayout();
		layout.setColumns( 1 );
		layout.setRows( 6 );
		c.setLayout( layout );


		uriField = new JTextField();
		uriField.setText( defaultlocation );
		c.add( uriField );

		connect = new JButton( "Connect" );
		connect.addActionListener( this );
		c.add( connect );

		close = new JButton( "Close" );
		close.addActionListener( this );
		close.setEnabled( false );
		c.add( close );

		JScrollPane scroll = new JScrollPane();
		taConexiones = new JTextArea();
		//scroll.setSize(new Dimension(800, 1000));
		scroll.setViewportView( taConexiones );
		c.add( scroll );

		JScrollPane scroll1 = new JScrollPane();
		taMensajes = new JTextArea();
		scroll1.setViewportView( taMensajes );
		c.add( scroll1);

		
		
		chatField = new JTextField();
		chatField.setText( "" );
		chatField.addActionListener( this );
		c.add( chatField );

		java.awt.Dimension d = new java.awt.Dimension( 300, 400 );
		setPreferredSize( d );
		setSize( d );

		addWindowListener( new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing( WindowEvent e ) {
				if( cc != null ) {
					
					cc.close();
				}
				dispose();
			}
		} );

		setLocationRelativeTo( null );
		setVisible( true );
	}

	public void actionPerformed( ActionEvent e ) {

		if( e.getSource() == chatField ) {  //very important
			if( cc != null ) {
				
				JSONObject obj=new JSONObject();
				obj.put("tipo", "SEND_MESSAGES");
				obj.put("info", chatField.getText());
				
				String jsonText = JSONValue.toJSONString(obj);
				
				cc.send(jsonText);
				
				chatField.setText( "" );
				chatField.requestFocus();
				
				JOptionPane.showMessageDialog(null, "MENSAJE ENVIADO");
			}

		} else if( e.getSource() == connect ) {
			try {
				
				
				
				cc = new WebSocketClient( new URI( uriField.getText() ), new Draft_17() ) {

					@Override
					public void onMessage( String message ) {
						
						JSONObject obj=(JSONObject) JSONValue.parse(message);
						if(((String)obj.get("tipo")).equals("SPEC")){
							
							taConexiones.append( "got: " + obj.get("info") + "\n" );
							taConexiones.setCaretPosition( taConexiones.getDocument().getLength() );
							
							JSONObject obj11=new JSONObject();
							obj11.put("tipo", "GET_ALL_CONECTIONS");//REFRESCAMOS LISTA DE CONECCIONES
							String jsonText = JSONValue.toJSONString(obj11);
							cc.send(jsonText);
						}
						
						if(((String)obj.get("tipo")).equals("NUM_SOCKETS")){
							
							JSONArray array=(JSONArray)obj.get("info");
							taMensajes.setText("");
							for(int i=0;i<array.size();i++){
								
								taMensajes.append( array.get(i) + "\n" );
								taMensajes.setCaretPosition( taMensajes.getDocument().getLength() );
							}
							
						}
						if(((String)obj.get("tipo")).equals("NOAUTORIZADOTRANSMISOR")){
							
								taMensajes.setText("NO AUTORIZADO");
							    chatField.setEnabled(false);
						}
						
						
					}

					@Override
					public void onOpen( ServerHandshake handshake ) {
						taConexiones.append( "You are connected to Server: " + getURI() + "\n" );
						taConexiones.setCaretPosition( taConexiones.getDocument().getLength() );
						
						JSONObject obj=new JSONObject();
						obj.put("tipo", "GET_ALL_CONECTIONS");
						String jsonText = JSONValue.toJSONString(obj);
						cc.send(jsonText);
						
					}

					@Override
					public void onClose( int code, String reason, boolean remote ) {
						taConexiones.append( "You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n" );
						taConexiones.setCaretPosition( taConexiones.getDocument().getLength() );
						connect.setEnabled( true );
						uriField.setEditable( true );
						close.setEnabled( false );
						chatField.setEnabled(false);
					}

					@Override
					public void onError( Exception ex ) {
						taConexiones.append( "Exception occured ...\n" + ex + "\n" );
						taConexiones.setCaretPosition( taConexiones.getDocument().getLength() );
						ex.printStackTrace();
						connect.setEnabled( true );
						uriField.setEditable( true );
						close.setEnabled( false );
						chatField.setEnabled(false);
					}
				};
				
				chatField.setEnabled(true);
				close.setEnabled( true );
				connect.setEnabled( false );
				uriField.setEditable( false );
				cc.connect();
			} catch ( URISyntaxException ex ) {
				taConexiones.append( uriField.getText() + " is not a valid WebSocket URI\n" );
			}
		} else if( e.getSource() == close ) {
			taMensajes.setText("");
			cc.close();
		}
	}

	public static void main( String[] args ) {
		String IP="localhost";
		String PORT="8887";
		if( args.length != 0 ) {
			IP = args[ 0 ];
			PORT = args[ 1 ];
		}
		
		WebSocketImpl.DEBUG = true;
		String location="ws://"+IP+":"+PORT;
		System.out.println( "Default server url specified: \'" + location + "\'" );
		new Transmisor( location );
	}

}
