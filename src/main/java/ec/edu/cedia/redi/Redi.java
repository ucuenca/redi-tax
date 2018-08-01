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

import ec.edu.cedia.redi.repository.Repositories;
import ec.edu.cedia.redi.repository.DbpediaRepository;
import ec.edu.cedia.redi.repository.RediRepository;
import corticalClasification.AreaUnesco;
import ec.edu.cedia.redi.unesco.UnescoDataSet;
import ec.edu.cedia.redi.unesco.model.UnescoHierarchy;
import ec.edu.cedia.redi.utils.StringUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plublication.Preprocessing;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Redi {

    private final Repositories conn;
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private static final Logger log = LoggerFactory.getLogger(Redi.class);
    private static final String UC_PREFIX = "http://ucuenca.edu.ec/ontology#";

    public Redi(Repositories conn) {
        this.conn = conn;
    }

    public List<Author> getAuthors(boolean filterAuthorsProcessed) throws RepositoryException {
        return getAuthors(-1, -1, filterAuthorsProcessed);
    }

    public Author getAuthor(String uri) throws RepositoryException {
        log.info("Getting author {}...", uri);
        RepositoryConnection connection = conn.getConnection();
        try {
            String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
                    + "PREFIX dct: <http://purl.org/dc/terms/> \n"
                    + "SELECT DISTINCT ?a (group_concat(DISTINCT ?kw1 ; separator=\";\") as ?kws1) (group_concat(DISTINCT ?kw2 ; separator=\";\") as ?kws2)\n"
                    + "WHERE { \n"
                    + "values ?a {?author}"
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
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("redi", vf.createURI(conn.getContext()));
            q.setBinding("authors", vf.createURI(conn.getAuthorGraph()));
            q.setBinding("author", vf.createURI(uri));
            TupleQueryResult result = q.evaluate();
            if (result.hasNext()) {
                BindingSet variables = result.next();
                URI author = (URI) variables.getBinding("a").getValue();
                log.info("Getting subjects of author: " + author);

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
                    return new Author(author, StringUtils.processTopics(join));
                }
            }
        } catch (MalformedQueryException | QueryEvaluationException ex) {
            log.error("Cannot execute query.", ex);
            throw new RuntimeException(ex);
        } finally {
            connection.close();
        }
        throw new RuntimeException("Author cannot be found.");
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
            q.setBinding("redi", vf.createURI(conn.getContext()));
            q.setBinding("authors", vf.createURI(conn.getAuthorGraph()));
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

    public List<Author> getAuthorsbyCluster(String cl) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        List<Author> authors = new ArrayList<>();
        try {
            String query = "PREFIX dct: <http://purl.org/dc/terms/>\n"
                    + "     PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                    + "     PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                    + " select distinct ?cname  ?author    (group_concat(DISTINCT ?s ; separator=\";\") as ?kws) (group_concat(DISTINCT ?tl ; separator=\";\") as ?tws)  \n"
                    + "                       { \n"
                    + "                         GRAPH ?graphCl { \n"
                    + "                          ?cl rdfs:label ?cname . \n"
                    + "                          ?author dct:isPartOf ?cl .\n"
                    + "                          GRAPH ?graphRedi {\n"
                    + "                            select ?pub ?s ?tl {\n"
                    + "                            ?author foaf:publications ?pub .\n"
                    + "                            ?pub dct:subject [rdfs:label ?s] . "
                    + "                              OPTIONAL {\n"
                    + "                              ?author foaf:topic_interest  ?tp  .\n"
                    + "                                      OPTIONAL  { "
                    + "                                      ?tp rdfs:label ?tl      "
                    + "                                      } "
                    + "                            }"
                    + "                       }   }\n"
                    + "                    } } GROUP BY ?cname ?author";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("graphRedi", vf.createURI(conn.getContext()));
            q.setBinding("graphCl", vf.createURI(conn.getClusterGraph()));
            q.setBinding("cl", vf.createURI(cl));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet variables = result.next();
                Author aut = new Author((URI) variables.getBinding("author").getValue(), variables.getBinding("kws").getValue().stringValue());
                if (variables.hasBinding("tws")) {
                    aut.setTopics(variables.getValue("tws").stringValue());
                }
                authors.add(aut);
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
            String query = "PREFIX dct: <http://purl.org/dc/terms/>\n"
                    + "SELECT Distinct ?cl WHERE {\n"
                    + "   GRAPH ?graphCl  {  \n"
                    + "   ?cl a ?v .\n"
                    + "   ?author dct:isPartOf ?cl .  \n"
                    + "   filter ( STR(?v) = \"http://ucuenca.edu.ec/ontology#Cluster\")  \n"
                    + "     \n"
                    + "   }\n"
                    + "}";

            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("graphCl", vf.createURI(conn.getClusterGraph()));
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
            String query = "ASK {GRAPH ?cluster { ?author ?b ?c }}";
            BooleanQuery q = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
            q.setBinding("cluster", vf.createURI(conn.getClusterGraph()));
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
            connection.add(dataset, vf.createURI(conn.getClusterGraph()));
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
            q.setBinding("graph", vf.createURI(conn.getContext()));
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

    public void storeSubcluster(Author author, Map<String, NodoDbpedia> nodos, String cluster) {
        try {
            int maxsubcl = 0;
            RepositoryConnection connection = conn.getConnection();
            Model dataset = new LinkedHashModel();
            final List<UnescoHierarchy> unescoset = UnescoDataSet.getInstance().getDataset();
            
            //List<URI> publications = getPublications(author);
            for (Map.Entry<String, NodoDbpedia> nb : nodos.entrySet()) {
                maxsubcl++;
                if (maxsubcl > 6) {
                    continue;
                }
                Statement SubClClass = vf.createStatement(vf.createURI(nb.getKey()), RDF.TYPE, vf.createURI("http://ucuenca.edu.ec/ontology#SubCluster"));
                dataset.add(SubClClass);

                Statement authorSubC = vf.createStatement(author.getURI(), DCTERMS.IS_PART_OF, vf.createURI(nb.getKey()));
                dataset.add(authorSubC);

                if (!nb.getKey().contains("http://ucuenca.edu.ec/resource/subcluster") && validateSubcluster(cluster, nb.getKey(), nb.getValue().getNameEn(),unescoset , 60.0)) {
                    Statement ClustSubC = vf.createStatement(vf.createURI(nb.getKey()), DCTERMS.IS_PART_OF, vf.createURI(cluster));
                    dataset.add(ClustSubC);
                }
// Statement SubClNameEn = vf.createStatement(vf.createURI(nb.getKey()) , RDFS.LABEL, vf.createURI(cluster));
                if (nb.getValue().getNameEn() != null) {
                    Statement SubClNameEn = vf.createStatement(vf.createURI(nb.getKey()), RDFS.LABEL, vf.createLiteral(nb.getValue().getNameEn(), "en"));
                    dataset.add(SubClNameEn);
                }

                if (nb.getValue().getNameEs() != null) {
                    Statement SubClNameEs = vf.createStatement(vf.createURI(nb.getKey()), RDFS.LABEL, vf.createLiteral(nb.getValue().getNameEs(), "es"));
                    dataset.add(SubClNameEs);
                }
            }
            try {
                connection.begin();
                connection.add(dataset, vf.createURI(conn.getClusterGraph()));
            } catch (RepositoryException ex) {
                log.error("", ex);
            } finally {
                connection.commit();
                connection.close();
                //log.info("Stored author {} with areas {}", author, areas);
            }
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(Redi.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Boolean askSubCluster(String cl) {
        try {
            RepositoryConnection connection = conn.getConnection();

            String query = "PREFIX dct: <http://purl.org/dc/terms/>\n"
                    + "SELECT  (COUNT(?scl) as ?c)    WHERE {\n"
                    + "   GRAPH  ?graphCl { ?scl a <http://ucuenca.edu.ec/ontology#SubCluster>  .\n"
                    + "   ?scl dct:isPartOf ?cl .\n"
                    + "} }";

            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("cl", vf.createURI(cl));
            q.setBinding("graphCl", vf.createURI(conn.getClusterGraph()));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet res = result.next();
                if (res.hasBinding("c")) {
                    int count = Integer.parseInt(res.getValue("c").stringValue());
                    if (count > 0) {
                        return true;
                    }
                }
                return false;

                //  publications.add(publication);
            }

            return false;
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            java.util.logging.Logger.getLogger(Redi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public void deleteSubclusters() throws RepositoryException {
        try {
            RepositoryConnection connection = conn.getConnection();

            String deletequery1 = "PREFIX dct: <http://purl.org/dc/terms/> \n"
                    + "DELETE "
                    + "{ "
                    + "GRAPH ?graphCl {\n"
                    + "?p  dct:isPartOf ?sub . "
                    + "}"
                    + "}"
                    + "WHERE { "
                    + "  GRAPH ?graphCl {\n"
                    + "  ?p  dct:isPartOf ?sub .\n"
                    + "  ?sub a  <http://ucuenca.edudeletequery1.ec/ontology#SubCluster> .\n"
                    + " } "
                    + "}";

            String deletequery2 = "PREFIX dct: <http://purl.org/dc/terms/> \n"
                    + "DELETE "
                    + "{ "
                    + "GRAPH ?graphCl {\n"
                    + "?sub  dct:isPartOf ?cl . "
                    + "}"
                    + "}"
                    + "WHERE { "
                    + "  GRAPH ?graphCl {\n"
                    + " ?sub  dct:isPartOf ?cl .\n"
                    + "  ?sub a  <http://ucuenca.edu.ec/ontology#SubCluster> ."
                    + "  ?cl a  <http://ucuenca.edu.ec/ontology#Cluster> \n"
                    + " } "
                    + "}";

            String deletequery3 = "PREFIX dct: <http://purl.org/dc/terms/> \n"
                    + "DELETE "
                    + "{ "
                    + "GRAPH ?graphCl {\n"
                    + "?sub a  <http://ucuenca.edu.ec/ontology#SubCluster> . "
                    + "}"
                    + "}"
                    + "WHERE { "
                    + "  GRAPH ?graphCl { "
                    + "  ?sub a  <http://ucuenca.edu.ec/ontology#SubCluster> .\n"
                    + " } "
                    + "}";

            Update q = connection.prepareUpdate(QueryLanguage.SPARQL, deletequery1);
            q.setBinding("graphCl", vf.createURI(conn.getClusterGraph()));
            q.execute();

            Update q2 = connection.prepareUpdate(QueryLanguage.SPARQL, deletequery2);
            q2.setBinding("graphCl", vf.createURI(conn.getClusterGraph()));
            q2.execute();

            Update q3 = connection.prepareUpdate(QueryLanguage.SPARQL, deletequery3);
            q3.setBinding("graphCl", vf.createURI(conn.getClusterGraph()));
            q3.execute();
            log.info("Delete succesful");

        } catch (MalformedQueryException | UpdateExecutionException ex) {
            java.util.logging.Logger.getLogger(Redi.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getLabelCluster(String uri) {
        try {
            RepositoryConnection connection = conn.getConnection();
            
            String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                    + "SELECT ?clabel WHERE {\n"
                    + "GRAPH  ?graphCl {\n"
                    + "  ?cl rdfs:label ?clabel .\n"
                    + "  filter (lang (?clabel) = \"en\"  )                                \n"
                    + "} }";
            
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("cl", vf.createURI(uri));
            q.setBinding("graphCl", vf.createURI(conn.getClusterGraph()));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet res = result.next();
                if (res.hasBinding("clabel")){
                 return   res.getBinding("clabel").getValue().stringValue();
                        }
            }
            return null;
        } catch (RepositoryException ex) {
            java.util.logging.Logger.getLogger(Redi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (QueryEvaluationException ex) {
            java.util.logging.Logger.getLogger(Redi.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedQueryException ex) {
            java.util.logging.Logger.getLogger(Redi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public Boolean validateSubcluster(String cl, String uri, String uriname, List<UnescoHierarchy> unescoset , Double scoremin) {
        try {
            DbpediaRepository drep = DbpediaRepository.getInstance();
            Dbpedia db = new Dbpedia(drep);
            Preprocessing p = Preprocessing.getInstance();
               
            String concept = uriname+", "+db.getAbstract(uri);
            String clabel = getLabelCluster(cl);
            String clusterElements = "";
            for (UnescoHierarchy u : unescoset){
             if (u.getLevel4Uri().toString().equals(cl)){
                clusterElements = u.getLevels6()+", "+clusterElements;
             }
            }
            if (concept != null && clabel != null) {
                Object number = p.CompareText(clusterElements + clabel, concept, "weightedScoring");
                
                Double val;
                if (number instanceof Double) {
                    val = (Double) number;
                } else {
                    val = ((BigDecimal) number).doubleValue();
                }

                if (val > scoremin) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (RepositoryException | IOException ex) {
            java.util.logging.Logger.getLogger(Redi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
