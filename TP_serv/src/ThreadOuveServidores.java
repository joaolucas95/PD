import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;

public class ThreadOuveServidores extends Thread{
	
	private String request;
	private Servidor servidor;
	BufferedReader in;
	byte []fileChunck = new byte[4000];
	
	public ThreadOuveServidores(Servidor servidor){
		this.servidor = servidor;
	}
	
	public void upload(String request) throws IOException{
		String []splited = request.split(" ");
		FileOutputStream localFileOutputStream = null;
		String localFilePath = null;
		int nbytes;
    	DataInputStream in;
    	localFilePath = servidor.localDirectory.getCanonicalPath()+File.separator+splited[1];
		localFileOutputStream = new FileOutputStream(localFilePath);
		in = new DataInputStream(servidor.socket.getInputStream());
		servidor.socket.setSoTimeout(5000);
		try{
			while((nbytes = in.read(fileChunck)) > 0){
		//nbytes = in.read(fileChunck);
			System.out.println(nbytes);
            localFileOutputStream.write(fileChunck, 0, nbytes);
            System.out.println("Acrescentados " + nbytes + " bytes ao ficheiro " + localFilePath+ ".");
			}
		}catch(SocketTimeoutException e){
			System.out.println("SOCKET_TIMEOUT");
		}
		System.out.println("Transferencia concluida.");
		servidor.socket.setSoTimeout(0);
		localFileOutputStream.close();
	}
	
	public void operationUpload() throws IOException{
		boolean canUpload = true;
		String []splited = request.split(" ");
		File []listFiles = servidor.localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(splited[1]))
				canUpload = false;
		if(canUpload) upload(request);
		PrintWriter pout;
		pout = new PrintWriter(servidor.socket.getOutputStream(), true);
		pout.println(canUpload);
		pout.flush();
		servidor.socket.setSoTimeout(3000);
		try{
			request = in.readLine();
			if(request.contains("confirm")){
				System.out.println("Operacao aceite");
						
			}	
		}catch(SocketTimeoutException e){
			System.out.println("Operacao cancelada");
		}
		servidor.socket.setSoTimeout(0);
	}
	
	public void operationDelete() throws IOException{
		boolean canDelete = false;
		String []splited = request.split(" ");
		File []listFiles = servidor.localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(splited[1]))
				canDelete = listFiles[i].getParentFile().canWrite();
		PrintWriter pout;
		pout = new PrintWriter(servidor.socket.getOutputStream(), true);
		pout.println(canDelete);
		pout.flush();
		servidor.socket.setSoTimeout(3000);
		try{
			request = in.readLine();
			if(request.contains("confirm")){
				System.out.println("Operacao aceite");
				for(int i=0;i<listFiles.length;i++)
					if(listFiles[i].getName().contains(splited[1]))
						listFiles[i].delete();
			}	
		}catch(SocketTimeoutException e){
			System.out.println("Operacao cancelada");
		}
		servidor.socket.setSoTimeout(0);
	}
	
	public void operationGetDir() throws IOException{

		servidor.bisGetDir = new BufferedInputStream(servidor.socket.getInputStream());
		servidor.disGetDir = new DataInputStream(servidor.bisGetDir);
		int filesCount = servidor.disGetDir.readInt();
		File[] files = new File[filesCount];

		servidor.socket.setSoTimeout(10000);
		try{
			for(int i = 0; i < filesCount; i++){
			    long fileLength = servidor.disGetDir.readLong();
			    String fileName = servidor.disGetDir.readUTF();
	
			    files[i] = new File(servidor.localDirectory + "/" + fileName);
	
			    try{
			    	FileOutputStream fos = new FileOutputStream(files[i]);
				    BufferedOutputStream bos = new BufferedOutputStream(fos);
		
				    for(int j = 0; j < fileLength; j++) bos.write(servidor.bisGetDir.read());
			    }catch(IOException e){System.out.println("ERRO A COPIAR");}
	
			    //bos.close();
			}
		}catch(SocketTimeoutException e){servidor.socket.setSoTimeout(0);}	
			System.out.println("DIRETORIA COPIADA");
	}
	
	@Override
	public void run(){
		
		try{
        	in = new BufferedReader(new InputStreamReader(servidor.socket.getInputStream()));
        	while(true){
	            
	            request = in.readLine();
	
	            if(request == null){ //EOF
	                return;
	            }
	            System.out.println("<Thread_Ouve_Prim" + "> Recebido \"" + request.trim());
	            
	            if(request.contains("DELETE"))
	            	operationDelete();
	            if(request.contains("UPLOAD"))
	            	operationUpload();
	            if(request.contains("GET_DIR")){
	            	operationGetDir();
	            	servidor.socket.getInputStream().read();
	            }
	            
        	}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
