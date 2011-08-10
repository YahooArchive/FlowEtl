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
package com.yahoo.flowetl.commons.web;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

import com.yahoo.flowetl.core.CoreException;
import com.yahoo.flowetl.core.InputValidator;
import com.yahoo.flowetl.core.pipe.AbstractPipe;
import com.yahoo.flowetl.core.pipe.PipeResult;
import com.yahoo.flowetl.core.pipe.result.BackedPipeResult;
import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.core.validator.MapInputValidator;
import com.yahoo.flowetl.services.HttpService;
import com.yahoo.flowetl.services.HttpService.GetHttpParams;
import com.yahoo.flowetl.services.HttpService.HttpParams;
import com.yahoo.flowetl.services.HttpService.HttpResult;
import com.yahoo.flowetl.services.HttpService.PostHttpParams;

/**
 * This is a pipe which can take some http params as input and give a http
 * result as output. It can be used by other classes if they find it useable, or
 * other classes can use the http service directly as they choose.
 * 
 * @author Joshua Harlow
 */
public class HttpPipe extends AbstractPipe
{
    // innies required
    public static final String IN_URI = makeParamName(HttpPipe.class, "uri", true);
    private static final Map<String, Class<?>> REQUIRED_PARAMS = new TreeMap<String, Class<?>>();
    static {
        REQUIRED_PARAMS.put(IN_URI, URI.class);
    }

    // innies optional
    public static final String IN_METHOD = makeParamName(HttpPipe.class, "method", true);
    public static final String IN_TIMEOUT_MS = makeParamName(HttpPipe.class, "in-timeout-ms", true);
    public static final String IN_POST_DATA = makeParamName(HttpPipe.class, "in-post-data", true);
    public static final String IN_USER_AGENT = makeParamName(HttpPipe.class, "in-useragent", true);
    private static final Map<String, Class<?>> OPTIONAL_PARAMS = new TreeMap<String, Class<?>>();
    static {
        OPTIONAL_PARAMS.put(IN_METHOD, String.class);
        OPTIONAL_PARAMS.put(IN_TIMEOUT_MS, Integer.class);
        OPTIONAL_PARAMS.put(IN_POST_DATA, String.class);
        OPTIONAL_PARAMS.put(IN_USER_AGENT, String.class);
    }

    // outties
    public static final String OUT_STATUS_CODE = makeParamName(HttpPipe.class, "statusCode", false);
    public static final String OUT_RESPONSE_BODY = makeParamName(HttpPipe.class, "responseBody", false);
    public static final String OUT_RESPONSE_HEADERS = makeParamName(HttpPipe.class, "responseHeaders", false);

    // the output result class
    // that allows u to extract the result set without doing anything special
    public static class Result extends BackedPipeResult
    {
        public Result() {
            super();
        }

        public Integer getStatusCode() {
            return getParam(OUT_STATUS_CODE);
        }

        public String getResponseBody() {
            return getParam(OUT_RESPONSE_BODY);
        }

        public Map<String, String> getResponseHeaders() {
            return getParam(OUT_RESPONSE_HEADERS);
        }
    }

    /** The input validation checker. */
    private final InputValidator inputChecker;

    // required http service
    private final HttpService httpService;

    /**
     * Instantiates a new http pipe.
     * 
     * @param name
     * @param services
     */
    public HttpPipe(String name, ServiceRegistry services) {
        super(name, services);
        httpService = services.getService(HttpService.class);
        if (httpService == null) {
            throw new CoreException("No " + HttpService.class + " service found - required");
        }
        // make input checker
        inputChecker = new MapInputValidator(REQUIRED_PARAMS, OPTIONAL_PARAMS);
    }

    /**
     * Makes the http output for the given input (already validated).
     * 
     * @param input
     * 
     * @return the http pipe result
     */
    private PipeResult makeOutput(PipeResult input) {
        // services already existent
        HttpService sv = httpService;
        // extract params and/or default them
        URI uri = input.getParam(IN_URI);
        String inMethod = input.getParam(IN_METHOD);
        HttpParams p = null;
        if (StringUtils.equalsIgnoreCase(inMethod, "POST")) {
            Object addData = input.getParam(IN_POST_DATA);
            PostHttpParams tmp = new PostHttpParams();
            tmp.additionalData = addData;
            p = tmp;
        }
        else {
            p = new GetHttpParams();
        }
        Integer soTout = input.getParam(IN_TIMEOUT_MS);
        p.uri = uri;
        p.userAgent = input.getParam(IN_USER_AGENT);
        if (soTout != null && soTout > 0) {
            p.socketTO = soTout.intValue();
        }
        HttpResult res = sv.call(p);
        Result out = new Result();
        out.setParam(OUT_STATUS_CODE, res.statusCode);
        out.setParam(OUT_RESPONSE_BODY, res.responseBody);
        out.setParam(OUT_RESPONSE_HEADERS, res.headers);
        return out;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.AbstractPipe#makeOutput(java.util.List)
     */
    @Override
    protected PipeResult makeOutput(List<PipeResult> inputs) {
        BackedPipeResult merged = new BackedPipeResult(inputs);
        inputChecker.checkInput(merged);
        return makeOutput(merged);
    }

}
