/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tinkerpop;

//import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import ec.edu.cedia.redi.utils.IOGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;
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

//        Configurations configs = new Configurations();
//        Configuration config = configs.properties(new File("config.properties"));
        Graph graph = TinkerGraph.open();
        GraphTraversalSource g = graph.traversal();
        Vertex v1 = graph.addVertex(T.label, "person", T.id, 1, "name", "marko", "age", 29);
        Vertex area = graph.addVertex(T.label, "area", T.id, 2, "name", "skos:120323");
        Vertex otro = graph.addVertex(T.label, "area", T.id, "original", "name", "skos:120323");
        Vertex subject = graph.addVertex(T.label, "subject", T.id, "dbpedia:programming_languages");

        System.out.println(g.V(1).values("name").next());
        System.out.println(g.V(1).next());
        System.out.println(g.V("original").properties().next());
        System.out.println(g.V("original").hasNext());
        System.out.println(g.V("falso").hasNext());
        System.out.println(g.V(1).id().next());
        //System.out.println(g.V("name","marko").next());
        //  System.out.println(g.V().properties().next());
        Vertex lop = graph.addVertex("name", "lop", "lang", "java");
        area.addEdge("owl:sameAs", subject, "weight", 1.0, T.id, 8);
        // area.addEdge("owl:sameAs", subject, "weight", 0.5 , T.id , 8);

        System.out.println(g.E().next());
        System.out.println(g.E().hasId(8).next());
        System.out.println(g.E(8).properties().next());

        area.addEdge("owl:sameAs", subject, "weight", 1d);
        IOGraph.write(graph, "sirve.graphml");
        Graph graphT = IOGraph.read("coco2.graphml");

        GraphTraversalSource g1 = graphT.traversal();
        g1.V("http://dbpedia.org/resource/Computer_science")
                .repeat(bothE().bothV().simplePath())
                .until(hasId("http://dbpedia.org/resource/Category:XML-based_programming_languages"))
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").select("cost", "p").limit(10).next(100).stream().forEach(System.out::println);

        String n1 = "http://dbpedia.org/resource/Computer_science";
        String n2 = "http://dbpedia.org/resource/Category:XML-based_programming_languages";
        System.out.println("");
        System.out.println("");
        System.out.println("#2");
        g.V(n1).repeat(bothE().bothV().simplePath()).until(hasId(n2)).
                path().by(constant(0.0)).by("weight").map(unfold().sum())
                .next(10).stream().forEach(System.out::println);
        System.out.println("");
        System.out.println("");
        System.out.println("#3");
        g1.V(n1)
                .repeat(outE().inV().outE().inV().simplePath()).until(hasId(n2)).
                path()
                .by(coalesce(values("weight"), constant(0.0))).
                map(unfold().sum()).next(10).stream().forEach(System.out::println);
    }

}
