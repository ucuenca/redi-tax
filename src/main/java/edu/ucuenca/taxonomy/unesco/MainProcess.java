/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.taxonomy.unesco;

import edu.ucuenca.taxonomy.entitymanagement.RecognizeArea;
import edu.ucuenca.taxonomy.unesco.tinkerpop.IOGraph;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class MainProcess {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        RecognizeArea ra = new RecognizeArea(IOGraph.read("coco2.graphml"));
        List<String> keywords = Arrays.asList("linked data", "semantic web", "computer science");
        ra.recognize(keywords);
    }

}
