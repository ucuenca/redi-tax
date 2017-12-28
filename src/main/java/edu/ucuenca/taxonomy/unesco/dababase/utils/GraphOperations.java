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
package edu.ucuenca.taxonomy.unesco.dababase.utils;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Common operations for managing a vertex/edge.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class GraphOperations {

    /**
     * Inserts a vertex with two properties, id and type.
     *
     * @param g
     * @param id   must be a URI
     * @param type node/unesco
     * @return
     */
    public static Vertex insertIdV(GraphTraversalSource g, String id, String type) {
        if (g.V().has("id", id).hasNext()) {
            Vertex v = g.V().has("id", id).next();
           // v.property("id", id, "label", type);
            return v;
        }
        return g.getGraph().get().addVertex("id", id, "label", type);
    }
}
