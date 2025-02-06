package ast;

public class Fichero {

	private String id;
	private Expression expr;
	
	public Fichero(String id, Expression expr) {
		this.id = id;
		this.expr = expr;
	}

	public String getId() {
		return id;
	}

	public Expression getExpr() {
		return expr;
	}
}
