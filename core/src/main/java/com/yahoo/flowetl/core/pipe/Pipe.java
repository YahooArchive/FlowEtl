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

import java.util.List;

/**
 * A pipe is a component that can be connected to other pipes, a pipe should not
 * appear in 2 different places in the same sequence of pipes (a different
 * instance can be). A pipe should implement the minimal needed functionality to
 * be useful for other pipes (but not to verbose, or to simple, but just
 * right...). The definition of just right is up to the developers that are
 * using these pipes.
 * 
 * @author Joshua Harlow
 */
public interface Pipe
{
    /**
     * What to return when attaching (useful for chaining)
     */
    public static enum AttachReturn
    {
        SELF, NEXT
    }

    /**
     * Attach an end to this pipe.
     * 
     * @param next
     *            the next pipe that will run after this pipe (this pipe will
     *            have to provide its outputs as input to that pipe before it
     *            can be ran). Thus you could say this creates a dependency
     *            between the next pipe and this one.
     * 
     * @param retWhat
     * 
     * @return the pipe or the next pipe depending
     */
    public Pipe attachOutput(Pipe next, AttachReturn retWhat);

    /**
     * Attach an end to this pipe. The current pipe is returned for chaining.
     * 
     * @param next
     *            the next pipe that will run after this pipe (this pipe will
     *            have to provide its outputs as input to that pipe before it
     *            can be ran). Thus you could say this creates a dependency
     *            between the next pipe and this one.
     * 
     * @return the current pipe
     */
    public Pipe attachOutput(Pipe next);

    /**
     * Gets the outputs of this pipe.
     * 
     * @return the outputs
     */
    public List<Pipe> getOutputs();

    /**
     * Attaches a produced output from a dependent pipe as an input to this
     * pipe.
     * 
     * @param in
     *            the output of another pipe as input to this pipe.
     */
    public void attachInput(PipeResult in);

    /**
     * Generates the output of the current pipe. At this point it can be ensured
     * that all input pipes have ran and there output has been attached to this
     * pipe.
     * 
     * @return the generated pipe result.
     */
    public PipeResult generateOutput();

    /**
     * Gets the name of this pipe.
     */
    public String getName();

    /**
     * This returns the maximum number of outputs this pipe can have. Some pipes
     * for example a database result set can not really have more than one
     * output since that result set should not be shared among different input
     * pipes.
     * 
     * @return the maximum number of outputs (or -1 for no limit)
     */
    public int maxOutputs();

    /**
     * Clears any previously set inputs.
     */
    public void clearInputs();

    /**
     * Gets the percent (0.0<->1.0 inclusive) that tells others how far this
     * pipe is from finishing its output generation.
     * 
     * This should be thread safe since it might be called externally by other
     * threads.
     * 
     * @return the float
     */
    public float percentDone();

}
