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
package com.yahoo.flowetl.services.tests.mocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;

import com.yahoo.flowetl.core.util.Pair;
import com.yahoo.flowetl.services.HttpService;

//an example mock http service
public class MockHttpService extends HttpService
{
    public static final String FETCH_HEADER = "X-Fetch-Test-Data";

    private static class MockGenerator implements HttpGenerator
    {
        private final Map<String, Pair<String, Integer>> outputMappings;

        public MockGenerator(Map<String, Pair<String, Integer>> outputMappings) {
            this.outputMappings = outputMappings;
        }

        public Pair<HttpClient, HttpMethod> generate(HttpParams in) {
            HttpMethod out = new HttpMethodBase()
            {
                private String identifyOutput() {
                    // lookup this header in the outgoing request headers
                    // and use it to identify which response to send back...
                    Header kv = getRequestHeader(FETCH_HEADER);
                    if (kv == null) {
                        return null;
                    }
                    return kv.getValue();
                }

                @Override
                public String getName() {
                    return "MockHttpMethod";
                }

                @Override
                public InputStream getResponseBodyAsStream() {
                    String which = identifyOutput();
                    String src = "";
                    if (which != null) {
                        src = outputMappings.get(which).getFirst();
                    }
                    return new ByteArrayInputStream(src.getBytes());
                }

                @Override
                public String getResponseBodyAsString() {
                    String which = identifyOutput();
                    if (which == null) {
                        return "";
                    }
                    return outputMappings.get(which).getFirst();
                }

                @Override
                public long getResponseContentLength() {
                    String which = identifyOutput();
                    if (which == null) {
                        return -1;
                    }
                    return outputMappings.get(which).getFirst().length();
                }

                @Override
                public String getResponseBodyAsString(int maxlen) {
                    String str = getResponseBodyAsString();
                    if (str == null) {
                        return "";
                    }
                    return str.substring(0, maxlen);
                }

                @Override
                public int getStatusCode() {
                    String which = identifyOutput();
                    if (which == null) {
                        return -1;
                    }
                    return outputMappings.get(which).getSecond();
                }
            };
            return new Pair<HttpClient, HttpMethod>(null, out);
        }
    }

    private static class MockHttpCaller implements HttpCaller
    {
        @Override
        public void execute(HttpClient hc, HttpMethod hm, int maxRetry) throws IOException {
            // doesn't need to do anything...
            return;
        }
    }

    public MockHttpService(Map<String, Pair<String, Integer>> outputMapping) {
        super(new MockGenerator(outputMapping), new MockHttpCaller());
    }

}
