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
package edu.ucuenca.taxonomy.unesco.tinkerpop;

import com.google.common.base.Preconditions;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities functions to read/write a {@link Graph}.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class IOGraph {

    private static final Logger log = LoggerFactory.getLogger(IOGraph.class);

    /**
     * Stores the graph provided in a specific file path using the GraphML
     * format.
     *
     * @param graph
     * @param file
     * @return Returns true if the file is read without problems.
     */
    public static boolean write(Graph graph, String file) {
        Preconditions.checkArgument(file.endsWith("graphml"), "Parameter 'file' should have a 'graphml' extension.");
        try (final OutputStream os = new FileOutputStream(file)) {
            graph.io(IoCore.graphml()).writer().normalize(true).create().writeGraph(os, graph);
        } catch (IOException ex) {
            log.error("Cannot write file", ex);
            return false;
        }
        return true;
    }

    /**
     * Reads a graph file in GraphML format.
     *
     * @param file
     * @return Returns null in case there is a problem while reading.
     */
    public static Graph read(String file) {
        Preconditions.checkArgument(file.endsWith("graphml"), "Parameter 'file' should have a 'graphml' extension.");
        Graph newGraph = TinkerGraph.open();
        try (final InputStream in = new FileInputStream(file)) {
            newGraph.io(IoCore.graphml()).reader().create().readGraph(in, newGraph);
        } catch (IOException ex) {
            log.error("Cannot read file", ex);
            return null;
        }
        return newGraph;
    }
}
