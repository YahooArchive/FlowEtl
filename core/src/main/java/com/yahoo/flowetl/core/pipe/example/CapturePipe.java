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
import com.yahoo.flowetl.core.pipe.result.BackedPipeResult;
import com.yahoo.flowetl.core.services.ServiceRegistry;

/**
 * A simple example pipe which will capture ever single input given and merge
 * them all into one pipe result and return no output but it provides a function
 * that allows you to fetch the merged input. This is useful for testing or for
 * capturing any output of a given set of pipes (by attaching it as the output
 * of each pipe).
 * 
 * @author Joshua Harlow
 */
public class CapturePipe extends AbstractPipe
{
    /** The input collector pipe that will store all inputs merged. */
    private final BackedPipeResult inputCollector;

    /**
     * Instantiates a new capture pipe.
     * 
     * @param name
     * @param services
     */
    public CapturePipe(String name, ServiceRegistry services) {
        super(name, services);
        inputCollector = new BackedPipeResult();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.AbstractPipe#makeOutput(java.util.List)
     */
    @Override
    protected PipeResult makeOutput(List<PipeResult> inputs) {
        inputCollector.clear();
        for (int i = 0; i < inputs.size(); i++) {
            inputCollector.merge(inputs.get(i));
        }
        return null;
    }

    /**
     * Gets the captured inputs (only useful after the pipe has been ran).
     */
    public PipeResult getCaptured() {
        return inputCollector;
    }

}
