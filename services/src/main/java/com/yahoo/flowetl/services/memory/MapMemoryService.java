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
package com.yahoo.flowetl.services.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yahoo.flowetl.services.MemoryService;

/**
 * A memory service backed by a map.
 * 
 * @author Joshua Harlow
 */
public class MapMemoryService extends MemoryService
{

    /** The objects we are storing. */
    private Map<String, Object> objects;

    /**
     * Instantiates a new map memory service.
     */
    public MapMemoryService() {
        super();
        this.objects = new ConcurrentHashMap<String, Object>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.MemoryService#getObject(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(String key) {
        return (T) objects.get(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.MemoryService#getType(java.lang.String)
     */
    @Override
    public Class<?> getType(String key) {
        Object o = getObject(key);
        if (o == null) {
            return null;
        }
        return o.getClass();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.services.MemoryService#putObject(java.lang.String,
     * java.lang.Object)
     */
    @Override
    public void putObject(String key, Object o) {
        objects.put(key, o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.services.MemoryService#removeObject(java.lang.String)
     */
    @Override
    public void removeObject(String key) {
        objects.remove(key);
    }

}
