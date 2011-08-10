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
package com.yahoo.flowetl.commons.db.actions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.yahoo.flowetl.core.CoreException;
import com.yahoo.flowetl.core.Logger;
import com.yahoo.flowetl.core.Logger.Level;

/**
 * This class processes a set of actions with the capability to rollback those
 * actions (if those actions can themselves be rolled back as a group).
 * 
 * @author Joshua Harlow
 */
public abstract class ActionSequencer
{
    private static final Logger logger = new Logger(ActionSequencer.class);

    /** Should we attempt to do rollback on errors */
    private final boolean enableRollback;

    /**
     * Instantiates a new action sequencer.
     * 
     * @param enableRollback
     */
    public ActionSequencer(boolean enableRollback) {
        this.enableRollback = enableRollback;
    }

    /**
     * Instantiates a new action sequencer without rollback support.
     */
    public ActionSequencer() {
        this(false);
    }

    /**
     * This can be derived from to determine when a given action is applied and
     * with what result and if it was committed (or will the commit wait till
     * the end of all actions).
     * 
     * @param who
     * @param whoResult
     * @param wasCommited
     */
    protected void onApplied(Action<?> who, Object whoResult, boolean wasCommited) {
        // does nothing
    }

    /**
     * Executes the given actions.
     * 
     * @param actions
     * 
     * @return the list of output from each action
     */
    public List<Object> executeActions(List<Action<?>> actions) {
        if (actions == null || actions.isEmpty()) {
            return new LinkedList<Object>();
        }
        final Connection con = getConnection();
        boolean oldAuto = true;
        try {
            oldAuto = con.getAutoCommit();
        }
        catch (SQLException e) {
            throw new CoreException("Can not read auto commit value for rollback support", e);
        }
        // save the state?
        if (enableRollback == true && oldAuto == true) {
            try {
                // gotta turn that off
                con.setAutoCommit(false);
            }
            catch (SQLException e) {
                throw new CoreException("Can not turn auto commit off for rollback support", e);
            }
        }
        // apply the actions
        final int am = actions.size();
        List<Object> out = new ArrayList<Object>(am);
        int amExec = 0;
        try {
            logger.log(Level.INFO, "Executing %s actions", am);
            for (int i = 0; i < am; i++) {
                Action<?> a = actions.get(i);
                logger.log(Level.INFO, "Executing %s : %s action", (i + 1), a);
                Object res = a.applyAction(con);
                if (enableRollback) {
                    onApplied(a, res, false);
                }
                else {
                    onApplied(a, res, true);
                }
                out.add(res);
                amExec++;
            }
            // commit all at the end of apply
            if (enableRollback == true) {
                con.commit();
            }
        }
        catch (SQLException e) {
            if (enableRollback) {
                try {
                    con.rollback();
                }
                catch (SQLException e1) {
                    logger.log(Level.WARN, "Unable to perform rollback", e1);
                }
            }
            // rethrow
            throw new CoreException("Unable to commit due to sql error", e);
        }
        catch (RuntimeException e) {
            if (enableRollback) {
                try {
                    con.rollback();
                }
                catch (SQLException e1) {
                    logger.log(Level.WARN, "Unable to perform rollback", e1);
                }
            }
            // rethrow
            throw e;
        }
        finally {
            if (oldAuto == true && enableRollback == true) {
                try {
                    con.setAutoCommit(oldAuto);
                }
                catch (SQLException e) {
                    logger.log(Level.WARN, "Unable to restore auto commit value to " + oldAuto, e);
                }
            }
            try {
                con.close();
            }
            catch (SQLException e) {
                logger.log(Level.WARN, "Unable to close connection", e);
            }
        }
        return out;
    }

    /**
     * Gets the db connection for the actions to use.
     * 
     * @return the connection
     */
    protected abstract Connection getConnection();

}
