package prefuse.data;

import java.util.Date;


/**
 * Tuples are objects representing a row of a data table, providing
 * a simplified interface to table data. They maintain a pointer to a
 * corresponding row in a table. When rows are deleted, any live Tuples
 * for that row become invalidated, and any further attempts to access
 * or set data with that Tuple will result in an exception.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public interface Tuple {

    /**
     * Returns the schema for this tuple's data.
     * @return the Tuple Schema
     */
    Schema getSchema();
    
    /**
     * Returns the Table instance that backs this Tuple, if it exists.
     * @return the backing Table, or null if there is none.
     */
    Table getTable();
    
    /**
     * Returns the row index for this Tuple's backing Table, if it exists.
     * @return the backing row index, or -1 if there is no backing table
     * or if this Tuple has been invalidated (i.e., the Tuple's row was
     * deleted from the backing table).
     */
    int getRow();
    
    /**
     * Indicates if this Tuple is valid. Trying to get or set values on an
     * invalid Tuple will result in a runtime exception.
     * @return true if this Tuple is valid, false otherwise
     */
    boolean isValid();
    
    // ------------------------------------------------------------------------
    // Column Methods
    
    /**
     * Returns the data type of the given field as a Java Class instance.
     * @param field the data field
     * @return the data type of the field, a Class instance indicating the
     * top-level type of data values in this field.
     */
    Class getColumnType(String field);
    
    /**
     * Returns the data type of the given column as a Java Class instance.
     * @param col the column index
     * @return the data type of the column, a Class instance indicating the
     * top-level type of data values in this field.
     */
    Class getColumnType(int col);
    
    /**
     * Get the column index corresponding to the given data field.
     * @param field the data field to look up
     * @return the column index of the field within the backing table, or
     * -1 if no columns with the given name were found
     */
    int getColumnIndex(String field);
    
    /**
     * Get the number of columns maintained by the backing table.
     * @return the number of columns / data fields.
     */
    int getColumnCount();
    
    /**
     * Get the data field name of the column at the given index.
     * @param col the column index to look up
     * @return the data field name of the given column index
     */
    String getColumnName(int col);
    
    // ------------------------------------------------------------------------
    // Data Access Methods
    
    /**
     * Check if the <code>get</code> method for the given data field returns
     * values that are compatible with a given target type.
     * @param field the data field to check
     * @param type a Class instance to check for compatibility with the
     * data field values.
     * @return true if the data field is compatible with provided type,
     * false otherwise. If the value is true, objects returned by
     * the {@link #get(String)} can be cast to the given type.
     * @see #get(String)
     */
    boolean canGet(String field, Class type);
    
    /**
     * Check if the <code>set</code> method for the given data field can
     * accept values of a given target type.
     * @param field the data field to check
     * @param type a Class instance to check for compatibility with the
     * data field values.
     * @return true if the data field is compatible with provided type,
     * false otherwise. If the value is true, objects of the given type
     * can be used as parameters of the {@link #set(String, Object)} method.
     * @see #set(String, Object)
     */
    boolean canSet(String field, Class type);
    
    /**
     * Get the data value at the given field as an Object.
     * @param field the data field to retrieve
     * @return the data value as an Object. The concrete type of this
     * Object is dependent on the underlying data column used.
     * @see #canGet(String, Class)
     * @see #getColumnType(String)
     */
    Object get(String field);

    /**
     * Set the value of a given data field.
     * @param field the data field to set
     * @param value the value for the field. If the concrete type of this
     * Object is not compatible with the underlying data model, an
     * Exception will be thrown. Use the {@link #canSet(String, Class)}
     * method to check the type-safety ahead of time.
     * @see #canSet(String, Class)
     * @see #getColumnType(String)
     */
    void set(String field, Object value);
    
    /**
     * Get the data value at the given column number as an Object.
     * @param col the column number
     * @return the data value as an Object. The concrete type of this
     * Object is dependent on the underlying data column used.
     * @see #canGet(String, Class)
     * @see #getColumnType(int)
     */
    Object get(int col);
    
    /**
     * Set the value of at the given column number.
     * @param col the column number
     * @param value the value for the field. If the concrete type of this
     * Object is not compatible with the underlying data model, an
     * Exception will be thrown. Use the {@link #canSet(String, Class)}
     * method to check the type-safety ahead of time.
     * @see #canSet(String, Class)
     * @see #getColumnType(String)
     */
    void set(int col, Object value);
    
    /**
     * Get the default value for the given data field.
     * @param field the data field
     * @return the default value, as an Object, used to populate rows
     * of the data field.
     */
    Object getDefault(String field);
    
    /**
     * Revert this tuple's value for the given field to the default value
     * for the field.
     * @param field the data field
     * @see #getDefault(String)
     */
    void revertToDefault(String field);
    
    // ------------------------------------------------------------------------
    // Convenience Data Access Methods
    
    /**
     * Check if the given data field can return primitive <code>int</code>
     * values.
     * @param field the data field to check
     * @return true if the data field can return primitive <code>int</code>
     * values, false otherwise. If true, the {@link #getInt(String)} method
     * can be used safely.
     */
    boolean canGetInt(String field);
    
    /**
     * Check if the <code>setInt</code> method can safely be used for the
     * given data field.
     * @param field the data field to check
     * @return true if the {@link #setInt(String, int)} method can safely
     * be used for the given field, false otherwise.
     */
    boolean canSetInt(String field);
    
    /**
     * Get the data value at the given field as an <code>int</code>.
     * @param field the data field to retrieve
     * @see #canGetInt(String)
     */
    int getInt(String field);
    
    /**
     * Set the data value of the given field with an <code>int</code>.
     * @param field the data field to set
     * @param val the value to set
     * @see #canSetInt(String)
     */
    void setInt(String field, int val);
    
    /**
     * Get the data value at the given field as an <code>int</code>.
     * @param col the column number of the data field to retrieve
     * @see #canGetInt(String)
     */
    int getInt(int col);
    
    /**
     * Set the data value of the given field with an <code>int</code>.
     * @param col the column number of the data field to set
     * @param val the value to set
     * @see #canSetInt(String)
     */
    void setInt(int col, int val);
    
    // --------------------------------------------------------------
    
    /**
     * Check if the given data field can return primitive <code>long</code>
     * values.
     * @param field the data field to check
     * @return true if the data field can return primitive <code>long</code>
     * values, false otherwise. If true, the {@link #getLong(String)} method
     * can be used safely.
     */
    boolean canGetLong(String field);
    
    /**
     * Check if the <code>setLong</code> method can safely be used for the
     * given data field.
     * @param field the data field to check
     * @return true if the {@link #setLong(String, long)} method can safely
     * be used for the given field, false otherwise.
     */
    boolean canSetLong(String field);
    
    /**
     * Get the data value at the given field as a <code>long</code>.
     * @param field the data field to retrieve
     * @see #canGetLong(String)
     */
    long getLong(String field);
    
    /**
     * Set the data value of the given field with a <code>long</code>.
     * @param field the data field to set
     * @param val the value to set
     * @see #canSetLong(String)
     */
    void setLong(String field, long val);
    
    /**
     * Get the data value at the given field as a <code>long</code>.
     * @param col the column number of the data field to retrieve
     * @see #canGetLong(String)
     */
    long getLong(int col);
    
    /**
     * Set the data value of the given field with a <code>long</code>.
     * @param col the column number of the data field to set
     * @param val the value to set
     * @see #canSetLong(String)
     */
    void setLong(int col, long val);

    // --------------------------------------------------------------
    
    /**
     * Check if the given data field can return primitive <code>float</code>
     * values.
     * @param field the data field to check
     * @return true if the data field can return primitive <code>float</code>
     * values, false otherwise. If true, the {@link #getFloat(String)} method
     * can be used safely.
     */
    boolean canGetFloat(String field);
    
    /**
     * Check if the <code>setFloat</code> method can safely be used for the
     * given data field.
     * @param field the data field to check
     * @return true if the {@link #setFloat(String, float)} method can safely
     * be used for the given field, false otherwise.
     */
    boolean canSetFloat(String field);
    
    /**
     * Get the data value at the given field as a <code>float</code>.
     * @param field the data field to retrieve
     * @see #canGetFloat(String)
     */
    float getFloat(String field);
    
    /**
     * Set the data value of the given field with a <code>float</code>.
     * @param field the data field to set
     * @param val the value to set
     * @see #canSetFloat(String)
     */
    void setFloat(String field, float val);
    
    /**
     * Get the data value at the given field as a <code>float</code>.
     * @param col the column number of the data field to retrieve
     * @see #canGetFloat(String)
     */
    float getFloat(int col);
    
    /**
     * Set the data value of the given field with a <code>float</code>.
     * @param col the column number of the data field to set
     * @param val the value to set
     * @see #canSetFloat(String)
     */
    void setFloat(int col, float val);
    
    // --------------------------------------------------------------
    
    /**
     * Check if the given data field can return primitive <code>double</code>
     * values.
     * @param field the data field to check
     * @return true if the data field can return primitive <code>double</code>
     * values, false otherwise. If true, the {@link #getDouble(String)} method
     * can be used safely.
     */
    boolean canGetDouble(String field);
    
    /**
     * Check if the <code>setDouble</code> method can safely be used for the
     * given data field.
     * @param field the data field to check
     * @return true if the {@link #setDouble(String, double)} method can safely
     * be used for the given field, false otherwise.
     */
    boolean canSetDouble(String field);
    
    /**
     * Get the data value at the given field as a <code>double</code>.
     * @param field the data field to retrieve
     * @see #canGetDouble(String)
     */
    double getDouble(String field);
    
    /**
     * Set the data value of the given field with a <code>double</code>.
     * @param field the data field to set
     * @param val the value to set
     * @see #canSetDouble(String)
     */
    void setDouble(String field, double val);
    
    /**
     * Get the data value at the given field as a <code>double</code>.
     * @param col the column number of the data field to retrieve
     * @see #canGetDouble(String)
     */
    double getDouble(int col);
    
    /**
     * Set the data value of the given field with a <code>double</code>.
     * @param col the column number of the data field to set
     * @param val the value to set
     * @see #canSetDouble(String)
     */
    void setDouble(int col, double val);
    
    // --------------------------------------------------------------
    
    /**
     * Check if the given data field can return primitive <code>boolean</code>
     * values.
     * @param field the data field to check
     * @return true if the data field can return primitive <code>boolean</code>
     * values, false otherwise. If true, the {@link #getBoolean(String)} method
     * can be used safely.
     */
    boolean canGetBoolean(String field);
    
    /**
     * Check if the <code>setBoolean</code> method can safely be used for the
     * given data field.
     * @param field the data field to check
     * @return true if the {@link #setBoolean(String, boolean)} method can
     * safely be used for the given field, false otherwise.
     */
    boolean canSetBoolean(String field);
    
    /**
     * Get the data value at the given field as a <code>boolean</code>.
     * @param field the data field to retrieve
     * @see #canGetBoolean(String)
     */
    boolean getBoolean(String field);
    
    /**
     * Set the data value of the given field with a <code>boolean</code>.
     * @param field the data field to set
     * @param val the value to set
     * @see #canSetBoolean(String)
     */
    void setBoolean(String field, boolean val);
    
    /**
     * Get the data value at the given field as a <code>boolean</code>.
     * @param col the column number of the data field to retrieve
     * @see #canGetBoolean(String)
     */
    boolean getBoolean(int col);
    
    /**
     * Set the data value of the given field with a <code>boolean</code>.
     * @param col the column number of the data field to set
     * @param val the value to set
     * @see #canSetBoolean(String)
     */
    void setBoolean(int col, boolean val);
    
    // --------------------------------------------------------------
    
    /**
     * Check if the given data field can return <code>String</code>
     * values.
     * @param field the data field to check
     * @return true if the data field can return <code>String</code>
     * values, false otherwise. If true, the {@link #getString(String)} method
     * can be used safely.
     */
    boolean canGetString(String field);
    
    /**
     * Check if the <code>setString</code> method can safely be used for the
     * given data field.
     * @param field the data field to check
     * @return true if the {@link #setString(String, String)} method can safely
     * be used for the given field, false otherwise.
     */
    boolean canSetString(String field);
    
    /**
     * Get the data value at the given field as a <code>String</code>.
     * @param field the data field to retrieve
     * @see #canGetString(String)
     */
    String getString(String field);
    
    /**
     * Set the data value of the given field with a <code>String</code>.
     * @param field the data field to set
     * @param val the value to set
     * @see #canSetString(String)
     */
    void setString(String field, String val);
    
    /**
     * Get the data value at the given field as a <code>String</code>.
     * @param col the column number of the data field to retrieve
     * @see #canGetString(String)
     */
    String getString(int col);
    
    /**
     * Set the data value of the given field with a <code>String</code>.
     * @param col the column number of the data field to set
     * @param val the value to set
     * @see #canSetString(String)
     */
    void setString(int col, String val);
    
    // --------------------------------------------------------------
    
    /**
     * Check if the given data field can return <code>Date</code>
     * values.
     * @param field the data field to check
     * @return true if the data field can return <code>Date</code>
     * values, false otherwise. If true, the {@link #getDate(String)} method
     * can be used safely.
     */
    boolean canGetDate(String field);
    
    /**
     * Check if the <code>setDate</code> method can safely be used for the
     * given data field.
     * @param field the data field to check
     * @return true if the {@link #setDate(String, Date)} method can safely
     * be used for the given field, false otherwise.
     */
    boolean canSetDate(String field);
    
    /**
     * Get the data value at the given field as a <code>Date</code>.
     * @param field the data field to retrieve
     * @see #canGetDate(String)
     */
    Date getDate(String field);
    
    /**
     * Set the data value of the given field with a <code>Date</code>.
     * @param field the data field to set
     * @param val the value to set
     * @see #canSetDate(String)
     */
    void setDate(String field, Date val);
    
    /**
     * Get the data value at the given field as a <code>Date</code>.
     * @param col the column number of the data field to retrieve
     * @see #canGetDate(String)
     */
    Date getDate(int col);
    
    /**
     * Set the data value of the given field with a <code>Date</code>.
     * @param col the column number of the data field to set
     * @param val the value to set
     * @see #canSetDate(String)
     */
    void setDate(int col, Date val);
    
} // end of interface Tuple
