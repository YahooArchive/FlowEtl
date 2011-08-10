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
package com.yahoo.flowetl.services;

import java.sql.Connection;

import com.yahoo.flowetl.core.services.Service;

/**
 * This class provides access to a service which can fetch a database connection
 * from a given database dsn. A dsn is basically a jdbc connection name, ie its
 * JNDI name. It is up to the implementation to cache it or not.
 * 
 * @author Joshua Harlow
 */
public abstract class DatabaseService implements Service
{
    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#creates(java.lang.Class)
     */
    public boolean creates(Class<? extends Service> c) {
        if (DatabaseService.class.equals(c)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#fetch(java.lang.Class)
     */
    public Service fetch(Class<? extends Service> c) {
        return this;
    }

    /**
     * Gets the connection for a given database jndi/dsn name.
     * 
     * @param dbDsn
     * 
     * @return the connection to that database
     */
    public abstract Connection getConnection(String dbDsn);

}
