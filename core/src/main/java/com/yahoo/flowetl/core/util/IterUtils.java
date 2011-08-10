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
package com.yahoo.flowetl.core.util;

import java.util.Iterator;
import java.util.List;

import com.yahoo.flowetl.core.iterator.CloseableIterator;

/**
 * This class provides some specific iterator utility functions.
 * 
 * @author harlowja
 */
public class IterUtils
{
    private IterUtils() {
        // a util class
    }

    /**
     * Closes the iterator quietly if it is a closeable iterator by ignoring any
     * errors it may generate when it is being closed.
     * 
     * @param it
     */
    public static void closeQuietly(Iterator<?> it) {
        try {
            close(it);
        }
        catch (Exception e) {
            // ignore
        }
    }

    /**
     * Closes the iterator if it is a closeable iterator.
     * 
     * @param it
     * 
     * @throws Exception
     */
    public static void close(Iterator<?> it) throws Exception {
        if (it == null) {
            return;
        }
        if (it instanceof CloseableIterator<?>) {
            ((CloseableIterator<?>) it).close();
        }
    }

    /**
     * Converts a iterator of a given type to a list of a given type by creating
     * an instance of that list type given and iterating over the iterator and
     * for each element placing it into the created list.
     * 
     * @param it
     * @param listType
     *            typically array list or linked list...
     * 
     * @return the list filled with the iterators results
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> toList(Iterator<T> it, Class<? extends List> listType) {
        List<T> list = (List<T>) KlassUtils.getInstanceOf(listType, new Object[] {});
        if (it == null) {
            return list;
        }
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
}
