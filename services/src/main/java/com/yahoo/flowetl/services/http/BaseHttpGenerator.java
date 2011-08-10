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
package com.yahoo.flowetl.services.http;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;

import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.core.util.Pair;
import com.yahoo.flowetl.services.ConfigService;
import com.yahoo.flowetl.services.HttpService;
import com.yahoo.flowetl.services.HttpService.HttpGenerator;
import com.yahoo.flowetl.services.HttpService.HttpParams;
import com.yahoo.flowetl.services.HttpService.PostHttpParams;
import com.yahoo.flowetl.services.config.DummyConfigService;

/**
 * This class handles translating our http params into a pair of apache http
 * client objects that can be used to make the actual http call.
 * 
 * @author Joshua Harlow
 */
public class BaseHttpGenerator implements HttpGenerator
{
    // default use agent if non given
    private static final String DEF_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)";

    // default outgoing data charset
    private static final String DEF_CHARSET = "UTF-8";

    // user agent header to check if someone else set it already
    private static final String USER_AGENT_HEADER = "User-Agent";

    // config key name to see if config has a user agent we should use
    private static final String CFG_USER_AGENT = "http.useragent";

    // useful variables :-P
    private final ServiceRegistry serviceRegistry;
    private final String userAgent;

    /**
     * Instantiates a new base http generator.
     * 
     * @param reg
     *            the reg
     */
    public BaseHttpGenerator(ServiceRegistry reg) {
        this.serviceRegistry = reg;
        ConfigService cfg = reg.getService(ConfigService.class);
        if (cfg == null) {
            cfg = new DummyConfigService();
        }
        String ua = cfg.getString(CFG_USER_AGENT, DEF_USER_AGENT);
        userAgent = ua;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.services.HttpService.HttpGenerator#generate(com.yahoo
     * .flowetl.services.HttpService.HttpParams)
     */
    @Override
    public Pair<HttpClient, HttpMethod> generate(HttpParams in) {
        HttpClient c = new HttpClient();
        c.getState().clear();
        c.getHttpConnectionManager().setParams(getManagerParams(in));
        c.setParams(getClientParams(in));
        HttpMethod toCall = null;
        if (in instanceof HttpService.PostHttpParams) {
            // post??
            toCall = getPostMethod(in);
        }
        else {
            // otherwise get (support more later??)
            toCall = getGetMethod(in);
        }
        applyCommonProperties(in, toCall);
        return new Pair<HttpClient, HttpMethod>(c, toCall);
    }

    /**
     * Applies any common properties to the http method based on properties of
     * the http params given.
     */
    protected void applyCommonProperties(HttpParams in, HttpMethod toCall) {
        // common ops
        if (in.headers != null) {
            for (Entry<String, String> e : in.headers.entrySet()) {
                toCall.addRequestHeader(e.getKey(), e.getValue());
            }
        }
        // we handle our own retries
        toCall.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        if (StringUtils.isBlank(in.userAgent) == false && toCall.getRequestHeader(USER_AGENT_HEADER) == null) {
            toCall.setRequestHeader(USER_AGENT_HEADER, in.userAgent);
        }
        else if (toCall.getRequestHeader(USER_AGENT_HEADER) == null) {
            toCall.setRequestHeader(USER_AGENT_HEADER, userAgent);
        }
    }

    /**
     * Translates a http param object into a post method.
     * 
     * @return the post method
     */
    @SuppressWarnings("deprecation")
    protected PostMethod getPostMethod(HttpParams in) {
        PostMethod meth = new PostMethod(String.valueOf(in.uri));
        if (in instanceof PostHttpParams) {
            PostHttpParams pin = (PostHttpParams) in;
            if (pin.additionalData instanceof Map<?, ?>) {
                Map<?, ?> bodyParams = (Map<?, ?>) pin.additionalData;
                NameValuePair[] pieces = new NameValuePair[bodyParams.size()];
                int i = 0;
                for (Entry<?, ?> kv : bodyParams.entrySet()) {
                    pieces[i] = new NameValuePair(String.valueOf(kv.getKey()), String.valueOf(kv.getValue()));
                    i++;
                }
                meth.setRequestBody(pieces);
            }
            else if (pin.additionalData instanceof String) {
                meth.setRequestBody((String) pin.additionalData);
            }
            else if (pin.additionalData != null) {
                meth.setRequestBody(String.valueOf(pin.additionalData));
            }
        }
        return meth;
    }

    /**
     * Translates a http params object into a get method object.
     * 
     * @return the get method
     */
    protected GetMethod getGetMethod(HttpParams in) {
        return new GetMethod(String.valueOf(in.uri));
    }

    /**
     * Gets the http connection manager params.
     * 
     * @return the manager params
     */
    protected HttpConnectionManagerParams getManagerParams(HttpParams in) {
        HttpConnectionManagerParams out = new HttpConnectionManagerParams();
        out.setConnectionTimeout(in.connectionTO);
        out.setSoTimeout(in.socketTO);
        out.setTcpNoDelay(true);
        return out;
    }

    /**
     * Gets the http client params.
     * 
     * @return the client params
     */
    protected HttpClientParams getClientParams(HttpParams in) {
        HttpClientParams out = new HttpClientParams();
        out.setSoTimeout(in.socketTO);
        out.setContentCharset(DEF_CHARSET);
        out.setUriCharset(DEF_CHARSET);
        out.setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        return out;
    }
}
