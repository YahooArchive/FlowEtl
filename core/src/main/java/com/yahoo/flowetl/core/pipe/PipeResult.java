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
package com.yahoo.flowetl.core.pipe;

/**
 * This is a abstraction of what the result of a pipe running is composed of.
 * The idea maps to a hash map or a map in general whereby a pipe returns a map
 * of string -> objects. The further in another pipe it can lookup that same
 * string -> object mapping and use what a previous pipe has made.
 * 
 * @author Joshua Harlow
 */
public interface PipeResult extends Iterable<String>
{
    /**
     * Gets the param for a given name (null if not there). Note here that we
     * are using some special java 1.5+ casting auto-magic here to infer from
     * what type you are assigning to this return result what its actual type
     * is. If they do not match, you will get a class cast exception, so its up
     * to the user to ensure that the param can be assigned (via other functions
     * listed here) if they desire to have those checks.
     * 
     * @param name
     * 
     * @return the param
     */
    public <T> T getParam(String name);

    /**
     * Checks if is param is existent, even if its null.
     * 
     * @param name
     * 
     * @return true, if param is existent in this result.
     */
    public boolean isParamExistent(String name);

    /**
     * Checks if is param castable to a given type. This can be used to check
     * the type if you do not like the auto-magic that is happening for you by
     * default in the <code>getParam</code> function.
     * 
     * @param name
     * @param kls
     *            the class to check
     * @return true, if is param is castable to the given class
     */
    public <T> boolean isParamCastable(String name, Class<T> kls);

    /**
     * Gets the class of a given param.
     * 
     * @param name
     * 
     * @return the param class
     */
    public Class<?> getParamClass(String name);
}
