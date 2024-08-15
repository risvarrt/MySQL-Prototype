package model;


import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
  It is table object which is stored in a database, including its name, column definitions, and rows of data.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Table implements Serializable {

    private String tablename;
    private Map<String, String> columnDefinitions = new HashMap<>();
    private List<Map<String, Object>> rows = new ArrayList<>();

    public Table(String tablename) {
        this.tablename = tablename;
    }

    public void addColumnDefinition(String columnName, String columnType) {
        columnDefinitions.put(columnName, columnType);
    }

    public void insertRow(Map<String, Object> row) {
        rows.add(row);
    }

    //Loading all the tables from a specified database JSON file.
    public List<Table> loadtables(String dbname) {
        try {
            File jsonFile = new File("C:\\Users\\risva\\IdeaProjects\\MYSQL_prototype\\Databases\\" + dbname + ".json");
            Gson gson = new Gson();
            Database db = new Database();
            try (FileReader reader = new FileReader(jsonFile)) {
                db = gson.fromJson(reader, Database.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Now, the database object contains all tables with their data
            return db.getTables();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
