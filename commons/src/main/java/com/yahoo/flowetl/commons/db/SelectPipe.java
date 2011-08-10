/*******************************************************
 *                                                     *
 * Copyright (C) 2011 Yahoo! Inc. All Rights Reserved. *
 *                                                     *
 *******************************************************/
package com.yahoo.flowetl.commons.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.yahoo.flowetl.core.CoreException;
import com.yahoo.flowetl.core.InputValidator;
import com.yahoo.flowetl.core.Logger.Level;
import com.yahoo.flowetl.core.pipe.AbstractPipe;
import com.yahoo.flowetl.core.pipe.PipeResult;
import com.yahoo.flowetl.core.pipe.result.BackedPipeResult;
import com.yahoo.flowetl.core.services.ServiceRegistry;
import com.yahoo.flowetl.core.validator.MapInputValidator;
import com.yahoo.flowetl.services.DatabaseService;

/**
 * This class represents a simple select like pipe for database interactions. It
 * takes a database dsn and a query with an optional query params list which
 * will be bound to the prepared statement that is that query (if it has
 * anything to bind). The result will be a result set object.
 * 
 * @author Joshua Harlow
 */
public class SelectPipe extends AbstractPipe
{
    // innies required
    public static final String IN_DSN = makeParamName(SelectPipe.class, "dsn", true);
    public static final String IN_SELECT = makeParamName(SelectPipe.class, "query", true);
    private static final Map<String, Class<?>> REQUIRED_PARAMS = new TreeMap<String, Class<?>>();
    static {
        REQUIRED_PARAMS.put(IN_SELECT, String.class);
        REQUIRED_PARAMS.put(IN_DSN, String.class);
    }

    // innies optional
    public static final String IN_SELECT_PARAMS = makeParamName(SelectPipe.class, "queryparams", true);
    private static final Map<String, Class<?>> OPTIONAL_PARAMS = new TreeMap<String, Class<?>>();
    static {
        OPTIONAL_PARAMS.put(IN_SELECT_PARAMS, List.class);
    }

    // outties
    public static final String OUT_RESULT_SET = AbstractPipe.makeParamName(SelectPipe.class, "resultset", false);

    // the output result class
    // that allows u to extract the result set without doing anything special
    public static class Result extends BackedPipeResult
    {
        public Result() {
            super();
        }

        public ResultSet getResultSet() {
            return getParam(OUT_RESULT_SET);
        }
    }

    /** The input validation class. */
    private final InputValidator validator;

    /** The db service. */
    private final DatabaseService dbService;

    /**
     * Instantiates a new select pipe.
     * 
     * @param name
     * @param services
     */
    public SelectPipe(String name, ServiceRegistry services) {
        super(name, services);
        validator = new MapInputValidator(REQUIRED_PARAMS, OPTIONAL_PARAMS);
        dbService = getServiceRegistry().getService(DatabaseService.class);
        if (dbService == null) {
            throw new IllegalArgumentException(SelectPipe.class + " requires a database service to operate");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.AbstractPipe#maxOutputs()
     */
    @Override
    public int maxOutputs() {
        // we are outputting a iterator
        // which should only be used by 1 output
        // so enforce that...
        return 1;
    }

    /**
     * Makes the output for the given input by forming the sql query, binding
     * the params and then returning the result set.
     * 
     * @param input
     *            the input
     * 
     * @return the query result set
     */
    private ResultSet makeOutput(PipeResult input) {
        String dsn = input.getParam(IN_DSN);
        String query = input.getParam(IN_SELECT);
        Connection con = dbService.getConnection(dsn);
        PreparedStatement m = null;
        if (getLogger().isEnabled(Level.INFO)) {
            getLogger().log(Level.INFO, "Running query " + query + " : " + dsn);
        }
        try {
            // we won't notice changes by others
            // and read only
            m = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        }
        catch (SQLException e) {
            throw new CoreException("Unable to prepare statement for query " + query, e);
        }
        List<?> pParams = input.getParam(IN_SELECT_PARAMS);
        if (pParams != null) {
            for (int i = 0; i < pParams.size(); i++) {
                try {
                    m.setObject((i + 1), pParams.get(i));
                }
                catch (SQLException e) {
                    throw new CoreException("Unable to set param " + (i + 1) + " on query " + query + " to " + pParams.get(i), e);
                }
            }
        }
        try {
            return m.executeQuery();
        }
        catch (SQLException e) {
            throw new CoreException("Unable to execute prepared statement " + m, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.AbstractPipe#makeOutput(java.util.List)
     */
    @Override
    protected PipeResult makeOutput(List<PipeResult> inputs) {
        BackedPipeResult merged = new BackedPipeResult(inputs);
        validator.checkInput(merged);
        Result res = new Result();
        res.setParam(OUT_RESULT_SET, makeOutput(merged));
        return res;
    }
}
