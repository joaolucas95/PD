import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;

public class ThreadsServidor extends Thread{
	
	public static final int MAX_SIZE = 4000;
	//int nAccepted = 0;
	String operation;
	BufferedReader in;
	Servidor servidor;
	protected byte []fileChunck = new byte[MAX_SIZE];
	
	public ThreadsServidor(Servidor servidor){//, Socket cliente){
		this.servidor = servidor;
		//this.cliente = cliente;
	}
	
	public void operationDownload(String request) throws IOException{
		byte []fileChunck = new byte[MAX_SIZE];
		int nbytes = -1;
		DataOutputStream out = new DataOutputStream(servidor.toClientSocket.getOutputStream());
		String []splited = request.split(" ");
		File []listFiles = servidor.localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(splited[1]))
				nbytes = (int) listFiles[i].length();
		String requestedCanonicalFilePath = new File(servidor.localDirectory+File.separator+splited[1]).getCanonicalPath();
		if(!requestedCanonicalFilePath.startsWith(servidor.localDirectory.getCanonicalPath()+File.separator)){
            System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
            System.out.println("A directoria de base nao corresponde a " + servidor.localDirectory.getCanonicalPath()+"!");
            return;
        }
		FileInputStream requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
		System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");
        
		while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
            out.write(fileChunck, 0, nbytes);
            out.flush();                        
        }    
        System.out.println("Transferencia concluida");
        if(requestedFileInputStream != null)
        	requestedFileInputStream.close();

	}
	
	public void sendFileSecundary(String request, int n) throws IOException{
		byte []fileChunck = new byte[MAX_SIZE];
		int nbytes = -1;
		DataOutputStream out = new DataOutputStream(servidor.sockets.get(n).getOutputStream());
		String []splited = request.split(" ");
		File []listFiles = servidor.localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(splited[1]))
				nbytes = (int) listFiles[i].length();
		String requestedCanonicalFilePath = new File(servidor.localDirectory+File.separator+splited[1]).getCanonicalPath();
		if(!requestedCanonicalFilePath.startsWith(servidor.localDirectory.getCanonicalPath()+File.separator)){
            System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
            System.out.println("A directoria de base nao corresponde a " + servidor.localDirectory.getCanonicalPath()+"!");
            return;
        }
		FileInputStream requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
		System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");
        
		while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
            out.write(fileChunck, 0, nbytes);
            out.flush();                        
        }    
        System.out.println("Transferencia concluida");
        if(requestedFileInputStream != null)
        	requestedFileInputStream.close();

	}
	
	public void sendFilePrimary(String request) throws IOException{
		byte []fileChunck = new byte[MAX_SIZE];
		int nbytes = -1;
		DataOutputStream out = new DataOutputStream(servidor.socket.getOutputStream());
		String []splited = request.split(" ");
		File []listFiles = servidor.localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(splited[1]))
				nbytes = (int) listFiles[i].length();
		String requestedCanonicalFilePath = new File(servidor.localDirectory+File.separator+splited[1]).getCanonicalPath();
		if(!requestedCanonicalFilePath.startsWith(servidor.localDirectory.getCanonicalPath()+File.separator)){
            System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
            System.out.println("A directoria de base nao corresponde a " + servidor.localDirectory.getCanonicalPath()+"!");
            return;
        }
		FileInputStream requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
		System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");
        
		while((nbytes = requestedFileInputStream.read(fileChunck))>0){                        
            out.write(fileChunck, 0, nbytes);
            out.flush();                        
        }    
        System.out.println("Transferencia concluida");
        if(requestedFileInputStream != null)
        	requestedFileInputStream.close();

	}
	
	
	public void upload(String request) throws IOException{
		String []splited = request.split(" ");
		FileOutputStream localFileOutputStream = null;
		String localFilePath = null;
		int nbytes;
    	DataInputStream in;
    	localFilePath = servidor.localDirectory.getCanonicalPath()+File.separator+splited[1];
		localFileOutputStream = new FileOutputStream(localFilePath);
		in = new DataInputStream(servidor.toClientSocket.getInputStream());
		servidor.toClientSocket.setSoTimeout(5000);
		try{
			while((nbytes = in.read(fileChunck)) > 0){
		//nbytes = in.read(fileChunck);
			System.out.println(nbytes);
            localFileOutputStream.write(fileChunck, 0, nbytes);
            System.out.println("Acrescentados " + nbytes + " bytes ao ficheiro " + localFilePath+ ".");
			}
		}catch(SocketTimeoutException e){}
		System.out.println("Transferencia concluida.");
		servidor.toClientSocket.setSoTimeout(0);
		localFileOutputStream.close();
	}
	
	public void askForUpload(String request) throws IOException{
		upload(request);
		PrintWriter pout;
		pout = new PrintWriter(servidor.socket.getOutputStream(), true);
		String []splited = request.split(" ");
		pout.println(splited[0] + " " + splited[1]);
		pout.flush();
		sendFilePrimary(request);
	}
	
	public void operationUpload(String request) throws IOException, InterruptedException{
		boolean canUpload = true;
		String []splited = request.split(" ");
		File []listFiles = servidor.localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(splited[1]))
				canUpload = false;
		if(canUpload) upload(request);
		for(int i=0; i<servidor.sockets.size(); i++){
			PrintWriter pout;
			pout = new PrintWriter(servidor.sockets.get(i).getOutputStream(), true);
			pout.println(request);
			sendFileSecundary(request, i);
		}
        servidor.toClientSocket.setSoTimeout(1000);
		try{
        	request = in.readLine();
        	if(request.contains("true")) servidor.nAccepted++;
        }catch(SocketTimeoutException e){
        	servidor.toClientSocket.setSoTimeout(0);}
        sleep(1000);
		if(servidor.nAccepted == servidor.nServers && canUpload){
			for(int i=0; i<servidor.sockets.size(); i++){
				if(servidor.socket.getPort() == servidor.toClientSocket.getPort()) continue;
				PrintWriter pout;
				pout = new PrintWriter(servidor.sockets.get(i).getOutputStream(), true);
				pout.println("confirm");
				pout.flush();
			}
			upload(splited[0] + " " + splited[1]);
		}else{
			System.out.println("NAO CONFIRMA: NACCPETED="+servidor.nAccepted+ " Serv=" + servidor.nServers);
			System.out.println(canUpload);
		}	
		servidor.nAccepted = 0;
	}
	
	public void askForDelete(String request) throws IOException{
		PrintWriter pout;
		pout = new PrintWriter(servidor.socket.getOutputStream(), true);
		String []splited = request.split(" ");
		pout.println(splited[0] + " " + splited[1]);
		pout.flush();
	}
	
	public void operationDelete(String request) throws IOException, InterruptedException{
		boolean canDelete = false;
		String []splited = request.split(" ");
		File []listFiles = servidor.localDirectory.listFiles();
		for(int i=0;i<listFiles.length;i++)
			if(listFiles[i].getName().contains(splited[1]))
				canDelete = listFiles[i].getParentFile().canWrite();
		//int nAccepted = 0;
		for(int i=0; i<servidor.sockets.size(); i++){
			PrintWriter pout;
			pout = new PrintWriter(servidor.sockets.get(i).getOutputStream(), true);
			pout.println(request);
			pout.flush();
		}
		//in = new BufferedReader(new InputStreamReader(servidor.toClientSocket.getInputStream()));
        servidor.toClientSocket.setSoTimeout(1000);
		try{
        	request = in.readLine();
        	if(request.contains("true")) servidor.nAccepted++;
        }catch(SocketTimeoutException e){servidor.toClientSocket.setSoTimeout(0);}
        sleep(1000);
		if(servidor.nAccepted == servidor.nServers && canDelete){
			for(int i=0; i<servidor.sockets.size(); i++){
				PrintWriter pout;
				pout = new PrintWriter(servidor.sockets.get(i).getOutputStream(), true);
				pout.println("confirm");
				pout.flush();
			}
			for(int i=0;i<listFiles.length;i++)
				if(listFiles[i].getName().contains(splited[1]))
					listFiles[i].delete();
		}else{
			System.out.println("NAO CONFIRMA: NACCPETED="+servidor.nAccepted+ " Serv=" + servidor.nServers);
			System.out.println(canDelete);
		}	
		servidor.nAccepted = 0;
	}
	
	public void operationSeeDir() throws IOException{
		PrintWriter pout;
		pout = new PrintWriter(servidor.toClientSocket.getOutputStream(), true);
		
		File []listFiles = servidor.localDirectory.listFiles();
		String files = "";
		for(int i=0;i<listFiles.length;i++)
			files += listFiles[i].getName() + " " + "(" + listFiles[i].length()+ " bytes)" +'\n';
		pout.println(files);
		pout.flush();
		
	}
	
	public void operationGetDir() throws IOException{
		File[] files = new File(servidor.localDirectory.getAbsolutePath()).listFiles();

		servidor.bosGetDir = new BufferedOutputStream(servidor.toClientSocket.getOutputStream());
		servidor.dosGetDir = new DataOutputStream(servidor.bosGetDir);

		servidor.dosGetDir.write("GET_DIR\n".getBytes());servidor.dosGetDir.flush();
		
		servidor.dosGetDir.writeInt(files.length);

		for(File file : files)
		{
		    long length = file.length();
		    servidor.dosGetDir.writeLong(length);

		    String name = file.getName();
		    servidor.dosGetDir.writeUTF(name);

		    FileInputStream fis = new FileInputStream(file);
		    BufferedInputStream bis = new BufferedInputStream(fis);

		    int theByte = 0;
		    while((theByte = bis.read()) != -1) servidor.bosGetDir.write(theByte);

		   bis.close();
		}

		//servidor.dosGetDir.close();
	}
	
	public void run(){
		String request; 
        
        try{
        	
        	while(true){
        		
        		in = new BufferedReader(new InputStreamReader(servidor.toClientSocket.getInputStream()));
	            request = in.readLine();
	
	            if(request == null){ //EOF
	                return;
	            }
	            System.out.println("<Thread_Servidor" + "> Recebido \"" + request.trim() + "\" de " + 
	                    servidor.toClientSocket.getInetAddress().getHostAddress() + ":" + 
	                    servidor.toClientSocket.getLocalPort());
	            System.out.println(this);
	            if(request.contains("true")) servidor.nAccepted++;
	            if(request.contains("GET_DIR")){
	            	servidor.sockets.add(servidor.toClientSocket);
	            	servidor.nServers++;
	            	//operationGetDir();
	            }
	            if(request.contains("CLIENT")) servidor.nClientes++;
	            if(request.contains("SEE_SERV_DIR"))
	            	operationSeeDir();
	            if(request.contains("DOWNLOAD"))
	            	operationDownload(request);
	            if(request.contains("UPLOAD"))
	            	if(!servidor.isPrimario())
	            		askForUpload(request);
	            	else
	            	operationUpload(request);
	            if(request.contains("DELETE")){
	            	if(!servidor.isPrimario())
	            		askForDelete(request);
	            	else
	            		operationDelete(request);
	            	
	            }
	           /* if(request.contains("GET_DIR")){
	            	operationGetDir();
	            	return;
	            }*/
	            
        	}
        }catch(SocketTimeoutException e){
        	
        }catch (IOException | InterruptedException e) {
			e.printStackTrace();
        }
	}
}
