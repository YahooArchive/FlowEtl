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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;

/**
 * Provides useful regex functions.
 * 
 * @author harlowja
 */
public class RegexUtil
{

    /** The regex pattern extractor. */
    private static final Pattern patExtractor = Pattern.compile("/(.*?)/([\\w]*)", Pattern.UNICODE_CASE);

    /** A cache of previously created patterns */
    private static final Map<String, Pattern> compiledPats = new ConcurrentHashMap<String, Pattern>();

    private RegexUtil() {
        // a util class...
    }

    /**
     * Gets the pattern for the given string by providing the rules to do
     * extraction. This turns on caching. Ensure that you are ok with caching
     * since this could eat up memory.
     */
    public static Pattern getPattern(String str) throws PatternSyntaxException {
        return getPattern(str, true);
    }

    /**
     * Gets the pattern for the given string by providing the rules to do
     * extraction.
     * 
     * This is similar to how php does regex to match you provide in the format
     * /REGEX/options where options currently are "i" for case insensitive and
     * "u" for unicode and "m" for multiline and "s" for dotall and the value
     * inside the // is the regex to use
     * 
     * @param str
     *            the string to parsed the pattern out of
     * 
     * @param cache
     *            whether to cache the compiled pattern
     * 
     * @return the pattern
     * 
     * @throws PatternSyntaxException
     * 
     *             the pattern syntax exception if it has wrong syntax
     */
    public static Pattern getPattern(String str, boolean cache) throws PatternSyntaxException {
        if (str == null) {
            return null;
        }
        // see if we made it before...
        Pattern p = compiledPats.get(str);
        if (p != null) {
            return p;
        }
        Matcher mat = patExtractor.matcher(str);
        if (mat.matches() == false) {
            throw new PatternSyntaxException("Invalid syntax provided", str, -1);
        }
        String regex = mat.group(1);
        String opts = mat.group(2);
        int optsVal = 0;
        if (StringUtils.contains(opts, "i")) {
            optsVal |= Pattern.CASE_INSENSITIVE;
        }
        if (StringUtils.contains(opts, "u")) {
            optsVal |= Pattern.UNICODE_CASE;
        }
        if (StringUtils.contains(opts, "m")) {
            optsVal |= Pattern.MULTILINE;
        }
        if (StringUtils.contains(opts, "s")) {
            optsVal |= Pattern.DOTALL;
        }
        // compile and store it
        p = Pattern.compile(regex, optsVal);
        if (cache) {
            compiledPats.put(str, p);
        }
        return p;
    }
}
