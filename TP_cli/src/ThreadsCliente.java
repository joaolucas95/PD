import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ThreadsCliente extends Thread{
	
	private static final int MAX_SIZE = 4000;
	private static final String VISUALIZE = "VISUALIZE";
	private static final String UPLOAD = "UPLOAD";
	private static final String DOWNLOAD = "DOWNLOAD";
	private static final String DELETE = "DELETE";
	private static final String UPDATE_INFO = "UPDATE_INFO";
	private static final String SEE_SERV_DIR = "SEE_SERV_DIR";
	private static final String CLIENT = "CLIENT";
	
	protected byte []fileChunck = new byte[MAX_SIZE];
	protected String operation;
	protected Socket socket = null;
	protected File localDirectory;
	protected String fileName;
	
	
	public ThreadsCliente(String operation){
		this.operation = operation;
		this.localDirectory = Cliente.localDirectory;
		this.socket = Cliente.tcpSocket;
	}
	public ThreadsCliente(String operation, String fileName){
		this.operation = operation;
		this.localDirectory = Cliente.localDirectory;
		this.fileName = fileName;
		this.socket = Cliente.tcpSocket;
	}
	
	
	
	
	public void operationVisualize() throws IOException, ClassNotFoundException{
		File []listFiles = localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(fileName)){
				BufferedReader br = new BufferedReader(new FileReader(listFiles[i]));
				String line = null;
				while ((line = br.readLine()) != null) {
				   System.out.println(line);
				}
				br.close();
				return;
			}
		operationDownload();
	}
	
	public void operationSeeDir() throws IOException{
		PrintWriter pout;
		BufferedReader in;
		String request;
		pout = new PrintWriter(socket.getOutputStream(), true);
		pout.println("SEE_SERV_DIR");
		pout.flush();
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while(true){
        	request = in.readLine();
        	System.out.println(request);
        	if(!in.ready()) break;
        }
	}
	
	public void operationDownload() throws IOException{
		File []listFiles = localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(fileName)){
				System.out.println("Ja' existe uma copia na sua diretoria!");
				return;
			}
		FileOutputStream localFileOutputStream = null;
		String localFilePath = null;
		int nbytes;
		PrintWriter pout;
    	DataInputStream in;
    	localFilePath = localDirectory.getCanonicalPath()+File.separator+fileName;
		localFileOutputStream = new FileOutputStream(localFilePath);
		in = new DataInputStream(socket.getInputStream());
		pout = new PrintWriter(socket.getOutputStream(), true);
		pout.println(DOWNLOAD + " " + fileName);
		pout.flush();
		socket.setSoTimeout(5000);
		try{
			while((nbytes = in.read(fileChunck)) > 0){
		//nbytes = in.read(fileChunck);
			System.out.println(nbytes);
            localFileOutputStream.write(fileChunck, 0, nbytes);
            System.out.println("Acrescentados " + nbytes + " bytes ao ficheiro " + localFilePath+ ".");
			}
		}catch(SocketTimeoutException e){}
		System.out.println("Transferencia concluida.");
		if(localFileOutputStream != null)
			localFileOutputStream.close();
	}
	
	public void operationUpload() throws IOException, ClassNotFoundException{
		PrintWriter pout;
		byte []fileChunck = new byte[MAX_SIZE];
		int nbytes;
		DataOutputStream out = new DataOutputStream(Cliente.tcpSocket.getOutputStream());
		String requestedCanonicalFilePath = new File(Cliente.localDirectory+File.separator+fileName).getCanonicalPath();
		if(!requestedCanonicalFilePath.startsWith(Cliente.localDirectory.getCanonicalPath()+File.separator)){
            System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
            System.out.println("A directoria de base nao corresponde a " + Cliente.localDirectory.getCanonicalPath()+"!");
            return;
        }
		pout = new PrintWriter(socket.getOutputStream(), true);
		pout.println(UPLOAD + " " + fileName);
		pout.flush();
		FileInputStream requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
		System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");
        while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
            out.write(fileChunck, 0, nbytes);
            out.flush();                        
        }     
        requestedFileInputStream.close();
        System.out.println("Transferencia concluida");
	}
	
	public void operationDelete() throws IOException{
		PrintWriter pout = new PrintWriter(Cliente.tcpSocket.getOutputStream());
		pout.println(DELETE + " " + fileName);
		pout.flush();
	}
	
	public void notifyServer() throws IOException{
		PrintWriter pout = new PrintWriter(Cliente.tcpSocket.getOutputStream());
		pout.println(CLIENT);
		pout.flush();
	}
	
	public void operationUpdate() throws IOException, ClassNotFoundException{
		while(true){
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Request request = (Request)in.readObject();
			System.out.println("SISTEMA DE FICHEIROS ATUALIZADO:");
			for(int i=0;i<request.filesNames.length;i++){
				System.out.println(request.filesNames[i] + " || " + request.filesSizes[i]);
			}
		}
	}
	
	public void run(){
		try{
			if(operation.equalsIgnoreCase(CLIENT))
				notifyServer();
			if(operation.equalsIgnoreCase(SEE_SERV_DIR))
				operationSeeDir();
			if(operation.equalsIgnoreCase(UPDATE_INFO))
				operationUpdate();
			if(operation.equalsIgnoreCase(VISUALIZE))
				operationVisualize();
			if(operation.equalsIgnoreCase(UPLOAD))
				operationUpload();
			if(operation.equalsIgnoreCase(DOWNLOAD))
				operationDownload();
			if(operation.equalsIgnoreCase(DELETE))
				operationDelete();
			
		}catch(IOException e){
			System.out.println("<THR_CLI> Erro ao visualizar");
		}catch(ClassNotFoundException e){
			System.out.println("<THR_CLI> Erro ao ler conteudo da diretoria");
		}
	}
}
