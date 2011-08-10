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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.jgrapht.DirectedGraph;
import org.jgrapht.EdgeFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.VertexNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.GraphIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import com.yahoo.flowetl.core.Logger.Level;
import com.yahoo.flowetl.core.pipe.Pipe;
import com.yahoo.flowetl.core.pipe.PipeResult;
import com.yahoo.flowetl.core.util.IterUtils;

/**
 * This classes job is to take a set of pipes and form the pipe runner which
 * will be used to execute the pipes in the correct order. It ensures that there
 * are no cycles in your pipes, that every pipe is connected somehow to each
 * other (connectivity).
 * 
 * @author Joshua Harlow
 */
public class Plumber
{
    /** The Constant logger. */
    private static final Logger logger = new Logger(Plumber.class);

    /**
     * Instantiates a new plumber.
     */
    public Plumber() {

    }

    /**
     * This is just an internal class which represents a edge of a pipe
     */
    private static class PipeEdge
    {

        /** The edge name. */
        private final String name;

        /**
         * Instantiates a new pipe edge.
         * 
         * @param name
         *            the edge name
         */
        private PipeEdge(String name) {
            this.name = name;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return getName();
        }

        /**
         * Gets the name.
         * 
         * @return the name of this edge
         */
        public String getName() {
            return name;
        }

    }

    /**
     * Makes a traversal iterator which will be used to iterate over the given
     * graph in a manner which will ensure that all inputs are satisfied before
     * a given pipe can be activated.
     * 
     * @param runGraph
     * 
     * @return the graph iterator
     */
    private GraphIterator<Pipe, PipeEdge> makeTraversalIterator(DirectedGraph<Pipe, PipeEdge> runGraph) {
        return new TopologicalOrderIterator<Pipe, PipeEdge>(runGraph);
    }

    /**
     * Translates a pipe that is a single root into a runnable object.
     * 
     * @param root
     * 
     * @return the pipe runner
     * 
     * @throws PipeException
     */
    public PipeRunner translate(Pipe root) throws PipeException {
        Set<Pipe> pipes = new HashSet<Pipe>();
        if (root != null) {
            pipes.add(root);
        }
        return translate(pipes);
    }

