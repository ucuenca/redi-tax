/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.entitymanagement;

import ec.edu.cedia.redi.entitymanagement.api.EntityExpansion;
import ec.edu.cedia.redi.unesco.UnescoPopulation;
import ec.edu.cedia.redi.utils.IOGraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.bothE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.constant;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.count;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
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
        Graph graph;
        // try (Graph graph = StardogConnection.instance().graph()) {
        graph = IOGraph.read("test.graphml");
        if (graph == null) {
            graph = TinkerGraph.open();
        }

        //Vertex m = graph.addVertex("id", "identifier1", "label", "nodo");
        GraphTraversalSource gt = graph.traversal();

        UnescoPopulation u = new UnescoPopulation(gt);
        //   u.populate();
        //gt.getGraph().get().addVertex("id", "identifier2", "label", "nodo2");
        EntityExpansion ex = new DBPediaExpansion(graph.traversal());

        List urisList = new ArrayList();
        urisList.add(new URIImpl("http://dbpedia.org/resource/Computer_science"));
        // urisList.add(new URIImpl("http://dbpedia.org/resource/Probability"));

        /*    Vertex v1 =   gt.V().has("id", "http://dbpedia.org/resource/Category:Dimensionless_numbers").next();c
         Vertex v2 =   gt.V().has("id", "http://dbpedia.org/resource/Category:Dimensionless_numbers").next();
         Vertex v3 =   gt.V().has("id", "http://dbpedia.org/resource/Category:Dimensionless_numbers").next();
         Vertex v4 =  gt.V().has("id", "http://dbpedia.org/resource/Category:Dimensionless_numbers").next();
         Vertex v5 =   gt.V().has("id", "http://dbpedia.org/resource/Category:Dimensionless_numbers").next();
         System.out.print (v5);*/
        // ex.expand(urisList, 2);
        // IOGraph.write(graph, "test.graphml");
        System.out.print("Path");
        Iterator p = gt.V().has("id", "http://dbpedia.org/resource/Linked_data") //.bothE().bothV().id().next(10).iterator();
                .repeat(bothE().bothV().simplePath()).until(has("label", "unesco")).path()
                .group().by(count()).limit(1).next(10).listIterator();

        while (p.hasNext()) {
            System.out.println(p.next());
        }

        /*  Iterator p1 = gt.V().has("id", "http://dbpedia.org/resource/Computer_science").repeat(bothE().bothV().simplePath()).until(has("id","http://dbpedia.org/resource/Category:Computing")).path()
              .by(constant(0.0)).by("weight").map(unfold().sum()).next(10).iterator();*/
        Iterator p1 = gt.V().has("id", "http://dbpedia.org/resource/Linked_data").repeat(bothE().bothV().simplePath()).until(has("label", "unesco")).
                path().as("p").
                map(unfold().coalesce(values("weight"), constant(0.0)).sum()).as("cost").
                select("cost", "p").next(10).iterator();

        System.out.print("Path2");
        while (p1.hasNext()) {
            System.out.println(p1.next());
        }
        //http://redi.cedi.edu.ec/resource/vertex-4a3d882c-8f34-4225-abd8-ca8d520a309d id

        //http://dbpedia.org/resource/Category:Electrical_engineering
        /*  Object uri = gt.V().has("id","http://dbpedia.org/page/Scientific_theory")
                .repeat(bothE().bothV().simplePath())
                .until(has("type", "unesco"))
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").order().by(incr).select("id").limit(1).next();*/
 /*  
           Object u =  gt.V("http://redi.cedi.edu.ec/resource/vertex-2c0f9e26-1ce4-4ce3-a314-f429186ffb67").next();
              System.out.println ("Resultados2");
            System.out.println (u);
            
            
         
            
            
                  //.limit(1).next(10).listIterator();
            
           // Object p = gt.V().properties().next(1);
            /* Iterator p = gt.V("http://redi.cedi.edu.ec/resource/vertex-44fc601e-1ec8-4137-bb4f-049dccd4c62a")
                       .repeat(bothE().bothV().simplePath()).until(gt.V("http://redi.cedi.edu.ec/resource/vertex-2c0f9e26-1ce4-4ce3-a314-f429186ffb67"))
                       .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                       .as("cost").next(10).listIterator();*/
        //.select("cost", "p").next(10).listIterator();
        //  
        /*  while(p.hasNext()) {
                 System.out.println(p.next());
             }*/
 /*  .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                      .as("cost").select("cost", "p").next(0);*/
 /*    Object p = gt.V().has("id","http://dbpedia.org/page/Scientific_theory")
                 .repeat(bothE().bothV().simplePath())
                .until(has("label", "unesco"))
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").select("cost", "p").next(); */
 /*  .repeat(bothE().bothV().simplePath())
                .until(has("type", "unesco")).
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").select("cost", "p").next();*/
        // System.out.println ("Resultados");
        //  System.out.println (p);
    }

}
