package customExceptions;

import java.lang.System.Logger.Level;

public class LoggedException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Level logLevel;
	
	public LoggedException(Level logLevel, String logMessage) {
		super(logMessage);
		this.logLevel = logLevel;
	}
	
	public LoggedException(Level logLevel, String logMessage, Throwable cause) {
		super(logMessage, cause);
		this.logLevel = logLevel;
	}
	
	public Level getLogLevel() {
		return logLevel;
	}

}
