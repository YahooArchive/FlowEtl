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

import java.util.List;
import java.util.Map;

import com.yahoo.flowetl.core.services.Service;

/**
 * This class can be derived from to provide config key access with various
 * return types depending upon what that key may mean to your application.
 * 
 * @author Joshua Harlow
 */
public abstract class ConfigService implements Service
{
    /**
     * Gets the integer at the given key path.
     * 
     * @param keyPath
     *            the key path which shall be looked up
     * 
     * @param def
     *            the default to use if not there
     * 
     * @return integer
     */
    public abstract Integer getInteger(String keyPath, Integer def);

    /**
     * Gets the float at the given key path.
     * 
     * @param keyPath
     *            the key path which shall be looked up
     * @param def
     *            the default to use if not there
     * @return float
     */
    public abstract Float getFloat(String keyPath, Float def);

    /**
     * Gets the double at the given key path.
     * 
     * @param keyPath
     *            the key path which shall be looked up
     * @param def
     *            the default to use if not there
     * @return double
     */
    public abstract Double getDouble(String keyPath, Double def);

    /**
     * Gets the string at the given key path.
     * 
     * @param keyPath
     *            the key path which shall be looked up
     * @param def
     *            the default to use if not there
     * @return the string
     */
    public abstract String getString(String keyPath, String def);

    /**
     * Gets the list at the given key path.
     * 
     * @param keyPath
     *            the key path which shall be looked up
     * 
     * @return the list or null if not there
     */
    public abstract List<String> getList(String keyPath);

    /**
     * Gets the map at the given key path.
     * 
     * @param keyPath
     *            the key path which shall be looked up
     * 
     * @return the map or null if not there/valid
     */
    public abstract Map<String, String> getMap(String keyPath);

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
     * @see com.yahoo.flowetl.core.services.Service#creates(java.lang.Class)
     */
    public boolean creates(Class<? extends Service> c) {
        if (ConfigService.class.equals(c)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#shutdown()
     */
    public void shutdown() {

    }
}
