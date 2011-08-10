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
package com.yahoo.flowetl.services.config;

import java.util.List;
import java.util.Map;

import com.yahoo.flowetl.services.ConfigService;

/**
 * A class that is backed by no config but instead just returns the default or
 * null for cases which have no default (ie list/map).
 * 
 * @author Joshua Harlow
 */
public class DummyConfigService extends ConfigService
{

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getDouble(java.lang.String,
     * java.lang.Double)
     */
    @Override
    public Double getDouble(String keyPath, Double def) {
        return def;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getFloat(java.lang.String,
     * java.lang.Float)
     */
    @Override
    public Float getFloat(String keyPath, Float def) {
        return def;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.services.ConfigService#getInteger(java.lang.String,
     * java.lang.Integer)
     */
    @Override
    public Integer getInteger(String keyPath, Integer def) {
        return def;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getList(java.lang.String)
     */
    @Override
    public List<String> getList(String keyPath) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getMap(java.lang.String)
     */
    @Override
    public Map<String, String> getMap(String keyPath) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getString(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String getString(String keyPath, String def) {
        return def;
    }

}
