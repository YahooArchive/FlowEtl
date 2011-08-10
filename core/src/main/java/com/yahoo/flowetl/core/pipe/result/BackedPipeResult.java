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
package com.yahoo.flowetl.core.pipe.result;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ClassUtils;

import com.yahoo.flowetl.core.pipe.PipeResult;

/**
 * An implementation of a pipe result which uses a map as backing store.
 * 
 * @author harlowja
 */
public class BackedPipeResult implements PipeResult
{
    /** The backing map storage. */
    private final Map<String, Object> backing;

    /**
     * Instantiates a new backed pipe result.
     */
    public BackedPipeResult() {
        backing = new TreeMap<String, Object>();
    }

    /**
     * Instantiates a new backed pipe result with a given list of other pipe
     * results which should be merged into this map. Key names which are the
     * same will be over-ridden with later keys/values replacing newer
     * keys/values.
     * 
     * @param toMerge
     */
    public BackedPipeResult(List<PipeResult> toMerge) {
        this();
        if (toMerge != null) {
            for (int i = 0; i < toMerge.size(); i++) {
                merge(toMerge.get(i));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<String> iterator() {
        return backing.keySet().iterator();
    }

    /**
     * Sets the param with the given name to the given value
     * 
     * @param name
     * @param value
     */
    public void setParam(String name, Object value) {
        backing.put(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.PipeResult#getParam(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getParam(String name) {
        return (T) backing.get(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.pipe.PipeResult#getParamClass(java.lang.String)
     */
    @Override
    public Class<?> getParamClass(String name) {
        Object o = getParam(name);
        if (o == null) {
            return null;
        }
        return o.getClass();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString() + " [backing=");
        builder.append(backing.keySet());
        builder.append("]");
        return builder.toString();
    }

    /**
     * Clears the backing storage.
     */
    public void clear() {
        backing.clear();
    }

    /**
     * Merges the given pipe result into this results backing storage.
     * 
     * @param inResult
     */
    public void merge(PipeResult inResult) {
        if (inResult == null) {
            return;
        }
        if (inResult == this) {
            return;
        }
        Iterator<String> pIt = inResult.iterator();
        if (pIt == null) {
            return;
        }
        while (pIt.hasNext()) {
            String k = pIt.next();
            if (k != null) {
                backing.put(k, inResult.getParam(k));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.pipe.PipeResult#isParamCastable(java.lang.String,
     * java.lang.Class)
     */
    @Override
    public <T> boolean isParamCastable(String name, Class<T> kls) {
        Class<?> pKls = getParamClass(name);
        if (pKls == null) {
            return false;
        }
        if (ClassUtils.isAssignable(pKls, kls) == false) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.pipe.PipeResult#isParamExistent(java.lang.String)
     */
    @Override
    public boolean isParamExistent(String name) {
        return backing.containsKey(name);
    }
}
