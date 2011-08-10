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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

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
 * This class represents a simple mysql based join that takes in a dsn for which
 * database to perform the join on (or which server). Then it takes in the join
 * object which will define how to form the join (its nothing complicated). As
 * output the result set of that join will be provided, or an exception will be
 * thrown if that result set can not be created.
 * 
 * @author Joshua Harlow
 */
public class JoinPipe extends AbstractPipe
{
    // innies required
    public static final String IN_DSN = makeParamName(JoinPipe.class, "dsn", true);
    public static final String IN_JOIN = makeParamName(JoinPipe.class, "join", true);
    private static final Map<String, Class<?>> REQUIRED_PARAMS = new TreeMap<String, Class<?>>();
    static {
        REQUIRED_PARAMS.put(IN_JOIN, Join.class);
        REQUIRED_PARAMS.put(IN_DSN, String.class);
    }

    // outties
    public static final String OUT_RESULT_SET = makeParamName(JoinPipe.class, "resultset", false);

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

    /**
     * This class represents our simplified join.
     */
    public static class Join
    {

        // the join type
        public static enum Type
        {
            LEFT, RIGHT
        }

        /** The fields that we will be selecting on (if empty * will be used) */
        private final Set<Field> fields;

        /** The src database name. */
        private final String dbName;

        /** The src table name. */
        private final String tableName;

        /** The fields and databases/tables we will join with. */
        private final Map<String, Join> joinWith;

        /** The join type. */
        private final Type joinType;

        /**
         * Instantiates a new join.
         * 
         * @param dbName
         * @param tableName
         */
        public Join(String dbName, String tableName) {
            this(dbName, tableName, null);
        }

        /**
         * Instantiates a new join.
         * 
         * @param dbName
         * @param tableName
         * @param joinType
         */
        public Join(String dbName, String tableName, Type joinType) {
            super();
            this.dbName = dbName;
            this.fields = new HashSet<Field>();
            this.joinWith = new HashMap<String, Join>();
            this.tableName = tableName;
            this.joinType = joinType;
        }

        /**
         * Gets the database name.
         * 
         * @return the current src database name
         */
        public String getDbName() {
            return dbName;
        }

        /**
         * Adds a field that we will be selecting on.
         * 
         * @param selectField
         */
        public void addField(Field selectField) {
            fields.add(selectField);
        }

        /**
         * Adds the given field and db/table that we will join with
         * 
         * @param onField
         * @param who
         */
        public void addJoin(String onField, Join who) {
            joinWith.put(onField, who);
        }

        /**
         * Gets the fields.
         */
        public Set<Field> getFields() {
            return fields;
        }

        /**
         * Gets who we will join with.
         */
        public Map<String, Join> getJoinWith() {
            return joinWith;
        }

        /**
         * Gets the join type.
         */
        public Type getJoinType() {
            return joinType;
        }

        /**
         * Gets the src table name.
         */
        public String getTableName() {
            return tableName;
        }

    }

    /** The db service for looking up database connections. */
    private final DatabaseService dbService;

    /** The validator that will check the inputs. */
    private final InputValidator validator;

    /**
     * Instantiates a new join pipe.
     * 
     * @param name
     * @param services
     */
    public JoinPipe(String name, ServiceRegistry services) {
        super(name, services);
        this.dbService = services.getService(DatabaseService.class);
        if (dbService == null) {
            throw new IllegalArgumentException(JoinPipe.class + " can not operate without a database service");
        }
        validator = new MapInputValidator(REQUIRED_PARAMS, null);
    }

    /**
     * Makes a db field string by combining the db name with the table name with
     * the fieldname.
     * 
     * @param dbName
     * @param tableName
     * @param fieldName
     * 
     * @return the string
     */
    public static String makeDbField(String dbName, String tableName, String fieldName) {
        return StringUtils.join(new Object[] { dbName, tableName, fieldName }, ".");
    }

