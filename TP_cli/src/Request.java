import java.io.Serializable;

public class Request implements Serializable{
	
	private static final long serialVersionUID = 1L;
	protected String []filesNames;
	protected int []filesSizes;
	
	public Request(String []filesNames, int []filesSizes){
		this.filesNames = filesNames;
		this.filesSizes = filesSizes;
	}

	public String[] getFilesNames() {
		return filesNames;
	}

	public void setFilesNames(String[] filesNames) {
		this.filesNames = filesNames;
	}

	public int[] getFilesSizes() {
		return filesSizes;
	}

	public void setFilesSizes(int[] filesSizes) {
		this.filesSizes = filesSizes;
	}
	
}
