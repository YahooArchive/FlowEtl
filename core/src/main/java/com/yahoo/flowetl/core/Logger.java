/*******************************************************
 *                                                     *
 * Copyright (C) 2011 Yahoo! Inc. All Rights Reserved. *
 *                                                     *
 *                Licensed under the New               *
 *                 BSD License. See the                *
 *              accompanying LICENSE file              *
 *              for the specific language              *
 *              governing permissions and              *
 *                limitations under the                *
 *                       License.                      *
 *******************************************************/
package com.yahoo.flowetl.core;

 
import org.apache.log4j.Priority;

/**
 * Logger that proxies to the log4j logging mechanism. This is similar to log5j
 * but is simpler in that we are proxying logging levels to log4j and we are
 * only adding in the variable param support.
 * 
 * @author Joshua Harlow
 */
public class Logger
{
    // logging levels
    public static enum Level
    {
        DEBUG, INFO, WARN, ERROR, FATAL
    }

    /** The logger that we will proxy to. */
    private final org.apache.log4j.Logger logger;

    /**
     * Instantiates a new logger with the class name of whoever called this
     * constructor as the name of the logger to use.
     */
    public Logger(String name) {
        this.logger = org.apache.log4j.Logger.getLogger(name);
    }

    /**
     * Instantiates a new logger.
     * 
     * @param name
     *            the name
     */
    public Logger(Class<?> name) {
        this(name.getSimpleName());
    }

    // translates our levels into log4j levels...
    @SuppressWarnings("deprecation")
    private static Priority translateLevel(Level in) {
        switch (in) {
        case DEBUG:
            return Priority.DEBUG;
        case ERROR:
            return Priority.ERROR;
        case INFO:
            return Priority.INFO;
        case WARN:
            return Priority.WARN;
        case FATAL:
            return Priority.FATAL;
        }
        return Priority.ERROR;
    }

    /**
     * Checks if is a given level is enabled.
     * 
     * @param lvl
     *            the logging level to check
     * 
     * @return true, if is that logging level is enabled
     */
    public boolean isEnabled(Level lvl) {
        if (lvl == null) {
            return false;
        }
        return this.logger.isEnabledFor(translateLevel(lvl));
    }

    /**
     * Logs the given message at the given level
     * 
     * @param lvl
     *            the logging level
     * @param e
     *            the exception that may have caused this message
     * @param msg
     *            the message to log
     * @param formatParams
     *            variable params to use for an expansion of msg variables
     *            (printf style)
     */
    public void log(Level lvl, Throwable e, String msg, Object... formatParams) {
        if (!isEnabled(lvl)) {
            return;
        }
        String outMsg = null;
        if (formatParams == null || formatParams.length == 0) {
            outMsg = msg;
        }
        else {
            outMsg = String.format(msg, formatParams);
        }
        if (e == null) {
            logger.log(translateLevel(lvl), outMsg);
        }
        else {
            logger.log(translateLevel(lvl), outMsg, e);
        }
    }

    /**
     * Logs a given message at a given level
     * 
     * @param lvl
     *            the logging level
     * @param msg
     *            the message to log
     */
    public void log(Level lvl, String msg) {
        log(lvl, msg, (Object[]) null);
    }

    /**
     * Logs a given message at a given level
     * 
     * @param lvl
     *            the logging level
     * @param msg
     *            the message to log
     * @param formatParams
     *            variable params to use for an expansion of msg variables
     *            (printf style)
     */
    public void log(Level lvl, String msg, Object... formatParams) {
        if (!isEnabled(lvl)) {
            return;
        }
        log(lvl, null, msg, formatParams);
    }
}
