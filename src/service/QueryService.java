package service;

import model.Database;
import model.Table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryService {

    private Database db = new Database();

    /*Loads the database JSON file with the specified name. If tables exist, it initializes
      the database with these tables. */
    public List<Table> loadDb(String dbname) {
        Table table = new Table();
        if (table.loadtables(dbname) == null)
            return new ArrayList<Table>();
        db.setTables(table.loadtables(dbname));
        db.setDbname(dbname);
        return table.loadtables(dbname);
    }

    /*
    Creates a new JSON file with the given name.
     */
    public void createDatabase(String dbname) {
        db.setDbname(dbname);
        db.createDB();
    }

/*
  Creates a new table object with the specified name and column definitions.
 */
 public void createTable(String tableName, Map<String, String> columns) {
        Table table = new Table();
        table.setTablename(tableName);
        table.setColumnDefinitions(columns);
        db.addTable(table);
    }

    //Inserts a new row into the specified table.
    public void insertrow(String tableName, Map<String, Object> resultmap) {
        Table table = db.getTableByName(tableName);
        if (table != null) {
            table.insertRow(resultmap);
            db.updatetable(table, db.getTables());
        }
    }

    /*
    Prints the specified table's structure and all its rows to the console.
    */
    public void selectTable(String tableName) {
        Table table = db.getTableByName(tableName);
        System.out.println("Table Name: " + table.getTablename());
        System.out.println("_____________________________________");
        table.getColumnDefinitions().keySet().forEach(column -> System.out.print(column + "\t"));
        System.out.println();
        table.getRows().forEach(row -> {
            row.forEach((key, value) -> System.out.print(value + "\t"));
            System.out.println();
        });
        System.out.println("_____________________________________");
    }

 /*
  Updates rows in a table object that match the specified conditions.
 */
 public void updaterow(String tablename, Map<String, String> update, Map<String, String> present) {
        Table table = db.getTableByName((tablename));
        for (Map<String, Object> row : table.getRows()) {
            // Checking if this row matches the 'present' condition
            boolean match = true;
            for (Map.Entry<String, String> condition : present.entrySet()) {
                if (!row.get(condition.getKey()).equals(condition.getValue())) {
                    match = false;
                    break;
                }
            }
            // If it's a match, updating the row
            if (match) {
                for (Map.Entry<String, String> entry : update.entrySet()) {
                    String column = entry.getKey();
                    Object value = entry.getValue();
                    row.put(column, value);
                    db.updatetable(table, db.getTables());
                    break;
                }
                break;
            }
        }

    }

    //the current state of the database, including all tables and rows, are stored in the JSON file.
    public void saveDB() {
        db.saveDB(db);
    }
}
