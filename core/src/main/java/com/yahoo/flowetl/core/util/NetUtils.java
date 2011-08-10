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
package com.yahoo.flowetl.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Provides useful network functions.
 * 
 * @author harlowja
 */
public class NetUtils
{
    private NetUtils() {
        // a util class
    }

    /**
     * Gets the local host name.
     * 
     * @return the local host name (or empty if error)
     */
    public static String getLocalHostName() {
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            return localMachine.getHostName();
        }
        catch (UnknownHostException uhe) {
            return "";
        }
    }

    /**
     * Gets the local address.
     * 
     * @return the local address (or empty if error)
     */
    public static String getLocalAddress() {
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            return localMachine.getHostAddress();
        }
        catch (UnknownHostException uhe) {
            return "";
        }
    }
}
