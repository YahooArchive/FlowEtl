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
package com.yahoo.flowetl.commons.runner;

import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.List;

import com.yahoo.flowetl.core.listener.FlowListener;
import com.yahoo.flowetl.core.pipe.Pipe;
import com.yahoo.flowetl.core.pipe.PipeResult;

/**
 * This class is a simple flow listener which will show status on a command line
 * terminal (or the like). It gets notified of pipe running and outputs this
 * information while also having a separate thread that shows the
 * status/progress of the current pipe.
 * 
 * @author Joshua Harlow
 */
public class CommandLineListener implements FlowListener
{
    // various static objects/constants used
    private static final NumberFormat PER_FORMAT = NumberFormat.getPercentInstance();
    private static final long MEGABYTE = 1024L * 1024L;
    // how often we show a dot, everyone 1/2 second
    private static final long WAIT_MS = 500;
    // how often we see if the progress of the current pipe has changed
    // ie every 25 dots
    private static final int PER_SHOW = 25;
    // a little dot will come out on the command line
    private static final String STATUS_INDICATOR = ".";

    // these
    // need to be accessed in thread safe way...
    private final Object statusLock = new Object();
    private boolean showStatus = false;
    private int numStatusOuts = 0;
    private float lastPerDone = 0;
    private Pipe activePipe = null;
    //

    // the output stream, typically stdout but could change
    private final PrintStream out;

    // the thread that is showing the pipe actions/progress...
    private final Thread showActions;

    // the total number of pipes that will run
    private int totalPipes;

    // the currently active pipe number (less than totalpipes)
    private int currPipe;

    // this is the thread that will show the current pipes status
    private final class StatusIndicatorRunnable implements Runnable
    {
        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            while (true) {
                synchronized (statusLock) {
                    if (showStatus) {
                        out.print(STATUS_INDICATOR);
                        numStatusOuts++;
                        // time to show how far along the current pipe is??
                        if ((numStatusOuts % PER_SHOW) == 0 && activePipe != null) {
                            float curPercentDone = activePipe.percentDone();
                            if (curPercentDone != lastPerDone) {
                                out.print(formatDonePercent(curPercentDone));
                                lastPerDone = curPercentDone;
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(WAIT_MS);
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Instantiates a new command line listener using system.out as the
     * outputstream.
     */
    public CommandLineListener() {
        this(System.out);
    }

    /**
     * Instantiates a new command line listener.
     * 
     * @param out
     *            the output stream to write to
     */
    public CommandLineListener(PrintStream out) {
        this.out = out;
        showActions = new Thread(new StatusIndicatorRunnable());
        showActions.setDaemon(true);
        showActions.start();
    }

    /**
     * Formats a float done percent to a more meaningful string.
     * 
     * @param perDone
     * 
     * @return the string
     */
    private static String formatDonePercent(float perDone) {
        return "%D{" + PER_FORMAT.format(perDone) + "}";
    }

    /**
     * Gets the heap info of the current JVM as a string
     * 
     * @param showMax
     *            if we should show the max heap size (not to useful after
     *            showing it once)
     * 
     * @return the heap info string
     */
    private static String getHeapInfo(boolean showMax) {
        long heapSize = Runtime.getRuntime().totalMemory();
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        StringBuilder out = new StringBuilder();
        out.append("size = " + (heapSize / MEGABYTE) + "Mb, ");
        if (showMax) {
            long heapMaxSize = Runtime.getRuntime().maxMemory();
            out.append("max size = " + (heapMaxSize / MEGABYTE) + "Mb, ");
        }
        out.append("free size = " + (heapFreeSize / MEGABYTE) + "Mb");
        return "%H{" + out.toString() + "}";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.listener.FlowListener#onStartGenerate(com.yahoo
     * .flowetl.core.pipe.Pipe)
     */
    @Override
    public void onStartGenerate(Pipe aboutToRunPipe) {
        out.println("Running pipe #" + (currPipe + 1) + " {" + aboutToRunPipe + "}");
        out.println("Heap info is " + getHeapInfo(false) + "");
        synchronized (statusLock) {
            activePipe = aboutToRunPipe;
            numStatusOuts = 0;
            showStatus = true;
            lastPerDone = 0;
            // show starting 0.0%
            out.print(formatDonePercent(0.0f));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.listener.FlowListener#onStart(java.util.List)
     */
    @Override
    public void onStart(List<Pipe> ordering) {
        totalPipes = ordering.size();
        currPipe = 0;
        out.println("About to start running " + totalPipes + " pipes");
        for (int i = 0; i < ordering.size(); i++) {
            out.println("  (" + (i + 1) + ") " + ordering.get(i));
        }
        synchronized (statusLock) {
            numStatusOuts = 0;
            lastPerDone = 0;
            showStatus = false;
            activePipe = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.core.listener.FlowListener#onFinishGenerate(com.yahoo
     * .flowetl.core.pipe.Pipe, com.yahoo.flowetl.core.pipe.PipeResult, long)
     */
    @Override
    public void onFinishGenerate(Pipe ranPipe, PipeResult ranResult, long timeTakenMs) {
        synchronized (statusLock) {
            showStatus = false;
            activePipe = null;
            numStatusOuts = 0;
            if (lastPerDone < 1) {
                // show end 100%
                out.println(formatDonePercent(1));
            }
            else {
                // already showed...
                out.println();
            }
            lastPerDone = 0;
        }
        double tSecs = (double) timeTakenMs / 1000.0d;
        out.println("Finished pipe #" + (currPipe + 1) + " in " + tSecs + " seconds");
        out.println("With result " + ranResult);
        currPipe += 1;
        out.println("Completed - " + formatDonePercent(((float) (currPipe) / (float) totalPipes)) + " of all pipes");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.listener.FlowListener#onCompletion(long)
     */
    @Override
    public void onCompletion(long timeTakenMs) {
        double tSecs = (double) timeTakenMs / 1000.0d;
        out.println("Finished in " + tSecs + " seconds or " + (tSecs / 60) + " minutes");
    }

}
