package in.zeta.exception;

public class DataNotFoundException extends RuntimeException {

    private final String tableName;
    private final String fieldName;
    private final Object fieldValue;

    public DataNotFoundException(String tableName, String fieldName, Object fieldValue) {
        super(String.format("Required data not found in table '%s' for field '%s' with value '%s'",
                tableName, fieldName, fieldValue));
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DataNotFoundException(String tableName, String fieldName, Object fieldValue, String message) {
        super(message);
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public DataNotFoundException(String tableName, String fieldName, Object fieldValue, String message, Throwable cause) {
        super(message, cause);
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }


    // Getters
    public String getTableName() { return tableName; }
    public String getFieldName() { return fieldName; }
    public Object getFieldValue() { return fieldValue; }
}