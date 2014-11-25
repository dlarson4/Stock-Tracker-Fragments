package com.stocktracker.log;

import android.util.Log;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.WARN;

public class Logger
{
    private final static String TAG = "StockTracker";

    private final static boolean DEBUG_LOG_ENABLED = false;
    private final static boolean INFO_LOG_ENABLED = false;
    private final static boolean WARN_LOG_ENABLED = true;
    private final static boolean ERROR_LOG_ENABLED = true;

    public static boolean isDebugEnabled()
    {
        return DEBUG_LOG_ENABLED;
    }

    public static boolean isInfoEnabled()
    {
        return INFO_LOG_ENABLED;
    }

    public static void debug(String message, Object... args)
    {
        log(DEBUG, TAG, null, message, args);
    }

    public static void debug(final Throwable t, final String message, final Object... args)
    {
        log(DEBUG, TAG, t, message, args);
    }

    public static void warn(String message, Object... args)
    {
        log(WARN, TAG, null, message, args);
    }

    public static void warn(final Throwable t, final String message, final Object... args)
    {
        log(WARN, TAG, t, message, args);
    }

    public static void error(String message, Object... args)
    {
        log(ERROR, TAG, null, message, args);
    }

    public static void error(final Throwable t, final String message, final Object... args)
    {
        log(ERROR, TAG, t, message, args);
    }

    private static void log(int level, final String tag, final Throwable error, final String message, final Object... args)
    {
        switch(level)
        {
            case DEBUG:
                if(DEBUG_LOG_ENABLED)
                {
                    if(error == null)
                    {
                        Log.d(tag, String.format(message, args));
                    }
                    else
                    {
                        Log.d(tag, String.format(message, args), error);
                    }
                }
                break;
            case INFO:
                if(INFO_LOG_ENABLED)
                {
                    if(error == null)
                    {
                        Log.i(tag, String.format(message, args));
                    }
                    else
                    {
                        Log.i(tag, String.format(message, args), error);
                    }
                }
                break;
            case WARN:
                if(WARN_LOG_ENABLED)
                {
                    if(error == null)
                    {
                        Log.w(tag, String.format(message, args));
                    }
                    else
                    {
                        Log.w(tag, String.format(message, args), error);
                    }
                }
                break;
            case ERROR:
                if(ERROR_LOG_ENABLED)
                {
                    if(error == null)
                    {
                        Log.e(tag, String.format(message, args));
                    }
                    else
                    {
                        Log.e(tag, String.format(message, args), error);
                    }
                }
                break;
        }
    }

}
