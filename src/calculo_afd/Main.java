package calculo_afd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import parser.*;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import ast.*;
import afd.*;

public class Main {

	static ArrayList<Character> listaLiterales;

	public static void main(String[] args) throws IOException {

		File file = new File("src/ejemplo3.txt");

		// ANALISIS LEXICO

		MyLexer lexer = new MyLexer(file);

		Token t = lexer.getNextToken();

		while (t.getKind() != TokenConstants.EOF) {

			System.out.print(t.getLexeme() + " ");
			t = lexer.getNextToken();
		}

		// ANALISIS SEMANTICO

		System.out.println("\n");

		FileInputStream fis = new FileInputStream(file);
		MyParser parser = new MyParser(fis);
		Fichero f = parser.parse();
		Expression ER = f.getExpr();

		listaLiterales = new ArrayList<Character>();

		System.out.print(f.getId() + " ::= ");
		System.out.print(analizarExpresion(ER, listaLiterales));
		System.out.println(" ;");
		System.out.println();

		// CALCULO AFD

		ArrayList<Integer> posicionesPunto = new ArrayList<Integer>();
		AtomicInteger literalAnalizado = new AtomicInteger(-1);
		calcularExpPunteadas(ER, posicionesPunto, literalAnalizado, -1, 1);

		ArrayList<ExpPunteada> expPunteadas = generarTablaEstadosTransiciones(posicionesPunto, ER);

		// GENERACION FICHERO

		generarFichero(f.getId(), expPunteadas);
	}

	private static ArrayList<ExpPunteada> generarTablaEstadosTransiciones(ArrayList<Integer> posicionesPunto,
			Expression expRegular) {

		Estado estadoIni = new Estado(0, posicionesPunto.contains(listaLiterales.size()));
		ArrayList<Transicion> transicionesEstadoIni = new ArrayList<>();

		ArrayList<ExpPunteada> expPunteadas = new ArrayList<ExpPunteada>();
		expPunteadas.add(new ExpPunteada(estadoIni, posicionesPunto, transicionesEstadoIni));

		for (int i = 0; i < expPunteadas.size(); i++) {

			addTransiciones(expPunteadas.get(i), expPunteadas, expRegular);

			if (expPunteadas.get(i).getEstado().esEstadoFinal())
				System.out.print("*");

			System.out.println("E" + expPunteadas.get(i).getEstado().getNumero() + ":");

			for (Transicion t : expPunteadas.get(i).getTransiciones()) {
				System.out.println(t.getSimbolo() + " -> " + t.getDestino());
			}

			System.out.println();
		}

		return expPunteadas;
	}

	private static String analizarExpresion(Expression e, ArrayList<Character> listaLiterales) {

		String expresion = "";

		if (e instanceof ConcatList) {

			int i = 0;

			for (Expression exp : ((ConcatList) e).getList()) {

				i++;

				expresion += analizarExpresion(exp, listaLiterales);

				if (((ConcatList) e).getList().size() > 1 && i != ((ConcatList) e).getList().size())
					expresion += " ";
			}

		} else if (e instanceof OptionList) {

			int i = 0;

			for (Expression exp : ((OptionList) e).getList()) {

				i++;

				expresion += analizarExpresion(exp, listaLiterales);

				if (((OptionList) e).getList().size() > 1 && i != ((OptionList) e).getList().size())
					expresion += " | ";
			}

		} else if (e instanceof Operation) {

			expresion += "(";
			expresion += analizarExpresion(((Operation) e).getOperand(), listaLiterales);
			expresion += ")";
			expresion += Operation.operatorToString(((Operation) e).getOperator());

		} else if (e instanceof Symbol) {

			expresion += (Symbol) e;
			listaLiterales.add(((Symbol) e).getSymbol());
		}

		return expresion;
	}

	/*
	 * flag = -1 -> abortar punteado 
	 * flag = 0 -> inicio 
	 * flag = 1 -> seguir punteando
	 * flag = 2 -> buscar punteado 
	 * flag = 3 -> concatList completo
	 */

	private static void calcularExpPunteadas(Expression exp, ArrayList<Integer> posicionesPunto,
			AtomicInteger literalAnalizado, int puntoInicial, int flag) {

		if (exp instanceof ConcatList) {

			List<Expression> concatList = ((ConcatList) exp).getList();
			int pos = 1;

			for (Expression e : concatList) {

				flag = actualizarPunteo(e, posicionesPunto, literalAnalizado, puntoInicial, flag);

				if (flag == 3) {
					flag = 1;
				}

				if (pos != concatList.size()) {

					if (flag == -1)
						break;

				} else if (flag == 1)
					posicionesPunto.add(listaLiterales.size());

				pos++;
			}
		} else if (exp instanceof OptionList)
			calcularExpPunteadas(((OptionList) exp).getList().get(0), posicionesPunto, literalAnalizado, puntoInicial,
					flag);
	}

