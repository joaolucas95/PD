import java.io.File;

public class Servidor {

	File localDirectory;
	int port;
	boolean primario;
	
	public Servidor(File localDirectory, int port){
		this.localDirectory = localDirectory;
		this.port = port;
		this.primario = false;
	}
	
	public static void main(String[] args) {
		File localDirectory;
		int port;
		Servidor servidor;
		
		if(args.length != 2){
			System.out.println("<SER> Sintaxe: Servidor localDirectory port");
			return;
		}
		localDirectory = new File(args[0].trim());
		port = Integer.parseInt(args[1]);
		servidor = new Servidor(localDirectory, port);
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
        
	}

}
