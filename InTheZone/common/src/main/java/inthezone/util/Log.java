package inthezone.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A really simple logger.  Logs everything to System.err.
 * */
public class Log {
	public static void trace(final String message, final Throwable e) {
		log("TRACE", message, e);
	}

	public static void debug(final String message, final Throwable e) {
		log("DEBUG", message, e);
	}

	public static void info(final String message, final Throwable e) {
		log("INFO", message, e);
	}

	public static void warn(final String message, final Throwable e) {
		log("WARN", message, e);
	}

	public static void error(final String message, final Throwable e) {
		log("ERROR", message, e);
	}

	public static void fatal(final String message, final Throwable e) {
		log("FATAL", message, e);
	}

	private static final DateFormat dateFormat =
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

	private static void log(
		final String level, final String message, final Throwable e
	) {
		final StringBuilder buffer = new StringBuilder();
		buffer.append("[").append(dateFormat.format(new Date()));
		buffer.append(" / ").append(level).append("] ");
		buffer.append(message);
		System.err.println(buffer.toString());
		if (e != null) {
			e.printStackTrace(System.err);
		}
	}
}

