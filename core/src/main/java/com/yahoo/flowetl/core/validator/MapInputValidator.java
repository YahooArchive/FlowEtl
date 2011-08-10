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
package com.yahoo.flowetl.core.validator;

import java.util.Map;
import java.util.Map.Entry;

import com.yahoo.flowetl.core.CoreException;
import com.yahoo.flowetl.core.InputValidator;
import com.yahoo.flowetl.core.pipe.PipeResult;

/**
 * A input validation class for pipes to use that takes a map of required params
 * and there types and a map of optional params and there types and checks the
 * input that you give for having the required param and the correct type and
 * the optional params (if they exist) for being the right type.
 * 
 * @author harlowja
 */
public class MapInputValidator implements InputValidator
{
    /** The required map. */
    private final Map<String, Class<?>> required;

    /** The optional map. */
    private final Map<String, Class<?>> optional;

    /**
     * Instantiates a new map input validator.
     * 
     * @param required
     * @param optional
     */
    public MapInputValidator(Map<String, Class<?>> required, Map<String, Class<?>> optional) {
        this.required = required;
        this.optional = optional;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.InputValidator#checkInput(com.yahoo.flowetl.core
     * .pipe.PipeResult)
     */
    @Override
    public void checkInput(PipeResult in) {
        if (in == null) {
            return;
        }
        if (required != null) {
            for (Entry<String, Class<?>> e : required.entrySet()) {
                if (in.isParamExistent(e.getKey()) == false) {
                    throw new CoreException("The param " + e.getKey() + " is a required but it is missing");
                }
                Object o = in.getParam(e.getKey());
                if (o == null) {
                    throw new CoreException("The param " + e.getKey() + " is a required but it is null");
                }
                if (in.isParamCastable(e.getKey(), e.getValue()) == false) {
                    throw new CoreException("The param " + e.getKey() + " has a value which is not castable to " + e.getValue());
                }
            }
        }
        if (optional != null) {
            for (Entry<String, Class<?>> e : optional.entrySet()) {
                if (in.isParamExistent(e.getKey()) == false) {
                    // ok, its optional
                    continue;
                }
                if (in.getParam(e.getKey()) == null) {
                    // ok, its optional
                    continue;
                }
                if (in.isParamCastable(e.getKey(), e.getValue()) == false) {
                    throw new CoreException("The param " + e.getKey() + " has a value which is not castable to " + e.getValue());
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString() + " [optional=");
        builder.append(optional);
        builder.append(", required=");
        builder.append(required);
        builder.append("]");
        return builder.toString();
    }

}