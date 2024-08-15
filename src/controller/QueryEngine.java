package controller;

import model.Database;
import model.Table;
import model.TableDetails;
import model.User;
import service.QueryService;
import util.QueryValidationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Controller class for the whole transcation and query processing as well as parsing
public class QueryEngine {
    private final QueryService queryService = new QueryService();
    private boolean inTransaction = true; // Flag to manage transaction state
    private boolean commitflag = false; // Flag to indicate if changes should be committed
    private boolean rollbackflag = false; // Flag to indicate if changes should be rolled back

    private List<TableDetails> tableschemas = new ArrayList<TableDetails>();
    private QueryValidationUtils queryvalidator = new QueryValidationUtils(tableschemas);

    private Database currentDB = new Database(); // Represents the current database in use
    private User loggedInUser = new User(); // Represents the currently logged-in user

    //Processes a single SQL query and executes the corresponding database operation.
    public void processQuery(String query) throws IOException {
        switch (query.split(" ")[0].toUpperCase()) {
            case "CREATE":
                switch (query.split(" ")[1].toUpperCase()) { // Parsing the CREATE queries here itself directly
                    case "TABLE":
                        if (loggedInUser.getCurrentDatabase() == null) {
                            System.out.println("Select the database first");
                            return;
                        }
                        if (query.length() > 2 &&
                                "TABLE".equalsIgnoreCase(query.split(" ")[1].toUpperCase())) {
                            createTable(query);

                        }
                        break;
                    case "DATABASE":
                        if (!loggedInUser.isDbcreationflag() &&
                                (query.length() > 2 &&
                                        "DATABASE".equalsIgnoreCase(query.split(" ")[1].toUpperCase()))) {
                            createDatabase(query);
                            loggedInUser.setDbcreationflag(true);
                            break;
                        } else {
                            System.out.println("You can create only one Database in a session");
                            return;
                        }
                    default:
                        System.out.println("Unknown command");
                }
                break;
            case "USE":

                if(query.split(" ").length > 2)
                {
                    System.out.println("Enter a valid query");
                    return;
                }
                if (!loggedInUser.isDblock()) {
                    // Locking the database to ensure that a user can use only one DB in a session
                    String dbname = query.split(" ")[1].toUpperCase();
                    loggedInUser.setDblock(true);
                    loggedInUser.setCurrentDatabase(dbname);
                    loggedInUser.setDbcreationflag(true);
                    currentDB.setDbname(dbname);
                    currentDB.setTables(queryService.loadDb(dbname));
                    for (Table table : currentDB.getTables()) {
                        TableDetails td = new TableDetails();
                        td.setTableName(table.getTablename());
                        td.setColumnDetails(table.getColumnDefinitions());
                        tableschemas.add(td);
                    }
                    System.out.println("Database:" + dbname + " Locked Successfully");
                } else {
                    System.out.println("Another Database already in use");
                }
                break;
            case "INSERT":
                if (loggedInUser.getCurrentDatabase() == null) {
                    return;
                }
                if (!queryvalidator.validateInsertQuery(query)) {
                    System.out.println("Provide a Valid Query");
                    return;
                }
                insertrow(query);
                break;
            case "UPDATE":
                if (loggedInUser.getCurrentDatabase() == null) {
                    return;
                }
                if (!queryvalidator.validateUpdateQuery(query)) {
                    System.out.println("Provide a Valid Update Query");
                    return;
                }
                updaterow(query);
                break;
            case "SELECT":
                if (loggedInUser.getCurrentDatabase() == null) {
                    return;
                }
                if (!queryvalidator.validateSelectQuery(query)) {
                    System.out.println("Provide a Valid SELECT Query");
                    return;
                }
                selecttable(query);
                break;
            case "COMMIT":
                if (loggedInUser.getCurrentDatabase() == null) {
                    return;
                }
                if (query.split(" ").length > 1) {
                    System.out.println("Provide a Valid COMMIT Query");
                    return;
                }
                queryService.saveDB();
                commitflag = true;
                break;
            case "END":
                if (loggedInUser.getCurrentDatabase() == null) {
                    return;
                }
                // Parsing the End transaction query
                if (query.split(" ").length > 2 || !query.split(" ")[1].equalsIgnoreCase("TRANSACTION")) {
                    System.out.println("Provide a valid END TRANSACTION Command");
                    return;
                }
                inTransaction = false;
                System.out.println("Transaction Ended");
                //Check if commit is done, else the changes are not saved
                if (!commitflag)
                    System.out.println("Changes are not saved");
                //Unlocking the Database and clearing the flags
                loggedInUser.setDblock(false);
                loggedInUser.setCurrentDatabase(null);
                loggedInUser.setDbcreationflag(false);
                System.out.println("Unlocked Database successfully");
                currentDB = new Database();
                return;
            case "ROLLBACK":
                if (loggedInUser.getCurrentDatabase() == null) {
                    System.out.println("Nothing to rollback");
                    return;
                }
                if (query.split(" ").length > 1) {
                    System.out.println("Provide a valid ROLLBACK command");
                    return;
                }
                loggedInUser.setDblock(false);
                loggedInUser.setCurrentDatabase(null);
                loggedInUser.setDbcreationflag(false);
                rollbackflag = true;
                System.out.println("Roll backed the changes in the Database:" + currentDB.getDbname());
                currentDB = new Database();
                return;
            default:
                System.out.println("Unknown command");
        }
    }


