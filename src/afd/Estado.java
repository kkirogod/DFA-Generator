package afd;

public class Estado {
	
	private boolean estadoFinal;
	private int numero;
	
	public Estado(int n, boolean ef) {
		numero = n;
		estadoFinal = ef;
	}
	
	public boolean esEstadoFinal() {
		return estadoFinal;
	}
	
	public int getNumero() {
		return numero;
	}
}