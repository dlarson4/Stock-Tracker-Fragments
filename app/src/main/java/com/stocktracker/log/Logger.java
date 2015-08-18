package com.stocktracker.log;

import com.stocktracker.BuildConfig;

import android.util.Log;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.WARN;

public class Logger
{
    private final static String TAG = "StockTracker";

    private final static boolean LOGGING_ENABLED = BuildConfig.DEBUG;

    public static boolean isLoggingEnabled()
    {
        return LOGGING_ENABLED;
    }

    public static void debug(String message, Object... args)
    {
        log(DEBUG, TAG, null, message, args);
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
        if(!LOGGING_ENABLED)
        {
            return;
        }
        
        switch(level)
        {
            case DEBUG:
            {
                //if(DEBUG_LOG_ENABLED)
                {
                    Log.d(tag, String.format(message, args));
                }
                break;
            }
            case WARN:
            {
                //if(WARN_LOG_ENABLED)
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
            }
            case ERROR:
            {
                //if(ERROR_LOG_ENABLED)
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

}
