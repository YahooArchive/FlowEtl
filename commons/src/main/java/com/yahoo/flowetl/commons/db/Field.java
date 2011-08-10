/*******************************************************
 *                                                     *
 * Copyright (C) 2011 Yahoo! Inc. All Rights Reserved. *
 *                                                     *
 *******************************************************/
package com.yahoo.flowetl.commons.db;

/**
 * This class represents a field inside a database and its potential rename.
 * 
 * @author Joshua Harlow
 */
public class Field
{

    /** The field. */
    private String field;

    /** The rename. */
    private String rename;

    /**
     * Instantiates a new field with no rename.
     * 
     * @param dbField
     */
    public Field(String dbField) {
        this(dbField, null);
    }

    /**
     * Instantiates a new field with a rename.
     * 
     * @param dbField
     * @param fieldRename
     */
    public Field(String dbField, String fieldRename) {
        super();
        this.field = dbField;
        this.rename = fieldRename;
    }

    /**
     * Gets the field.
     * 
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * Gets the rename.
     * 
     * @return the rename
     */
    public String getRename() {
        return rename;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        result = prime * result + ((rename == null) ? 0 : rename.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Field other = (Field) obj;
        if (field == null) {
            if (other.field != null)
                return false;
        }
        else if (!field.equals(other.field))
            return false;
        if (rename == null) {
            if (other.rename != null)
                return false;
        }
        else if (!rename.equals(other.rename))
            return false;
        return true;
    }
}
