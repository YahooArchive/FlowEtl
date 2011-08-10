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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.yahoo.flowetl.core.CoreException;
import com.yahoo.flowetl.core.Logger;
import com.yahoo.flowetl.core.Logger.Level;

/**
 * This class represents a update action on a database which maps to a sql
 * update. It returns the number of records that that update affected.
 * 
 * @author Joshua Harlow
 */
public abstract class UpdateAction implements Action<Integer>
{
    private static final Logger logger = new Logger(UpdateAction.class);

    /**
     * Instantiates a new update action.
     */
    public UpdateAction() {
        super();
    }

    /**
     * This should return the sql for the given update
     */
    protected abstract String getSql();

    /**
     * Gets the bindings for that sql if any.
     */
    protected abstract List<Object> getBindings();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.flowetl.commons.db.actions.Action#applyAction(java.sql.Connection
     * )
     */
    @Override
    public Integer applyAction(Connection db) {
        PreparedStatement st = null;
        try {
            String sql = getSql();
            try {
                st = db.prepareStatement(sql);
            }
            catch (SQLException e) {
                throw new CoreException("Unable to prepare update action for query " + sql, e);
            }
            List<Object> binds = getBindings();
            if (binds != null && binds.isEmpty() == false) {
                for (int i = 0; i < binds.size(); i++) {
                    Object o = binds.get(i);
                    try {
                        st.setObject((i + 1), o);
                    }
                    catch (SQLException e) {
                        throw new CoreException("Unable to bind param " + (i + 1) + " for sql " + sql + " with param " + o, e);
                    }
                }
            }
            try {
                logger.log(Level.DEBUG, "Executing %s with bound params %s", sql, binds);
                int res = st.executeUpdate();
                logger.log(Level.DEBUG, "Executing %s affected %s rows", sql, res);
                return res;
            }
            catch (SQLException e) {
                throw new CoreException("Unable to execute update for sql " + sql + " with params " + StringUtils.join(binds, ","), e);
            }
        }
        finally {
            if (st != null) {
                try {
                    st.close();
                }
                catch (SQLException e) {
                    logger.log(Level.WARN, "Error closing update action prepared statement", e);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString() + " [sql=" + getSql() + "][bindings=" + getBindings() + "]");
        return builder.toString();
    }

}
