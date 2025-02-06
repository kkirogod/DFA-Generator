//------------------------------------------------------------------//
//                        COPYRIGHT NOTICE                          //
//------------------------------------------------------------------//
// Copyright (c) 2017, Francisco Jos� Moreno Velo                   //
// All rights reserved.                                             //
//                                                                  //
// Redistribution and use in source and binary forms, with or       //
// without modification, are permitted provided that the following  //
// conditions are met:                                              //
//                                                                  //
// * Redistributions of source code must retain the above copyright //
//   notice, this list of conditions and the following disclaimer.  // 
//                                                                  //
// * Redistributions in binary form must reproduce the above        // 
//   copyright notice, this list of conditions and the following    // 
//   disclaimer in the documentation and/or other materials         // 
//   provided with the distribution.                                //
//                                                                  //
// * Neither the name of the University of Huelva nor the names of  //
//   its contributors may be used to endorse or promote products    //
//   derived from this software without specific prior written      // 
//   permission.                                                    //
//                                                                  //
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND           // 
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,      // 
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF         // 
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE         // 
// DISCLAIMED. IN NO EVENT SHALL THE COPRIGHT OWNER OR CONTRIBUTORS //
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,         // 
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED  //
// TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,    //
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND   // 
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT          //
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   //
// IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF   //
// THE POSSIBILITY OF SUCH DAMAGE.                                  //
//------------------------------------------------------------------//

//------------------------------------------------------------------//
//                      Universidad de Huelva                       //
//           Departamento de Tecnolog�as de la Informaci�n          //
//   �rea de Ciencias de la Computaci�n e Inteligencia Artificial   //
//------------------------------------------------------------------//
//                     PROCESADORES DE LENGUAJE                     //
//------------------------------------------------------------------//
//                                                                  //
//                  Compilador del lenguaje Tinto                   //
//                                                                  //
//------------------------------------------------------------------//

package parser;

import java.io.FileInputStream;
import ast.*;

public class MyParser implements TokenConstants {

	private MyLexer lexer; // Analizador lexico
	private Token nextToken; // Siguiente token de la cadena de entrada
	private Token prevToken; // Ultimo token analizado
	private int errorCount; // Contador de errores
	private String errorMsg; // Mensaje de errores

	public MyParser(FileInputStream fis) 
	{
		this.lexer = new MyLexer(fis);
		this.prevToken = null;
		this.nextToken = lexer.getNextToken();
		this.errorCount = 0;
		this.errorMsg = "";
	}

	public int getErrorCount() // Obtiene el numero de errores del analisis
	{
		return this.errorCount;
	}

	public String getErrorMsg() // Obtiene el mensaje de error del analisis
	{
		return this.errorMsg;
	}

	private void catchError(Exception ex) // Almacena un error de analisis
	{
		this.errorCount++;
		this.errorMsg += ex.toString();
	}

	private void skipTo(int[] left, int[] right) { // Sincroniza la cadena de tokens
		boolean flag = false;
		if(prevToken.getKind() == EOF || nextToken.getKind() == EOF) flag = true;
		for(int i=0; i<left.length; i++) if(prevToken.getKind() == left[i]) flag = true;
		for(int i=0; i<right.length; i++) if(nextToken.getKind() == right[i]) flag = true;

		while(!flag) 
		{
			prevToken = nextToken;
			nextToken = lexer.getNextToken();
			if(prevToken.getKind() == EOF || nextToken.getKind() == EOF) flag = true;
			for(int i=0; i<left.length; i++) if(prevToken.getKind() == left[i]) flag = true;
			for(int i=0; i<right.length; i++) if(nextToken.getKind() == right[i]) flag = true;
		}
	}

	private Token match(int kind) throws SintaxException { // Consume un token de la cadena de entrada
		
		if (nextToken.getKind() == kind) {
			prevToken = nextToken;
			nextToken = lexer.getNextToken();
			return prevToken;
		}
		else
			throw new SintaxException(nextToken, kind);
	}
	
