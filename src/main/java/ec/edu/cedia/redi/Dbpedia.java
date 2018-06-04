/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
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
 * @author joe
 */
public class Dbpedia {

    private final DbpediaRepository conn;
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private static final Logger log = LoggerFactory.getLogger(Dbpedia.class);

    public Dbpedia(DbpediaRepository conn) {
        this.conn = conn;
    }

    public boolean isEntityValid(String ent) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        try {
            String query = "ASK { ?entity a ?t .\n"
                    + "filter ( ?t = dbo:Place || ?t = dbo:Person  )\n"
                    + "}";
            Statement obj_place = new StatementImpl(vf.createURI(ent), RDF.TYPE, vf.createURI("http://dbpedia.org/ontology/PopulatedPlace"));
            Statement obj_person = new StatementImpl(vf.createURI(ent), RDF.TYPE, vf.createURI("http://dbpedia.org/ontology/Person"));
            if (connection.hasStatement(obj_place, true) || connection.hasStatement(obj_person, true)) {
                return true;
            }

            //BooleanQuery q = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
            // q.setBinding("cluster", vf.createURI(RediRepository.CLUSTERS_CONTEXT));
            //  q.setBinding("entity", vf.createURI(ent));
            // return q.evaluate();
        } catch (Exception ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return false;
    }

    public Boolean isCorrectType(String ent) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        //   List<String> cluster = new ArrayList<>();
        try {
            String query = "select  distinct ?response where {\n"
                    + "?entity a ?t .\n"
                    + "BIND(EXISTS{ ?entity  a ?t  } AS ?response) .\n"
                    + "filter ( ?t = dbo:Place || ?t = dbo:Person  || ?t = geo:SpatialThing )\n"
                    + "}";
            // String query = "Select * where {?a a dbo:Place } limit 10";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("entity", vf.createURI(ent));
            TupleQueryResult result = q.evaluate();
            while (result.hasNext()) {
                BindingSet variables = result.next();
                System.out.print(variables);
                if (variables.hasBinding("response") && "1".equals(variables.getBinding("response").getValue().stringValue())) {
                    return true;
                } else {
                    return false;
                }
                // cluster.add(variables.getBinding("cl").getValue().stringValue());
                //authors.add(new Author((URI) variables.getBinding("cl").getValue(), variables.getBinding("kws").getValue().stringValue()));
            }
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return false;
    }

    public List<NodoDbpedia> isAcademicDbpedia(String ent) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        //   List<String> cluster = new ArrayList<>();
        try {
            String query = "select distinct ?entity ?lesp ?leng where {\n"
                    + " ?s dbo:academicDiscipline ?entity  .\n"
                    + "OPTIONAL {\n"
                    + "?entity rdfs:label ?lesp .\n"
                    + "     FILTER ( lang(?lesp) = \"es\" ) .\n"
                    + "}\n"
                    + "OPTIONAL {\n"
                    + "?entity rdfs:label ?leng .\n"
                    + "    FILTER ( lang(?leng) = \"en\" ) .\n"
                    + "}\n"
                    + " \n"
                    + "}";
            // String query = "Select * where {?a a dbo:Place } limit 10";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("entity", vf.createURI(ent));
            TupleQueryResult result = q.evaluate();
            List<NodoDbpedia> laca = new ArrayList();
            //dbn.setOrigin(ent);

            while (result.hasNext()) {

                BindingSet variables = result.next();
                if (variables.hasBinding("leng") || variables.hasBinding("les")) {
                    NodoDbpedia academic = new NodoDbpedia(ent);
                    if (variables.hasBinding("leng")) {
                    academic.setNameEn(variables.getValue("leng").stringValue());
                    }
                    if (variables.hasBinding("lesp")) {
                    academic.setNameEs(variables.getValue("lesp").stringValue());
                    }
                    laca.add(academic);
                    // dbn.getAcademic().add(academic);

                }
                //  String 
                // cluster.add(variables.getBinding("cl").getValue().stringValue());
                //authors.add(new Author((URI) variables.getBinding("cl").getValue(), variables.getBinding("kws").getValue().stringValue()));
            }
            return laca;
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return null;
    }

    public List<NodoDbpedia> getNearDbpediaAca(String ent) throws RepositoryException {
      
        //   List<String> cluster = new ArrayList<>();
    
            String query = "select distinct ?res ?lesp ?leng  where \n"
                    + "{   "
                    + " ?entity  <http://purl.org/dc/terms/subject> ?lv1 .\n"
                    + " ?res     <http://purl.org/dc/terms/subject>  ?lv1 .\n"
                    + "  ?d dbo:academicDiscipline ?res .\n"
                    + "  OPTIONAL {\n"
                    + "   ?res rdfs:label ?lesp .\n"
                    + "      FILTER ( lang(?lesp) = \"es\" ) . \n"
                    + "   }\n"
                    + "    OPTIONAL {\n"
                    + "    ?res rdfs:label ?leng .\n"
                    + "     FILTER ( lang(?leng) =  \"en\" ) .\n"
                    + "      }\n"
                    + "}";
            // String query = "Select * where {?a a dbo:Place } limit 10";
          return getNodesDbpedia (query , ent);
    }

