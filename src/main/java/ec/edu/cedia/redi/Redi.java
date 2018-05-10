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

import corticalClasification.AreaUnesco;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
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
public class Redi {

    private final RediRepository conn;
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private static final Logger log = LoggerFactory.getLogger(Redi.class);

    public Redi(RediRepository conn) {
        this.conn = conn;
    }

    public List<Author> getAuthors() throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        List<Author> authors = new ArrayList<>();
        try {
            String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
                    + "PREFIX dct: <http://purl.org/dc/terms/> \n"
                    + "SELECT DISTINCT ?a (group_concat(DISTINCT ?kw1 ; separator=\";\") as ?kws1) (group_concat(DISTINCT ?kw2 ; separator=\";\") as ?kws2)\n"
                    + "WHERE { \n"
                    + "  GRAPH ?redi {  \n"
                    + "    ?a a foaf:Person. \n"
                    + "    OPTIONAL { \n"
                    + "      ?a foaf:publications [dct:subject ?subject].\n"
                    + "      ?subject rdfs:label ?kw1 .\n"
                    + "    }\n"
                    + "    OPTIONAL { ?a foaf:topic_interest ?topic . }\n"
                    + "    OPTIONAL { ?topic rdfs:label ?kw2. }\n"
                    + "  }\n"
                    + "  GRAPH ?authors {    \n"
                    + "    ?a a foaf:Person 	\n"
                    + "  }\n"
                    + "} GROUP BY ?a";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("redi", vf.createURI(RediRepository.DEFAULT_CONTEXT));
            q.setBinding("authors", vf.createURI(RediRepository.AUTHOR_CONTEXT));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet variables = result.next();
                URI author = (URI) variables.getBinding("a").getValue();
                String keywords = "";
                String topics = "";
                if (variables.getBinding("kws1") != null) {
                    keywords = variables.getBinding("kws1").getValue().stringValue();
                }
                if (variables.getBinding("kws2") != null) {
                    topics = variables.getBinding("kws2").getValue().stringValue();
                }
                String join = (keywords + ";" + topics).trim();

                if (!join.isEmpty()) {
                    authors.add(new Author(author, join));
                }
            }
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return authors;
    }

    public boolean isAuthorInCluster(URI author) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        try {
            String query = "ASK {GRAPH ?cluster { ?s ?p ?author }}";
            BooleanQuery q = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
            q.setBinding("cluster", vf.createURI(RediRepository.CLUSTERS_CONTEXT));
            q.setBinding("author", author);
            return q.evaluate();
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return false;
    }

    public void store(URI author, List<AreaUnesco> areas) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        Model dataset = new LinkedHashModel();
        List<URI> publications = getPublications(author);
        for (AreaUnesco area : areas) {
            for (URI publication : publications) {
                Statement regPub = vf.createStatement(area.getUri(), FOAF.PUBLICATIONS, publication);
                Statement regAuthor = vf.createStatement(publication, vf.createURI("http://ucuenca.edu.ec/ontology#hasPerson"), author);
                dataset.add(regPub);
                dataset.add(regAuthor);
            }
            Statement regAreas = vf.createStatement(area.getUri(), RDFS.LABEL, vf.createLiteral(area.getLabel(), "en"));
            dataset.add(regAreas);
        }
        try {
            connection.begin();
            connection.add(dataset, vf.createURI(RediRepository.CLUSTERS_CONTEXT));
        } catch (RepositoryException ex) {
            log.error("", ex);
        } finally {
            connection.commit();
            connection.close();
            log.info("Stored author {} with areas {}", author, areas);
        }
    }

    private List<URI> getPublications(URI author) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        List<URI> publications = new ArrayList<>();
        try {
            String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                    + "PREFIX dct: <http://purl.org/dc/terms/> "
                    //                    + "CONSTRUCT {?author foaf:publications ?p} WHERE {"
                    + "SELECT ?p "
                    + "WHERE { GRAPH ?graph {"
                    + "  ?author a foaf:Person;"
                    + "    foaf:publications ?p."
                    + "}}";//}";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("graph", vf.createURI(RediRepository.DEFAULT_CONTEXT));
            q.setBinding("author", author);
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                URI publication = vf.createURI(result.next().getValue("p").stringValue());
                publications.add(publication);
            }
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return publications;
    }
}
