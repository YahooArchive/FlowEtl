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
package com.yahoo.flowetl.commons.runner;

/**
 * This is what a runner class which the main class we have provided can proxy
 * to. Most people will choose to extend from base runner which provides more
 * useful operations than this simple interface.
 * 
 * @author Joshua Harlow
 */
public interface Runner
{
    // runs the given program with the given arguments
    // throws if it can not run...
    public void runProgram(String[] args) throws Exception;
}
