public class Expresion {

	public int transition(int state, char symbol) {
 		switch(state) {
			case 0: 
				if(symbol == 'b') return 1;
				return -1; 
			case 1: 
				if(symbol == 'a') return 2;
				return -1; 
			case 2: 
				if(symbol == 'b') return 2;
				if(symbol == 'o') return 2;
				if(symbol == 'a') return 3;
				return -1; 
			case 3: 
				if(symbol == 'a') return 3;
				if(symbol == 'o') return 2;
				if(symbol == 'b') return 4;
				return -1; 
			case 4: 
				return -1; 
			default:
				return -1;
		}
	}

	public boolean isFinal(int state) {
		switch(state) {
			case 0: return false;
			case 1: return false;
			case 2: return false;
			case 3: return false;
			case 4: return true;
			default: return false;
		}
	}
}