    private void selecttable(String query) {
        String tableName = query.substring("SELECT * FROM ".length()).replace(";", "").trim();
        queryService.selectTable(tableName);
    }

    private void insertrow(String query) {
        String tableName = query.substring(query.indexOf("INTO") + 5, query.indexOf("(")).trim();

        // Extracting the column names
        String columnsPart = query.substring(query.indexOf("(") + 1, query.indexOf(") VALUES"));
        String[] columns = columnsPart.split(",\\s*");

        // Extracting the values
        String valuesPart = query.substring(query.lastIndexOf("(") + 1, query.lastIndexOf(")"));
        String[] values = valuesPart.split(",\\s*");

        // Populating the map with column-value pairs which is to be added in the Table object
        Map<String, Object> row = new HashMap<>();
        for (int i = 0; i < columns.length; i++) {
            // Assuming values are simple and not containing internal commas or quotes
            row.put(columns[i], values[i].replaceAll("'", ""));
        }

        queryService.insertrow(tableName, row);
    }

    private void updaterow(String query) {
        String[] parts = query.split(" SET ", 2);
        String tablename = parts[0].substring("UPDATE ".length()).trim();

        String setClause = parts[1].substring(0, parts[1].indexOf(" WHERE "));
        String[] setPairs = setClause.split(",\\s*");

        Map<String, String> update = new HashMap<>();
        for (String pair : setPairs) {
            String[] keyValue = pair.split("=");
            update.put(keyValue[0].trim(), keyValue[1].trim().replaceAll("^'|'$", ""));
        }

        String whereClause = parts[1].substring(parts[1].indexOf(" WHERE ") + " WHERE ".length()).trim();
        String[] wherePairs = whereClause.split("=");

        Map<String, String> present = new HashMap<>();
        present.put(wherePairs[0].trim(), wherePairs[1].trim().replaceAll("^'|'$", ""));
        queryService.updaterow(tablename, update, present);
    }
    // Processing the transaction query wise
    public void processTransaction(String[] queries, User user) throws IOException {
        loggedInUser = user;
        for (String query : queries) {
            //If there is a rollback command, then the remaining queries are not processed
            if (inTransaction && !rollbackflag) {
                processQuery(query);
            }
        }
    }

    private void createDatabase(String query) throws IOException {
        String[] parts = query.split(" ");
        queryService.createDatabase(parts[2]);
    }

    private void createTable(String query) {

        // Removing the semicolon and split the query
        String[] parts = query.split("\\(");

        // Getting table name from the query
        String tableName = parts[0].split("\\s+")[2];

        //Storing the column names and their data types in a HashMap
        Map<String, String> columnDetails = new HashMap<>();
        String[] columnDefinitions = parts[1].replace(")", "").split(",");

        for (String columnDefinition : columnDefinitions) {
            String[] column = columnDefinition.trim().split("\\s+");
            if (column.length == 2) {
                String columnName = column[0];
                String dataType = column[1];
                columnDetails.put(columnName, dataType);
            }
        }
        //Loading the column details of all the tables in the table schemas object which is used for query parsing
        queryService.createTable(tableName, columnDetails);
        TableDetails td = new TableDetails();
        td.setTableName(tableName);
        td.setColumnDetails(columnDetails);
        tableschemas.add(td);
    }
}