	public Fichero parse() // Analiza un fichero de entrada
	{
		this.errorCount = 0;
		this.errorMsg = "";
		return tryFichero();
	}

	public Fichero tryFichero() // Analiza el simbolo <Fichero>
	{
		int[] lsync = { };
		int[] rsync = { EOF };
		
		Fichero fichero_s = null;
		
		try
		{
			fichero_s = parseFichero();
		}
		catch(Exception ex)
		{
			catchError(ex);
			skipTo(lsync,rsync);
		}
		
		return fichero_s;
	}

	private Fichero parseFichero() throws SintaxException {
		
		int[] expected = { ID };
		
		Fichero fichero_s = null;
		
		switch (nextToken.getKind()) {
		case ID:
			String id = nextToken.getLexeme();
			match(ID);
			match(EQ);
			OptionList expr_s = tryExpr();
			match(SEMICOLON);
			fichero_s = new Fichero(id, expr_s);
			break;
		default:
			throw new SintaxException(nextToken, expected);
		}
		
		return fichero_s;
	}
	
	public OptionList tryExpr() // Analiza el simbolo <Expr>
	{
		int[] lsync = { };
		int[] rsync = { SEMICOLON, RPAREN };
		
		OptionList expr_s = null;
		
		try
		{
			expr_s = parseExpr();
		}
		catch(Exception ex)
		{
			catchError(ex);
			skipTo(lsync,rsync);
		}
		
		return expr_s;
	}

	private OptionList parseExpr() throws SintaxException {
		
		int[] expected = { SYMBOL, LPAREN };
		
		OptionList expr_s = null;
		
		switch (nextToken.getKind()) {
		case SYMBOL:
		case LPAREN:
			ConcatList option_s = tryOption();
			OptionList exprClau_s = tryExprClau();
			exprClau_s.addOption(option_s);
			expr_s = exprClau_s;
			break;
		default:
			throw new SintaxException(nextToken, expected);
		}
		
		return expr_s;
	}
	
	public OptionList tryExprClau() // Analiza el simbolo <ExprClau>
	{
		int[] lsync = { };
		int[] rsync = { SEMICOLON, RPAREN };
		
		OptionList exprClau_s = null;
		
		try
		{
			exprClau_s = parseExprClau();
		}
		catch(Exception ex)
		{
			catchError(ex);
			skipTo(lsync,rsync);
		}
		
		return exprClau_s;
	}

	private OptionList parseExprClau() throws SintaxException {
		
		int[] expected = { OR, SEMICOLON, RPAREN };
		
		OptionList exprClau_s = null;
		
		switch (nextToken.getKind()) {
		case OR:
			match(OR);
			ConcatList option_s = tryOption();
			OptionList exprClau1_s = tryExprClau();
			exprClau1_s.addOption(option_s);
			exprClau_s = exprClau1_s;
			break;
		case SEMICOLON:
		case RPAREN:
			exprClau_s = new OptionList();
			break;
		default:
			throw new SintaxException(nextToken, expected);
		}
		
		return exprClau_s;
	}
	
	public ConcatList tryOption() // Analiza el simbolo <Option>
	{
		int[] lsync = { };
		int[] rsync = { OR, SEMICOLON, RPAREN };
		
		ConcatList option_s = null;
		
		try
		{
			option_s = parseOption();
		}
		catch(Exception ex)
		{
			catchError(ex);
			skipTo(lsync,rsync);
		}
		
		return option_s;
	}

	private ConcatList parseOption() throws SintaxException {
		
		int[] expected = { SYMBOL, LPAREN };
		
		ConcatList option_s = null;
		
		switch (nextToken.getKind()) {
		case SYMBOL:
		case LPAREN:
			Expression base_s = tryBase();
			ConcatList optionClauPos_s = tryOptionClauPos();
			optionClauPos_s.concat(base_s);
			option_s = optionClauPos_s;
			break;
		default:
			throw new SintaxException(nextToken, expected);
		}
		
		return option_s;
	}

