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

/**
 * A simple class which holds a pair of values.
 * 
 * @author harlowja
 */
public class Pair<First, Second>
{
    /** The first value. */
    private First first;

    /** The second value. */
    private Second second;

    /**
     * Instantiates a new pair.
     * 
     * @param first
     * @param second
     */
    public Pair(First first, Second second) {
        super();
        this.first = first;
        this.second = second;
    }

    /**
     * Gets the first value.
     */
    public First getFirst() {
        return first;
    }

    /**
     * Gets the second value.
     */
    public Second getSecond() {
        return second;
    }
}
