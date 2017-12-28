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

import ec.edu.cedia.redi.exceptions.ResourceSizeException;
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
        List<URI> twoDigit = new ArrayList<>(24);
        try {
            TupleQuery q = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { [] skos:hasTopConcept ?field. }}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));

            twoDigit = processQueryURIFields(q);
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        }
        return twoDigit;
    }

    /**
     * Return a list of {@link URI}s for 4-digit codes.
     *
     * @see <a href="https://en.wikipedia.org/wiki/UNESCO_nomenclature">UNESCO
     * Nomenclature two-digit system</a>
     * @return
     * @throws RepositoryException
     */
    public List<URI> fourDigitResources() throws RepositoryException {
        RepositoryConnection connection = unesco.getConnection();
        List<URI> fourDigit = new ArrayList<>(248);
        try {
            TupleQuery q = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { [] skos:hasTopConcept [ skos:narrower ?field].}}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));

            fourDigit = processQueryURIFields(q);
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        }
        return fourDigit;
    }

    /**
     * Return a list of {@link URI}s for 6-digit codes.
     *
     * @see <a href="https://en.wikipedia.org/wiki/UNESCO_nomenclature">UNESCO
     * Nomenclature two-digit system</a>
     * @return
     * @throws RepositoryException
     */
    public List<URI> sixDigitResources() throws RepositoryException {
        RepositoryConnection connection = unesco.getConnection();
        List<URI> sixDigit = new ArrayList<>(2232);
        try {
            TupleQuery q = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { [] skos:hasTopConcept [ skos:narrower [ skos:narrower ?field ]].}}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));

            sixDigit = processQueryURIFields(q);
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        }
        return sixDigit;
    }

    /**
     * Given a field or resources return a list of children.
     *
     * @param field
     * @return
     * @throws RepositoryException
     */
    public List<URI> narrow(URI field) throws RepositoryException {
        List<URI> resources = new ArrayList<>(24);

        try {
            TupleQuery q = unesco.getConnection().prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { ?f skos:narrower ?field. }}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));
            q.setBinding("f", field);

            resources = processQueryURIFields(q);
        } catch (MalformedQueryException ex) {
            log.error("Query malformed.", ex);
        }
        return resources;
    }

    /**
     * Given a resource return a parent. Returns <code>null</code> if there is
     * not parent.
     *
     * @param field
     * @return
     * @throws RepositoryException
     * @throws ResourceSizeException
     */
    public URI broad(URI field) throws ResourceSizeException {
        try {
            TupleQuery q = unesco.getConnection().prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { ?f skos:broader ?field. }}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));
            q.setBinding("f", field);

            List<URI> resources = processQueryURIFields(q);
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

    /**
     * Given a resource return the UNESCO code.
     *
     * @param subject
     * @return
     */
    public String code(URI subject) {
        try {
            TupleQuery q = unesco.getConnection().prepareTupleQuery(
                    QueryLanguage.SPARQL,
                    "SELECT * WHERE { GRAPH ?graph { ?subject skos:notation ?code. }}",
                    DEFAULT_CONTEXT);
            q.setBinding("graph", vf.createURI(DEFAULT_CONTEXT));
            q.setBinding("subject", subject);

            TupleQueryResult result = q.evaluate();
            if (result.hasNext()) {
                return result.next().getBinding("code").getValue().stringValue();
            }
        } catch (MalformedQueryException ex) {
            log.error("Query malformed.", ex);
        } catch (RepositoryException ex) {
            log.error("Problem with repository connection.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        }
        return null;
    }

    public Literal label(URI resource, String lang) {
        try {
            TupleQuery q = unesco.getConnection().prepareTupleQuery(
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
        } catch (RepositoryException ex) {
            log.error("Repository Exception.", ex);
        }
        return null;
    }

    /**
     * Returns a dataset with all properties about the resource.
     *
     * @param resource
     * @return
     * @throws RepositoryException
     */
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

    private List<URI> processQueryURIFields(TupleQuery q) throws RepositoryException {
        RepositoryConnection connection = unesco.getConnection();
        List<URI> resources = new ArrayList<>();
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
