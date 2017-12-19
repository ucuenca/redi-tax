/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.taxonomy.unesco.tinkerpop;

//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        Graph g = new SailRepo.createTinkerGraph();
//        GremlinPipeline pipe = new GremlinPipeline();
//        pipe.start(g.getVertex(1)).out("knows").property("name");
//        Graph g = TinkerFactory.createModern();
//        new LinkedDataSailGraph(new MemoryStoreSailGraph());
        Graph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        Vertex v1 = g.addV("person").property(T.id, 1).property("name", "marko").property("age", 29).next();
        Vertex area = g.addV("area").property(T.id, 2).property("name", "skos:120323").next();
        Vertex subject = g.addV("subject").property(T.id, "dbpedia:programming_languages").next();

        System.out.println(g.V(1).values("name"));
        Vertex lop = graph.addVertex("name", "lop", "lang", "java");
        area.addEdge("owl:sameAs", subject, "weight", 1d);

    }

}
