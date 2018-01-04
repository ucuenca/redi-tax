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
package ec.edu.cedia.redi.entitymanagement;

import ec.edu.cedia.redi.entitymanagement.api.EntityExpansion;
import ec.edu.cedia.redi.utils.GraphOperations;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static tinkerpop.GraphOperations.getMD5;

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
    public Graph expand(List<URI> uris, int lvl) {
        SPARQLRepository repository = null;
        Map<String, String> additionalHttpHeaders = new HashMap<>();
        additionalHttpHeaders.put("Accept", "application/ld+json");

        try {
            repository = new SPARQLRepository(DBPEDIA_CONTEXT);
            repository.setAdditionalHttpHeaders(additionalHttpHeaders);
            repository.initialize();
            for (URI uri : uris) {
                log.info("Expanding {}", uri);
                if (g.V().has("id", uri.stringValue()).has("expand", true).hasNext()) {
                    continue;
                }
                queryDbpedia(repository.getConnection(), uri, lvl);
                Vertex v;
                if (!g.V().has("id", uri.stringValue()).hasNext()) {
                    v = GraphOperations.insertIdV(g, uri.stringValue(), "node");
                } else {
                    v = g.V().has("id", uri.stringValue()).next();
                }
                v.property("expand", true);
                g.tx().commit();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (repository != null) {
                    repository.shutDown();
                }
            } catch (RepositoryException ex) {
                log.error("Cannot shutdown repository", ex);
            }
        }
        return g.getGraph().get();
    }

    @Override
    public Graph expand(List<URI> uris) {
        return expand(uris, DEFAULT_LVL);
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
            if (properties.contains(stmt.getPredicate()) && stmt.getObject() instanceof URI) {
                log.debug("New lvl {} - {}", level - 1, stmt.getObject());
                queryDbpedia(dbpediaConnection, (URI) stmt.getObject(), level - 1);
            }
        }

    }

    private void registerStatement(Statement stmt) {
        if (m.containsKey(stmt.getPredicate())) {
            log.info("Registering statement: {}", stmt);
            String s = stmt.getSubject().stringValue();
            String p = stmt.getPredicate().stringValue();
            String o = stmt.getObject().stringValue();
            if (NodeType.Vertex == m.get(stmt.getPredicate()).type) {
                Vertex v1 = GraphOperations.insertIdV(g, s, "node");
                Vertex v2 = GraphOperations.insertIdV(g, o, "node");
                String codeE = getMD5(v1.id().toString() + v2.id().toString() + p);
                if (!g.E().has("id", codeE).hasNext()) {
                    v1.addEdge(p, v2, "id", codeE, "weight", m.get(stmt.getPredicate()).weight);
                }
            } else if (NodeType.Edge == m.get(stmt.getPredicate()).type) {
                if (g.V().has("id", s).hasNext()) {
                    Vertex v = g.V().has("id", s).next();
                    if (!g.V(v).has(p, o).hasNext()) {
                        v.property(p, o);
                    }
                } else {
                    Vertex v = GraphOperations.insertIdV(g, s, "node");
                    v.property(p, o);
                }
            }
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
