package calculo_afd;

import java.util.ArrayList;

import afd.Estado;
import afd.Transicion;

public class ExpPunteada {
	
	private Estado estado;
	private ArrayList<Transicion> transiciones;
	private ArrayList<Integer> posicionesPuntos;
	
	public ExpPunteada(Estado e, ArrayList<Integer> p, ArrayList<Transicion> t) {
		estado = e;
		posicionesPuntos = p;
		transiciones = t;
	}
	
	public void addTransicion(Transicion T) {
		transiciones.add(T);
	}

	public Estado getEstado() {
		return estado;
	}

	public ArrayList<Integer> getPosicionesPuntos() {
		return posicionesPuntos;
	}

	public ArrayList<Transicion> getTransiciones() {
		return transiciones;
	}
}
