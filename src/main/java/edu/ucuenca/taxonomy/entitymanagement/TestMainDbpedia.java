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
import java.util.Map;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.incr;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.bothE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.constant;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author joe
 */
public class TestMainDbpedia {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        try (Graph graph = StardogConnection.intance().graph()) {
            //Vertex m = graph.addVertex("id", "identifier1", "label", "nodo");
            GraphTraversalSource gt = graph.traversal();
            //gt.getGraph().get().addVertex("id", "identifier2", "label", "nodo2");
            EntityExpansion ex = new DBPediaExpansion(graph.traversal());

            List urisList = new ArrayList();
           // urisList.add(new URIImpl("http://dbpedia.org/resource/Computer_science"));
             urisList.add(new URIImpl("http://dbpedia.org/page/Scientific_theory"));
            
            
            //ex.expand(urisList, 3);
            
          /*  Object uri = gt.V().has("id","http://dbpedia.org/page/Scientific_theory")
                .repeat(bothE().bothV().simplePath())
                .until(has("type", "unesco"))
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").order().by(incr).select("id").limit(1).next();*/
           Object u =  gt.V().has("label","unesco").next();
              System.out.println ("Resultados");
            System.out.println (u);
            
            //Object p = gt.V().has("id","http://dbpedia.org/page/Scientific_theory").next();
             Object p = gt.V().has("id","http://dbpedia.org/page/Scientific_theory")
                 .repeat(bothE().bothV().simplePath())
                .until(has("label", "unesco"))
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").select("cost", "p").next();
              /*  .repeat(bothE().bothV().simplePath())
                .until(has("type", "unesco")).
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").select("cost", "p").next();*/
            System.out.println ("Resultados");
            System.out.println (p);
        }

    }

}
