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
import java.util.TreeMap;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;

import com.yahoo.flowetl.services.ConfigService;

/**
 * A class that implements the config service definition by using a apache
 * commons configuration composite configuration to retrieve keys from. This is
 * a very flexible source since apache commons configuration supports many
 * different config types.
 * 
 * @author Joshua Harlow
 */
public class CompositeConfigService extends ConfigService
{
    /** The configuration that we will use. */
    private final CompositeConfiguration cfg;

    // constant used for map creation
    private static final String KV_SEP = ":";

    /**
     * Instantiates a new composite config service.
     * 
     * @param cfgSources
     * 
     * @throws ConfigurationException
     */
    public CompositeConfigService(List<Configuration> cfgSources) throws ConfigurationException {
        this.cfg = new CompositeConfiguration();
        for (Configuration cfgInstance : cfgSources) {
            this.cfg.addConfiguration(cfgInstance);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getDouble(java.lang.String,
     * java.lang.Double)
     */
    @Override
    public Double getDouble(String keyPath, Double def) {
        return cfg.getDouble(keyPath, def);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getFloat(java.lang.String,
     * java.lang.Float)
     */
    @Override
    public Float getFloat(String keyPath, Float def) {
        return cfg.getFloat(keyPath, def);
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
        return cfg.getInteger(keyPath, def);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getList(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getList(String keyPath) {
        return (List<String>) cfg.getList(keyPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getMap(java.lang.String)
     */
    @Override
    public Map<String, String> getMap(String keyPath) {
        List<String> pieces = getList(keyPath);
        if (pieces == null) {
            return null;
        }
        Map<String, String> mapped = new TreeMap<String, String>();
        for (String str : pieces) {
            String[] comp = StringUtils.split(str, KV_SEP, 2);
            if (comp == null) {
                continue;
            }
            String k = comp[0];
            String v = null;
            if (comp.length > 1) {
                v = comp[1];
            }
            mapped.put(k, v);
        }
        return mapped;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.ConfigService#getString(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String getString(String keyPath, String def) {
        String str = cfg.getString(keyPath, def);
        if (StringUtils.isEmpty(str)) {
            return def;
        }
        return str;
    }

}
