package pck;

import java.awt.AWTException;
import java.awt.Dialog;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class BarraServer{
	
	private  PopupMenu popup = new PopupMenu();
	
	public List<WebSocket> SOCKETS;
	public List<String> HOSTS;
	
	public BarraServer(){
		
		MenuItem estadoConectados = new MenuItem("Usuarios Conectados");
		estadoConectados.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(SOCKETS!=null)
					showDialogConectados(SOCKETS);
			}
		});
		MenuItem conexionAcercaDe = new MenuItem("Acerca de");
		conexionAcercaDe.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JOptionPane.showMessageDialog(null, "Todos los Derechos Reservados para: Jumanor\n   "
						+ " sugerencias: jumanor@gmail.com");
			}
		});
		MenuItem estadoTransmisores = new MenuItem("Usuarios Trasmisores");
		estadoTransmisores.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				if(HOSTS!=null)
					showDialogTransmisores(HOSTS);

			}
		});
		
		popup.add(estadoTransmisores);
		popup.add(estadoConectados);
		popup.add(conexionAcercaDe);
		
		final TrayIcon iconoSystemTray;
        ImageIcon imagenIcono=new ImageIcon(this.getClass().getResource("/pck/server.png"));
        
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
			
            try {
            	iconoSystemTray=new TrayIcon(imagenIcono.getImage(),"Servicio Mensajero",popup);
                tray.add(iconoSystemTray);
            } catch (AWTException e) {
                System.err.println("No es posible agregar el icono al System Tray");
            }
        } else {
            System.err.println("Tu sistema no soporta el System Tray");
        }
	}
	
	private void showDialogConectados(List<WebSocket> sockets) 
	{

	    JDialog dialog = new JDialog(null, Dialog.ModalityType.MODELESS);
	    dialog.setTitle("USUARIOS CONECTADOS");
	    
	    JScrollPane scroll = new JScrollPane();
	    JTextArea ta = new JTextArea();
		scroll.setViewportView( ta );
		
	    dialog.add(scroll);
	    
	    int x=(Toolkit.getDefaultToolkit().getScreenSize().width-1000)/2;
	    
	    dialog.setBounds(x, 10, 500, 200);
	    dialog.setVisible(true);
	    
	    for(WebSocket tmp:sockets){
	    	
	    	ta.append("IP:"+tmp.getRemoteSocketAddress().getAddress().getHostAddress()+" KEY:"+tmp.toString().split("@")[1]+"\n" );
			ta.setCaretPosition( ta.getDocument().getLength() );
				
	    }
	    
	}
	private void showDialogTransmisores(List<String> usuarios) 
	{

	    JDialog dialog = new JDialog(null, Dialog.ModalityType.MODELESS);
	    dialog.setTitle("USUARIOS TRANSMISORES");
	    
	    JScrollPane scroll = new JScrollPane();
	    JTextArea ta = new JTextArea();
		scroll.setViewportView( ta );
		
	    dialog.add(scroll);
	    
	    int x=(Toolkit.getDefaultToolkit().getScreenSize().width-1000)/2;
	    
	    dialog.setBounds(x, 10, 500, 200);
	    dialog.setVisible(true);
	    
	    for(String tmp:usuarios){
	    	
	    	ta.append("IP:"+tmp+"\n");
			ta.setCaretPosition( ta.getDocument().getLength() );
				
	    }
	    
	}
}

public class Server extends WebSocketServer {

	private static BarraServer barraServer;
	
	private static int PORT;
	private static String HOSTNAME;
	
	private static List<String> HOSTS=new ArrayList<String>();
	
	private static List<WebSocket> HOSTS_SOCKETS=new ArrayList<WebSocket>();

