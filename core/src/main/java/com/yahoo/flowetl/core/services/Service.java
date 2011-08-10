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

/**
 * This is mainly a marker interface for some type of service that can provide
 * some type of service (ie http, db connections, ...) for others to use as
 * operations that they should not have to worry about. For example any pipe
 * that wants to use a http client should not have to worry about setting up
 * apache http client and so on, but instead should call into a service that
 * knows how to call into that apache http client library (or other library).
 * 
 * @author harlowja
 */
public interface Service
{
    /**
     * Fetches the service of the given type. Typically since most services will
     * not create other service types this is just the implementation itself but
     * this does leave open the possibility that some other service can create
     * different services as it wishes (say for mocking during testing).
     * 
     * @param serType
     * 
     * @return the service that is that type.
     */
    public Service fetch(Class<? extends Service> serType);

    /**
     * Checks if this service can create a service of the given type.
     * 
     * @param serType
     * 
     * @return true, if this service can make the given service type.
     */
    public boolean creates(Class<? extends Service> serType);

    /**
     * Perform any shutdown of this service as needed, if any.
     */
    public void shutdown();
}
