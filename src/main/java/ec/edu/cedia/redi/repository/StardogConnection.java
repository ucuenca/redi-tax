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
package ec.edu.cedia.redi.repository;

//import com.complexible.stardog.gremlin.StardogGraphConfiguration;
//import com.complexible.stardog.gremlin.StardogGraphConfiguration.Builder;
//import com.complexible.stardog.gremlin.StardogGraphFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * Connection management to a Stardog database instance.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class StardogConnection implements AutoCloseable {

    private static StardogConnection stardog;
    private final static String DATABASE = "http://localhost:5820/myDB";
    private final static String USER = "admin";
    private final static String PASSWD = "admin";
    private final static String BASE_IRI = "http://redi.cedi.edu.ec/resource/";
    private final static String CONTEXT = "http://redi.cedi.edu.ec/context/redi";

    private Graph graph;

    private StardogConnection() {
        this(DATABASE, USER, PASSWD, BASE_IRI, CONTEXT);
    }

    private StardogConnection(String database) {
        this(database, USER, PASSWD, BASE_IRI, CONTEXT);
    }

    private StardogConnection(String database, String user, String passwd, String baseIRI, String context) {
//        Builder conf = StardogGraphConfiguration.builder();
//        conf.connectionString(database)
//                .credentials(user, passwd)
//                .baseIRI(baseIRI)
//                .namedGraph(context).labelIRI("http://redi.edu.ec/label");
//        graph = StardogGraphFactory.open(conf.build());
    }

    public static StardogConnection instance() {
        if (stardog == null) {
            return new StardogConnection();
        }
        return stardog;
    }

    public static StardogConnection instance(String database) {
        if (stardog == null) {
            return new StardogConnection(database);
        }
        return stardog;
    }

    public static StardogConnection instance(String database, String user, String passwd, String baseIRI, String context) {
        if (stardog == null) {
            return new StardogConnection(database, user, passwd, baseIRI, context);
        }
        return stardog;
    }

    public Graph graph() {
        return graph;
    }

    @Override
    public void close() throws Exception {
        if (graph != null) {
            graph.close();
        }
    }
}
