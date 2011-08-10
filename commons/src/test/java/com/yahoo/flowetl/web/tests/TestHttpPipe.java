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
package com.yahoo.flowetl.web.tests;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.yahoo.flowetl.commons.web.HttpPipe;
import com.yahoo.flowetl.core.CoreException;
import com.yahoo.flowetl.core.Plumber;
import com.yahoo.flowetl.core.pipe.AbstractPipe;
import com.yahoo.flowetl.core.pipe.PipeResult;
import com.yahoo.flowetl.core.pipe.Pipe.AttachReturn;
import com.yahoo.flowetl.core.pipe.example.CapturePipe;
import com.yahoo.flowetl.core.pipe.example.VoidPipe;
import com.yahoo.flowetl.core.pipe.result.BackedPipeResult;
import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.services.HttpService;

@Test
public class TestHttpPipe
{
    private static class UriPipe extends AbstractPipe
    {
        private static String[] uriList = new String[] { "http://www.yahoo.com", "http://www.google.com" };
        private Random rand = new Random();

        public UriPipe(String name, ServiceRegistry services) {
            super(name, services);
        }

        @Override
        protected PipeResult makeOutput(List<PipeResult> inputs) {
            BackedPipeResult p = new BackedPipeResult();
            String url = uriList[rand.nextInt(uriList.length)];
            try {
                URI made = new URI(url); 
                p.setParam(HttpPipe.IN_URI, made);
                p.setParam(HttpPipe.IN_TIMEOUT_MS, new Integer(10000));
                return p;
            } 
            catch (URISyntaxException e) {
                throw new CoreException("Couldn't make uri from " + url, e);
            }
        }
    }

    private static class AnyPipe extends AbstractPipe
    {
        private String k;
        private Object v;

        public AnyPipe(String name, ServiceRegistry services, String k, Object v) {
            super(name, services);
            this.k = k;
            this.v = v;
        }

        @Override
        protected PipeResult makeOutput(List<PipeResult> collectedInputs) {
            BackedPipeResult out = new BackedPipeResult();
            out.setParam(k, v);
            return out;
        }

    }

    @Test
    public void testDualPipe() throws Exception {
        ServiceRegistry reg = new ServiceRegistry();
        reg.registerService(new HttpService(reg));
        
        VoidPipe root = new VoidPipe("root", reg);
        AnyPipe agentP = new AnyPipe("user_agent_pipe", reg, HttpPipe.IN_USER_AGENT, "blah");
        UriPipe uriP = new UriPipe("uridecider_pipe", reg);
        
        root.attachOutput(uriP);
        
        HttpPipe httpP = new HttpPipe("http_pipe", reg);
        
        agentP.attachOutput(httpP);
        uriP.attachOutput(httpP);
        
        httpP.attachOutput(new CapturePipe("end", reg));
        
        Plumber runner = new Plumber();
        Runnable r = runner.translate(root);
        r.run();
    }

    @Test
    public void testSmartHttpPipe() throws Exception {
        ServiceRegistry reg = new ServiceRegistry();
        reg.registerService(new HttpService(reg));
        UriPipe start = new UriPipe("uriPiper", reg);
        CapturePipe end = new CapturePipe("end", reg);
        start.attachOutput(new HttpPipe("piper", reg), AttachReturn.NEXT).attachOutput(end);
        Plumber runner = new Plumber();
        Runnable r = runner.translate(start);
        r.run();
        PipeResult endVal = end.getCaptured();
        Assert.assertNotNull(endVal);
        Assert.assertNotNull(endVal.getParam(HttpPipe.OUT_RESPONSE_BODY));
    }

    @Test
    public void testHttpPipe() throws Exception {
        ServiceRegistry reg = new ServiceRegistry();
        reg.registerService(new HttpService(reg));
        HttpPipe p = new HttpPipe("piper", reg);
        CapturePipe end = new CapturePipe("blah", reg);
        p.attachOutput(end);

        BackedPipeResult startVal = new BackedPipeResult();
        startVal.setParam(HttpPipe.IN_URI, new URI("http://www.yahoo.com"));
        startVal.setParam(HttpPipe.IN_TIMEOUT_MS, new Integer(10000));
        Plumber runner = new Plumber();
        p.attachInput(startVal);
        Runnable r = runner.translate(p);
        r.run();

        PipeResult endVal = end.getCaptured();
        Assert.assertNotNull(endVal);
        Assert.assertNotNull(endVal.getParam(HttpPipe.OUT_RESPONSE_BODY));
    }
}
