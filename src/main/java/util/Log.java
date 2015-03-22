package util;

/**
 * A simple logger.
 * Not using util's logger because it is too complex for current needs.
 */
public class Log {
    public static final int LOG_VERBOSE = 0;
    public static final int LOG_DEBUG = 1;
    public static final int LOG_STATUS = 2;
    public static final int LOG_WARN = 3;
    public static final int LOG_ERROR = 4;

    public static int debugLevel = LOG_STATUS;

    public static void v(String s) {
        if (debugLevel <= LOG_VERBOSE) {
            System.out.println(s);
        }
    }

    public static void d(String s) {
        if (debugLevel <= LOG_DEBUG) {
            System.out.println(s);
        }
    }

    public static void s(String s) {
        if (debugLevel <= LOG_STATUS) {
            System.out.println(s);
        }
    }

    public static void w(String s) {
        if (debugLevel <= LOG_WARN) {
            System.err.println(s);
        }
    }

    public static void e(String s) {
        if (debugLevel >= LOG_ERROR) {
            System.err.println(s);
        }
    }
}
