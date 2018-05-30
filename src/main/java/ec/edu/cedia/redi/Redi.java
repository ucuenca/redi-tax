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
import java.util.Map;
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

    public Redi(RediRepository conn) {
        this.conn = conn;
    }

    public List<Author> getAuthors() throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        List<Author> authors = new ArrayList<>();
        try {
            String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
                    + "PREFIX dct: <http://purl.org/dc/terms/> "
                    + "SELECT ?a (group_concat(DISTINCT ?kw ; separator=\";\") as ?kws) "
                    + "WHERE { GRAPH ?graph {"
                    + "  ?a a foaf:Person;"
                    + "    foaf:publications ?p."
                    + "  ?p dct:subject [rdfs:label ?kw] ."
                    + " GRAPH ?author{ "
                    + "   ?a a foaf:Person }"
                    + "}} GROUP BY ?a offset 1500 limit 100";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("graph", vf.createURI(RediRepository.DEFAULT_CONTEXT));
            q.setBinding("author", vf.createURI(RediRepository.AUTHOR_CONTEXT));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet variables = result.next();
                authors.add(new Author((URI) variables.getBinding("a").getValue(), variables.getBinding("kws").getValue().stringValue()));
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

    public List<Author> getAuthorsbyCluster(String cl) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        List<Author> authors = new ArrayList<>();
        try {
            String query = "PREFIX dct: <http://purl.org/dc/terms/>\n"
                    + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                    + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "select distinct ?cname  ?author   (group_concat(DISTINCT ?s ; separator=\";\") as ?kws)  \n"
                    + "{\n"
                    + "  GRAPH ?graphCl {\n"
                    + "     ?cl rdfs:label ?cname .\n"
                    + "     ?cl foaf:publications ?pub \n "
                 //   + ".  filter ( ?pub != <https://redi.cedia.edu.ec/resource/publication/cd34a0fa47da479eb059a538f95c0b77>)"
                    + "  }\n"
                    + "     ?pub  <http://ucuenca.edu.ec/ontology#hasPerson>  ?author .\n"
                    + "    GRAPH ?graphRedi {\n"
                    + "      select ?pub ?s {\n"
                    + "        ?pub dct:subject [rdfs:label ?s] ."
                    + "        ?pub dct:title ?t ."
                    + "    } \n"
                    + "    }\n"
                    + "} GROUP BY ?cname ?author ";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("graphRedi", vf.createURI(RediRepository.DEFAULT_CONTEXT));
            q.setBinding("graphCl", vf.createURI(RediRepository.CLUSTERS_CONTEXT));
            q.setBinding("cl", vf.createURI(cl));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet variables = result.next();
                authors.add(new Author((URI) variables.getBinding("author").getValue(), variables.getBinding("kws").getValue().stringValue()));
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

    public List<String> getclustersAvailable() throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        List<String> cluster = new ArrayList<>();
        try {
            String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                    + "SELECT distinct ?cl ?clabel  WHERE {\n"
                    + " GRAPH ?graphCl {\n"
                    + "   ?cl foaf:publications ?pub . \n"
                    + "   ?cl rdfs:label ?clabel\n"
                    + " }\n"
                    + "} ";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("graphCl", vf.createURI(RediRepository.CLUSTERS_CONTEXT));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet variables = result.next();

                cluster.add(variables.getBinding("cl").getValue().stringValue());
                //authors.add(new Author((URI) variables.getBinding("cl").getValue(), variables.getBinding("kws").getValue().stringValue()));
            }
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return cluster;
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
    
    
     public void storeSubcluster( Author author,  Map <String,NodoDbpedia> nodos, String cluster) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        Model dataset = new LinkedHashModel();
        //List<URI> publications = getPublications(author);
        for ( Map.Entry<String, NodoDbpedia> nb : nodos.entrySet()) {
         
         Statement SubClClass= vf.createStatement(vf.createURI(nb.getKey()) , RDF.TYPE, vf.createURI("http://ucuenca.edu.ec/ontology#SubCluster"));
         dataset.add(SubClClass);
         
         Statement authorSubC = vf.createStatement(author.getURI(), DCTERMS.IS_PART_OF, vf.createURI(nb.getKey()));
         dataset.add(authorSubC);
         
         Statement ClustSubC = vf.createStatement(vf.createURI(nb.getKey()) , DCTERMS.IS_PART_OF, vf.createURI(cluster));
         dataset.add(ClustSubC);        
// Statement SubClNameEn = vf.createStatement(vf.createURI(nb.getKey()) , RDFS.LABEL, vf.createURI(cluster));
         Statement SubClNameEn= vf.createStatement(vf.createURI(nb.getKey()) , RDFS.LABEL , vf.createURI(nb.getValue().getNameEn(),"en"));
         dataset.add(SubClNameEn);
         
         if (nb.getValue().getNameEs() != null){
         Statement SubClNameEs= vf.createStatement(vf.createURI(nb.getKey()) , RDFS.LABEL, vf.createURI(nb.getValue().getNameEs(),"es"));
         dataset.add(SubClNameEs);
         }
        }
        try {
            connection.begin();
            connection.add(dataset, vf.createURI(RediRepository.CLUSTERS_CONTEXT));
        } catch (RepositoryException ex) {
            log.error("", ex);
        } finally {
            connection.commit();
            connection.close();
            //log.info("Stored author {} with areas {}", author, areas);
        }
    }
}
