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
package com.yahoo.flowetl.services.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PooledDataSource;
import com.yahoo.flowetl.core.CoreException;
import com.yahoo.flowetl.core.Logger;
import com.yahoo.flowetl.core.Logger.Level;
import com.yahoo.flowetl.services.DatabaseService;

/**
 * A caching database service that keeps connections for a given jndi dsn in a
 * map and uses the c3p0 library to fetch new connections. This library should
 * reconnect if a connection times out so that the map can remain valid
 * throughout your running (even if you run for a long time...).
 * 
 * @see http://www.mchange.com/projects/c3p0/index.html
 * 
 * @author Joshua Harlow
 */
public class CachingDatabaseService extends DatabaseService
{
    // logging....
    private static final Logger logger = new Logger(CachingDatabaseService.class);

    /** The connection cache. */
    private Map<String, DataSource> conCache;

    /**
     * Instantiates a new caching database service.
     */
    public CachingDatabaseService() {
        super();
        this.conCache = new ConcurrentHashMap<String, DataSource>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.services.DatabaseService#getConnection(java.lang.String
     * )
     */
    @Override
    public Connection getConnection(String dbDsn) {
        if (StringUtils.isEmpty(dbDsn)) {
            throw new IllegalArgumentException("Invalid db dsn " + dbDsn);
        }
        logger.log(Level.INFO, "Fetching datasource for dsn %s", dbDsn);
        PooledDataSource dsP = (PooledDataSource) conCache.get(dbDsn);
        if (dsP == null) {
            try {
                dsP = (PooledDataSource) DataSources.pooledDataSource(DataSources.unpooledDataSource(dbDsn));
            }
            catch (SQLException e) {
                throw new CoreException("Unable to get db connection to " + dbDsn + "", e);
            }
            conCache.put(dbDsn, dsP);
        }
        try {
            return dsP.getConnection();
        }
        catch (SQLException e) {
            throw new CoreException("Unable to get db connection to " + dbDsn + "", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#shutdown()
     */
    @Override
    public void shutdown() {
        for (Entry<String, DataSource> e : conCache.entrySet()) {
            try {
                DataSources.destroy(e.getValue());
            }
            catch (SQLException e1) {
                if (logger.isEnabled(Level.WARN)) {
                    logger.log(Level.WARN, "Unable to shutdown connection to db " + e.getKey());
                }
            }
        }
        conCache.clear();
    }

}
