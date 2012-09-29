package tgfx;

public class Command {
	private int size;
	private long lineNumber;
	private String expectedReturn;
	
	public Command(String command, long lineNumber) {
		expectedReturn = command.substring(0, command.length()-2); //will probably not have the end tag at the same spot
		size = command.length() + 1; //command + newline + any extra bytes we are accouting for
		this.lineNumber = lineNumber;
	}
	
	public void addExtraBytes(int extraBytes) {
		size += extraBytes;
	}
	
	public boolean asEarlyAs(long ln) {
		if( lineNumber <= ln)
			return true;
		return false;
	}
	
	public int getSize() {
		return size;
	}
	
	public String getExpectedReturn() {
		return expectedReturn;
	}
	
	public long getLineNumber() {
		return lineNumber;
	}

}
