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
package com.yahoo.flowetl.services.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.services.HttpService;
import com.yahoo.flowetl.services.config.CompositeConfigService;
import com.yahoo.flowetl.services.db.CachingDatabaseService;
import com.yahoo.flowetl.services.memory.MapMemoryService;

/**
 * A simple factory that can help in setting up a registry for you.
 * 
 * @author Joshua Harlow
 */
public class ServiceFactory
{

    /**
     * Parses the config file and returns a configuration object for it. It
     * attempts to look at the extension of the file and then create the
     * corresponding apache commons configuration reader for that file type.
     * Currently this is supporting a .xml file and a .properties file.
     * 
     * @param configFileName
     * 
     * @return the configuration
     * 
     * @throws ConfigurationException
     */
    protected Configuration parseConfigFile(String configFileName) throws ConfigurationException {
        if (configFileName == null) {
            return null;
        }
        Configuration out = null;
        String ext = FilenameUtils.getExtension(configFileName);
        if (StringUtils.equalsIgnoreCase("xml", ext)) {
            out = new XMLConfiguration(configFileName);
        }
        else if (StringUtils.equalsIgnoreCase("properties", ext)) {
            out = new PropertiesConfiguration(configFileName);
        }
        return out;
    }

    /**
     * Makes a service registry with the necessary components using the given
     * configuration file name.
     * 
     * @param configFileName
     * @return the service registry
     * @throws Exception
     */
    public ServiceRegistry makeRegistry(String configFileName) throws Exception {
        ServiceRegistry reg = new ServiceRegistry();

        // load the config first incase others want to use it...
        List<Configuration> cfgs = new ArrayList<Configuration>();
        Configuration baseConfig = parseConfigFile(configFileName);
        if (baseConfig != null) {
            cfgs.add(baseConfig);
        }
        reg.registerService(new CompositeConfigService(cfgs));

        // add the rest in
        reg.registerService(new CachingDatabaseService());
        reg.registerService(new HttpService(reg));
        reg.registerService(new MapMemoryService());

        return reg;
    }
}
