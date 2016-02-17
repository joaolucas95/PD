import java.io.Serializable;

public class Heartbeat implements Serializable{
	
	protected boolean primario;
	protected int port;
	protected String registry;
	static final long serialVersionUID = 1010L;
	
	public Heartbeat(boolean primario){
		this.primario = primario;
	}
	public boolean getPrimario(){
		return this.primario;
	}
	public void setPort(int port){
		this.port = port;
	}
	public int getPort(){
		return this.port;
	}
	public void setRegistry(String registry){
		this.registry = registry;
	}
	public String getRegistry(){
		return this.registry;
	}
}
