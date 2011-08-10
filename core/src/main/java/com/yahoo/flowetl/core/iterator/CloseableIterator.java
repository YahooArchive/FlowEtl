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
package com.yahoo.flowetl.core.iterator;

import java.util.Iterator;

/**
 * A derivative of an iterator which supports the ability to be closed when it
 * is finished iterating. This is useful say when u have translated a database
 * result set into a iterator and when its finished that result set should be
 * closed. Similarly maybe u have made an iterator for a http connection that
 * gives back results, but u need to close the http connection when done
 * iterating.
 * 
 * @author Joshua Harlow
 */
public interface CloseableIterator<E> extends Iterator<E>
{
    // closes the iterator or fails if it can't close for whatever reason
    public void close() throws Exception;
}
