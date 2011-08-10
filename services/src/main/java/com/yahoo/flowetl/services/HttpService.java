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
package com.yahoo.flowetl.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;

import com.yahoo.flowetl.core.Logger;
import com.yahoo.flowetl.core.Logger.Level;
import com.yahoo.flowetl.core.services.Service;
import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.core.util.Pair;
import com.yahoo.flowetl.services.http.BaseHttpCaller;
import com.yahoo.flowetl.services.http.BaseHttpGenerator;

/**
 * This is a http service which allows the user to make calls to external http
 * based services by providing a set of input params that define the outgoing
 * call and then getting back a result that represents that calls status and
 * response.
 * 
 * @author Joshua Harlow
 */
public class HttpService implements Service
{
    // logging
    private static final Logger logger = new Logger(HttpService.class);

    // this object will actually make the corresponding objects to call upon
    private final HttpGenerator generator;

    // this object will take generated objects and give back a response
    private final HttpCaller caller;

    /**
     * This class represents what typically composes a http parameter set.
     */
    public static abstract class HttpParams
    {
        public HttpParams() {
            this.headers = new TreeMap<String, String>();
            this.connectionTO = 1000;
            this.socketTO = 1000;
            this.retries = 0;
        }

        /** The uri to call. */
        public URI uri;

        /** The headers that may be sent out. */
        public Map<String, String> headers;

        /** The connection timeout. */
        public int connectionTO;

        /** The socket timeout. */
        public int socketTO;

        /** The retries amount to try if failed. */
        public int retries;

        /** The user agent which may override the useragent header if set. */
        public String userAgent;

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString() + " [connectionTO=");
            builder.append(connectionTO);
            builder.append(", headers=");
            builder.append(headers);
            builder.append(", retries=");
            builder.append(retries);
            builder.append(", socketTO=");
            builder.append(socketTO);
            builder.append(", uri=");
            builder.append(uri);
            builder.append(", userAgent=");
            builder.append(userAgent);
            builder.append("]");
            return builder.toString();
        }
    };

    /**
     * This is a http post type, which adds in some additional data
     */
    public static class PostHttpParams extends HttpParams
    {
        // any additional data for this http call
        // ie map for POST or a string for POST or other object (which will be
        // converted to a string via toString)
        public Object additionalData;

        /*
         * (non-Javadoc)
         * 
         * @see com.yahoo.flowetl.services.HttpService.HttpParams#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString() + " [additionalData=");
            builder.append(additionalData);
            builder.append("]");
            return builder.toString();
        }
    }

    /**
     * A market class for http get type since nothing else is needed...
     */
    public static class GetHttpParams extends HttpParams
    {
        // just a marker class
    }

    /**
     * A http call results in the following
     */
    public static class HttpResult
    {

        /** The response body. */
        public String responseBody;

        /** The http status code. */
        public int statusCode;

        /** The returned headers. */
        public Map<String, String> headers;

        /** The source params. */
        public HttpParams sourceParams;
    }

    /**
     * A class that generates a http client and http method must implement the
     * following.
     */
    public static interface HttpGenerator
    {
        // returns a valid pair (can be useful for mocking)
        public Pair<HttpClient, HttpMethod> generate(HttpParams in);
    }

    /**
     * This represents a interface that takes the generated http client and http
     * method and executes it with the given amount of retries. The http method
     * itself contains the result so no return is needed.
     */
    public static interface HttpCaller
    {
        public void execute(HttpClient hc, HttpMethod hm, int maxRetry) throws IOException;
    }

    /**
     * Instantiates a new http service.
     */
    public HttpService(ServiceRegistry reg) {
        this(new BaseHttpGenerator(reg), new BaseHttpCaller(reg));
    }

    /**
     * Instantiates a new http service (useful for mocking).
     * 
     * @param generator
     * @param caller
     */
    public HttpService(HttpGenerator generator, HttpCaller caller) {
        this.generator = generator;
        this.caller = caller;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#shutdown()
     */
    public void shutdown() {
        // doesn't shutdown anything...
    }

    /**
     * Calls the given http params and returns a result object.
     * 
     * @param params
     * 
     * @return the http result
     */
    public HttpResult call(HttpParams params) {
        Pair<HttpClient, HttpMethod> clientMet = generator.generate(params);
        HttpClient client = clientMet.getFirst();
        HttpMethod toCall = clientMet.getSecond();
        HttpResult out = new HttpResult();
        out.statusCode = -1;
        out.sourceParams = params;
        InputStream is = null;
        try {
            if (logger.isEnabled(Level.INFO)) {
                logger.log(Level.INFO, "Running http method " + toCall + " with params " + params);
            }
            caller.execute(client, toCall, params.retries);
            is = toCall.getResponseBodyAsStream();
            String responseBody = IOUtils.toString(is);
            int st = toCall.getStatusCode();
            Header[] hv = toCall.getResponseHeaders();
            // copy over
            out.statusCode = st;
            out.responseBody = responseBody;
            Map<String, String> headersIn = new TreeMap<String, String>();
            if (hv != null) {
                for (Header h : hv) {
                    headersIn.put(h.getName(), h.getValue());
                }
            }
            out.headers = headersIn;
        }
        catch (HttpException e) {
            if (logger.isEnabled(Level.WARN)) {
                logger.log(Level.WARN, e, "Failed calling " + toCall);
            }
        }
        catch (IOException e) {
            if (logger.isEnabled(Level.WARN)) {
                logger.log(Level.WARN, e, "Failed calling " + toCall);
            }
        }
        finally {
            IOUtils.closeQuietly(is);
            toCall.releaseConnection();
        }
        return out;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#creates(java.lang.Class)
     */
    public boolean creates(Class<? extends Service> c) {
        if (HttpService.class.equals(c)) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.services.Service#fetch(java.lang.Class)
     */
    public Service fetch(Class<? extends Service> c) {
        return this;
    }

}
