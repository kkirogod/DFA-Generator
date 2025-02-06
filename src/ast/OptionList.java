package ast;

import java.util.ArrayList;
import java.util.List;

// Clase que describe un lista de opciones “ a | b | c ”
public class OptionList extends Expression {

	private List<Expression> list;
	
	public OptionList() {
		list = new ArrayList<Expression>();
	}

	public OptionList(Expression exp) {
		list = new ArrayList<Expression>();
		list.add(exp);
	}

	public void addOption(Expression exp) {
		list.add(0, exp);
	}

	public List<Expression> getList() {
		return list;
	}
}