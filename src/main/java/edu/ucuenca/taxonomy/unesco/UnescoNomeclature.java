/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucuenca.taxonomy.unesco;

import edu.ucuenca.taxonomy.unesco.exceptions.ResourceSizeException;
import info.aduna.iteration.Iterations;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class UnescoNomeclature {
    
    private final UnescoNomeclatureConnection unesco;
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private final Logger log = LoggerFactory.getLogger(UnescoNomeclature.class);
    private final static String DEFAULT_CONTEXT = "http://skos.um.es/unesco6";
    
    public UnescoNomeclature(UnescoNomeclatureConnection unesco) {
        this.unesco = unesco;
    }

    /**
     * Return a list of {@link URI}s for 2-digit codes.
     *
     * @see <a href="https://en.wikipedia.org/wiki/UNESCO_nomenclature">UNESCO
     * Nomenclature two-digit system</a>
     * @return
     * @throws RepositoryException
     */
    public List<URI> twoDigitResources() throws RepositoryException {
        RepositoryConnection connection = unesco.getConnection();
        List<URI> twoDigit = new ArrayList(24);
        try {
            TupleQuery q = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { [] skos:hasTopConcept ?field. }}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));
            
            twoDigit = processQuery(q);
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        }
        return twoDigit;
    }

    /**
     * Given a field or resources return a list of children.
     *
     * @param field
     * @return
     * @throws RepositoryException
     */
    public List<URI> narrow(URI field) throws RepositoryException {
        List<URI> resources = new ArrayList(24);
        
        try {
            TupleQuery q = unesco.getConnection().prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { ?f skos:narrower ?field. }}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));
            q.setBinding("f", field);
            
            resources = processQuery(q);
        } catch (MalformedQueryException ex) {
            log.error("Query malformed.", ex);
        }
        return resources;
    }

    /**
     * Given a resource return a parent.
     *
     * @param field
     * @return
     * @throws RepositoryException
     * @throws ResourceSizeException
     */
    public URI broad(URI field) throws RepositoryException, ResourceSizeException {
        try {
            TupleQuery q = unesco.getConnection().prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { ?f skos:broader ?field. }}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));
            q.setBinding("f", field);
            
            List<URI> resources = processQuery(q);
            if (resources.isEmpty()) {
                return null;
            } else if (resources.size() == 1) {
                return resources.get(0);
            } else {
                throw new ResourceSizeException("Incorrect Size; this query should return only a resource.");
            }
        } catch (MalformedQueryException ex) {
            log.error("Query malformed.", ex);
        }
        return null;
    }
    
    public Literal getLabel(URI resource, String lang) throws RepositoryException {
        RepositoryConnection conn = unesco.getConnection();
        try {
            TupleQuery q = conn.prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT ?label WHERE { GRAPH ?g {?field skos:prefLabel ?label.} "
                    + "FILTER langMatches( lang(?label), \"" + lang + "\" )}"
            );
            q.setBinding("g", vf.createURI(DEFAULT_CONTEXT));
            q.setBinding("field", resource);
            
            TupleQueryResult r = q.evaluate();
            if (r.hasNext()) {
                return (Literal) r.next().getValue("label");
            }
        } catch (MalformedQueryException | QueryEvaluationException ex) {
            log.error("Cannot process query.", ex);
        }
        return null;
    }
    
    public Model getResoureMetadata(URI resource) throws RepositoryException {
        RepositoryConnection connection = unesco.getConnection();
        Model model = new LinkedHashModel();
        try {
            GraphQuery query = connection.prepareGraphQuery(
                    QueryLanguage.SPARQL,
                    "CONSTRUCT { ?resource ?p ?o } WHERE { GRAPH ?graph { ?resource ?p ?o } . }");
            query.setBinding("graph", connection.getValueFactory().createURI(DEFAULT_CONTEXT));
            query.setBinding("resource", resource);
            GraphQueryResult result = query.evaluate();
            Iterations.addAll(result, model);
        } catch (MalformedQueryException | QueryEvaluationException ex) {
            log.error("Cannot get metadata for resource {}", resource.stringValue(), ex);
        } finally {
            connection.close();
        }
        return model;
    }
    
    private List<URI> processQuery(TupleQuery q) throws RepositoryException {
        RepositoryConnection connection = unesco.getConnection();
        List<URI> resources = new ArrayList();
        try {
            TupleQueryResult result = q.evaluate();
            
            for (BindingSet b : Iterations.asList(result)) {
                resources.add((URI) b.getValue("field"));
            }
        } catch (QueryEvaluationException ex) {
            log.error("Cannot execute query.", ex);
        } finally {
            connection.close();
        }
        return resources;
    }
}
