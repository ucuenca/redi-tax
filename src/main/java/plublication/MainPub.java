/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plublication;

import edu.ucuenca.taxonomy.unesco.tinkerpop.IOGraph;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author joe
 */
public class MainPub {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

//        System.out.print("Ejecutando:");
        Preprocessing p = Preprocessing.getInstance();
//        Object response = p.detectLanguage("caracteres de prueba del servicio de  cortical");
//
//        System.out.println("Cortical Lang:" + response);
//
//        Object response2 = p.traductor("Hola bola, vamos a la fiesta");
//        System.out.println("Trad:" + response2);
//
//        List response3 = (ArrayList) p.detectDbpediaEntities("First documented in the 13th century, Berlin was the capital of the Kingdom of Prussia (1701–1918), the German Empire (1871–1918), the Weimar Republic (1919–33) and the Third Reich (1933–45). Berlin in the 1920s was the third largest municipality in the world. After World War II, the city became divided into East Berlin -- the capital of East Germany -- and West Berlin, a West German exclave surrounded by the Berlin Wall from 1961–89. Following German reunification in 1990, the city regained its status as the capital of Germany, hosting 147 foreign embassies.\n"
//                + "First documented in the 13th century, Berlin was the capital of the Kingdom of Prussia (1701–1918), the German Empire (1871–1918), the Weimar Republic (1919–33) and the Third Reich (1933–45). Berlin in the 1920s was the third largest municipality in the world. After World War II, the city became divided into East Berlin -- the capital of East Germany -- and West Berlin, a West German exclave surrounded by the Berlin Wall from 1961–89. Following German reunification in 1990, the city regained its status as the capital of Germany, hosting 147 foreign embassies. ");
//        //System.out.println("Entities:"+response3); 
        try {
            Graph programming = IOGraph.read("coco3.graphml");
            p.entitiesEnrichment("http://dbpedia.org/resource/Computer_science");
        } catch (RepositoryException ex) {
            Logger.getLogger(MainPub.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
