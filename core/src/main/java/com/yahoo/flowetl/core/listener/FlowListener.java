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
package com.yahoo.flowetl.core.listener;

import java.util.List;

import com.yahoo.flowetl.core.pipe.Pipe;
import com.yahoo.flowetl.core.pipe.PipeResult;

/**
 * The listener interface for receiving flow events. The class that is
 * interested in processing a flow event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addFlowListener<code> method. When
 * the flow event occurs, that object's appropriate
 * method is invoked.
 */
public interface FlowListener
{

    /**
     * This is called before any pipes have ran but the order in which they will
     * run is known.
     * 
     * @param ordering
     */
    public void onStart(List<Pipe> ordering);

    /**
     * This is called before each pipe is about to run.
     * 
     * @param aboutToRunPipe
     */
    public void onStartGenerate(Pipe aboutToRunPipe);

    /**
     * This is called when the given pipe has been ran with the given result and
     * how long that pipe took to complete its output.
     * 
     * @param ranPipe
     * @param ranResult
     * @param timeTakenMs
     */
    public void onFinishGenerate(Pipe ranPipe, PipeResult ranResult, long timeTakenMs);

    /**
     * This is called when all pipes have completed with the given amount of
     * time it took to complete all the pipes that we were told will be ran.
     * 
     * @param timeTakenMs
     */
    public void onCompletion(long timeTakenMs);

}