	public ConcatList tryOptionClauPos() // Analiza el simbolo <OptionClauPos>
	{
		int[] lsync = { };
		int[] rsync = { OR, SEMICOLON, RPAREN };
		
		ConcatList optionClauPos_s = null;

		try
		{
			optionClauPos_s = parseOptionClauPos();
		}
		catch(Exception ex)
		{
			catchError(ex);
			skipTo(lsync,rsync);
		}
		
		return optionClauPos_s;
	}

	private ConcatList parseOptionClauPos() throws SintaxException {
		
		int[] expected = { SYMBOL, LPAREN, OR, SEMICOLON, RPAREN };
		
		ConcatList optionClauPos_s = null;
		
		switch (nextToken.getKind()) {
		case SYMBOL:
		case LPAREN:
			Expression base_s = tryBase();
			ConcatList optionClauPos1_s = tryOptionClauPos();
			optionClauPos1_s.concat(base_s);
			optionClauPos_s = optionClauPos1_s;
			break;
		case SEMICOLON:
		case OR:
		case RPAREN:
			optionClauPos_s = new ConcatList();
			break;
		default:
			throw new SintaxException(nextToken, expected);
		}
		
		return optionClauPos_s;
	}
	
	public Expression tryBase() // Analiza el simbolo <Base>
	{
		int[] lsync = { };
		int[] rsync = { SYMBOL, LPAREN, OR, SEMICOLON, RPAREN };
		
		Expression base_s = null;
		
		try
		{
			base_s = parseBase();
		}
		catch(Exception ex)
		{
			catchError(ex);
			skipTo(lsync,rsync);
		}
		
		return base_s;
	}

	private Expression parseBase() throws SintaxException {
		
		int[] expected = { SYMBOL, LPAREN };
		
		Expression base_s = null;
		
		switch (nextToken.getKind()) {
		case SYMBOL:
			
			if(nextToken.getLexeme().length() > 1)
				base_s = new Symbol(nextToken.getLexeme().charAt(1));
			else
				base_s = new Symbol(nextToken.getLexeme().charAt(0));
			
			match(SYMBOL);
			break;
		case LPAREN:
			match(LPAREN);
			Expression expr_s = tryExpr();
			match(RPAREN);
			Expression oper_s = tryOper(expr_s);
			base_s = oper_s;
			break;
		default:
			throw new SintaxException(nextToken, expected);
		}
		
		return base_s;
	}
	
	public Expression tryOper(Expression expr) // Analiza el simbolo <Oper>
	{
		int[] lsync = { };
		int[] rsync = { SYMBOL, LPAREN, OR, SEMICOLON, RPAREN };
		
		Expression oper_s = null;
		
		try
		{
			oper_s = parseOper(expr);
		}
		catch(Exception ex)
		{
			catchError(ex);
			skipTo(lsync,rsync);
		}
		
		return oper_s;
	}

	private Expression parseOper(Expression expr) throws SintaxException {
		
		int[] expected = { STAR, PLUS, HOOK, SYMBOL, LPAREN, OR, SEMICOLON, RPAREN };
		
		Expression oper_s = null;
		
		switch (nextToken.getKind()) {
		case STAR:
			match(STAR);
			oper_s = new Operation(Operation.STAR, expr);
			break;
		case PLUS:
			match(PLUS);
			oper_s = new Operation(Operation.PLUS, expr);
			break;
		case HOOK:
			match(HOOK);
			oper_s = new Operation(Operation.HOOK, expr);
			break;
		case SYMBOL:
		case LPAREN:
		case OR:
		case SEMICOLON:
		case RPAREN:
			oper_s = expr;
			break;
		default:
			throw new SintaxException(nextToken, expected);
		}
		
		return oper_s;
	}
}