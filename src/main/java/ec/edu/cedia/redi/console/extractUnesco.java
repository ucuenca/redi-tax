/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.console;

import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.unesco.UnescoDataSet;
import ec.edu.cedia.redi.unesco.model.UnescoHierarchy;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author joe
 */
public class extractUnesco {

  /**
   * @param args the command line arguments
   */
  
  
  
  public static void main(String[] args) {
   List<UnescoHierarchy> dataset = UnescoDataSet.getInstance().getDataset();
   //System.out.print (dataset);
   HashMap<String, String> hm = new HashMap ();
   for (  UnescoHierarchy d : dataset) {
     hm.put(d.getLevel4(), d.getLevels6());
   System.out.println (d.getLevel4() + ": " + d.getLevels6());
   }
   writeToCSVRaw( hm , "Unesco");
   
   
   
  }
  
  
     public static void writeToCSVRaw(HashMap<String, String> hm , String name)
    { final String CSV_SEPARATOR = ",";
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name+".csv"), "UTF-8"));
            for (String k    : hm.keySet()){
            StringBuffer oneLine = new StringBuffer();
            oneLine.append(k);
            oneLine.append(CSV_SEPARATOR);
            oneLine.append(hm.get(k).replace(",",";"));
            bw.write(oneLine.toString());
            bw.newLine();
            
            }
            
            bw.flush();
            bw.close();
            System.out.print ("Create file");
          
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
    }
  
}
