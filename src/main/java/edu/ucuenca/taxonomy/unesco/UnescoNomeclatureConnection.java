/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.taxonomy.unesco;

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
            log.debug("Initializing a repository to unesco SPARQL endpint.");
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
