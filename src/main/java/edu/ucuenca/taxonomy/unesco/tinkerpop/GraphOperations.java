/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.taxonomy.unesco.tinkerpop;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 *
 * @author joe
 */
public class GraphOperations {

    Graph graph;
    GraphTraversalSource g;

    public Graph getGraph() {
        return graph;
    }

    public GraphOperations(Graph graph) {
        this.graph = graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public GraphTraversalSource getG() {
        return g;
    }

    public void setG(GraphTraversalSource g) {
        this.g = g;
    }

    public void RDF2Graph(String subject, String predicate, String Object) {
        Map<String, String> mt = getMapType();
        Map<String, Double> mw = this.getMapW();
        if (mt.containsKey(predicate)) {
            if ("V".equals(mt.get(predicate))) {
                Vertex v1 = insertV(subject, "node");
                Vertex v2 = insertV(Object, "node");

                insertE(v1, v2, predicate, mw.get(predicate));

            } else if ("P".equals(mt.get(predicate))) {
                Vertex v1 = insertVProperty(subject, "node", predicate, Object);

            }

        }

    }

    public GraphOperations() {
        graph = TinkerGraph.open();
        g = graph.traversal();
    }

    public Vertex insertV(String id, String type) {

        if (g.V(id).hasNext()) {
            Vertex v = g.V(id).next();

            return v;
        } else {
            return g.addV(type).property(T.id, id).next();
        }
    }

    public Vertex insertVProperty(String id, String type, String propertyName, String pValue) {

        if (g.V(id).hasNext()) {
            Vertex v = g.V(id).next();
            v.property(propertyName, pValue);
            return v;
        } else {
            return g.addV(type).property(T.id, id).property(propertyName, pValue).next();
        }
    }

    public void insertE(Vertex v1, Vertex v2, String propertyName, Double w) {

        String codeE = getMD5(v1.id().toString() + v2.id().toString() + propertyName);

        if (!g.E(codeE).hasNext()) {
            v1.addEdge(propertyName, v2, T.id, codeE, "weight", w);
        }
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);

            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Map getMapW() {
        Map<String, Double> mw = new HashMap<>();

        mw.put("http://purl.org/dc/terms/subject", 1.0);
        mw.put("http://www.w3.org/2004/02/skos/core#broader", 1.0);
        mw.put("http://dbpedia.org/ontology/genre", 0.5);
        mw.put("http://dbpedia.org/ontology/wikiPageRedirects", 0.8);
        mw.put("http://www.w3.org/2004/02/skos/core#related", 1.0);
        mw.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", 0.7);
        mw.put("http://www.w3.org/2002/07/owl#Thing", -0.5);
        return mw;
    }

    public Map getMapType() {
        Map<String, String> mt = new HashMap<>();

        mt.put("http://purl.org/dc/terms/subject", "V");
        mt.put("http://www.w3.org/2004/02/skos/core#broader", "V");
        mt.put("http://dbpedia.org/ontology/genre", "V");
        mt.put("http://dbpedia.org/ontology/wikiPageRedirects", "V");
        mt.put("http://www.w3.org/2004/02/skos/core#related", "V");
        mt.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "P");
        mt.put("http://www.w3.org/2002/07/owl#Thing", "P");
        mt.put("http://www.w3.org/2000/01/rdf-schema#label", "P");
        return mt;
    }

}
