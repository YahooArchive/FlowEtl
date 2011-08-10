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
package com.yahoo.flowetl.core.services;

import java.util.ArrayList;
import java.util.List;

/**
 * A central lookup location for different services.
 * 
 * @author harlowja
 */
public class ServiceRegistry
{
    /** The services that this registry is aware of. */
    private final List<Service> services;

    /**
     * Instantiates a new service registry.
     */
    public ServiceRegistry() {
        this.services = new ArrayList<Service>();
    }

    /**
     * Registers a given service implementation.
     * 
     * @param s
     */
    public void registerService(Service s) {
        if (s != null) {
            services.add(s);
        }
    }

    /**
     * Gets the service of a given type via the first service that says it can
     * create that type.
     * 
     * @param type
     * 
     * @return the service
     */
    public <T extends Service> T getService(Class<T> type) {
        for (Service s : services) {
            if (s.creates(type)) {
                return type.cast(s.fetch(type));
            }
        }
        return null;
    }

    /**
     * Shutdown all contained services.
     */
    public void shutdownServices() {
        for (Service s : services) {
            s.shutdown();
        }
        services.clear();
    }

}
