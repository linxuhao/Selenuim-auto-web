package customExceptions;

import java.util.logging.Level;

public class LoggedException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Level logLevel;
	
	public LoggedException(final Level logLevel, final String logMessage) {
		super(logMessage);
		this.logLevel = logLevel;
	}
	
	public LoggedException(final Level logLevel, final String logMessage, final Throwable cause) {
		super(logMessage, cause);
		this.logLevel = logLevel;
	}
	
	public Level getLogLevel() {
		return logLevel;
	}

}
