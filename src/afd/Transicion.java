package afd;

public class Transicion {

	private int destino;
	private char simbolo;
	
	public Transicion(char s, int d) {
		simbolo = s;
		destino = d;
	}
	
	public char getSimbolo() {
		return simbolo;
	}
	
	public int getDestino() {
		return destino;
	}
}
