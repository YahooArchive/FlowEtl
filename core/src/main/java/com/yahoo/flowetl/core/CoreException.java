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

/**
 * This class is used to signal anywhere in the pipe code where a unrecoverable
 * error has occurred, without needing to declare every function that uses own
 * as a checked "function". This saves time and readability, but does mean u can
 * not easily look at a function to determine what it throws. This maybe good or
 * bad depending on your preferences.
 * 
 * @author Joshua Harlow
 */
@SuppressWarnings("serial")
public class CoreException extends RuntimeException
{

    /**
     * Instantiates a new core exception.
     */
    public CoreException() {
        super();
    }

    /**
     * Instantiates a new core exception.
     * 
     * @param msg
     *            the message for this exception
     */
    public CoreException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new core exception.
     * 
     * @param msg
     *            the message for this exception
     * @param cause
     *            the cause
     */
    public CoreException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
