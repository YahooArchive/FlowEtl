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

import java.util.ArrayList;
import java.util.List;

import com.yahoo.flowetl.core.Logger;
import com.yahoo.flowetl.core.Logger.Level;
import com.yahoo.flowetl.core.services.ServiceRegistry;

/**
 * An implementation of the pipe interface which provides the necessary base
 * components to form a useful pipe from. This aids in getting a pipe going
 * since the key components that will be most useful to most pipes will be
 * already established here.
 * 
 * @author Joshua Harlow
 */
public abstract class AbstractPipe implements Pipe
{
    /** The logger for children to use. */
    private final Logger logger;

    /** The name of this pipe. */
    private final String name;

    /** The services object that we can lookup services from. */
    private final ServiceRegistry services;

    /** The pipes that are dependent on this pipe. */
    private final List<Pipe> nextPipes;

    /** The collected inputs from pipes we are dependent on. */
    private final List<PipeResult> collectedInputs;

    /**
     * The lock that is used for ensuring thread safe read and write access to
     * the percent done variable.
     */
    private final Object percentLock;

    /** The percent done. */
    private float percentDone;

    /**
     * Instantiates a new abstract pipe.
     * 
     * @param name
     *            the name
     * @param services
     *            the services
     */
    public AbstractPipe(String name, ServiceRegistry services) {
        this.name = name;
        this.services = services;
        this.logger = new Logger(getClass());
        this.nextPipes = new ArrayList<Pipe>();
        this.collectedInputs = new ArrayList<PipeResult>();
        this.percentDone = 0.0f;
        this.percentLock = new Object();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.pipe.Pipe#attachOutput(com.yahoo.flowetl.core.
     * pipe.Pipe)
     */
    @Override
    public Pipe attachOutput(Pipe next) {
        return attachOutput(next, AttachReturn.SELF);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.pipe.Pipe#attachOutput(com.yahoo.flowetl.core.
     * pipe.Pipe, com.yahoo.flowetl.core.pipe.Pipe.AttachReturn)
     */
    @Override
    public Pipe attachOutput(Pipe next, AttachReturn retWhat) {
        nextPipes.add(next);
        switch (retWhat) {
        case NEXT:
            return next;
        case SELF:
            return this;
        }
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString() + "[name=" + getName() + "]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.Pipe#clearInputs()
     */
    @Override
    public void clearInputs() {
        collectedInputs.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.pipe.Pipe#attachInput(com.yahoo.flowetl.core.pipe
     * .PipeResult)
     */
    @Override
    public void attachInput(PipeResult in) {
        collectedInputs.add(in);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.Pipe#generateOutput()
     */
    @Override
    public final PipeResult generateOutput() {
        setPercentDone(0.0f);
        PipeResult made = makeOutput(collectedInputs);
        setPercentDone(1.0f);
        return made;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.Pipe#maxOutputs()
     */
    @Override
    public int maxOutputs() {
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.Pipe#percentDone()
     */
    @Override
    public float percentDone() {
        synchronized (percentLock) {
            return percentDone;
        }
    }

    /**
     * Derived classes need to implement this. They will at this point be
     * ensured to have the needed inputs given in the provided list. It should
     * be the policy of each implementation that they ensure the collected
     * inputs are valid for there use case.
     * 
     * @param collectedInputs
     * 
     * @return the pipe result from those inputs
     */
    protected abstract PipeResult makeOutput(List<PipeResult> collectedInputs);

    /**
     * Sets the percent done. Ie how far we are along from 0.0 to 1.0 in
     * completion if such a metric can be generated during creation of outputs.
     * 
     * @param perDone
     */
    protected void setPercentDone(float perDone) {
        if (perDone > 1) {
            perDone = 1;
        }
        if (perDone < 0) {
            perDone = 0;
        }
        synchronized (percentLock) {
            percentDone = perDone;
        }
        logger.log(Level.INFO, "Set percent done to %s", perDone);
    }

    /**
     * Gets the service registry.
     */
    protected ServiceRegistry getServiceRegistry() {
        return services;
    }

    /**
     * Gets the logger.
     */
    protected Logger getLogger() {
        return logger;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.Pipe#getOutputs()
     */
    public List<Pipe> getOutputs() {
        return this.nextPipes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.Pipe#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Makes a param name.
     * 
     * TODO - should we use more of this info to make a better name. Not
     * convinced yet either way since using the owner makes it hard to make a
     * pipe easily connectable to another pipe without knowing who you are
     * connecting to....
     * 
     * @param owner
     * @param id
     * @param input
     *            if it is an input variable or output variable name
     * 
     * @return the string
     */
    protected static String makeParamName(Class<?> owner, String id, boolean input) {
        // right now just use the id
        return id;
    }
}
