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
package com.yahoo.flowetl.flow;

import java.util.List;

import org.testng.annotations.Test;

import com.yahoo.flowetl.core.Plumber;
import com.yahoo.flowetl.core.Logger.Level;
import com.yahoo.flowetl.core.pipe.AbstractPipe;
import com.yahoo.flowetl.core.pipe.Pipe;
import com.yahoo.flowetl.core.pipe.PipeResult;
import com.yahoo.flowetl.core.pipe.Pipe.AttachReturn;
import com.yahoo.flowetl.core.services.ServiceRegistry;

@Test
public class TestFlow
{
    private static class NullPipe extends AbstractPipe
    {
        private int id;

        public NullPipe(ServiceRegistry services, int id) {
            super("" + id, services);
            this.id = id;
        }

        @Override
        protected PipeResult makeOutput(List<PipeResult> inputs) {
            getLogger().log(Level.INFO, "Running pipe " + id);
            return null;
        }

    };

    @Test
    public void testSimpleFlow() throws Exception {
        ServiceRegistry sreg = new TestServiceRegistry();
        Pipe start = new NullPipe(sreg, 0);
        start.attachOutput(new NullPipe(sreg, 1), AttachReturn.NEXT).attachOutput(new NullPipe(sreg, 2), AttachReturn.NEXT);
        Plumber runner = new Plumber();
        Runnable r = runner.translate(start);
        r.run();
    }
}
