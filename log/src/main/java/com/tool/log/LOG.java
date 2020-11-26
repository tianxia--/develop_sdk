package com.tool.log;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

public class LOG {
    static final String LOG_TAG_DEFAULT = "Blue";
    static final String LOG_TAG_ALPHA = "BlueAlpha";

    // Don't change the level values!
    // The implementation dependents on the order (level values)
    public static final int LEVEL_VERBOSE = Log.VERBOSE; // 2
    public static final int LEVEL_DEBUG = Log.DEBUG; // 3
    public static final int LEVEL_INFO = Log.INFO; // 4
    public static final int LEVEL_WARN = Log.WARN; // 5
    public static final int LEVEL_ERROR = Log.ERROR; // 6

    @IntDef({LEVEL_VERBOSE, LEVEL_DEBUG, LEVEL_INFO, LEVEL_WARN, LEVEL_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogLevel {
    }

    private static Logger consoleLogger;

    @Deprecated
    public static void logI(String msg) {
        internalLog(1, LEVEL_INFO, LOG_TAG_DEFAULT, null, msg);
    }

    @Deprecated
    public static void logD(String msg) {
        internalLog(1, LEVEL_DEBUG, LOG_TAG_DEFAULT, null, msg);
    }

    @Deprecated
    public static void logE(String msg) {
        internalLog(1, LEVEL_ERROR, LOG_TAG_DEFAULT, null, msg);
    }

    @Deprecated
    public static void logF(String msg) {
        internalLog(1, LEVEL_INFO, LOG_TAG_DEFAULT, null, msg);
    }

    @Deprecated
    public static void logI(String msg, Throwable tr) {
        internalLog(1, LEVEL_INFO, LOG_TAG_DEFAULT, tr, msg);
    }

    @Deprecated
    public static void logD(String msg, Throwable tr) {
        internalLog(1, LEVEL_DEBUG, LOG_TAG_DEFAULT, tr, msg);
    }

    @Deprecated
    public static void logE(String msg, Throwable tr) {
        internalLog(1, LEVEL_ERROR, LOG_TAG_DEFAULT, tr, msg);
    }

    @Deprecated
    public static void logF(String msg, Throwable tr) {
        internalLog(1, LEVEL_INFO, LOG_TAG_DEFAULT, tr, msg);
    }

    // -----------------
    // New APIs

    public static void setConsoleLogger(@Nullable Logger logger) {
        consoleLogger = logger;
    }


    public static void v(@NonNull String msg, Object... args) {
        internalLog(1, LEVEL_VERBOSE, LOG_TAG_DEFAULT, null, msg, args);
    }

    public static void v(Throwable t, @NonNull String msg, Object... args) {
        internalLog(1, LEVEL_VERBOSE, LOG_TAG_DEFAULT, t, msg, args);
    }

    public static void v(Throwable t) {
        internalLog(1, LEVEL_VERBOSE, LOG_TAG_DEFAULT, t, null);
    }

    public static void d(@NonNull String msg, Object... args) {
        internalLog(1, LEVEL_DEBUG, LOG_TAG_DEFAULT, null, msg, args);
    }

    public static void d(Throwable t, @NonNull String msg, Object... args) {
        internalLog(1, LEVEL_DEBUG, LOG_TAG_DEFAULT, t, msg, args);
    }

    public static void d(Throwable t) {
        internalLog(1, LEVEL_DEBUG, LOG_TAG_DEFAULT, t, null);
    }

    public static void i(@NonNull String msg, Object... args) {
        internalLog(1, LEVEL_INFO, LOG_TAG_DEFAULT, null, msg, args);
    }

    public static void i(Throwable t, @NonNull String msg, Object... args) {
        internalLog(1, LEVEL_INFO, LOG_TAG_DEFAULT, t, msg, args);
    }

    public static void i(Throwable t) {
        internalLog(1, LEVEL_INFO, LOG_TAG_DEFAULT, t, null);
    }

    public static void w(@NonNull String msg, Object... args) {
        internalLog(1, LEVEL_WARN, LOG_TAG_DEFAULT, null, msg, args);
    }

    public static void w(Throwable t, @NonNull String msg, Object... args) {
        internalLog(1, LEVEL_WARN, LOG_TAG_DEFAULT, t, msg, args);
    }

    public static void w(Throwable t) {
        internalLog(1, LEVEL_WARN, LOG_TAG_DEFAULT, t, null);
    }

    public static void e(@NonNull String msg, Object... args) {
        internalLog(1, LEVEL_ERROR, LOG_TAG_DEFAULT, null, msg, args);
    }

    public static void e(Throwable t, @NonNull String msg, Object... args) {
        internalLog(1, LEVEL_ERROR, LOG_TAG_DEFAULT, t, msg, args);
    }

    public static void e(Throwable t) {
        internalLog(1, LEVEL_ERROR, LOG_TAG_DEFAULT, t, null);
    }

    public static void log(int level, @NonNull String tag, @Nullable Throwable t,
            @Nullable String msg, Object... args) {
        internalLog(1, level, tag, t, msg, args);
    }

    public static void logCrash(@NonNull Throwable t) {
        internalLog(0, LEVEL_ERROR, LOG_TAG_DEFAULT, t, "!!!!!!!! app crashed !!!!!!!!");
    }

    private static void internalLog(int callerStackIndex, int level, @NonNull String tag, @Nullable Throwable tr,
            @Nullable String msg, Object... args) {
        if (consoleLogger != null) {
            consoleLogger.log(callerStackIndex + 1, level, tag, tr, msg, args);
        }
    }

    public static abstract class Logger {
        protected int logLevel = LEVEL_INFO;
        protected boolean fullClassName = false;

        public Logger setLogLevel(@LogLevel int level) {
            logLevel = level;
            return this;
        }

        public Logger enableFullClassName(boolean enable) {
            fullClassName = enable;
            return this;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean showLog(int level, String tag) {
            if (level >= logLevel) {
                return true;
            }

            try {
                return Log.isLoggable(LOG_TAG_ALPHA, level);
            } catch (Exception e) {
                log(1, LEVEL_ERROR, LOG_TAG_DEFAULT, null, "please check the log tag length [%s]", tag);
            }

            return false;
        }

        public void flush() {
            // nothing to do by default
        }

        public abstract void log(int callerStackIndex, int level, @NonNull String tag,
                @Nullable Throwable tr, @Nullable String msg, Object... args);

        protected void appendMessage(@NonNull Throwable callerStack, int callerStackIndex,
                StringBuilder sb, @Nullable Throwable t, @Nullable String msg, Object... args) {
            StackTraceElement[] elements = callerStack.getStackTrace();
            String callerClassName = "NA";
            String callerMethodName = "NA";
            if (elements.length > callerStackIndex) {
                StackTraceElement callerElement = elements[callerStackIndex];
                callerClassName = callerElement.getClassName();
                if (!fullClassName) {
                    int index = callerClassName.lastIndexOf('.');
                    if (index > 0 && index + 1 < callerClassName.length()) {
                        callerClassName = callerClassName.substring(index + 1);
                    }
                }
                callerMethodName = callerElement.getMethodName();
            }

            if (msg != null && args != null && args.length > 0) {
                try {
                    msg = String.format(Locale.US, msg, args);
                } catch (Exception e) {
                    if (throwLogError()) {
                        throw new RuntimeException(e.toString(), callerStack);
                    } else {
                        sb.append(LogTag.TAG_LOG_ERROR).append(' ').append(e.toString()).append('\n');
                        sb.append(Log.getStackTraceString(callerStack));
                    }
                }
            }

            sb.append('(').append(callerClassName).append('#').append(callerMethodName).append(") ");
            if (msg != null) {
                sb.append(msg);
            }
            if (t != null) {
                sb.append('\n').append(Log.getStackTraceString(t));
            }
        }

        protected boolean throwLogError() {
            return false;
        }
    }

    public static class JvmLogger extends Logger {
        @Override
        public boolean showLog(int level, String tag) {
            return level >= logLevel;
        }

        @Override
        public void log(int callerStackIndex, int level, @NonNull String tag,
                @Nullable Throwable tr, @Nullable String msg, Object... args) {
            if (msg != null && args != null && args.length > 0) {
                msg = String.format(Locale.US, msg, args);
            }
            if (msg != null) {
                System.out.println("[" + tag + "] " + msg);
            }
            if (tr != null) {
                tr.printStackTrace();
            }
        }
    }

    public static class LogcatLogger extends Logger {
        private static final int LOG_LENGTH_LIMIT = 4050;
        private static final int LOG_TAIL_LENGTH = 100;

        @Override
        public void log(int callerStackIndex, int level, @NonNull String tag,
                @Nullable Throwable t, @Nullable String msg, Object... args) {
            if (!showLog(level, tag)) {
                return;
            }

            // We only build a message when we need to output it
            Throwable callerStack = new Throwable();
            StringBuilder sb = new StringBuilder();
            appendMessage(callerStack, callerStackIndex + 1, sb, t, msg, args);
            String log = sb.toString();
            if (log.length() < LOG_LENGTH_LIMIT) {
                Log.println(level, tag, log);
            } else {
                Log.println(level, tag, log.substring(0, LOG_LENGTH_LIMIT - LOG_TAIL_LENGTH)
                        + "\n<...>" + log.substring(log.length() - LOG_TAIL_LENGTH));
            }
        }

        @Override
        protected boolean throwLogError() {
            return true;
        }
    }

    public static String getStackTrace() {
        StackTraceElement[] stackTraceArray = new Throwable().getStackTrace();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < stackTraceArray.length; i++) {
            StackTraceElement stackTraceElement = stackTraceArray[i];
            stringBuilder.append("\n").append(stackTraceElement.toString());
        }
        return stringBuilder.toString();
    }
}
