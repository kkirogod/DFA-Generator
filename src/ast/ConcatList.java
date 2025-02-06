package ast;

import java.util.ArrayList;
import java.util.List;

//Clase que describe una concatenación de expresiones “ a b c ”
public class ConcatList extends Expression {

	private List<Expression> list;
	
	public ConcatList() {
		list = new ArrayList<Expression>();
	}

	public ConcatList(Expression exp) {
		list = new ArrayList<Expression>();
		list.add(exp);
	}

	public void concat(Expression exp) {
		list.add(0, exp);
	}
	
	public List<Expression> getList() {
		return list;
	}
}
