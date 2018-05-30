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
import ec.edu.cedia.redi.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
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
    private static final String UC_PREFIX = "http://ucuenca.edu.ec/ontology#";

    public Redi(RediRepository conn) {
        this.conn = conn;
    }

    public List<Author> getAuthors(boolean filterAuthorsProcessed) throws RepositoryException {
        return getAuthors(-1, -1, filterAuthorsProcessed);
    }

    public List<Author> getAuthors(int offset, int limit, boolean filterAuthorsProcessed) throws RepositoryException {
        log.info("Getting authors...");
        RepositoryConnection connection = conn.getConnection();
        List<Author> authors = new ArrayList<>();
        try {
            String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
                    + "PREFIX dct: <http://purl.org/dc/terms/> \n"
                    + "SELECT DISTINCT ?a (group_concat(DISTINCT ?kw1 ; separator=\";\") as ?kws1) (group_concat(DISTINCT ?kw2 ; separator=\";\") as ?kws2)\n"
                    + "WHERE { \n"
                    // + "values ?a {<https://redi.cedia.edu.ec/resource/authors/EPN/oai-pmh/ALFONSO_SOLAR__D>}"
                    + "  GRAPH ?redi {  \n"
                    + "    ?a a foaf:Person. \n"
                    + "    OPTIONAL { \n"
                    + "      ?a foaf:publications [dct:subject ?subject].\n"
                    + "      OPTIONAL { "
                    + "        ?subject rdfs:label ?kw1 ."
                    + "      }\n"
                    + "    }\n"
                    + "    OPTIONAL { ?a foaf:topic_interest ?topic . \n"
                    + "         OPTIONAL { ?topic rdfs:label ?kw2. }"
                    + "    }\n"
                    + "  }\n"
                    + "  GRAPH ?authors {    \n"
                    + "    ?a a foaf:Person 	\n"
                    + "  }\n"
                    + "} GROUP BY ?a";
            if (offset != -1 && limit != -1) {
                query += String.format("\nOFFSET %s LIMIT %s ", offset, limit);
            }
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("redi", vf.createURI(RediRepository.DEFAULT_CONTEXT));
            q.setBinding("authors", vf.createURI(RediRepository.AUTHOR_CONTEXT));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet variables = result.next();
                URI author = (URI) variables.getBinding("a").getValue();
                log.info("Getting subjects of author: " + author);
                if (filterAuthorsProcessed && isAuthorInCluster(author)) {
                    log.info("Author {} is already in clusters graph.", author);
                    continue;
                }
                String keywords = "";
                String topics = "";
                if (variables.getBinding("kws1") != null) {
                    keywords = variables.getBinding("kws1").getValue().stringValue();
                }
                if (variables.getBinding("kws2") != null) {
                    topics = variables.getBinding("kws2").getValue().stringValue();
                }
                String join = (keywords + ";" + topics).trim();

                if (!join.isEmpty() && !";".equals(join)) {
                    authors.add(new Author(author, StringUtils.processTopics(join)));
                }
            }
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        log.info("Getting {} authors.", authors.size());
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
        for (AreaUnesco area : areas) {
            Statement authorCluster = vf.createStatement(author, DCTERMS.IS_PART_OF, area.getUri());
            Statement authorType = vf.createStatement(author, RDF.TYPE, FOAF.PERSON);
            Statement clusterType = vf.createStatement(area.getUri(), RDF.TYPE, vf.createURI(UC_PREFIX, "Cluster"));
            Statement clusterLbl = vf.createStatement(area.getUri(), RDFS.LABEL, vf.createLiteral(area.getLabel(), "en"));
            dataset.add(authorCluster);
            dataset.add(authorType);
            dataset.add(clusterType);
            dataset.add(clusterLbl);
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
