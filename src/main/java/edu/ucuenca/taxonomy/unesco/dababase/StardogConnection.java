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
package edu.ucuenca.taxonomy.unesco.dababase;

//import com.blazegraph.gremlin.embedded.BlazeGraphEmbedded;
//import com.blazegraph.gremlin.embedded.BlazeGraphFactory;
import com.complexible.stardog.gremlin.StardogGraphConfiguration;
import com.complexible.stardog.gremlin.StardogGraphConfiguration.Builder;
import com.complexible.stardog.gremlin.StardogGraphFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class StardogConnection {

    public static void main(String[] args) throws RepositoryException, Exception {
//        final BlazeGraphEmbedded graph = BlazeGraphFactory.open("coco.jnl");
//        Graph coco1 = IOGraph.read("coco2.graphml");
//        graph.bulkLoad(coco1);
//        graph.commit();
//        GraphTraversalSource g = graph.traversal();
        Builder conf = StardogGraphConfiguration.builder();
        conf.connectionString("http://localhost:5820/myDB")
                .credentials("admin", "admin").baseIRI("http://redi.cedi.edu.ec/");

        try (Graph graph = StardogGraphFactory.open(conf.build())) {
            Vertex marko = graph.addVertex("label", "person", "id", 1, "name", "marko", "age", 29);
            System.out.println(marko.values());
            GraphTraversalSource g = graph.traversal();
            System.out.println(g.V().has("id", 1).hasNext());
//            Preprocessing p = Preprocessing.getInstance();
//            p.entitiesEnrichment("http://dbpedia.org/resource/Computer_science", graph);
        }
    }

//    public static BlazeGraphEmbedded open(final File file) throws Exception {
//        final String journal = file.getAbsolutePath();
//        final BlazeGraphEmbedded graph = BlazeGraphFactory.open(journal);
//        return graph;
//    }
}
