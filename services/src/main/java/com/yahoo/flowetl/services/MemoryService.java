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

import com.yahoo.flowetl.core.services.Service;

/**
 * A service which offers a shared people like pool for various threads to use,
 * say a pipe wants to give all other pipes access to X variable, this kind of
 * service can be used instead of passing X around to everyone.
 * 
 * Be careful with the usage of this! It is only meant for special globals that
 * u are sure should be here and not for all variables in the world....
 * 
 * @author Joshua Harlow
 */
public abstract class MemoryService implements Service
{
    /**
     * Puts an object for a certain key name
     * 
     * @param key
     * @param value
     */
    public abstract void putObject(String key, Object value);

    /**
     * Gets the object at a given key name.
     * 
     * @param key
     * @return the object at that key or null if not there
     */
    public abstract <T> T getObject(String key);

    /**
     * Gets the type of the given key name
     * 
     * @param key
     * @return the type or null if not there
     */
    public abstract Class<?> getType(String key);

    /**
     * Removes the object with the given key
     * 
     * @param key
     */
    public abstract void removeObject(String key);

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#creates(java.lang.Class)
     */
    public boolean creates(Class<? extends Service> c) {
        if (MemoryService.class.equals(c)) {
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

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#shutdown()
     */
    public void shutdown() {

    }

}
