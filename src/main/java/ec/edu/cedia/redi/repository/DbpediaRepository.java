/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ec.edu.cedia.redi.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 *
 * @author joe
 */


public class DbpediaRepository implements AutoCloseable {

    public final static String ENDPOINT_DBPEDIA = "https://dbpedia.org/sparql";
  //  private final static String QUERY_ENDPOINT_DBPEDIA = ENDPOINT_DBPEDIA + "/select";
   /* private final static String UPDATE_ENDPOINT_REDI = ENDPOINT_REDI + "/update";
    public final static String DEFAULT_CONTEXT = "https://redi.cedia.edu.ec/context/redi";
    public final static String AUTHOR_CONTEXT = "https://redi.cedia.edu.ec/context/authors";
    public final static String CLUSTERS_CONTEXT = "https://redi.cedia.edu.ec/context/clusters";
    private static final Logger log = LoggerFactory.getLogger(UnescoNomeclatureConnection.class);*/
    private static final Map<String, String> headers = new HashMap<>();
    private SPARQLRepository repository;

    static {
        //headers.put("Accept", "text/html");
       // headers.put("Accept", "application/xhtml+xml");
       // headers.put("Accept", "application/xml");
       // text/html,application/xhtml+xml,application/xml;
    }
    private static DbpediaRepository instance;

    private DbpediaRepository() throws RepositoryException {
        repository = new SPARQLRepository(ENDPOINT_DBPEDIA);
        repository.initialize();
      //  repository.setAdditionalHttpHeaders(headers);
        System.out.print ("Initializing repository Dbpedia");
       /* log.debug("Initializing a repository REDI endpoint.");*/
    }

    public static synchronized DbpediaRepository getInstance() throws RepositoryException {
        if (instance == null) {
            return new DbpediaRepository();
        }
        return instance;
    }
    
    public static synchronized void NullInstance () {
      instance = null;
        try {
            Thread.sleep(1000*5);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(DbpediaRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
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