    /**
     * Makes the sql that represents the given join.
     * 
     * @param start
     * 
     * @throws IllegalArgumentException
     *             if the input join is not valid
     * 
     * @return the string
     */
    private String makeSql(Join start) {
        if (StringUtils.isEmpty(start.getDbName())) {
            throw new IllegalArgumentException("Attempt to make a join without any start database to join on");
        }
        if (StringUtils.isEmpty(start.getTableName())) {
            throw new IllegalArgumentException("Attempt to make a join without any start database table name to join on");
        }
        List<String> tmpList = new LinkedList<String>();
        StringBuilder tmpBuilder = new StringBuilder();
        for (Field f : start.getFields()) {
            if (f == null) {
                continue;
            }
            String name = f.getField();
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            tmpBuilder.append(name);
            if (StringUtils.isEmpty(f.getRename()) == false) {
                tmpBuilder.append(" AS ");
                tmpBuilder.append(f.getRename());
            }
            tmpList.add(tmpBuilder.toString());
            tmpBuilder.setLength(0);
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        if (tmpList.isEmpty()) {
            sql.append(" * ");
        }
        else {
            sql.append(StringUtils.join(tmpList, ", "));
        }
        sql.append(" FROM ");
        sql.append(start.getDbName());
        sql.append(" ");
        if (start.getJoinType() == null) {
            sql.append(Join.Type.RIGHT);
        }
        else {
            sql.append(start.getJoinType());
        }
        sql.append(" JOIN ");
        Set<String> dbNames = new HashSet<String>();
        for (Entry<String, Join> joinMe : start.getJoinWith().entrySet()) {
            String thereDbName = joinMe.getValue().getDbName();
            if (StringUtils.isEmpty(thereDbName)) {
                continue;
            }
            dbNames.add(thereDbName);
        }
        if (dbNames.isEmpty()) {
            // not really a join...
            throw new IllegalArgumentException("Attempt to make a join without any db names to join on");
        }
        sql.append(" (");
        sql.append(StringUtils.join(dbNames, ", "));
        sql.append(") ");
        tmpList.clear();
        String myName = start.getDbName();
        String myTable = start.getTableName();
        tmpBuilder.setLength(0);
        for (Entry<String, Join> joinMe : start.getJoinWith().entrySet()) {
            String sharedField = joinMe.getKey();
            String thereDbName = joinMe.getValue().getDbName();
            String thereTableName = joinMe.getValue().getTableName();
            if (StringUtils.isEmpty(thereTableName) || StringUtils.isEmpty(thereDbName) || StringUtils.isEmpty(sharedField)) {
                continue;
            }
            tmpBuilder.append(makeDbField(myName, myTable, sharedField));
            tmpBuilder.append("=");
            tmpBuilder.append(makeDbField(thereDbName, thereTableName, sharedField));
            tmpList.add(tmpBuilder.toString());
            tmpBuilder.setLength(0);
        }
        if (tmpList.isEmpty()) {
            // not really a join...
            throw new IllegalArgumentException("Attempt to make a join without any fields to join on");
        }
        sql.append(" ON (");
        sql.append(StringUtils.join(tmpList, " AND "));
        sql.append(")");
        return sql.toString();
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
     * Executes the join string with the given dsn.
     * 
     * @param dsn
     * @param sql
     * 
     * @return the result set
     */
    protected ResultSet executeJoin(String dsn, String sql) {
        Connection dbConnector = dbService.getConnection(dsn);
        PreparedStatement call = null;
        try {
            call = dbConnector.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        }
        catch (SQLException e) {
            throw new CoreException("Unable to prepare statement for query " + sql, e);
        }
        try {
            return call.executeQuery();
        }
        catch (SQLException e) {
            throw new CoreException("Unable to execute prepared statement " + call, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.flowetl.core.pipe.AbstractPipe#makeOutput(java.util.List)
     */
    @Override
    protected PipeResult makeOutput(List<PipeResult> collectedInputs) {
        BackedPipeResult merged = new BackedPipeResult(collectedInputs);
        validator.checkInput(merged);
        String dsn = merged.getParam(IN_DSN);
        Join start = merged.getParam(IN_JOIN);
        Result out = new Result();
        String sql = makeSql(start);
        if (getLogger().isEnabled(Level.INFO)) {
            getLogger().log(Level.INFO, "Running query " + sql + " on dsn " + dsn);
        }
        ResultSet rs = executeJoin(dsn, sql);
        out.setParam(OUT_RESULT_SET, rs);
        return out;
    }

}
