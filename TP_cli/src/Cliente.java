import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Cliente {
	
	public static final int MAX_SIZE = 3000;
	static File localDirectory;
	static InetAddress serviceAddr, serverAddr;
	static int servicePort, serverPort;
	static DatagramSocket dgramSocket = null;
	static Socket tcpSocket = null;
	static Scanner sc;
	static RemoteServiceInterface servico;
	
	public static void autenticacao() throws IOException{
		
        sc = new Scanner(System.in);
		System.out.print("Username: ");
		String user = sc.nextLine();
		System.out.print("Password: ");
		String pass = sc.nextLine();
		//sc.close();
		String str = user+" "+pass;
		DatagramPacket pkt = new DatagramPacket(str.getBytes(), str.length(), serviceAddr, servicePort);
		dgramSocket.send(pkt);
		pkt = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
		dgramSocket.receive(pkt);
		str = new String(pkt.getData(), 0, pkt.getLength());
		String[] splited = str.split(" ");
		System.out.println(splited[0]);
		System.out.println(splited[1]);
		System.out.println("AAA");
		serverPort = Integer.parseInt(splited[1]);
		serverAddr = InetAddress.getByName(splited[0].substring(1));

	}
	
	public static void estabeleceLigacaoServidor() throws IOException{
		System.out.println("<CLI> Estabelecida ligacao com servidor " + serverAddr + ":" + serverPort);
		tcpSocket = new Socket(serverAddr, serverPort);
	}
	
	public static void executaOperacoes() throws InterruptedException, IOException{
		Thread t;
		String s = "";
		t = new ThreadsCliente("CLIENT");
		t.setDaemon(true); t.start(); t.join();
		System.out.print(">> ");
		do{
			while(!(s = sc.nextLine()).equalsIgnoreCase("EXIT")){
				if(s.contains("DOWNLOAD")){
					String []splited = s.split(" ");
					if(splited[1] != null){
						t = new ThreadsCliente("DOWNLOAD", splited[1]);
						t.setDaemon(true); t.start(); t.join();
					}
				}
				if(s.contains("UPLOAD")){
					String []splited = s.split(" ");
					if(splited[1] != null){
						t = new ThreadsCliente("UPLOAD", splited[1]);
						t.setDaemon(true); t.start(); t.join();
					}
				}
				if(s.contains("VISUALIZE")){
					String []splited = s.split(" ");
					if(splited[1] != null){
						t = new ThreadsCliente("VISUALIZE", splited[1]);
						t.setDaemon(true); t.start(); t.join();
					}	
				}
				if(s.contains("SEE")){
					t = new ThreadsCliente("SEE_SERV_DIR", null);
					t.setDaemon(true); t.start(); t.join();
				}
				if(s.contains("DELETE")){
					String []splited = s.split(" ");
					if(splited[1] != null){
						t = new ThreadsCliente("DELETE", splited[1]);
						t.setDaemon(true); t.start(); t.join();
					}
				}
				if(s.contains("GET_SERV"))
					System.out.println(servico.getServidores());
				System.out.print(">> ");
			}
			
		}while(!(s.contains("EXIT")));
	}
	
	public static void main(String[] args) {
		
		if(args.length < 3){
			System.out.println("<CLI> Sintaxe: java Cliente dirLocal IP_servico porto_servico");
			return;
		}
		
		String registry = "localhost";
		if (args.length > 3){ registry = args[3]; }
		String registration = "rmi://" + registry + "/Service";
		
		try {
			Remote remoteService = Naming.lookup ( registration );
			servico = (RemoteServiceInterface) remoteService;
		} catch (MalformedURLException | RemoteException | NotBoundException e1) {
			e1.printStackTrace();
		}
		
		localDirectory = new File(args[0].trim());
		if(!localDirectory.exists()){
            System.out.println("<CLI> A diretoria " + localDirectory + " nao existe!");
            return;
        }
        if(!localDirectory.isDirectory()){
            System.out.println("<CLI> O caminho " + localDirectory + " nao se refere a uma diretoria!");
            return;
        }
        if(!localDirectory.canWrite()){
            System.out.println("<CLI> Sem permissoes de escrita na diretoria " + localDirectory);
            return;
        }
        
        try {
			serviceAddr = InetAddress.getByName(args[1]);
			servicePort = Integer.parseInt(args[2]);
		} catch (UnknownHostException e) {
			System.out.println("<CLI> Host desconhecido"); return;
		}
        ///////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////
        try {
			dgramSocket = new DatagramSocket();
			sc = new Scanner(System.in);
			autenticacao();
                         
			estabeleceLigacaoServidor();
			//Thread t = new ThreadsCliente("UPDATE_INFO");
			//t.setDaemon(true); t.start();
			executaOperacoes();
		} catch (SocketException e) {
			System.out.println("<CLI> Socket exception"); return;
		} catch (IOException e) {
			System.out.println("<CLI> Erro na autenticacao/ligacao"); return;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(dgramSocket != null) dgramSocket.close();
		}

	}

}
