/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package corticalClasification;

import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.RediRepository;
import ec.edu.cedia.redi.unesco.UnescoNomeclature;
import ec.edu.cedia.redi.unesco.UnescoNomeclatureConnection;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
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
        List<URI> listUris = unesco.twoDigitResources();

        Preprocessing p = Preprocessing.getInstance();

        RediRepository rp = RediRepository.getInstance();
        Redi r = new Redi(rp);
        Author actual = null;
        List<Author> authors = r.getAuthors();
        for (Author a : authors) {
            if (a.getURI().toString().contains("saquicela")) {
                actual = a;
            }
        }

        // System.out.print (p.CompareText("Computer applications , Archive automation, Artificial intelligence, Expert systems,  Pattern recognition, Robotics",
        //        "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration"));
        // String userKeywords = "TECHNOLOGIES OF INFORMATION AND COMMUNICATION, Big Data, ontologies, NATURAL LANGUAGE PROCESSING, WEB SERVICE, Data Integration";
        System.out.print("AUTOR:" + actual.getURI());
        String userKeywords = actual.getKeywords();

        String bestCategory = "";
        Double bestScore = 0.0;
        for (URI l : listUris) {
            String arrayLabels = "";
            System.out.println("*********");
            String label2 = unesco.label(l, "en").getLabel();
            System.out.println(label2);
            List<URI> listN = unesco.narrow(l);
            for (URI nl : listN) {
                String label4 = unesco.label(nl, "en").getLabel();
                System.out.println(label4);
                // arrayLabels = arrayLabels + ", " + label4;
                String arrayLabels4 = label4;
                if (!label4.contains("Other")) {
                    //  if (listN.size() < 30) {
                    List<URI> listNl = unesco.narrow(nl);
                    int count = 0;
                    for (URI nnl : listNl) {
                        String label6 = unesco.label(nnl, "en").getLabel();
                        if (!label6.contains("Other")) {
                            System.out.println(label6);
                            // arrayLabels = arrayLabels + ", " + label6.replace(".", "");
                            arrayLabels4 = arrayLabels4 + ", " + StringUtils.stripAccents(label6);
                            count++;

                            //     }
                        }
                    }
                    
                     Double val ;
                   Object number = p.CompareText(arrayLabels4, userKeywords, "weightedScoring");
                   if (number  instanceof Double) {
                          val = (Double) number;
                   }else {
                        val = ((BigDecimal) number ).doubleValue();
                   }
                   
                   
                  
                    if (bestScore.doubleValue() < val.doubleValue()) {
                        bestScore = val;
                        bestCategory = label4;
                    }
                    System.out.println("Score : " + label4 + "-" + val);

                }
            }
            //   Double val = (Double) p.CompareText(arrayLabels, userKeywords, "weightedScoring");

        }

        System.out.print("La mejor categoria es :" + bestCategory + bestScore);
        conn.close();
    }

}
