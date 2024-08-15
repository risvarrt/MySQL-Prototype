package model;


import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// The Database object which stores the list of table
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Database implements Serializable {
    private String dbname;
    private List<Table> tables = new ArrayList<>();


    public void addTable(Table t) {
        tables.add(t);
    }

    /*
      Creates a new JSON file in the directory.
      Attempts to create a new json file for the database if it does not already exist.
     */
    public void createDB() {

        try {
            File file = new File("C:\\Users\\risva\\IdeaProjects\\MYSQL_prototype\\Databases\\" + dbname + ".json");
            file.createNewFile();
            System.out.println("Database created successfully with the name: " + dbname);
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(String.valueOf(e));
        }
    }

    // Updates a table in the database with new data.
    public void updatetable(Table updatedtable, List<Table> tables) {
        for (Table table : tables) {
            if (table.getTablename().equalsIgnoreCase(updatedtable.getTablename())) {
                table.setRows(updatedtable.getRows());
            }
        }
    }
    // Gets the specific table from the DB
    public Table getTableByName(String name) {
        for (Table table : tables) {
            if (table.getTablename().equalsIgnoreCase(name)) {
                return table;
            }
        }
        return null;
    }

    // Saves the current DB object used in the service class to a JSON file. It happens when COMMIT is done
    public void saveDB(Database db) {

        try {
            Gson gson = new Gson();
            FileWriter writer = new FileWriter("C:\\Users\\risva\\IdeaProjects\\MYSQL_prototype\\Databases\\" + db.getDbname() + ".json");
            System.out.println(db);
            gson.toJson(db, writer);
            writer.flush();
            System.out.println("Database Saved Successfully");

        } catch (JsonIOException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
