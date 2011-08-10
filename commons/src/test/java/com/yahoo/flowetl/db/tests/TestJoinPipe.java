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
package com.yahoo.flowetl.db.tests;

import java.sql.ResultSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.flowetl.commons.db.Field;
import com.yahoo.flowetl.commons.db.JoinPipe;
import com.yahoo.flowetl.commons.db.JoinPipe.Join;
import com.yahoo.flowetl.core.pipe.result.BackedPipeResult;
import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.services.db.CachingDatabaseService;

@Test
public class TestJoinPipe
{
    @Test
    public void testJoinSimple() {
        ServiceRegistry reg = new ServiceRegistry();
        reg.registerService(new CachingDatabaseService());
        final StringBuilder capture = new StringBuilder();
        JoinPipe p = new JoinPipe("b", reg)
        {
            @Override
            protected ResultSet executeJoin(String dsn, String sql) {
                // override for test
                capture.append(sql);
                return null;
            }
        };
        Join j = new Join("bob", "bbtable");
        j.addField(new Field("j"));
        j.addJoin("blah", new Join("bb", "bbtable2"));
        String dsn = "blah";
        BackedPipeResult in = new BackedPipeResult();
        in.setParam(JoinPipe.IN_DSN, dsn);
        in.setParam(JoinPipe.IN_JOIN, j);
        p.attachInput(in);
        p.generateOutput();
        String sql = capture.toString();
        Assert.assertTrue(sql.length() != 0);
    }
}
