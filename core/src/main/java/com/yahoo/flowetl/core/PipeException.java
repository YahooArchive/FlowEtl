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
 * This class is a checked exception for cases where users of this framework
 * think they should use one, instead of using a core exception which is not
 * checked. Both provide similar functionality, but sometimes u want a checked
 * exception. This is for that case.
 * 
 * @author Joshua Harlow
 */
@SuppressWarnings("serial")
public class PipeException extends Exception
{
    /**
     * Instantiates a new pipe exception.
     */
    public PipeException() {
        super();
    }

    /**
     * Instantiates a new pipe exception.
     * 
     * @param msg
     *            the exception message
     */
    public PipeException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new pipe exception.
     * 
     * @param msg
     *            the exception message
     * @param cause
     *            the cause
     */
    public PipeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