	private static int actualizarPunteo(Expression exp, ArrayList<Integer> posicionesPunto,
			AtomicInteger literalAnalizado, int puntoInicial, int flag) {

		List<Expression> expList = null;

		if (exp instanceof OptionList) {

			expList = ((OptionList) exp).getList();

			switch (flag) {
			case 1:
				for (Expression e : expList) {
					actualizarPunteo(e, posicionesPunto, literalAnalizado, puntoInicial, flag);
				}

				return -1;
			case 2:
				for (Expression e : expList) {

					flag = actualizarPunteo(e, posicionesPunto, literalAnalizado, puntoInicial, flag);

					if (flag == -1 || flag == 3)
						return flag;

				}

				return flag;
			}

		} else if (exp instanceof ConcatList) {

			expList = ((ConcatList) exp).getList();

			int pos = 1;

			switch (flag) {
			case 1:
				for (Expression e : expList) {

					flag = actualizarPunteo(e, posicionesPunto, literalAnalizado, puntoInicial, flag);

					if (flag == -1) {

						literalAnalizado.set(literalAnalizado.get() + (expList.size() - pos));
						break;
					}
					pos++;
				}

				return -1;
			case 2:
				for (Expression e : expList) {

					flag = actualizarPunteo(e, posicionesPunto, literalAnalizado, puntoInicial, flag);

					if (pos == expList.size()) {
						if (flag == 1)
							return 3;
					} else if (flag == -1) {
						literalAnalizado.set(literalAnalizado.get() + (expList.size() - pos));
						return -1;
					}
					pos++;
				}

				return flag;
			}

		} else if (exp instanceof Operation) {

			Expression operando = ((Operation) exp).getOperand();
			String operador = Operation.operatorToString(((Operation) exp).getOperator());

			switch (flag) {
			case 1:
				actualizarPunteo(operando, posicionesPunto, literalAnalizado, puntoInicial, flag);

				if (operador == "+")
					return -1;
				else if (operador == "*")
					return 1;

			case 2:
				if (operador == "+" || operador == "*") {

					AtomicInteger literalAnalizadoAux = new AtomicInteger(literalAnalizado.get());

					flag = actualizarPunteo(operando, posicionesPunto, literalAnalizado, puntoInicial, flag);

					if (flag == 3) {
						actualizarPunteo(operando, posicionesPunto, literalAnalizadoAux, puntoInicial, 1);
						literalAnalizado.set(literalAnalizadoAux.get());
						return 1;
					}

					if (flag == 2)
						return flag;
					else
						return -1;
				}
			}

		} else if (exp instanceof Symbol) {

			literalAnalizado.incrementAndGet();

			switch (flag) {
			case 0:
			case 1:
				posicionesPunto.add(literalAnalizado.get());
				return -1;
			case 2:
				if (literalAnalizado.get() == puntoInicial)
					return 1;
				else if (literalAnalizado.get() < puntoInicial)
					return 2;
			}
		}
		return 0;
	}

