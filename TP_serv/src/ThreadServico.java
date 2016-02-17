import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class ThreadServico extends Thread{
	
	public static final int HEARTBEAT_TIME = 5000;
	
	ArrayList<InetAddress> addresses;
	ArrayList<Integer> ports;
	ArrayList<String> registries;
	MulticastSocket multicastSocket;
	
	public ThreadServico(ArrayList<InetAddress> addresses, ArrayList<Integer> ports, ArrayList<String>registries, MulticastSocket multicastSocket){
		this.addresses = addresses;
		this.ports = ports;
		this.multicastSocket = multicastSocket;
		this.registries = registries;
	}
	
	public void run(){
		boolean flag = false;
		while(true){
			try{
				Heartbeat heart;
				DatagramPacket packet;
	        	ObjectInputStream in = null;
	        	packet = new DatagramPacket(new byte[4000], 4000);
	            System.out.println("'A espera de heartbeats");
	        	multicastSocket.receive(packet);  	
	        	       	
	        	in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
	        	heart = (Heartbeat)in.readObject();
	        	flag = false;
	        	for(int i=0; i<ports.size(); i++)
	        		if(ports.get(i)==(heart.getPort())){
	        			System.out.println("Hearbeat de servidor ja' existente!");
	        			System.out.println(ports.size() + "servidores ativos!");
	        			flag = true;
	        			continue;
	        		}
	        	if(flag) continue;
	        	System.out.println("Ouvido heartbeat de um novo servidor!");
	        	System.out.println(ports.size() + "servidores ativos!");
	        	System.out.println("ADDR:" + packet.getAddress());
	        	System.out.println("PORT:" + heart.getPort());

	        	addresses.add(packet.getAddress());
	        	ports.add(heart.getPort());
	        	registries.add(heart.getRegistry());
	        	
			}catch(IOException e){
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        }
	}

}
