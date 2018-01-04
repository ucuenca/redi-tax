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
package ec.edu.cedia.redi.unesco;

import ec.edu.cedia.redi.entitymanagement.DBPediaExpansion;
import ec.edu.cedia.redi.entitymanagement.SpotlightRecognition;
import ec.edu.cedia.redi.entitymanagement.api.EntityExpansion;
import ec.edu.cedia.redi.entitymanagement.api.EntityRecognition;
import ec.edu.cedia.redi.exceptions.ResourceSizeException;
import ec.edu.cedia.redi.utils.GraphOperations;
import java.util.Collections;
import java.util.List;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class UnescoPopulation {

    Logger log = LoggerFactory.getLogger(UnescoPopulation.class);

    private final GraphTraversalSource g;
    private static final ValueFactory vf = ValueFactoryImpl.getInstance();
    private final EntityRecognition spotlight = SpotlightRecognition.getInstance();
    private final EntityExpansion dbpedia;

    public UnescoPopulation(GraphTraversalSource g) {
        this.g = g;
        this.dbpedia = new DBPediaExpansion(g);
    }

    public long populate() throws Exception {
        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance()) {
            UnescoNomeclature unesco = new UnescoNomeclature(conn);
//            List<URI> uris = Collections.singletonList(vf.createIRI("http://skos.um.es/unesco6/120302"));
//            return populateNodes(uris, unesco);
            return populateNodes(unesco.sixDigitResources(), unesco);
        } catch (Exception ex) {
            log.error("Cannot populate Unesco nomenclature", ex);
            throw new RuntimeException(ex);
        }
    }

    private long populateNodes(List<URI> unescoURIs, UnescoNomeclature unesco) {
        return unescoURIs.stream()
                .filter(uri -> !unesco.code(uri).contains("99"))
                .map(uri -> findEntities(uri, unesco))
                .map(uris -> dbpedia.expand(uris))
                .count();
    }

    private List<URI> findEntities(URI uri, UnescoNomeclature unesco) {
        String label = unesco.label(uri, "en").getLabel();
        List<URI> entities = spotlight.getEntities(label);
        Vertex unescoVertex = GraphOperations.insertIdV(g, uri.stringValue(), "unesco");

        entities.stream().map(entity -> {
            Vertex entityVertex = GraphOperations.insertIdV(g, entity.stringValue(), "node");
            entityVertex.addEdge("sameAs", unescoVertex, "label", label);
            return entity;
        }).forEach(entity -> log.info("SPOTLIGTH - [{} SAME AS {}]", unescoVertex, entity));

        if (entities.isEmpty()) {
            try {
                URI parent = unesco.broad(uri);
                if (parent != null) {
                    populateNodes(Collections.singletonList(parent), unesco);
                }
            } catch (ResourceSizeException ex) {
                log.error("{} should return only a parent", uri, ex);
            }
        }
        return entities;
    }
}
