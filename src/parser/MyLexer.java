package parser;

import java.io.*;

public class MyLexer extends Lexer implements TokenConstants {

	protected int transition(int state, char symbol) {
		switch (state) {
		case 0:
			if (symbol >= 'a' && symbol <= 'z')
				return 5;
			else if (symbol >= 'A' && symbol <= 'Z')
				return 5;
			else if (symbol == '_')
				return 5;
			else if (symbol == '\'')
				return 1;
			else if (symbol == '(')
				return 15;
			else if (symbol == ')')
				return 14;
			else if (symbol == ';')
				return 7;
			else if (symbol == '|')
				return 6;
			else if (symbol == '+')
				return 12;
			else if (symbol == '*')
				return 8;
			else if (symbol == '?')
				return 13;
			else if (symbol == ':')
				return 9;
			else if (symbol == ' ' || symbol == '\t')
				return 16;
			else if (symbol == '\r' || symbol == '\n')
				return 16;
			else if (symbol == '/')
				return 17;
			else
				return -1;
		case 1:
			if (symbol == '\\')
				return 2;
			else if (symbol != '\'' && symbol != '\n' && symbol != '\r')
				return 3;
			else
				return -1;
		case 2:
			if (symbol == 'n' || symbol == 't' || symbol == 'b')
				return 3;
			else if (symbol == 'r' || symbol == 'f' || symbol == '\\')
				return 3;
			else if (symbol == '\'' || symbol == '\"')
				return 3;
			else
				return -1;
		case 3:
			if (symbol == '\'')
				return 4;
			else
				return -1;
		case 5:
			if (symbol >= 'a' && symbol <= 'z')
				return 5;
			else if (symbol >= 'A' && symbol <= 'Z')
				return 5;
			else if (symbol >= '0' && symbol <= '9')
				return 5;
			else if (symbol == '_')
				return 5;
			else
				return -1;
		case 9:
			if (symbol == ':')
				return 10;
			else
				return -1;
		case 10:
			if (symbol == '=')
				return 11;
			else
				return -1;
		case 16:
			if (symbol == ' ' || symbol == '\t')
				return 16;
			else if (symbol == '\r' || symbol == '\n')
				return 16;
			else
				return -1;
		case 17:
			if (symbol == '*')
				return 18;
		case 18:
			if (symbol == '*')
				return 19;
			else
				return 18;
		case 19:
			if (symbol == '*')
				return 19;
			else if (symbol == '/')
				return 20;
			else
				return 18;
		default:
			return -1;
		}
	}

	protected boolean isFinal(int state) {
		if (state <= 0 || state > 20)
			return false;
		switch (state) {
		case 1:
		case 2:
		case 3:
		case 9:
		case 10:
		case 17:
		case 18:
		case 19:
			return false;
		default:
			return true;
		}
	}

	protected Token getToken(int state, String lexeme, int row, int column) {
		switch (state) {
		case 4:
			return new Token(SYMBOL, lexeme, row, column);
		case 5:
			return new Token(ID, lexeme, row, column);
		case 6:
			return new Token(OR, lexeme, row, column);
		case 7:
			return new Token(SEMICOLON, lexeme, row, column);
		case 8:
			return new Token(STAR, lexeme, row, column);
		case 11:
			return new Token(EQ, lexeme, row, column);
		case 12:
			return new Token(PLUS, lexeme, row, column);
		case 13:
			return new Token(HOOK, lexeme, row, column);
		case 14:
			return new Token(RPAREN, lexeme, row, column);
		case 15:
			return new Token(LPAREN, lexeme, row, column);
		default:
			return null;
		}
	}

	public MyLexer(File file) throws IOException {
		super(file);
	}

	public MyLexer(FileInputStream fis) {
		super(fis);
	}
}
