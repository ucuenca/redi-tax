/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.utils;

import ec.edu.cedia.redi.Author;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plublication.Preprocessing;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public final class Utils {

 
   private static final String CSV_SEPARATOR = ",";
    private static final Preprocessing cortical = Preprocessing.getInstance();
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static String keepUniqueTopics(String str) {
        LinkedList<String> keywords = new LinkedList<>(Arrays.asList(str.split(";")));

        str = keywords.stream()
                .map(Utils::cleanString)
                .distinct()
                .collect(Collectors.joining(";"));

        return str;
    }

    public static String cleanString(String s) {
        s = s.toLowerCase();
        // clean everything in parenthesis
        s = s.replaceAll("\\(.*\\)", "");
        s = s.trim();

        return s;
    }

    public static String processTopics(String topics) {
        topics = cleanString(topics);
        String result = topics;
        try {
            String lang = String.valueOf(cortical.detectLanguage(topics));
            if (!"en".equals(lang)) {
                result = String.valueOf(cortical.traductor(topics));
                result = JSONAraytoString(result);
            }
        } catch (IOException ex) {
            log.error("Cannot translate/detect language for '" + topics + "'", ex);
        }
        return keepUniqueTopics(result);
    }

    private static String JSONAraytoString(String json) {
//        JSONArray array;
        String result = "";
//        try {
//            array = new JSONArray(json);
//            for (int i = 1; i < array.length(); i++) {
//                result += array.getString(i) + ";";
//            }
//        } catch (JSONException ex) {
//            log.error("I don't understand the json document: " + json, ex);
//        }
        // TODO: parse json correctly.
        result = json.replaceFirst("\\[\"context, ", "").replace("\"]", "");
        return result;
    }
    
     public  List<List<String>> readToCSV(String dir)
    {   List<List<String>> records = new ArrayList<>();
         try (BufferedReader br = new BufferedReader(new FileReader(dir))) {
         String line;
         while ((line = br.readLine()) != null) {
             String[] values = line.split(",");
              records.add(Arrays.asList(values));
       }
         return records;
       } catch (FileNotFoundException ex) {
       java.util.logging.Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       } catch (IOException ex) {
        java.util.logging.Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
       }
     return null;
        
    }
    
    
     
    public  void writeToCSV(ArrayList<Author> authorList)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("products.csv"), "UTF-8"));
            for (Author author : authorList)
            {   System.out.print (author.getURI());
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(author.getURI() == null ? "" : author.getURI());
                oneLine.append(CSV_SEPARATOR);
                String k = author.getKeywords() != null &&  author.getKeywords().length() > 0 ?  author.getKeywords().replace(","," ").trim() : "" +" "+ author.getTopics() != null && author.getTopics().length() > 0? author.getTopics().replace(","," ").trim() : "";
                oneLine.append(k);
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
    
    
     public  void writeToCSVRaw(ArrayList<Author> authorList , String name)
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name+".csv"), "UTF-8"));
            for (Author author : authorList)
            {   System.out.print (author.getURI());
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(author.getURI() == null ? "" : author.getURI());
                oneLine.append(CSV_SEPARATOR);
                String k = author.getKeywords() != null &&  author.getKeywords().length() > 0 ?  author.getKeywords().replace(",",";").trim() : "" ; 
                oneLine.append(k);
                oneLine.append(CSV_SEPARATOR);
                String t = author.getTopics() != null && author.getTopics().length() > 0? author.getTopics().replace(",",";").trim() : "";
                oneLine.append(t);
                oneLine.append(CSV_SEPARATOR);
                String a = author.getOthers() != null && author.getOthers().length() > 0? author.getOthers().replace(",",";").trim() : "";
                oneLine.append(a);
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
