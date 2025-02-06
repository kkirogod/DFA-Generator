package ast;

public class Operation extends Expression {
	
	public static final int STAR = 1;
	public static final int PLUS = 2;
	public static final int HOOK = 3;
	
	private int operator;
	private Expression operand;

	public Operation(int op, Expression exp) {
		
		operator = op;
		operand = exp;
	}

	public int getOperator() {
		return operator;
	}

	public Expression getOperand() {
		return operand;
	}

	public static String operatorToString(int operator) {
		
		switch (operator) {
		case 1: 
			return "*";
		case 2:
			return "+";
		case 3:
			return "?";
		}
		
		return null;
	}
	
	@Override
    public String toString() {
        return operand.toString() + operatorToString(operator);
    }
}
