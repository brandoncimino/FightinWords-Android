package android.util;

/**
 * A simple implementation of Android's `Log` class, which isn't available during tests.
 */
@SuppressWarnings("unused")
public final class Log {
    public static int d(String tag, String msg) {
        return println(DEBUG, tag, msg);
    }

    public static int i(String tag, String msg) {
        return println(INFO, tag, msg);
    }

    public static int e(String tag, String msg) {
        return println(ERROR, tag, msg);
    }

    public static int v(String tag, String msg) {
        return println(VERBOSE, tag, msg);
    }

    public static boolean isLoggable(String tag, int level) {
        return true; // Always allow logging in tests
    }

    public static int println(int level, String tag, String message) {
        var levelLabel = switch (level) {
            case VERBOSE -> "💬";
            case DEBUG -> "🐛";
            case INFO -> "ℹ️";
            case WARN -> "⚠️";
            case ERROR -> "❌";
            case ASSERT -> "🧪";
            default -> "" + level;
        };

        var line = "[%s] %s %s".formatted(
              tag,
              levelLabel,
              message
        );

        if (level >= ERROR) {
            System.err.println(line);
        } else {
            System.out.println(line);
        }

        return 0;
    }

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = 7;
}
