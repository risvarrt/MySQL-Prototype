package util;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.TableDetails;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class QueryValidationUtils {

    private List<TableDetails> currentDBTables;

    public QueryValidationUtils(List<TableDetails> currentDBTables) {
        this.currentDBTables = currentDBTables;
    }

    /*
       Validates an INSERT query against the current database tables and their columns.
     */
    public boolean validateInsertQuery(String query) {
        // Remove semicolon and normalize spaces
        query = query.trim().replaceAll("\\s+", " ").replace(";", "");

        if (!query.toUpperCase().startsWith("INSERT INTO")) {
            return false;
        }

        String[] parts = query.split("VALUES");
        if (parts.length != 2) {
            return false;
        }

        String[] tableAndColumnsPart = parts[0].trim().split("\\(");
        String tableName = tableAndColumnsPart[0].substring("INSERT INTO ".length()).trim();
        String[] columnNames = tableAndColumnsPart[1].trim().replace(")", "").split(",");

        // Validate table name
        TableDetails table = currentDBTables.stream()
                .filter(t -> t.getTableName().equalsIgnoreCase(tableName))
                .findFirst()
                .orElse(null);

        if (table == null) {
            return false;
        }

        Map<String, String> columnDetails = table.getColumnDetails();

        // Validate column names
        for (String columnName : columnNames) {
            columnName = columnName.trim();
            if (!columnDetails.containsKey(columnName)) {
                return false;
            }
        }

        return true;
    }

    /*
       Validates an UPDATE query syntax.
    */
    public boolean validateUpdateQuery(String query) {
        if (!query.toUpperCase().startsWith("UPDATE ")) {
            return false;
        }

        String[] parts = query.split(" SET ", 2);
        if (parts.length != 2) {
            return false;
        }
        return true;
    }

    /*
    Validates a SELECT query against the current database tables.
    Specifically, it checks if the query is a simple 'SELECT * FROM <table>' query
    and verifies the existence of the table in the database schema. */
    public boolean validateSelectQuery(String query) {
        String normalizedQuery = query.trim().replaceAll("\\s+", " ").toUpperCase();

        // Basic syntax check
        if (!normalizedQuery.startsWith("SELECT * FROM ")) {
            System.out.println("Query does not start with 'SELECT * FROM'");
            return false;
        }

        // Extract table name
        String tableName = normalizedQuery.substring("SELECT * FROM ".length()).replace(";", "").trim();
        // Validate table name
        for (TableDetails table : currentDBTables) {
            if (table.getTableName().equalsIgnoreCase(tableName)) {
                return true;
            }
        }

        System.out.println("Table '" + tableName + "' does not exist.");
        return false;
    }
}
