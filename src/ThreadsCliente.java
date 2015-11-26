import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class ThreadsCliente extends Thread{
	
	private static final int MAX_SIZE = 4000;
	private static final String VISUALIZE = "VISUALIZE";
	private static final String UPLOAD = "UPLOAD";
	private static final String DOWNLOAD = "DOWNLOAD";
	private static final String DELETE = "DELETE";
	
	protected byte []fileChunck = new byte[MAX_SIZE];
	protected String operation;
	protected Socket socket = null;
	protected File localDirectory;
	protected String fileName;
	
	
	public ThreadsCliente(String operation){
		this.operation = operation;
		this.localDirectory = Cliente.localDirectory;
	}
	public ThreadsCliente(String operation, String fileName){
		this.operation = operation;
		this.localDirectory = Cliente.localDirectory;
		this.fileName = fileName;
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
	
	public void operationDownload() throws IOException{
		int nbytes;
		String localFilePath;
		FileOutputStream localFileOutputStream = null;
		File []listFiles = localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(fileName))
				return;
		localFilePath = localDirectory.getCanonicalPath()+File.separator+fileName;
		localFileOutputStream = new FileOutputStream(localFilePath);
		InputStream in = Cliente.tcpSocket.getInputStream();
		PrintWriter pout = new PrintWriter(Cliente.tcpSocket.getOutputStream());
		pout.println(fileName);
		pout.flush();
		while((nbytes = in.read(fileChunck))>0){
			localFileOutputStream.write(fileChunck, 0, nbytes);
		}
		localFileOutputStream.close();
		System.out.println("Download efetuado!");
	}
	
	public void operationUpload() throws IOException, ClassNotFoundException{
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		String s = "UPLOAD";
		out.writeObject(s);
		out.flush();
		Request request = (Request)in.readObject();
		for(int i=0;i<request.filesNames.length;i++){
			
		}
		
	}
	
	public void operationDelete(){
	}
	
	public void run(){
		try{
			if(operation.equalsIgnoreCase(VISUALIZE))
				operationVisualize();
			if(operation.equalsIgnoreCase(UPLOAD))
				operationUpload();
			if(operation.equalsIgnoreCase(DOWNLOAD))
				operationDownload();
			if(operation.equalsIgnoreCase(DELETE))
				operationDelete();
		}catch(IOException e){
			System.out.println("<THR> Erro ao visualizar");
		}catch(ClassNotFoundException e){
			System.out.println("<THR> Erro ao ler conteudo da diretoria");
		}
	}
}


/*public void update() throws IOException, ClassNotFoundException{
ObjectInputStream in;
Request request;
while(true){
	in = new ObjectInputStream(socket.getInputStream());
	request = (Request)in.readObject();
	if(request == null) continue;
	System.out.println("CONTEUDO DO SISTEMA");
	for(int i=0; i<request.getFilesNames().length;i++)
		System.out.println(request.getFilesNames()[i] + " : " + request.getFilesSizes()[i]);
}
}*/
/*public void operationVisualize() throws IOException, ClassNotFoundException{
	File []listFiles = localDirectory.listFiles();
	for(int i=0;i<listFiles.length;i++)
		System.out.println(listFiles[i].getName() + " || " + listFiles[i].length() + " bytes");
}*/