    public List<NodoDbpedia> DirectCategory(String ent) {
     
            String query = "select distinct ?res ?lesp ?leng\n"
                    + "where {\n"
                    + "?entity dct:subject ?s .\n"
                    + "bind( strafter( STR(?s), \":\" ) as ?prefix ) .\n"
                    + "bind( strafter( STR(?prefix ), \":\" ) as ?cat ) .\n"
                    + "bind (URI (CONCAT(\"http://dbpedia.org/resource/\",?cat)) as ?res) ."
                    + "?res a ?tipo . "
                    + "OPTIONAL {\n"
                    + "?res rdfs:label ?lesp .\n"
                    + "     FILTER ( lang(?lesp) = \"es\" ) .\n"
                    + "}\n"
                    + "OPTIONAL {\n"
                    + "?res rdfs:label ?leng .\n"
                    + "    FILTER ( lang(?leng) = \"en\" ) .\n"
                    + "}\n"
                    + "} ";
         
                return getNodesDbpedia (query , ent);
           
    }

    public List<NodoDbpedia> getBroaderAca(String entity) {
        String query = "select distinct ?res ?lesp ?leng\n"
                + "where {\n"
                + "?entity dct:subject ?s .\n"
                + "?s skos:broader ?b\n"
                + "bind( strafter( STR(?b), \":\" ) as ?prefix ) .\n"
                + "bind( strafter( STR(?prefix ), \":\" ) as ?cat ) .\n"
                + "bind (URI (CONCAT(\"http://dbpedia.org/resource/\",?cat)) as ?res) .\n"
                + "?a <http://dbpedia.org/ontology/academicDiscipline> ?res .\n"
                + "\n"
                + "OPTIONAL {\n"
                + "?res rdfs:label ?lesp .\n"
                + "     FILTER ( lang(?lesp) = \"es\" ) .\n"
                + "}\n"
                + "OPTIONAL {\n"
                + "?res rdfs:label ?leng .\n"
                + "    FILTER ( lang(?leng) = \"en\" ) .\n"
                + "}\n"
                + "} ";
        
            return   getNodesDbpedia (query , entity);

    }
    
    
      public List<NodoDbpedia> getLabels (String ent) throws RepositoryException {
        RepositoryConnection connection = conn.getConnection();
        //   List<String> cluster = new ArrayList<>();
        try {
            String query = "select distinct ?entity ?lesp ?leng where {\n"
                    + "OPTIONAL {\n"
                    + "?entity rdfs:label ?lesp .\n"
                    + "     FILTER ( lang(?lesp) = \"es\" ) .\n"
                    + "}\n"
                    + "OPTIONAL {\n"
                    + "?entity rdfs:label ?leng .\n"
                    + "    FILTER ( lang(?leng) = \"en\" ) .\n"
                    + "}\n"
                    + " \n"
                    + "}";
            // String query = "Select * where {?a a dbo:Place } limit 10";
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("entity", vf.createURI(ent));
            TupleQueryResult result = q.evaluate();
            List<NodoDbpedia> laca = new ArrayList();
            //dbn.setOrigin(ent);

            while (result.hasNext()) {

                BindingSet variables = result.next();
                if (variables.hasBinding("leng") || variables.hasBinding("les")) {
                    NodoDbpedia academic = new NodoDbpedia(ent);
                    if (variables.hasBinding("leng")){
                    academic.setNameEn(variables.getValue("leng").stringValue());
                    }
                    if (variables.hasBinding("lesp")){
                    academic.setNameEs(variables.getValue("lesp").stringValue());
                    }
                    laca.add(academic);
                    // dbn.getAcademic().add(academic);

                }
                //  String 
                // cluster.add(variables.getBinding("cl").getValue().stringValue());
                //authors.add(new Author((URI) variables.getBinding("cl").getValue(), variables.getBinding("kws").getValue().stringValue()));
            }
            return laca;
        } catch (MalformedQueryException ex) {
            log.error("Cannot execute query.", ex);
        } catch (QueryEvaluationException ex) {
            log.error("Cannot evaluate query.", ex);
        } finally {
            connection.close();
        }
        return null;
    }
    
    public List<NodoDbpedia> getNodesDbpedia  (String query , String entity) {
    try {
            RepositoryConnection connection = conn.getConnection();
            
            TupleQuery q = connection.prepareTupleQuery(QueryLanguage.SPARQL, query);
            q.setBinding("entity", vf.createURI(entity));
            TupleQueryResult result = q.evaluate();
            List<NodoDbpedia> nl = new ArrayList();

            while (result.hasNext()) {

                BindingSet variables = result.next();
                if (variables.hasBinding("res")) {
                    NodoDbpedia naca = new NodoDbpedia(variables.getValue("res").stringValue());
                    if (variables.hasBinding("leng")) {
                        naca.setNameEn(variables.getValue("leng").stringValue());
                    }
                    if (variables.hasBinding("lesp")) {
                        naca.setNameEs(variables.getValue("lesp").stringValue());
                    }
                    nl.add(naca);
                }

            }

            return nl;

        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            java.util.logging.Logger.getLogger(Dbpedia.class.getName()).log(Level.SEVERE, null, ex);
        }
     
    return null;
    }
}
