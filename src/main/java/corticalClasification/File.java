/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package corticalClasification;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openrdf.model.URI;

/**
 *
 * @author joe
 */
public class File {
   public void writeFile () throws IOException {
   FileWriter writer = new FileWriter("/Users/artur/tmp/csv/sto1.csv");

    List<String> test = new ArrayList<>();
    test.add("Word1");
    test.add("Word2");
    test.add("Word3");
    test.add("Word4");

    String collect = test.stream().collect(Collectors.joining(","));
    System.out.println(collect);

    writer.write(collect);
    writer.close();
   } 
   
   public void writeMapCSV (Map<String, String[]> myHashMap ) {
   String eol = ";";

try (Writer writer = new FileWriter("outputClasification.csv")) {
  for (Map.Entry<String, String[]> entry : myHashMap.entrySet()) {
     String[] val= myHashMap.get(entry.getKey());
    System.out.print("Insert "+entry.getKey());
    writer.append(entry.getKey())
          .append(',')
          .append(val[0])
          .append(',')
          .append(val[1])  
          .append(eol);
  }
} catch (IOException ex) {
  ex.printStackTrace(System.err);
}
   
   
   }
    
}
