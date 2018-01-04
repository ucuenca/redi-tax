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

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connection to UNESCO SPARQL endpoint.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class UnescoNomeclatureConnection implements AutoCloseable {

    private final static String ENDPOINT_UNESKOS = "http://skos.um.es/sparql/";
    private final Repository repository = new SPARQLRepository(ENDPOINT_UNESKOS);
    private static final Logger log = LoggerFactory.getLogger(UnescoNomeclatureConnection.class);

    private static UnescoNomeclatureConnection instance;

    private UnescoNomeclatureConnection() throws RepositoryException {
        if (!repository.isInitialized()) {
            repository.initialize();
            log.debug("Initializing a repository to unesco SPARQL endpoint.");
        }
    }

    public static synchronized UnescoNomeclatureConnection getInstance() throws RepositoryException {
        if (instance == null) {
            return new UnescoNomeclatureConnection();
        }

        return instance;
    }

    /**
     * Get the connection for the {@link Repository} to the UNESCO nomenclature.
     *
     * @return @throws RepositoryException
     */
    public RepositoryConnection getConnection() throws RepositoryException {
        return repository.getConnection();
    }

    @Override
    public void close() throws Exception {
        if (repository != null) {
            this.repository.shutDown();
        }
    }
}
