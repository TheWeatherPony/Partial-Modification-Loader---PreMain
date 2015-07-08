package weatherpony.util.misc;

public enum Tristate{
	TRUE,
	FALSE,
	OTHER;
	public boolean isSolid(){
		return this != OTHER;
	}
	public boolean isPossibleTrue(){
		return this != FALSE;
	}
	public boolean isPossibleFalse(){
		return this != TRUE;
	}
	public static Tristate asTri(boolean bool){
		return bool ? TRUE : FALSE;
	}
}
