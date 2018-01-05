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
package ec.edu.cedia.redi;

import ec.edu.cedia.redi.unesco.UnescoNomeclatureConnection;
import java.util.HashMap;
import java.util.Map;
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
public class RediRepository implements AutoCloseable {

    private final static String ENDPOINT_REDI = "http://redi.cedia.edu.ec/sparql";
    public final static String DEFAULT_CONTEXT = "http://redi.cedia.edu.ec/context/redi";
    private final SPARQLRepository repository = new SPARQLRepository(ENDPOINT_REDI);
    private static final Logger log = LoggerFactory.getLogger(UnescoNomeclatureConnection.class);
    private static final Map<String, String> headers = new HashMap<>();

    static {
        headers.put("Accept", "application/ld+json");
    }
    private static RediRepository instance;

    private RediRepository() throws RepositoryException {
        if (!repository.isInitialized()) {
            repository.initialize();
            repository.setAdditionalHttpHeaders(headers);
            log.debug("Initializing a repository to unesco SPARQL endpoint.");
        }
    }

    public static synchronized RediRepository getInstance() throws RepositoryException {
        if (instance == null) {
            return new RediRepository();
        }
        return instance;
    }

    /**
     * Get the connection for the {@link Repository} to the REDI repository.
     *
     * @return @throws RepositoryException
     */
    public RepositoryConnection getConnection() throws RepositoryException {
        return repository.getConnection();
    }

    @Override
    public void close() throws Exception {
        if (repository != null) {
            repository.shutDown();
        }
    }
}