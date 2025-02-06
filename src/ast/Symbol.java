package ast;

// Clase que describe un símbolo del lenguaje
public class Symbol extends Expression {
	
	private char symbol;

	public Symbol(char s) {
		symbol = s;
	}
	
	public char getSymbol() {
		return symbol;
	}
	
	@Override
    public String toString() {
        return Character.toString(symbol);
    }
}