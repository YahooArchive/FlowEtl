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

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringUtils;

import com.yahoo.flowetl.core.Logger;
import com.yahoo.flowetl.core.Logger.Level;
import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.services.ConfigService;
import com.yahoo.flowetl.services.HttpService.HttpCaller;
import com.yahoo.flowetl.services.config.DummyConfigService;

/**
 * This class handles executing a http call, and attempting up to X redirects
 * and attempting to retry a connection Y times, incase it fails or times out...
 * 
 * @author Joshua Harlow
 */
public class BaseHttpCaller implements HttpCaller
{
    /** The header which should have the redirect location */
    private static final String REDIR_HEADER = "Location";

    /** Set of redirect codes for easy checking */
    private static final Set<Integer> REDIR_CODES = new TreeSet<Integer>();
    static {
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
        REDIR_CODES.add(301);
        REDIR_CODES.add(302);
        REDIR_CODES.add(303);
        REDIR_CODES.add(307);
    }

    private static final Logger logger = new Logger(BaseHttpCaller.class);

    // config constants
    private static final String CFG_REDIR = "http.maxredir";

    // max amount of times we will attempt redirecting... 
    private final int maxRedirAm;

    // max redir am >= 1 <=0 for no limit
    public BaseHttpCaller(int maxRedirAttempts) {
        this.maxRedirAm = maxRedirAttempts <= 0 ? Integer.MAX_VALUE : maxRedirAttempts;
    }

    // figures the redirect amount from config
    public BaseHttpCaller(ServiceRegistry reg) {
        int maxRedir = 0;
        ConfigService cfg = reg.getService(ConfigService.class);
        if (cfg == null) {
            cfg = new DummyConfigService();
        }
        maxRedir = cfg.getInteger(CFG_REDIR, -1);
        if (maxRedir <= 0) {
            maxRedir = Integer.MAX_VALUE;
        }
        this.maxRedirAm = maxRedir;
    }

    /**
     * Attempts to call the given method using the given client and will attempt
     * this repeatedly up to the max redirect amount. If no redirect location is
     * found a http exception will be propagated upwards. Otherwise for
     * non-redirect codes this method will stop. This function is recursively
     * called.
     * 
     * @throws HttpException
     * @throws IOException
     */
    private void handleRedirects(final HttpClient client, final HttpMethod method, final int curRedirAm, final int maxRedirAm) throws HttpException,
            IOException {
        if (logger.isEnabled(Level.DEBUG)) {
            logger.log(Level.DEBUG, "Executing " + method + " redir count = " + curRedirAm + " of " + maxRedirAm + " possible redirects ");
        }
        // exec and see what happened
        client.executeMethod(method);
        int code = method.getStatusCode();
        if (logger.isEnabled(Level.DEBUG)) {
            logger.log(Level.DEBUG, "Executing " + method + " got status code " + code + "");
        }
        // supposed redirect codes
        // everything else will just stop this function
        if (REDIR_CODES.contains(code) == false) {
            return;
        }
        // die or continue?
        if (curRedirAm < maxRedirAm) {
            // ok to try to find it
            Header locationHeader = method.getResponseHeader(REDIR_HEADER);
            String redirLoc = null;
            if (locationHeader != null) {
                redirLoc = locationHeader.getValue();
            }
            // cleanup and see if we can use it...
            redirLoc = StringUtils.trim(redirLoc);
            if (StringUtils.isEmpty(redirLoc) == false) {
                // reset uri
                URI nUri = new URI(redirLoc, false);
                method.setURI(nUri);
                if (logger.isEnabled(Level.DEBUG)) {
                    logger.log(Level.DEBUG, "Attempting redirect " + (curRedirAm + 1) + " due to status code " + code + " to location " + nUri);
                }
                handleRedirects(client, method, curRedirAm + 1, maxRedirAm);
            }
            else {
                // failure at finding header
                throw new HttpException("Unable to execute " + method + " - no " + REDIR_HEADER + " header found to redirect to during redirect "
                        + curRedirAm);
            }
        }
        else {
            // max redirects done
            throw new HttpException("Unable to execute " + method + " after attempting " + curRedirAm + " redirects of " + maxRedirAm + " attempts");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.services.HttpService.HttpCaller#execute(org.apache.
     * commons.httpclient.HttpClient, org.apache.commons.httpclient.HttpMethod,
     * int)
     */
    @Override
    public void execute(HttpClient hc, HttpMethod hm, int maxRetryAm) throws IOException {
        if (maxRetryAm <= 0) {
            maxRetryAm = Integer.MAX_VALUE;
        }
        int curAttempt = 0;
        while (true) {
            try {
                if (logger.isEnabled(Level.DEBUG)) {
                    logger.log(Level.DEBUG, "Attempting to call " + hm + " for attempt " + (curAttempt + 1) + " of " + maxRetryAm);
                }
                handleRedirects(hc, hm, 0, maxRedirAm);
                // it worked if we got here..
                if (logger.isEnabled(Level.DEBUG)) {
                    logger.log(Level.DEBUG, "Attempting to call " + hm + " worked at attempt " + (curAttempt + 1) + " of " + maxRetryAm);
                }
                // stop
                break;
            }
            catch (IOException e) {
                if (logger.isEnabled(Level.WARN)) {
                    logger.log(Level.WARN, e, "Failed calling " + hm + " during attempt " + (curAttempt + 1) + " of " + maxRetryAm);
                }
                if ((curAttempt + 1) < maxRetryAm) {
                    curAttempt += 1;
                }
                else {
                    // retries ran out...
                    throw e;
                }
            }
        }
    }
}
