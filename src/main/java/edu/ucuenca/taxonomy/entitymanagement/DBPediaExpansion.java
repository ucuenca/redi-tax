/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ucuenca.taxonomy.entitymanagement;

import edu.ucuenca.taxonomy.entitymanagement.api.EntityExpansion;
import static edu.ucuenca.taxonomy.unesco.tinkerpop.GraphOperations.getMD5;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class DBPediaExpansion implements EntityExpansion {

    private final static List<URI> properties = new ArrayList<>();
    private final static Map<URI, NodeConfiguration> m = new HashMap<>();
    private final static String DBPEDIA_CONTEXT = "https://dbpedia.org/sparql";
    private final Logger log = LoggerFactory.getLogger(DBPediaExpansion.class);
    private final GraphTraversalSource g;

    static {
        properties.add(new URIImpl("http://purl.org/dc/terms/subject"));
        properties.add(new URIImpl("http://www.w3.org/2004/02/skos/core#broader"));

        m.put(new URIImpl("http://purl.org/dc/terms/subject"), new NodeConfiguration(1.0, NodeType.Vertex));
        m.put(new URIImpl("http://www.w3.org/2004/02/skos/core#broader"), new NodeConfiguration(1.0, NodeType.Vertex));
        m.put(new URIImpl("http://dbpedia.org/ontology/genre"), new NodeConfiguration(0.5, NodeType.Vertex));
        m.put(new URIImpl("http://dbpedia.org/ontology/wikiPageRedirects"), new NodeConfiguration(0.8, NodeType.Vertex));
        m.put(new URIImpl("http://www.w3.org/2004/02/skos/core#related"), new NodeConfiguration(1.0, NodeType.Vertex));
        m.put(new URIImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), new NodeConfiguration(0.7, NodeType.Edge));
        m.put(new URIImpl("http://www.w3.org/2002/07/owl#Thing"), new NodeConfiguration(-0.5, NodeType.Edge));
        m.put(new URIImpl("http://www.w3.org/2000/01/rdf-schema#label"), new NodeConfiguration(1.0, NodeType.Edge));
    }

    public DBPediaExpansion(GraphTraversalSource g) {
        this.g = g;
    }

    @Override
    public Graph expand(List<URI> uris, String label, int lvl) {
        Repository repository = null;
        try {
            repository = new SPARQLRepository(DBPEDIA_CONTEXT);
            repository.initialize();
            for (URI uri : uris) {
                queryDbpedia(repository.getConnection(), uri, lvl);
                Vertex v;
                if ( !g.V().has("id", uri).hasNext()) {
                //g.V(uri).property("expand", true).property("type", label);
                    v = this.insertV(uri.stringValue() , label);
                  
                } else {
                   v = g.V().has("id", uri).next();
                }
                    v.property("expand", true);
            }
        } catch (Exception ex
                ) {
            log.error("Error executing query", ex);
        } finally {
            try {
                repository.shutDown();
            } catch (RepositoryException ex) {
                java.util.logging.Logger.getLogger(DBPediaExpansion.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    @Override
    public Graph expand(List<URI> uris, String label) {
        return expand(uris, label, DEFAULT_LVL);
    }

    private void queryDbpedia(RepositoryConnection dbpediaConnection, URI uri, int level) throws Exception {
        if (level < 1) {
            return;
        }

        String query = String.format("DESCRIBE <%s> ", uri.stringValue());
        GraphQueryResult result = dbpediaConnection.prepareGraphQuery(QueryLanguage.SPARQL, query, DBPEDIA_CONTEXT).evaluate();
        while (result.hasNext()) {
            Statement stmt = result.next();
            log.debug("Actual lvl: {}, Statement: {}", uri, stmt);
            registerStatement(stmt);
            if (uri.equals(stmt.getObject())) {
                continue;
            }
            if (properties.contains(stmt.getPredicate())) {
                log.debug("New lvl", stmt.getObject());
                queryDbpedia(dbpediaConnection, (URI) stmt.getObject(), level - 1);
            }
        }
    }

    private void registerStatement(Statement stmt) {
        if (m.containsKey(stmt.getPredicate())) {
            String s = stmt.getSubject().stringValue();
            String p = stmt.getPredicate().stringValue();
            String o = stmt.getObject().stringValue();
            if (NodeType.Vertex == m.get(stmt.getPredicate()).type) {
                Vertex v1 = insertV(s, "node");
                Vertex v2 = insertV(o, "node");
                String codeE = getMD5(v1.id().toString() + v2.id().toString() + p);
                if (!g.E().has("id",codeE ).hasNext()) {
                       v1.addEdge(p, v2, "id", codeE , "weight", m.get(stmt.getPredicate()).weight);
//                    g.addE(p).from(v1).to(v2)
//                            .property(T.id, codeE)
//                            .property("weight", m.get(stmt.getPredicate()).weight);
                }
            } else if (NodeType.Edge == m.get(stmt.getPredicate()).type) {
                if (g.V().has("id",s).hasNext()) {
                    Vertex v = g.V(s).next();
                    v.property(p, o);
                } else {
                      Vertex v = insertV (s , "node");
                      v.property(p, o);
//                    g.addV("node").property(T.id, s).property(p, o);
                }
            }
        }
    }

    private Vertex insertV(String uri, String type) {
        if (g.V().has("id", uri).hasNext()) {
            return g.V(uri).next();
        } else {
            g.getGraph().get().addVertex("id" , uri , "label" , type);
//            return g.addV(type).property(T.id, uri).next();
            return null;
        }
    }

    private static class NodeConfiguration {

        double weight;
        NodeType type;

        public NodeConfiguration(double weight, NodeType t) {
            this.weight = weight;
            this.type = t;
        }

    }

    private enum NodeType {
        Vertex, Edge
    }
}
