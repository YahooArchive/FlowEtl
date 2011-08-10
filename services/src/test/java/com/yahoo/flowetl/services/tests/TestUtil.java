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
package com.yahoo.flowetl.services.tests;

import java.net.URL;

public class TestUtil
{
    private TestUtil() {

    }

    public static URL getResource(String id) {
        URL res = TestUtil.class.getClassLoader().getResource(id);
        return res;
    }
}