    /**
     * Translates a set of roots into a runnable object.
     * 
     * @param roots
     * 
     * @return the pipe runner
     * 
     * @throws PipeException
     */
    public PipeRunner translate(final Set<Pipe> roots) throws PipeException {

        if (roots == null || roots.isEmpty()) {
            throw new IllegalArgumentException("No valid pipes provided");
        }

        // first translate to a graph
        final DefaultDirectedGraph<Pipe, PipeEdge> runGraph = new DefaultDirectedGraph<Pipe, PipeEdge>(new EdgeFactory<Pipe, PipeEdge>()
        {
            @Override
            public PipeEdge createEdge(Pipe src, Pipe tgt) {
                StringBuilder tmp = new StringBuilder();
                tmp.append("{" + src.getName() + "}");
                tmp.append("->");
                tmp.append("{" + tgt.getName() + "}");
                return new PipeEdge(tmp.toString());
            }
        });

        // find all reachable pipes from the given roots
        final Set<Pipe> reachableInputs = new HashSet<Pipe>();
        Set<Pipe> reachablePipesTmp = new HashSet<Pipe>();
        for (Pipe p : roots) {
            discoverReachable(p, reachablePipesTmp);
            reachableInputs.addAll(reachablePipesTmp);
            reachableInputs.add(p);
            reachablePipesTmp.clear();
        }

        // add as vertexes..
        for (Pipe p : reachableInputs) {
            runGraph.addVertex(p);
        }

        // connect together
        for (Pipe v : reachableInputs) {
            List<Pipe> outs = v.getOutputs();
            if (outs != null) {
                int max = v.maxOutputs();
                int cur = outs.size();
                if (max != -1 && (max < cur)) {
                    throw new PipeException("Pipe " + v + " is only allowed " + max + " outputs but it has " + cur + " outputs");
                }
                for (Pipe t : outs) {
                    if (t == null) {
                        continue;
                    }
                    PipeEdge edgeName = runGraph.addEdge(v, t);
                    if (logger.isEnabled(Level.INFO)) {
                        logger.log(Level.INFO, "Connected " + v + " to " + t + " with edge " + edgeName);
                    }
                }
            }
        }

        // do cycle detection
        CycleDetector<Pipe, PipeEdge> cycleDetect = new CycleDetector<Pipe, PipeEdge>(runGraph);
        Set<Pipe> cycleNodes = cycleDetect.findCycles();
        if (cycleNodes != null && cycleNodes.isEmpty() == false) {
            StringBuilder msg = new StringBuilder("The following pipes are causing cycles [");
            msg.append(StringUtils.join(cycleNodes, ","));
            msg.append("]");
            throw new PipeException(msg.toString());
        }

        // check connected components
        ConnectivityInspector<Pipe, PipeEdge> cInspector = new ConnectivityInspector<Pipe, PipeEdge>(runGraph);
        if (cInspector.isGraphConnected() == false) {
            throw new PipeException("The pipes provided have occurences which do not actually connect to other pipes");
        }

        // display
        if (logger.isEnabled(Level.DEBUG)) {
            StringWriter w = new StringWriter();
            DOTExporter<Pipe, PipeEdge> d = new DOTExporter<Pipe, PipeEdge>(new VertexNameProvider<Pipe>()
            {
                @Override
                public String getVertexName(Pipe p) {
                    return p.getName();
                }
            }, new VertexNameProvider<Pipe>()
            {
                @Override
                public String getVertexName(Pipe p) {
                    return p.getName();
                }
            }, new EdgeNameProvider<PipeEdge>()
            {
                @Override
                public String getEdgeName(PipeEdge e) {
                    return String.valueOf(e);
                }
            });
            d.export(w, runGraph);
            try {
                w.close();
            }
            catch (IOException e1) {
                // should be ok to ignore this...
            }
            logger.log(Level.DEBUG, w.toString());
        }

        // all verified, yippe
        PipeRunner out = new PipeRunner()
        {
            @Override
            public void run() {

                // use topological order to figure out
                // how to run this graph in a way
                // that will ensure the inputs are satisfied
                // before a vertex is ran...
                GraphIterator<Pipe, PipeEdge> it = makeTraversalIterator(runGraph);

                // get the ordering first
                // which doesn't involve activating any of the pipes
                // just seeing what the iteration order will be...
                final List<Pipe> order = IterUtils.toList(it, ArrayList.class);

                // now make the real run iterator
                it = makeTraversalIterator(runGraph);
                it.addTraversalListener(new TraversalListenerAdapter<Pipe, PipeEdge>()
                {
                    @Override
                    public void vertexTraversed(VertexTraversalEvent<Pipe> v) {
                        if (logger.isEnabled(Level.INFO)) {
                            logger.log(Level.INFO, "Vertex " + v.getVertex() + " was visited");
                        }
                    }
                });

                StopWatch overallTimer = new StopWatch();
                overallTimer.start();

                notifyStart(order);

                // keep track of which ones we exec'ed
                // maybe for use later??
                final List<Pipe> curExecd = new ArrayList<Pipe>(order.size());

                // iterate
                StopWatch perRunTimer = new StopWatch();
                List<Pipe> pipeOutputs = null;
                PipeResult pipeRes = null;
                while (it.hasNext()) {
                    Pipe toRun = it.next();
                    perRunTimer.reset();
                    perRunTimer.start();
                    notifyStartGenerate(toRun);
                    {
                        pipeRes = toRun.generateOutput();
                    }
                    perRunTimer.stop();
                    curExecd.add(toRun);
                    pipeOutputs = toRun.getOutputs();
                    if (pipeOutputs != null) {
                        for (Pipe tmp : pipeOutputs) {
                            if (tmp == null) {
                                continue;
                            }
                            tmp.attachInput(pipeRes);
                        }
                    }
                    notifyFinishGenerate(toRun, pipeRes, perRunTimer.getTime());
                    // now clear it
                    toRun.clearInputs();
                }

                overallTimer.stop();
                notifyComplete(overallTimer.getTime());

            }
        };
        return out;
    }

    /**
     * Discovers the reachable pipes from the given root pipe.
     * 
     * @param rootPipe
     * 
     * @param reachablePipes
     *            the currently known reachable pipes
     */
    private void discoverReachable(Pipe rootPipe, Set<Pipe> reachablePipes) {
        if (rootPipe == null || reachablePipes.contains(rootPipe)) {
            return;
        }
        reachablePipes.add(rootPipe);
        List<Pipe> outs = rootPipe.getOutputs();
        if (outs == null) {
            return;
        }
        for (int i = 0; i < outs.size(); i++) {
            discoverReachable(outs.get(i), reachablePipes);
        }
    }
}
