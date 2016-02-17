import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
public class ThreadEnviaHeart extends Thread{
	
	private Servidor servidor;
	
	public ThreadEnviaHeart(Servidor servidor){
		this.servidor = servidor;
	}
	
	
	@Override
	public void run(){
		try{
			ObjectOutputStream out;
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bOut);
			
			
			//ObjectOutputStream out = new ObjectOutputStream(servidor.multiSocket.getOutputStream());
			if(servidor.isPrimario()){
				Heartbeat heart = new Heartbeat(true);
				System.out.println("ENVIADO HEARTPRIMARIO COM PORTO " + servidor.serverSocket.getLocalPort());
				heart.setPort(servidor.serverSocket.getLocalPort());
				heart.setRegistry(servidor.registry);
				while(true){
					out.writeUnshared(heart);
					out.flush();
					DatagramPacket packet = new DatagramPacket(bOut.toByteArray(), bOut.size());
					packet.setAddress(InetAddress.getByName("225.15.15.15"));
					packet.setPort(7000);
					System.out.println("ENVIADO HEARTBEAT PRIMARIO");
		    		servidor.multiSocket.send(packet);
					sleep(5000);
				}
			}else{
				Heartbeat heart = new Heartbeat(false);
				heart.setPort(servidor.serverSocket.getLocalPort());
				heart.setRegistry(servidor.registry);
				while(true){
					out.writeUnshared(heart);
					out.flush();
					DatagramPacket packet = new DatagramPacket(bOut.toByteArray(), bOut.size());
					packet.setAddress(InetAddress.getByName("225.15.15.15"));
					packet.setPort(7000);
					System.out.println("ENVIADO HEARTBEAT SECUNDARIO");
		    		servidor.multiSocket.send(packet);
					sleep(5000);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
