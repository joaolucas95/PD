import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;

public class Servidor extends java.rmi.server.UnicastRemoteObject implements RemoteServerInterface{

	private static final long serialVersionUID = 1L;
	private static final int HEARTBEAT = 2000;
	File localDirectory;;
	private boolean primario;
	ServerSocket serverSocket;
	Socket socket;
	Socket toClientSocket;
	MulticastSocket multiSocket;
	String registry;
	
	ArrayList<Socket> sockets;
	
	int nAccepted = 0;
	int nServers = 0;
	int nClientes = 0;
	PrintWriter pout;
	BufferedInputStream bisGetDir;
	DataInputStream disGetDir;
	BufferedOutputStream bosGetDir;
	DataOutputStream dosGetDir;
	
	
	public Servidor(File localDirectory, int port, String registry) throws IOException{
		this.localDirectory = localDirectory;
		this.serverSocket = new ServerSocket(port);
		this.setPrimario(false);
		sockets = new ArrayList<>();
		this.registry = registry;
	}
	
	public String getListaFich() throws RemoteException {
		File []listFiles = this.localDirectory.listFiles();
		String files = "";
		files = "Numero Clientes: " + this.nClientes + '\n';
		for(int i=0;i<listFiles.length;i++)
			files += listFiles[i].getName() + " " + "(" + listFiles[i].length()+ " bytes)" +'\n';
		return files;
	}
	
	public void escutaHeartbeats() throws IOException, ClassNotFoundException{
		System.out.println("ESCUTANDO HEARTBEATS:");
		try{
			Heartbeat heart;
			ObjectInputStream in;
			DatagramPacket packet = new DatagramPacket(new byte[4000],4000);
			multiSocket = new MulticastSocket(7000);
			multiSocket.joinGroup(InetAddress.getByName("225.15.15.15"));
			multiSocket.setSoTimeout(HEARTBEAT*3);
			multiSocket.receive(packet);
			in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
			heart = (Heartbeat)in.readObject();
			if(heart.getPrimario()){
				System.out.println("Recebido heartbeat primario");
				System.out.println("ASSUME-SE COMO SERV SECUNDARIO");
				this.setPrimario(false);
				System.out.println("ESTABELECE LIGACAO COM PRIMARIO do packet " + heart.getPort());
				socket = new Socket(packet.getAddress(), heart.getPort());
				pout = new PrintWriter(socket.getOutputStream());
				System.out.println("COPIA DIRETORIA DO PRIMARIO");
				pout.println("GET_DIR");pout.flush();
	
				/*File []listFiles = this.localDirectory.listFiles();
				for(int i=0;i<listFiles.length;i++)
					listFiles[i].delete();
				*/
				//Thread getDir = new ThreadGetDir(this); getDir.setDaemon(true); getDir.start();
				System.out.println("INICIA ENVIO DE HEARTBEATS");
				Thread t = new ThreadEnviaHeart(this);
				t.setDaemon(true); t.start();
			}
		}catch(SocketTimeoutException e){
			System.out.println("Nao foi recebido nenhum heartbeat");
			System.out.println("Vou comecar a enviar heartbeats");
			this.setPrimario(true);
			Thread t = new ThreadEnviaHeart(this);
			t.setDaemon(true); t.start();
		}finally{
		}
	}
	
	public void processRequests() throws IOException, InterruptedException{	
        if(serverSocket == null) return;
        
        if(!isPrimario())
        	new ThreadOuveServidores(this).start();
        
        while(true){     
        	System.out.println("'A espera de ligacoes: ");
            toClientSocket = serverSocket.accept();

            System.out.println("RECEBIDA LIGACAO DE: " + toClientSocket.getLocalPort());
            new ThreadsServidor(this).start();
        }
    }
	
	public static void main(String[] args) {
		File localDirectory;
		int port;
		Servidor servidor;
		
		if(args.length < 2){
			System.out.println("<SER> Sintaxe: java Servidor localDirectory port <port_registry> <ip_registry>");
			return;
		}
		localDirectory = new File(args[0].trim());
		port = Integer.parseInt(args[1]);
		int registry_port = 1100;
		if (args.length > 2){ registry_port = Integer.parseInt(args[2]); }
		String s = "localhost";
		if (args.length > 3){ s = args[3]; }
		try {
			servidor = new Servidor(localDirectory, port, s);
		
			if(!servidor.localDirectory.exists()){
	            System.out.println("<SER> A diretoria " + localDirectory + " nao existe!");
	            return;
	        }
	        if(!servidor.localDirectory.isDirectory()){
	            System.out.println("<SER> O caminho " + localDirectory + " nao se refere a uma diretoria!");
	            return;
	        }
	        if(!servidor.localDirectory.canWrite()){
	            System.out.println("<SER> Sem permissoes de escrita na diretoria " + localDirectory);
	            return;
	        }

	        try {
				
				LocateRegistry.createRegistry(registry_port);	

				String registration = "rmi://" + servidor.registry + "/Server";
				Naming.rebind( registration, servidor);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
	        
	        servidor.escutaHeartbeats();
	        servidor.processRequests();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public boolean isPrimario() {
		return primario;
	}

	public void setPrimario(boolean primario) {
		this.primario = primario;
	}

}
