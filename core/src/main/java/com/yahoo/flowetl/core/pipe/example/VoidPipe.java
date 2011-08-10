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
package com.yahoo.flowetl.core.pipe.example;

import java.util.List;

import com.yahoo.flowetl.core.pipe.AbstractPipe;
import com.yahoo.flowetl.core.pipe.PipeResult;
import com.yahoo.flowetl.core.services.ServiceRegistry;

/**
 * A simple example pipe which will do nothing with its inputs and return null
 * for its output.
 * 
 * @author Joshua Harlow
 */
public class VoidPipe extends AbstractPipe
{
    /**
     * Instantiates a new void pipe.
     * 
     * @param name
     * @param services
     */
    public VoidPipe(String name, ServiceRegistry services) {
        super(name, services);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.AbstractPipe#makeOutput(java.util.List)
     */
    @Override
    protected PipeResult makeOutput(List<PipeResult> inputs) {
        return null;
    }
}
