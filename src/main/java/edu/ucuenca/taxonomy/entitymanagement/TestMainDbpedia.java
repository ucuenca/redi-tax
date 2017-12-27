/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucuenca.taxonomy.entitymanagement;

import edu.ucuenca.taxonomy.entitymanagement.api.EntityExpansion;
import edu.ucuenca.taxonomy.unesco.dababase.StardogConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;


/**
 *
 * @author joe
 */
public class TestMainDbpedia {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        StardogConnection c = StardogConnection.intance();

    Graph graph = c.graph();
    Vertex  m =  graph.addVertex("id","identifier1", "label" ,"nodo");
     GraphTraversalSource gt =  graph.traversal();
     gt.getGraph().get().addVertex("id","identifier2", "label" ,"nodo2");
    EntityExpansion ex = new DBPediaExpansion(graph.traversal());
   
             List urisList= new ArrayList ();
            urisList.add(new URIImpl("http://dbpedia.org/resource/Computer_science"));
            //URI p=vf.createURI("http://p");
            ex.expand(urisList , 3);
        
      
        try {
            c.close();
        } catch (Exception ex1) {
            Logger.getLogger(TestMainDbpedia.class.getName()).log(Level.SEVERE, null, ex1);
        }
    
    
    
    }
    
}
