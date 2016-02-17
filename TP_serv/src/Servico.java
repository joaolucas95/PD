import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Servico extends java.rmi.server.UnicastRemoteObject implements RemoteServiceInterface{

	private static final long serialVersionUID = 1L;
	static ArrayList<InetAddress> addresses;
	static ArrayList<Integer> ports;
	static ArrayList<String> registries;
	static MulticastSocket multicastSocket;
	static DatagramSocket dgramSocket;
	static RemoteServerInterface servidor;
	static String registry;

	protected Servico() throws RemoteException {}
	
	public String getServidores() throws RemoteException {
		
		String s = "";
		for(int i=0; i<addresses.size(); i++){
			s += "-> SERV " + i + " ";
			if(i==0) s += "(primario): ";
			else s+= "(secundario): ";
			s += addresses.get(i) + ":" + ports.get(i) + '\n';
			String registration = "rmi://" + registries.get(i) + "/Server";
			Remote remoteService;
			try {
				remoteService = Naming.lookup ( registration );
				servidor = (RemoteServerInterface) remoteService;
				s += servidor.getListaFich();
			} catch (MalformedURLException | NotBoundException e) {
				e.printStackTrace();
			}
			s += '\n';
		}
		return s;
	}
	
	public static void main(String[] args) {
		DatagramPacket pkt;
		int pos = 0;
		Servico serv;

		try {
			
			serv = new Servico();
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			
			registry = "localhost";
			if (args.length >= 1){ registry = args[0]; }

			String registration = "rmi://" + registry + "/Service";
			Naming.rebind( registration, serv);
			
		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		
		try {
			File file = new File("clientes.txt");
			if(!file.exists()){
    			file.createNewFile();
             }
			else{
				PrintWriter writer = new PrintWriter(file);
				writer.print("");
				writer.close();
			}
			addresses = new ArrayList<>();
			ports = new ArrayList<>();
			registries = new ArrayList<>();
			multicastSocket = new MulticastSocket(7000);
			multicastSocket.joinGroup(InetAddress.getByName("225.15.15.15"));
			dgramSocket = new DatagramSocket(5020);
			Thread t = new ThreadServico(addresses, ports, registries, multicastSocket);
			t.setDaemon(true); t.start();
			while(true){
				pkt = new DatagramPacket(new byte[3000], 3000);
				dgramSocket.receive(pkt);
				String user= new String(pkt.getData(),0,pkt.getLength());
				System.out.println("<SVC> Recebido pedido de " + user + pkt.getAddress() + " " + pkt.getPort());
				if(pos >= addresses.size())
					pos=0;
				if(addresses.size() == 0) continue;
				String str = addresses.get(pos) + " " + ports.get(pos);
				pkt = new DatagramPacket(str.getBytes(), str.length(), pkt.getAddress(), pkt.getPort());
				dgramSocket.send(pkt);

				System.out.println("<SVC> Enviado coordenadas do " + pos + ".o servidor em nservers = " + addresses.size());
				pos++;
				FileWriter fileWriter = new FileWriter(file.getName(), true);
				BufferedWriter buf = new BufferedWriter(fileWriter);
				String[] splited = user.split(" ");
				buf.write(splited[0] + ":" + splited[1]);
				buf.newLine();
				buf.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
