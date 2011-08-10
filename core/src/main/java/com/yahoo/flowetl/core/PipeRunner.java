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
package com.yahoo.flowetl.core;

import java.util.ArrayList;
import java.util.List;

import com.yahoo.flowetl.core.listener.FlowListener;
import com.yahoo.flowetl.core.pipe.Pipe;
import com.yahoo.flowetl.core.pipe.PipeResult;

/**
 * This class implements what a set of pipes that are to be ran will derive
 * from, it has a concept of a listener who can and will be notified of various
 * events that the running derivative decides are relevant.
 * 
 * @author Joshua Harlow
 */
public abstract class PipeRunner implements Runnable
{
    /** The listeners that we have. */
    private List<FlowListener> listeners;

    /**
     * Instantiates a new pipe runner.
     */
    public PipeRunner() {
        this.listeners = new ArrayList<FlowListener>();
    }

    /**
     * Adds the flow listener.
     * 
     * @param listener
     */
    public void addFlowListener(FlowListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the flow listener.
     * 
     * @param listener
     */
    public void removeFlowListener(FlowListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that we are starting with a given ordering.
     * 
     * @param ordering
     */
    protected void notifyStart(List<Pipe> ordering) {
        for (FlowListener f : listeners) {
            f.onStart(ordering);
        }
    }

    /**
     * Notifies all listeners of completion with the given time for how long we
     * took to complete all pipes.
     * 
     * @param timeTakenMs
     */
    protected void notifyComplete(long timeTakenMs) {
        for (FlowListener f : listeners) {
            f.onCompletion(timeTakenMs);
        }
    }

    /**
     * Notifies listeners that we are bout to start running the given pipe.
     * 
     * @param startingPipe
     */
    protected void notifyStartGenerate(Pipe startingPipe) {
        for (FlowListener f : listeners) {
            f.onStartGenerate(startingPipe);
        }
    }

    /**
     * Notifies the listeners that we have finished the given pipe in the given
     * amount of time in milliseconds with the given generated output from that
     * pipe.
     * 
     * @param finishedPipe
     * @param pipeResult
     * @param timeTakenMs
     */
    protected void notifyFinishGenerate(Pipe finishedPipe, PipeResult pipeResult, long timeTakenMs) {
        for (FlowListener f : listeners) {
            f.onFinishGenerate(finishedPipe, pipeResult, timeTakenMs);
        }
    }
}
