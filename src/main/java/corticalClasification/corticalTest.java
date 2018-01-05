/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package corticalClasification;

import ec.edu.cedia.redi.unesco.UnescoNomeclature;
import ec.edu.cedia.redi.unesco.UnescoNomeclatureConnection;
import java.util.List;
import org.openrdf.model.URI;
import plublication.Preprocessing;

/**
 *
 * @author joe
 */
public class corticalTest {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
       UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();
       UnescoNomeclature unesco = new UnescoNomeclature(conn);
        List<URI> listUris =   unesco.twoDigitResources();
        
        Preprocessing p = Preprocessing.getInstance();
       
       // System.out.print (p.CompareText("Computer applications , Archive automation, Artificial intelligence, Expert systems,  Pattern recognition, Robotics",
        //        "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration"));
         String userKeywords = "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration";
        String bestCategory ="";
        Double bestScore = 0.0;
        for ( URI l : listUris ) {
            String arrayLabels = "";
            System.out.println ("*********");
            String label2 = unesco.label(l, "en").getLabel();
            System.out.println (label2);
             List<URI> listN = unesco.narrow(l);
             for (URI nl : listN ){
                String label4  = unesco.label(nl, "en").getLabel();
              System.out.println (label4);
              arrayLabels= arrayLabels+", "+label4;
               if (listN.size() < 50){ 
              List<URI> listNl = unesco.narrow(nl);
               for (URI nnl : listNl ){
                String label6  = unesco.label(nnl, "en").getLabel();
                if (!label6.contains("Other")){
                System.out.println (label6);
                arrayLabels= arrayLabels+", "+label6.replace(".", "");
                }
               }
               }
             }
            Double val =   (Double) p.CompareText(arrayLabels , userKeywords);
            if (bestScore > val) {
            bestScore = val;
            bestCategory = label2;
            }
            System.out.println ("Score : "+label2+"-"+val);
        }
        
        System.out.print ("La mejor categoria es :"+bestCategory);
        conn.close();
    }
    
}
