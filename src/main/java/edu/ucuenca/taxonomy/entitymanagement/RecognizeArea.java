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
import edu.ucuenca.taxonomy.entitymanagement.api.EntityRecognition;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.incr;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.bothE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.constant;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.has;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.unfold;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.values;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class RecognizeArea {

    Graph graph;
    EntityExpansion ex = new DBPediaExpansion(graph.traversal());
    EntityRecognition entityRcgntn = SpotlightRecognition.getInstance();

    public RecognizeArea(Graph graph) {
        this.graph = graph;
    }

    public URI recognize(List<String> keywords) {
        List<String> indexes = new ArrayList<>();
        GraphTraversalSource g = graph.traversal();
        for (String keyword : keywords) {
            List<URI> uris = entityRcgntn.getEntities(keyword);
            List<String> urisStr = uris.stream().map(uri -> uri.stringValue()).collect(Collectors.toList());
            indexes.addAll(urisStr);
            List<URI> selected = uris.stream()
                    .filter(uri -> !g.V(uri.stringValue()).has("expand").hasNext())
                    .collect(Collectors.toList());
            ex.expand(selected, "keyword");
        }
        Object uri = g.V(indexes.toArray())
                .repeat(bothE().bothV().simplePath())
                .until(has("type", "unesco"))
                .path().as("p").map(unfold().coalesce(values("weight"), constant(0.0)).sum())
                .as("cost").order().by(incr).select("id").limit(1).next();
        return new URIImpl(uri.toString());
    }

    private void expandNewURIS(List<String> keywords) {

    }
}