	private static void addTransiciones(ExpPunteada expPunteada, ArrayList<ExpPunteada> expPunteadas,
			Expression expRegular) {

		ArrayList<ArrayList<Integer>> listaPosPuntos = new ArrayList<ArrayList<Integer>>();

		for (int punto : expPunteada.getPosicionesPuntos()) {

			if (punto == listaLiterales.size())
				continue;

			AtomicInteger literalAnalizado = new AtomicInteger(-1);
			ArrayList<Integer> posPuntos = new ArrayList<Integer>();

			calcularExpPunteadas(expRegular, posPuntos, literalAnalizado, punto, 2);

			listaPosPuntos.add(posPuntos);
		}

		if (!listaPosPuntos.isEmpty()) {

			ArrayList<Character> listaLiteralesPunteados = getListaLiteralesPunteados(
					expPunteada.getPosicionesPuntos());

			if (listaLiteralesPunteados.size() <= 1) {

				ArrayList<Integer> posPuntos = listaPosPuntos.get(0);
				int numEstado = estado(expPunteadas, posPuntos);

				char literalPunteado = listaLiteralesPunteados.get(0);
				Transicion t = new Transicion(literalPunteado, numEstado);

				expPunteada.addTransicion(t);

				if (numEstado == expPunteadas.size()) {

					Estado e = new Estado(numEstado, posPuntos.contains(listaLiterales.size()));
					expPunteadas.add(new ExpPunteada(e, posPuntos, new ArrayList<>()));
				}

			} else {

				Set<Character> set = new LinkedHashSet<>(listaLiteralesPunteados);
				ArrayList<Character> listaLiteralesNoRepetidos = new ArrayList<>(set);

				ArrayList<ArrayList<Object>> posLiterales = new ArrayList<ArrayList<Object>>();

				for (Character literalNoRep : listaLiteralesNoRepetidos) {

					posLiterales.add(new ArrayList<Object>());
					posLiterales.get(posLiterales.size() - 1).add(literalNoRep);

					for (int i = 0; i < listaLiteralesPunteados.size(); i++) {

						if (literalNoRep == listaLiteralesPunteados.get(i))
							posLiterales.get(posLiterales.size() - 1).add(i);
					}

					if (posLiterales.get(posLiterales.size() - 1).size() == 2) {
						posLiterales.remove(posLiterales.size() - 1);
					}
				}

				for (int i = 0; i < listaLiteralesPunteados.size(); i++) {

					ArrayList<Integer> posPuntos = listaPosPuntos.get(i);
					ArrayList<Object> listaLiteralesRep = getListaLiteralesRep(listaLiteralesPunteados.get(i),
							posLiterales);

					if (listaLiteralesRep != null) {

						if (listaLiteralesRep.size() <= 1)
							continue;

						else {

							int l1 = (Integer) listaLiteralesRep.get(1);

							for (int j = 2; j < listaLiteralesRep.size(); j++) {

								int l2 = (Integer) listaLiteralesRep.get(j);

								listaPosPuntos.get(l1).addAll(listaPosPuntos.get(l2));
							}

							Set<Integer> set2 = new LinkedHashSet<>(listaPosPuntos.get(l1));
							posPuntos = new ArrayList<>(set2);

							listaLiteralesRep.clear();
							listaLiteralesRep.add(listaLiteralesPunteados.get(i));
						}
					}

					int numEstado = estado(expPunteadas, posPuntos);
					Transicion t = new Transicion(listaLiteralesPunteados.get(i), numEstado);
					expPunteada.addTransicion(t);

					if (expPunteadas.size() == numEstado) {

						Estado e = new Estado(numEstado, posPuntos.contains(listaLiterales.size()));
						ExpPunteada exp = new ExpPunteada(e, posPuntos, new ArrayList<>());
						expPunteadas.add(exp);
					}
				}
			}
		}

	}

	private static int estado(ArrayList<ExpPunteada> expPunteadas, ArrayList<Integer> posPuntos) {

		int estado = 0;

		for (ExpPunteada exp : expPunteadas) {

			Collections.sort(posPuntos);
			Collections.sort(exp.getPosicionesPuntos());

			if (Objects.equals(posPuntos, exp.getPosicionesPuntos()))
				return estado;

			estado++;
		}

		return estado;
	}

	private static ArrayList<Object> getListaLiteralesRep(Character literal,
			ArrayList<ArrayList<Object>> posLiterales) {

		for (ArrayList<Object> literales : posLiterales) {

			if ((Character) literales.get(0) == literal)
				return literales;
		}

		return null;
	}

	private static ArrayList<Character> getListaLiteralesPunteados(ArrayList<Integer> posPuntos) {

		ArrayList<Character> listaLiteralesPunteados = new ArrayList<Character>();

		for (int i : posPuntos) {
			if (i != listaLiterales.size())
				listaLiteralesPunteados.add(listaLiterales.get(i));
		}

		return listaLiteralesPunteados;
	}

	private static void generarFichero(String id, ArrayList<ExpPunteada> expPunteadas) throws IOException {

		String fichero = "public class " + id + " {\n\n" + "\tpublic int transition(int state, char symbol) {\n"
				+ " \t\tswitch(state) {\n";

		for (ExpPunteada e : expPunteadas) {
			fichero += "\t\t\tcase " + e.getEstado().getNumero() + ": \n";

			for (Transicion t : e.getTransiciones()) {
				fichero += "\t\t\t\tif(symbol == '" + t.getSimbolo() + "') return " + t.getDestino() + ";\n";
			}

			fichero += "\t\t\t\treturn -1; \n";
		}

		fichero += "\t\t\tdefault:\n" + "\t\t\t\treturn -1;\n" + "\t\t}\n" + "\t}\n\n"
				+ "\tpublic boolean isFinal(int state) {\n" + "\t\tswitch(state) {\n";

		for (ExpPunteada e : expPunteadas) {
			fichero += "\t\t\tcase " + e.getEstado().getNumero() + ": return " + e.getEstado().esEstadoFinal()
					+ ";\n";
		}

		fichero += "\t\t\tdefault: return false;\n" + "\t\t}\n" + "\t}\n" + "}";

		BufferedWriter writer = new BufferedWriter(new FileWriter(id + ".java"));
		writer.write(fichero);
		writer.close();
	}
}
