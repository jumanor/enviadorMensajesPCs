package pck;

import java.awt.AWTException;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

class Barra{
	
	private  PopupMenu popup = new PopupMenu();
	public String mensajeEstado="";
	public String mensajeIp="";
	
	public Barra(){
		
		MenuItem estadoItem = new MenuItem("Estado");
		estadoItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JOptionPane.showMessageDialog(null, mensajeEstado);
			}
		});
		MenuItem conexionItem = new MenuItem("Acerca de");
		conexionItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				JOptionPane.showMessageDialog(null, "Todos los Derechos Reservados para: Jumanor\n   "
						+ " sugerencias: jumanor@gmail.com");
			}
		});
		popup.add(estadoItem);
		popup.add(conexionItem);
		
		final TrayIcon iconoSystemTray;
        ImageIcon imagenIcono=new ImageIcon(this.getClass().getResource("/pck/cliente.png"));
        
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
}
public class Cliente extends JFrame {

private static Barra barra;	
	
private static boolean estado=false;	
	
private static void showDialog(String mensaje) 
{

    JDialog dialog = new JDialog(null, Dialog.ModalityType.MODELESS);
    dialog.setTitle("URGENTE");
    
    Font font1 = new Font("SansSerif", Font.BOLD, 20);
    JTextField chatField=new JTextField();
    chatField.setHorizontalAlignment(JTextField.CENTER);

    chatField.setFont(font1);
    chatField.setText(mensaje);

 
    dialog.add(chatField);
    
    int x=(Toolkit.getDefaultToolkit().getScreenSize().width-1000)/2;
    
    dialog.setBounds(x, 10, 1000, 200);
    dialog.setVisible(true);
}
public static void conexion(final String location) throws URISyntaxException {
	
WebSocketClient wsc=new WebSocketClient(new URI(location),new Draft_17()) {
		
		@Override
		public void onOpen(ServerHandshake handshakedata) {
			// TODO Auto-generated method stub
		    System.out.println("Conectado al Servidor: "+location);
		    estado=true;
		    if(barra!=null)
		    	barra.mensajeEstado="Conectado al Servidor: "+location;
		}
		
		@Override
		public void onMessage(String message) {
			// TODO Auto-generated method stub
			
			JSONObject obj=(JSONObject) JSONValue.parse(message);
			if(((String)obj.get("tipo")).equals("ALL")){
				
				showDialog(obj.get("info")+"");
				
			}
			
			System.out.println("Se capturo MSN: "+message);
			estado=true;
			
		}
		
		@Override
		public void onError(Exception ex) {
			// TODO Auto-generated method stub
			System.out.println("ERROR!!! "+location);
			estado=false;
			if(barra!=null)
				barra.mensajeEstado="ERROR!!! "+location;
		}
		
		@Override
		public void onClose(int code, String reason, boolean remote) {
			// TODO Auto-generated method stub
			System.out.println("El Servidor se cerro "+location);
			estado=false;
			if(barra!=null)
				barra.mensajeEstado="El Servidor se cerro "+location;
		}
	};
	
    wsc.connect();
	
}
public static void main(String[] args) throws URISyntaxException 
{   String IP="";
	String PORT="";
	
	if( args.length != 0 ) {
		IP = args[ 0 ];
		PORT = args[ 1 ];
	}
	else{
		IP="192.168.1.33";
		PORT="8887";
	}
	
	barra=new Barra();
	
	final String location="ws://"+IP+":"+PORT;
	barra.mensajeIp=location;
	
	int random = (int )(Math.random() * 20 + 1);//1-20 //Por si acaso un cuello de botella
	Timer timer=new Timer();
	timer.schedule(new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(estado==false){
				try {
					
					conexion(location);
					
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
			
		}
	}, 500,1000*(20+random));//Intervalos de 20 segundos reconectamos al servidor
	
}
}