	public Server( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

	public Server( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		
		sendAutorizacion(conn);
		this.sendToTransmisores( "new connection: " + conn.getRemoteSocketAddress().getAddress().getHostAddress() );
		HOSTS_SOCKETS.add(conn);
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
		if(barraServer!=null)
			barraServer.SOCKETS=HOSTS_SOCKETS;
		
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		this.sendToTransmisores( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " has left the room!" );
		System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " has left the room!" );
		
		HOSTS_SOCKETS.remove(conn);
		if(barraServer!=null)
			barraServer.SOCKETS=HOSTS_SOCKETS;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onMessage( WebSocket conn, String message ) {
		
		for(String host:HOSTS){//SOLO HOST CON PERMISOS DE TRANSMISOR
			
			if(conn.getRemoteSocketAddress().getAddress().getHostAddress().equals(host)==true){
				
				JSONObject obj=(JSONObject) JSONValue.parse(message);
				if(((String)obj.get("tipo")).equals("GET_ALL_CONECTIONS")){//EL TRANSMISOR SOLICITA TODAS LAS CONEXIONES
						
					JSONArray list2 = new JSONArray();
					
					for(WebSocket socket:HOSTS_SOCKETS){
						
						list2.add(socket.getRemoteSocketAddress().getAddress().getHostAddress()+" # "+socket.toString().split("@")[1]);
						
					}
					 
					JSONObject tmp=new JSONObject();
					tmp.put("tipo", "NUM_SOCKETS");
					tmp.put("info",list2);
					
					String jsonText = JSONValue.toJSONString(tmp);
					conn.send( jsonText);
					
				}
				if(((String)obj.get("tipo")).equals("SEND_MESSAGES")){//EL TRANSMISOR ENVIA MENSAJES A TODOS LOS CLIENTES
					
					this.sendToAllClientes(obj.get("info")+"");
				}
				
				System.out.println( conn + ": " + message );
				
			
				
				break;
			}
		}

	}

	@Override
	public void onFragment( WebSocket conn, Framedata fragment ) {
		System.out.println( "received fragment: " + fragment );
	}

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = true;
		HOSTNAME="192.168.1.33";
		PORT=8887;
		HOSTS.add("192.168.1.33");
		
		if( args.length != 0 ) {
			
			
				HOSTNAME = args[ 0 ];
				PORT = Integer.parseInt(args[ 1 ]);
				
				if(args.length>2){
					HOSTS=new ArrayList<String>();
					for(int i=2;i<args.length;i++){
						
						HOSTS.add(args[i]);
						System.out.println( "Transmisor en" + args[i]);
					}
				}
		
		}
		barraServer=new BarraServer();
		barraServer.HOSTS=HOSTS;
		
		InetSocketAddress dir=new InetSocketAddress(HOSTNAME, PORT);
		Server s = new Server(dir);
		s.start();
		System.out.println( "Server started " +HOSTNAME+":"+s.getPort() );
		
		
	}
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}
	@SuppressWarnings("unchecked")
	public void sendAutorizacion(WebSocket cliente){
		
		for(String host:HOSTS){
			
			if(cliente.getRemoteSocketAddress().getAddress().getHostAddress().equals(host)==true){
				
				JSONObject obj=new JSONObject();
				obj.put("tipo", "AUTORIZADOTRANSMISOR");
				
				String jsonText = JSONValue.toJSONString(obj);
				cliente.send( jsonText);	
				return;
			}
		}
		
		JSONObject obj=new JSONObject();
		obj.put("tipo", "NOAUTORIZADOTRANSMISOR");
		
		String jsonText = JSONValue.toJSONString(obj);
		cliente.send( jsonText);	

	}
	@SuppressWarnings("unchecked")
	public void sendToAllClientes( String text ) {
		Collection<WebSocket> con = connections();
		
		synchronized ( con ) {
			for( WebSocket c : con ) {
				
				JSONObject obj=new JSONObject();
				obj.put("tipo", "ALL");
				obj.put("info", text);
				
				String jsonText = JSONValue.toJSONString(obj);
				c.send(jsonText);
			}
		}
	}
	@SuppressWarnings("unchecked")
	public void sendToTransmisores( String text ) {
		Collection<WebSocket> con = connections();
		
		synchronized ( con ) {
			for( WebSocket c : con ) {
				
				for(String host:HOSTS){
					
					if(c.getRemoteSocketAddress().getAddress().getHostAddress().equals(host)==true){
						
						JSONObject obj=new JSONObject();
						obj.put("tipo", "SPEC");
						obj.put("info", text);
						
						String jsonText = JSONValue.toJSONString(obj);
						c.send( jsonText);
						
						
					}
				}
				
			}
		}
	}
}
