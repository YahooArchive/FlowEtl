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
package com.yahoo.flowetl.commons.db.actions;

import java.sql.Connection;

/**
 * An interface that represents some result of an action on a database
 * connection.
 * 
 * @author Joshua Harlow
 */
public interface Action<T>
{
    /**
     * Applies some action on a database.
     * 
     * @param db
     *            the db to apply the action to
     * 
     * @return the result of that action
     */
    public abstract T applyAction(Connection db);
}
