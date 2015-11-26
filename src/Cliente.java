import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Cliente {
	
	public static final int MAX_SIZE = 3000;
	static File localDirectory;
	static InetAddress serviceAddr, serverAddr;
	static int servicePort, serverPort;
	static DatagramSocket dgramSocket = null;
	static Socket tcpSocket = null;
	
	
	public static void autenticacao() throws IOException{
		Scanner sc = new Scanner(System.in);
		System.out.print("Username: ");
		String user = sc.nextLine();
		System.out.print("Password: ");
		String pass = sc.nextLine();
		sc.close();
		String str = user+":"+pass;
		DatagramPacket pkt = new DatagramPacket(str.getBytes(), str.length(), serviceAddr, servicePort);
		dgramSocket.send(pkt);
		pkt = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
		dgramSocket.receive(pkt);
		serverAddr = pkt.getAddress();
		serverPort = pkt.getPort();
	}
	
	public static void estabeleceLigacaoServidor() throws IOException{
		System.out.println(serverAddr + " "+ serverPort);
		tcpSocket = new Socket(serverAddr, serverPort);
	}
	
	public static void executaOperacoes(){
		Thread t;
		String s = "";
		Scanner sc = new Scanner(System.in);
		do{
			if(sc.hasNextLine()){
				s = sc.nextLine();
				if(s.contains("VISUALIZE")){
					String []splited = s.split(" ");
						if(splited[1] != null){
						t = new ThreadsCliente("VISUALIZE", splited[1]);
						t.setDaemon(true); t.start();
					}
				}
				if(s.contains("DOWNLOAD")){
					String []splited = s.split(" ");
					if(splited[1] != null){
						t = new ThreadsCliente("DOWNLOAD", splited[1]);
						t.setDaemon(true); t.start();
					}
				}
			}
		}while(!(s.contains("EXIT")));
		sc.close();
	}
	
	public static void main(String[] args) {
		
		if(args.length != 3){
			System.out.println("<CLI> Sintaxe: java Cliente dirLocal IP porto");
			return;
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
			//autenticacao();
			//estabeleceLigacaoServidor();
			executaOperacoes();
		} catch (SocketException e) {
			System.out.println("<CLI> Socket exception"); return;
		} catch (IOException e) {
			System.out.println("<CLI> Erro na autenticacao/ligacao"); return;
		}finally{
			if(dgramSocket != null) dgramSocket.close();
		}

	}

}
