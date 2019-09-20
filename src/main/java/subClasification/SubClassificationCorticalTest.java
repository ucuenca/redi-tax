/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subClasification;


import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.Dbpedia;

import ec.edu.cedia.redi.repository.DbpediaRepository;
import ec.edu.cedia.redi.KimukRepository;
import ec.edu.cedia.redi.NodoDbpedia;
import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.repository.RediRepository;
import ec.edu.cedia.redi.repository.Repositories;
import ec.edu.cedia.redi.utils.Ngrams;
import ec.edu.cedia.redi.utils.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.repository.RepositoryException;
import org.slf4j.LoggerFactory;
import plublication.Preprocessing;

/**
 *
 * @author joe
 */
public class SubClassificationCorticalTest {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SubClassificationCorticalTest.class);
   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, IOException {
         // executeSubGroup(false, false , 1 , 0 , "");
     //   String [] years = {"2010","2011","2012","2013","2014","2015","2016","2017","2018","2019"};
     //   executeSubGroup (false , false , 1, 0 , years);
       // executeSubGroup(true, true , 2 , 0);
      //  csvoutput ();
      // executeSubGroup(false, false , 1 , 0 , "");
      //cluster (getclusterfile ());
      //cluster (getclusterfileA ());
      cluster (getclusterfileB ());
    }
    
 
    
    
    public static void cluster ( HashMap <String, List<documentCl>> map) {
           HashMap <String,HashMap<String, Integer>> hm = new HashMap ();
        for ( String k : map.keySet() ) {
           System.out.println ("CLUSTER:"+k);
          try {
            List<String> valid = oneclusterproc (k ,map.get(k));
           /*   List<clusterLabel> cll = new ArrayList ();
                      for (String v : valid) {
                        clusterLabel cl = new clusterLabel (v, 0);
                        cll.add(cl);
                      }*/
           
           
                
            
             
            // List <String> authorUri = indexDoc ( map.get(k)) ;
                List <String> authorUri = indexAuth ( map.get(k)) ;
               /*for (  documentCl d :map.get(k)) {
                 for (String aut : d.getAuthors()){ 
                     if (!authorUri.contains(aut))
                     {
                        authorUri.add(aut);
                     }
                 }
               }*/
             
             
                for  (String aut : authorUri) {
                   HashMap<String, Integer> hms;
                  
                     if (hm.containsKey(aut)){
                       
                       hms =  hm.get(aut);
                                             } 
                     else {
                         hms =  new HashMap ();
                       }

                       for (String v :valid) {
                         
                           int count = hms.getOrDefault(v, 0); 
                            hms.put(v, count + 1); 
                         
                       }
                       //list.addAll(valid);
                       
                    hm.put(aut, hms);
                      
                }
                  
             verdata (hm);
             
            
          } catch (RepositoryException ex) {
            Logger.getLogger(SubClassificationCorticalTest.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
    
    
    }
    
    private  static List <String> indexDoc ( List<documentCl> map) {
       List <String> authorUri = new ArrayList (); 
               for (  documentCl d : map) {
                 for (String aut : d.getAuthors()){ 
                     if (!authorUri.contains(aut))
                     {
                        authorUri.add(aut);
                     }
                 }
               }
               return authorUri;
    }
    
      private  static List <String> indexAuth ( List<documentCl> map) {
       List <String> authorUri = new ArrayList (); 
               for (  documentCl d : map) {
                   String aut =  d.getId();
                     if (!authorUri.contains(aut))
                     {
                        authorUri.add(aut);
                     }
                 
               }
               return authorUri;
    }
    
     private static void verdata(HashMap<String, HashMap<String, Integer>> hm) {
       for ( String k : hm.keySet()) {
         System.out.println ("Author-"+k);
         hm.get(k).entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(System.out::println);
          
       }
     }
    
    public static List<String>  oneclusterproc (String c , List<documentCl> map) throws RepositoryException {
       List<String> total = new ArrayList<>();
      for  (documentCl d : map){
        System.out.println (d.getId()+"-"+d.getKeywords()+"-"+d.getAuthors());
        List<String> stringList = new ArrayList<String>(Arrays.asList(d.getKeywords().split(";")));
        total.addAll(stringList);
      }
       DbpediaRepository drep = DbpediaRepository.getInstance();
            Dbpedia db = new Dbpedia(drep);
            Preprocessing p = Preprocessing.getInstance();
            
       
      
       Object objdbpedia = null;
                    if (!total.isEmpty()) {
                    
                    List<String> topk = topkeyword(total, 1);

                    int num = 0;
                    if (topk.size() > 20) {
                        num = 20;
                    } else {
                        num = topk.size();
                    }
                    System.out.print("Dbpedia Entities");
                     objdbpedia = p.detectDbpediaEntitiestoArray(String.join(", ", topk.subList(0, num)));
                    }
                    if (objdbpedia != null) {
                        Map<String, String> entities = (Map<String, String>) objdbpedia;
                        System.out.print(entities);
                        // Map <String,String> valid = new HashMap ();
                        if (!entities.isEmpty()) {
                            List<String> valid = new ArrayList<>();
                            List<NodoDbpedia> nd = new ArrayList<>();
                            for (Map.Entry<String, String> mdp : entities.entrySet()) {
                                if (!db.isCorrectType(mdp.getValue())) {
                                    NodoDbpedia aux = testacademic(mdp.getKey(), mdp.getValue(), db);
                                    if (aux != null) {
                                        nd.add(aux);

                                        if (aux != null && !aux.getAcademic().isEmpty()) {
                                            for (NodoDbpedia ndb : aux.getAcademic()) {
                                                valid.add(ndb.getUri());
                                            }
                                        }
                                    }

                                }
                            }

                            for (NodoDbpedia nd2 : nd) {

                                if (nd2.getAcademic() == null || nd2.getAcademic().isEmpty()) {
                                    nd2 = secondchance(nd2, valid, db);
                                    System.out.println("Segunda Pasada");
                                }

                                for (NodoDbpedia n : nd2.getAcademic()) {
                                    System.out.println(nd2.getOrigin() + "-" + nd2.getUri() + "=" + n.getUri() + "-" + n.getNameEn());

                                }

                            } 
                            
                             
                               for (String val:valid) {
                             System.out.println ("Resultados "+c);
                             System.out.println (val);
                            }
                              return valid; 
                                                            
                        }
                    }
      return null;

    }

    public static void test(Redi r, Dbpedia db, Preprocessing p) throws RepositoryException, IOException {
        System.out.print("Test is Academic");
        System.out.print(db.isAcademicDbpedia("http://dbpedia.org/resource/Revolution"));
        System.out.print(db.isAcademicDbpedia("http://dbpedia.org/resource/Distinctive_feature"));

        System.out.printf("Test dbpedia");
        System.out.print(db.DirectCategory("http://dbpedia.org/resource/Typhoon_Vicente"));

        Object v = p.CompareText(StringUtils.stripAccents("Computer Sciences"), StringUtils.stripAccents("Computer Sciences"), "weightedScoring");
        System.out.print(v);
        System.out.print("TEST TYPE");
        System.out.println(db.isCorrectType("http://dbpedia.org/resource/Ecuador"));
        // db.testquery();
//            db.isEntityValid ("http://dbpedia.org/resource/Ecuador");
        System.out.print("DBPEDIA TEST");
        System.out.println(p.detectDbpediaEntitiestoArray("Ecuador, computer Sciences"));
        List<NodoDbpedia> l = db.isAcademicDbpedia("http://dbpedia.org/resource/Semantic_Web");
        System.out.println(l.get(0).getNameEn() + l.get(0).getNameEs());

    }
    
    public static HashMap <String, List<documentCl>>  getclusterfile () {
     Utils uti = new Utils (); 
     List<List<String>> l = uti.readToCSV("/home/joe/TestClust/data/salida.csv");
     HashMap <String, List<documentCl>> map = new HashMap ();
    
     for ( List<String> list :  l) {
       documentCl d = new documentCl (list.get(1), list.get(0) , list.get(3).split(";"), list.get(2)); 
      // System.out.print (list.get(1) +"-"+list.get(0));
              if (map.containsKey(d.getCluster())){
                 map.get(d.getCluster()).add(d);
              }else {
                   List<documentCl> documents = new ArrayList ();
                   documents.add(d);
                   map.put(d.getCluster(), documents);
                   
        }
     }
     
     return map;
    }
    
    
     public static HashMap <String, List<documentCl>>  getclusterfileA () {
     Utils uti = new Utils (); 
     List<List<String>> l = uti.readToCSV("/home/joe/TestClust/data/salidaAut.csv");
     HashMap <String, List<documentCl>> map = new HashMap ();
    
     for ( List<String> list :  l) {
       documentCl d = new documentCl (list.get(1), list.get(0) ,null , list.get(2)); 
      // System.out.print (list.get(1) +"-"+list.get(0));
              if (map.containsKey(d.getCluster())){
                 map.get(d.getCluster()).add(d);
              }else {
                   List<documentCl> documents = new ArrayList ();
                   documents.add(d);
                   map.put(d.getCluster(), documents);
                   
        }
     }
     
     return map;
    }
     
       public static HashMap <String, List<documentCl>>  getclusterfileB () {
     Utils uti = new Utils (); 
     List<List<String>> l = uti.readToCSV("/home/joe/TestClust/data/salidaAut2.csv");
     HashMap <String, List<documentCl>> map = new HashMap ();
      
     for ( List<String> list :  l) {
       documentCl d = new documentCl (list.get(0), list.get(1)  ,null , list.get(2)); 
      // System.out.print (list.get(1) +"-"+list.get(0));
              if (map.containsKey(d.getCluster())){
                 map.get(d.getCluster()).add(d);
              }else {
                   List<documentCl> documents = new ArrayList ();
                   documents.add(d);
                   map.put(d.getCluster(), documents);
                   
        }
     }
     
     return map;
    }
    
    public static void executeSubGroup(Boolean filter, Boolean Save , int key , int rep , String [] years) {
    
       for (String y : years){
         System.out.print ("Procesando "+y);  
       executeSubGroup(false, true , 1 , 0 , y);
       System.out.print ("Procesado con exito "+y);  
       
       }
    
    }
    
    

    public static void executeSubGroup(Boolean filter, Boolean Save , int key , int rep , String year) {
        try {
            Repositories rp;
            if (rep == 0){
            rp = RediRepository.getInstance();
            }else {
            rp = KimukRepository.getInstance();
            }
            Redi r = new Redi(rp);

            DbpediaRepository drep = DbpediaRepository.getInstance();
            Dbpedia db = new Dbpedia(drep);
            Preprocessing p = Preprocessing.getInstance();

            List<String> clusters = r.getclustersAvailable();
            for (String cl : clusters) {
                log.info("Cluster: " + cl); // Borrar normal
               //if (!(cl.equals("http://skos.um.es/unesco6/1203")||cl.equals(" http://skos.um.es/unesco6/3304")))
                if (!cl.equals("http://skos.um.es/unesco6/1203"))
                { continue; }
                
                // if (true){
                if (!(filter && r.askSubCluster(cl))) {
                     log.info("Consultando autores para : " + cl);
                    List<String> total = new ArrayList<>();
                    
                    // List<Author> authors = r.getAuthorsbyCluster("http://skos.um.es/unesco6/1203");
                     //BORRAR
                    //List<Author> authors = r.getPublicationsbyClusterbyYear("",year);      //Año                    
                     // List<Author> authors =   r.getPublicationsbyClusterandAuthors("");  // Test           
                     List<Author> authors = r.getAuthorsbyCluster(cl); // Normal
                    System.out.print("Numero de autores encontrados "+authors.size());
                     Utils uti = new Utils ();
                     uti.writeToCSVRaw((ArrayList<Author>) authors , "Raw"); // Test
                    for (Author a : authors) {
                        System.out.println("AUTOR: " + a.getURI());
                        
                        List<String> listauthork = preProccesingKeywords(a.getKeywords() , key);
                        a.setKeywords(String.join(", ", listauthork));
                        total.addAll(listauthork); 

                        if (a.getTopics() != null && a.getTopics().length() > 0) {
                            List<String> listauthorT = preProccesingKeywords(a.getTopics() , key);
                            a.setTopics(String.join(", ", listauthorT));
                            total.addAll(listauthorT);
                        }

                    }
                    
                    // uti.writeToCSV((ArrayList<Author>) authors); 
                      uti.writeToCSVRaw((ArrayList<Author>) authors , "AutoresKeyP"); 
               
                    System.out.println("FINISH PREPRO");
                    

                    /*    for (String k  :topkeyword (total)) {
                     System.out.println (k);
            
                     }*/
                    
                    Object objdbpedia = null;
                    if (!total.isEmpty()) {
                    
                    List<String> topk = topkeyword(total, 1);

                    int num = 0;
                    if (topk.size() > 10) {
                        num = 10;
                    } else {
                        num = topk.size();
                    }
                    System.out.print("Dbpedia Entities");
                     objdbpedia = p.detectDbpediaEntitiestoArray(String.join(", ", topk.subList(0, num)));
                    }
                    if (objdbpedia != null) {
                        Map<String, String> entities = (Map<String, String>) objdbpedia;
                        System.out.print(entities);
                        // Map <String,String> valid = new HashMap ();
                        if (!entities.isEmpty()) {
                            List<String> valid = new ArrayList<>();
                            List<NodoDbpedia> nd = new ArrayList<>();
                            for (Map.Entry<String, String> mdp : entities.entrySet()) {
                                if (!db.isCorrectType(mdp.getValue())) {
                                    NodoDbpedia aux = testacademic(mdp.getKey(), mdp.getValue(), db);
                                    if (aux != null) {
                                        nd.add(aux);

                                        if (aux != null && !aux.getAcademic().isEmpty()) {
                                            for (NodoDbpedia ndb : aux.getAcademic()) {
                                                valid.add(ndb.getUri());
                                            }
                                        }
                                    }

                                }
                            }

                            for (NodoDbpedia nd2 : nd) {

                                if (nd2.getAcademic() == null || nd2.getAcademic().isEmpty()) {
                                    nd2 = secondchance(nd2, valid, db);
                                    System.out.println("Segunda Pasada");
                                }

                                for (NodoDbpedia n : nd2.getAcademic()) {
                                    System.out.println(nd2.getOrigin() + "-" + nd2.getUri() + "=" + n.getUri() + "-" + n.getNameEn());

                                }

                            }
                         

                            //subClusterAuthor(authors, nd, cl, r, Save);
                           // subClusterAuthorYear (authors, nd, cl, r, Save , year);
                        }
                    } else {
                        log.info("Cant be found cluster" + cl);
                    }
                } else {
                    log.info(cl + "  already proccess ");
                }
                /*
                 System.out.println ("Related K");
                 List <String> Relatedk = getrelatedkey (topk.subList(0,num) , "Computer Sciences" , 1.0);             
                 // LinkedHashMap<String, String> topkey = joinkey(topk.subList(0,num));
                 System.out.println ("Keyword mas importantes por Cluster");
                 System.out.println (Relatedk);
                 subclusterAuthors (authors , Relatedk); */
                /*  System.out.println ("Keyword mas importantes por Cluster");
                 System.out.print (topkey);
                 
                 subclusterAuthor (authors , topkey);*/

            }
        } catch (RepositoryException ex) {

            Logger.getLogger(SubClassificationCorticalTest.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    public static List<String> preProccesingKeywords(String keys , int k) {
        try {
            Preprocessing p = Preprocessing.getInstance();
           // System.out.println ( a.getURI());
            //  Object r = p.detectLanguage(a.getKeywords());
            //  System.out.println (r.toString());
            //System.out.println (a.getKeywords());
           // String all = key;
            List <String>  kgrams;
            String prek = keys.replace("null", "").replaceAll("[\"-\']", "").trim();
            if (prek.length() > 1000){
              kgrams = countergrams (prek);
            }else {
                kgrams  = Arrays.asList(prek.split(";"));
            }
            String tplane = "";
           
               kgrams= kgrams.size() > 100 ? kgrams.subList(0, 99): kgrams;
               // tplane = kgrams.subList(0,99);
                tplane = StringUtils.join(kgrams, ";");
            
            
           /* if (prek.length() > 3000) {
                prek = prek.substring(0, 3000);
            }*/
            List<String> kt = Arrays.asList(p.traductor(tplane , k-1).toString().replace("[\"", "").replace("\"]", "").replace("context,", "").toLowerCase().split(";"));
            kt = kt.stream().map(s -> s.trim()).filter(s -> s.length() > 0).collect(Collectors.toList());
            List<String> ktnd = deleteDuplicate(kt);
           // System.out.println(ktnd);

          //  System.out.println (s);
           //List<String> deleteDuplicate = deleteDuplicate (trad);
            // topkeyword (ktnd)
            
            System.out.println (StringUtils.join(ktnd, ";"));
            System.out.println("**********************");

            return kt;

        } catch (IOException ex) {
            Logger.getLogger(SubClassificationCorticalTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
  

    public static List<String> deleteDuplicate(List<String> list) {

        return new ArrayList<>(new HashSet<>(list));
    }

    
    public static List lamdaCounter(String [] sepk) {
        
      //  String [] sepk = keyraw.split(";");
      /*   String[] c = { "China", "Australia", "India", "USA", "USSR", "UK", "China", 
  "France", "Poland", "Austria", "India", "USA", "Egypt", "China" };*/
        
        Map<String, Long> counterMap = new HashMap<>();
  
    Stream.of(sepk).collect(Collectors.groupingBy(k -> k, ()-> counterMap,Collectors.counting()));
  //counterMap.entrySet().stream().map();
  // counterMap.entrySet().stream().m
    List <String> l = counterMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());
    // List<String> collect1 = collect.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    System.out.print (String.join(",", l)); 
    return l;   
// counterMap.forEach((k, v) -> System.out.println((k + ":" + v)));
}
    
    public static ArrayList<String> ngrams (String text , int number) {
        Ngrams n = new Ngrams();	
   // String text = "   Turning and turning in the widening gyre\r\n    The falcon cannot hear the falconer;\r\n    Things fall apart; the centre cannot hold;\r\n    Mere anarchy is loosed upon the world   ";
   // ArrayList<String> words = Ngrams.sanitiseToWords(text); 
    ArrayList<String> words = new ArrayList<String>(Arrays.asList(text.split(" "))); 
    ArrayList<String> ngrams = Ngrams.ngrams(words, number);
    return ngrams;
    }
    
    public static List <String> countergrams (String text) {
    // String keyraw = "Computer Science;Education;Computer Science Applications;Artificial Intelligence;Engineering;Electrical and Electronic Engineering;Computer Networks and Communications;Control and Systems Engineering;Biomedical Engineering;Computer Science (all);Control and Optimization;Information Systems;Information Systems and Management;Signal Processing;Safety, Risk, Reliability and Quality;Hardware and Architecture;Software;Civil and Structural Engineering;Earth-Surface Processes;Mathematics;Health Informatics;Applied Mathematics;Condensed Matter Physics;Electronic, Optical and Magnetic Materials;Computer Vision and Pattern Recognition;Mathematics (all);Geography, Planning and Development;Bioengineering;Theoretical Computer Science;Computers in Earth Sciences;Human-Computer Interaction;Modeling and Simulation;COMPUTER SCIENCE, INFORMATION SYSTEMS;Semantic Web;Computer Graphics and Computer-Aided Design;Library and Information Sciences;Linkeddata;Web Services;Data Integration;semantic annotations";
    // String list2 =" Query Languages;data integration;Data Integration;data mining;Data mining;Data Mining;linked data;Linked Data;Linked data;semantic web;Semantic Web;semantic Web;Semantic web;DATA INTEGRATION;QUERY LANGUAGES;LINKED DATA;DATA MINING;SEMANTIC WEB;APLICACIONES INTERACTIVAS;GINGA-NCL;GUIA DE PROGRAMACION ELECTRONICA;ISDB-TB;LABORATORIO DE TELEVISION DIGITAL;REGISTRO DE ACTIVIDAD DEL USUARIO;SERVIDOR DE APLICACIONES;TELEVISION DIGITAL TERRESTRE;DATA WAREHOUSE;HEFESTO;OLAP;ON-LINE PROCESO ANALITICO;PENTAHO;SISTEMA DE SOPORTE A DECISIONES;ONTOLOGIAS;PROPIEDADES SEMANTICAS;SISTEMAS DE RECOMENDACION SEMANTICOS;TELEVISION DIGITAL;WEB SEMANTICA;BIBLIOTECA;REGLAS DE ASOCIACION;BIBLIOMINING;MINERIA DE DATOS;ARQUITECTURA DE SISTEMAS;METODO DHARMA;METODOLOGIA NEON;RED DE ONTOLOGIAS;SISTEMAS DE INFORMACION;ISTAR;APACHE HIVE;APACHE SERVICEMIX;BIG DATA;CUMULOSRDF;D2RQ;FUENTES SEMI-ESTRUCTURADAS;INTEGRACION DE DATOS;NOSQL;RDF;TRANSFORMACION AUTOMATICA A RDF;DATOS HIDRO-METEOROLOGICOS;CONSULTAS FEDERADAS;EXPLOTACION DE DATOS;GEO LINKED DATA;GEOSPARQL;ANOTACIONES SEMANTICAS;ONTOLOGIAS MEDICAS;SEGMENTACION;VISUALIZADOR 3D BASADO EN WEB;DICOM Ontology;Web 3D-Visualizer;Volumetric Image;semantic annotations;Semantic Annotations;DICOM ONTOLOGY;SEMANTIC ANNOTATIONS;VOLUMETRIC IMAGE;WEB 3D-VISUALIZER;Speech to text;Automatic speech recognition;Audio content analysis;Automatic audio segmentation;Python;AUDIO CONTENT ANALYSIS;AUTOMATIC AUDIO SEGMENTATION;AUTOMATIC SPEECH RECOGNITION;PYTHON;SPEECH TO TEXT;Bibliomining;Academic libraries;Literature review;literature review;ACADEMIC LIBRARIES;LITERATURE REVIEW;perceptual image hashing;video identification;GeoNames;DBpedia;DBPedia;Geospatial RESTful Service;World Geodetic System (WGS84);Semantic Annotation;semantic annotation;Semantic annotation;Geospatial and statistical information;INSPIRE;semantic enrichment;Web services;web services;EPG;NLP;ontologies;Ontologies;WEB SERVICES;SEMANTIC ENRICHMENT;ONTOLOGIES;Author Name Disambiguation;Digital libraries;Digital Repositories;Digital repositories;ARRAY;multi-screen;interactive advertising;ad delivery;audio fingerprint;advertising monitoring;automatic content recognition;signal processing;Signal processing;demographic stereotyping;semantic recommender system;cold-start;COLD-START;DEMOGRAPHIC STEREOTYPING;SEMANTIC RECOMMENDER SYSTEM;Federated Queries;federated queries;Virtual Integration;SPARQL;Ecuadorian universities;Ecuadorian Universities;DIGITAL REPOSITORIES;ECUADORIAN UNIVERSITIES;FEDERATED QUERIES;VIRTUAL INTEGRATION;Linked Health Data cloud;Automatic Semantic Annotation;RDF-ization;visualization;Visualization;AUTOMATIC SEMANTIC ANNOTATION;LINKED HEALTH DATA CLOUD;RDF-IZATION;VISUALIZATION;user authentication;recommender system;Recommender system;Digital TV;digital TV;GeoRDFization;GeoSPARQL;CLASIFICADOR;DACTILOLOGIA;LENGUAJE DE SEÑAS;REDES NEURONALES ARTIFICIALES;Ontology of Water Resources;Ontological Engineering;NeOn Methodology;Digital repository;communication and language;Mobile applications;mobile applications;Mobile Applications;REST;geospatial RESTful services;RESTful service;Co-reference;Links;heuristics;Heuristics;Geospatial linked data;higher education;Higher Education;Higher education;text mining;Text mining;Text Mining;linked government data;Semantic Repository;Semantic repository;Context Models;Context models;Context Actors;Context actors;Strategic Dependency;Strategic dependency;ontology;Ontology;CONTEXT ACTORS;ONTOLOGY;STRATEGIC DEPENDENCY;CONTEXT MODELS;SEMANTIC REPOSITORY;Strategic Dependency;Repositorio Semántico;Modelos de Contexto;Semantic Repository;Actores de Contexto;Dependencias estratégicas;Context Actors;Context Models;Datos Enlazados;Linked Data;Linked Data;Ontology;Ontología;Digital library;Digital Library;OAI-MPH;Web Semántica;Repositorios Digitales;OAI-MPH;OAI-MPH;Digital Library;RDF;RDF;Semantic Web;Semantic Web ";
      List<String> list = new ArrayList<String>();
//add some stuff
    // String text = (keyraw+";"+list2).toLowerCase();

     

     String [] sepk = text.toLowerCase().split(";");
    
     
     for (String  t :sepk ){
        //  System.out.println(t+"--------");
         if (t.split(" ").length > 1){
          ArrayList<String> ngramw  =  ngrams (t,2);
          for (String ng :ngramw){
          if (stopwords( ng)){
          // System.out.print (ng+"Es stop word");
          }else {
          list.add(ng);
          }
          }
        // list.addAll(ngramw);
         System.out.println (ngramw);
         }else {
         list.add(t);
         }
     }
     
     String[] stringArray = list.toArray(new String[0]);
      return lamdaCounter(stringArray);
    }
    
    public static boolean stopwords(String compare) {
        
        List<String> stoplist =  new ArrayList();
        stoplist.add("and");
        stoplist.add("of");
        stoplist.add("from");
        stoplist.add("to");
        stoplist.add("y");
        stoplist.add("the");
        stoplist.add("el");
        stoplist.add("los");
        stoplist.add("las");
        stoplist.add("de");
        stoplist.add("desde");
        stoplist.add("a");

       return !Collections.disjoint(stoplist, Arrays.asList(compare.split(" ")));
    
    }
    //assertEquals(3, counterMap.get("China").intValue());
    //assertEquals(2, counterMap.get("India").intValue());

    
    public static List<String> topkeyword(List<String> list, int min) {
 
        Map<String, Integer> mp = new HashMap<>();
       // list.stream().forEach((l) -> {
         for  (String l :list) {
       System.out.println (l);
        String lkey = l.trim();
            if (l!= null &&  lkey.length() > 0 && mp.containsKey(lkey)) {

                mp.put(lkey, (mp.get(lkey)) + 1);
            } else if (lkey!= null && lkey.length() > 0) {
                mp.put(lkey, 0);
            }
         }
     //   });
        return orderMap(mp, min);
       // return null;

    }

    private static List<String> orderMap(Map<String, Integer> aux, int min) {

        Comparator<Map.Entry<String, Integer>> valueComparator = new Comparator<Map.Entry<String, Integer>>() {

            @Override
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                int v1 = e1.getValue();
                int v2 = e2.getValue();
                return v2 - v1;
            }
        };

        // Sort method needs a List, so let's first convert Set to List in Java
        List<Map.Entry<String, Integer>> listOfEntries = new ArrayList<Map.Entry<String, Integer>>(aux.entrySet());

        // sorting HashMap by values using comparator
        Collections.sort(listOfEntries, valueComparator);
        List<String> auxlist = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Integer> e : listOfEntries) {
            System.out.println(e.getKey() + "-" + e.getValue());
            /*  if (count == 0 && e.getValue() < 1){
             min = 0;
             }*/

            if (e.getValue() >= min || count < 5) {
                auxlist.add(e.getKey());
            }
            count++;
        }
        return auxlist;
    }

    private static LinkedHashMap<String, String> joinkey(List<String> topkeyword) throws IOException {
        LinkedHashMap<String, String> kjoin = new LinkedHashMap<>();
        Preprocessing p = Preprocessing.getInstance();
        for (int i = 0; i < topkeyword.size(); i++) {
            for (int j = i + 1; j < topkeyword.size(); j++) {
                if (topkeyword.get(i).equals("SCPECG") || topkeyword.get(j).equals("SCPECG")) {
                    continue;
                }
                if (topkeyword.get(i).equals("ECGML") || topkeyword.get(j).equals("ECGML")) {
                    continue;
                }
                if (topkeyword.get(i).equals("SCP-ECG") || topkeyword.get(j).equals("SCP-ECG")) {
                    continue;
                }
                if (topkeyword.get(i).equals("PROMAS") || topkeyword.get(j).equals("PROMAS")) {
                    continue;
                }

                System.out.println("Comparando: " + topkeyword.get(i) + "-" + topkeyword.get(j));
                kjoin.put(topkeyword.get(i), "");
                Object number = p.CompareText(StringUtils.stripAccents(topkeyword.get(i)), StringUtils.stripAccents(topkeyword.get(j).replaceAll("\\p{C}", "")), "weightedScoring");
                Double val;
                if (number instanceof Double) {
                    val = (Double) number;
                } else {
                    val = ((BigDecimal) number).doubleValue();
                }
                System.out.print(val);
                if (val > 60) {
                   // kjoin.get(topkeyword.get(j)).add(topkeyword.get(j));

                    kjoin.put(topkeyword.get(j), topkeyword.get(i));
                    //  kjoin.put(topkeyword.get(i), kjoin.get(topkeyword.get(i)).add(topkeyword.get(j)));
                    topkeyword.remove(j);
                }
            }
            // kjoin.put(topkeyword.get(i), kjoin.get(topkeyword.get(i)));
        }
        return kjoin;
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void subClusterAuthor(List<Author> authors, List<NodoDbpedia> nodo, String cluster, Redi r, Boolean save) {

        for (Author a : authors) {
            int topicCluster = 0;
            int topicsize = a.getTopics() == null ? 0 : a.getTopics().split(",").length;
            int actual = 0;
            String all = "";
            if (a.getTopics() != null) {
                all = a.getTopics() + ", " + a.getKeywords();
            } else {
                all = a.getKeywords();
            }
            // String [] kauthor = a.getKeywords().split(",");
            String[] kauthor = all.split(",");
            System.out.println("Author " + a.getURI() + "---");
            Map<String, NodoDbpedia> finalclusters = new HashMap<>();
            for (String k : kauthor) {
                actual++;
                // System.out.println ("K: "+k);
                k = k.trim();
                for (NodoDbpedia no : nodo) {
                    if (no.getOrigin().equals(k)) {
                        log.info("Clasf" + k);
                        if (actual <= topicsize) {
                            topicCluster++;
                        }
                        for (NodoDbpedia aca : no.getAcademic()) {
                            log.info("Clust" + aca.getUri() + "-" + aca.getNameEn());
                            if (!finalclusters.containsKey(aca.getUri())) {
                                finalclusters.put(aca.getUri(), aca);
                            }

                        }
                    }
                }
            }
            if (topicsize > 0 && topicCluster < 1) {
                finalclusters.putAll(completeTopicCluster(a.getTopics(), finalclusters));
            }
            if (save) {
                r.storeSubcluster(a, finalclusters, cluster);
            }
            //saveSubclusterGraph (a , finalclusters ,cluster);
        }
    }
    
     private static void subClusterAuthorYear(List<Author> authors, List<NodoDbpedia> nodo, String cluster, Redi r, Boolean save , String year) {

        for (Author a : authors) {
            int topicCluster = 0;
            int topicsize = a.getTopics() == null ? 0 : a.getTopics().split(",").length;
            int actual = 0;
            String all = "";
            if (a.getTopics() != null) {
                all = a.getTopics() + ", " + a.getKeywords();
            } else {
                all = a.getKeywords();
            }
            // String [] kauthor = a.getKeywords().split(",");
            String[] kauthor = all.split(",");
            System.out.println("Author " + a.getURI() + "---");
            Map<String, NodoDbpedia> finalclusters = new HashMap<>();
            for (String k : kauthor) {
                actual++;
                // System.out.println ("K: "+k);
                k = k.trim();
                for (NodoDbpedia no : nodo) {
                    if (no.getOrigin().equals(k)) {
                        log.info("Clasf" + k);
                        if (actual <= topicsize) {
                            topicCluster++;
                        }
                        for (NodoDbpedia aca : no.getAcademic()) {
                            log.info("Clust" + aca.getUri() + "-" + aca.getNameEn());
                            if (!finalclusters.containsKey(aca.getUri())) {
                                finalclusters.put(aca.getUri(), aca);
                            }

                        }
                    }
                }
            }
            if (topicsize > 0 && topicCluster < 1) {
                finalclusters.putAll(completeTopicCluster(a.getTopics(), finalclusters));
            }
            if (save) {
                r.storeSubclusterYear(a, finalclusters, cluster , year);
            }
            //saveSubclusterGraph (a , finalclusters ,cluster);
        }
    }

    @Deprecated
    private static void subclusterAuthors(List<Author> authors, List<String> topkey) {

        for (Author a : authors) {
            String[] kauthor = a.getKeywords().split(",");
            System.out.println("Author " + a.getURI() + "---");
            for (String k : kauthor) {
                // System.out.println ("K: "+k);
                k = k.trim();
                if (topkey.contains(k)) {
                    System.out.println(k);
                }
            }
        }
    }

    private static void subclusterAuthor(List<Author> authors, LinkedHashMap<String, String> topkey) {
        System.out.println("Agrupacion de autores");
        // System.out.println (topkey.keySet());
        System.out.print("Value" + topkey.get("COMPUTER SIMULATION"));
        for (Author a : authors) {
            String[] kauthor = a.getKeywords().split(",");
            System.out.println("Author" + a.getURI());
            for (String k : kauthor) {
                System.out.println("K: " + k);
                k = k.trim();

                if (topkey.containsKey(k)) {
                    System.out.print("V");
                    System.out.print(topkey.get(k));
                    if (topkey.get(k) != null && topkey.get(k).length() > 0) {
                        System.out.println(a.getURI() + "- -" + topkey.get(k));
                    } else {
                        System.out.println(a.getURI() + "---" + k);
                    }

                }

            }

        }

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static List<String> getrelatedkey(List<String> subList, String key, Double min) {
        Preprocessing p = Preprocessing.getInstance();
        List<String> mostrelevantk = new ArrayList<>();
        for (String mk : subList) {
            //  if (mk.equals("SCP-ECG") ){continue;}
            System.out.print(mk + "PROCESSING");
            try {
                Object number = p.CompareText(StringUtils.stripAccents(mk), StringUtils.stripAccents(key), "weightedScoring");
                if (number != null) {
                    Double val;
                    if (number instanceof Double) {
                        val = (Double) number;
                    } else {
                        val = ((BigDecimal) number).doubleValue();
                    }
                    System.out.print(val);

                    if (val > min) {
                        mostrelevantk.add(mk);
                    }
                } else {
                    System.out.println("Bad Request");
                }
            } catch (IOException e) {

                Logger.getLogger(SubClassificationCorticalTest.class.getName()).log(Level.SEVERE, null, e);
                // return null;
            }
        }
        return mostrelevantk;
    }

    private static NodoDbpedia testacademic(String ori, String value, Dbpedia db) {
        try {
            NodoDbpedia newnode = new NodoDbpedia(value);
            newnode.setOrigin(ori);
            System.out.println("|||||||" + ori + " " + value + "|||||||");
            newnode.setAcademic(db.isAcademicDbpedia(value));

            if (newnode.getAcademic() != null && !newnode.getAcademic().isEmpty()) {
                System.out.println("FIRST");

            } else {
                System.out.println("SECOND");
                newnode.setBroader(db.getBroaderAca(value));
                newnode.setNarrower(db.getNearDbpediaAca(value));

                if (newnode.getNarrower() != null && !newnode.getNarrower().isEmpty() && newnode.getNarrower().size() < 2) {
                    newnode.setAcademic(newnode.getNarrower());
                }

                if (newnode.getBroader() != null && !newnode.getBroader().isEmpty() && newnode.getBroader().size() < 2) {
                    newnode.setAcademic(newnode.getBroader());
                }

                if (newnode.getAcademic() != null && newnode.getAcademic().isEmpty()) {
                    List<NodoDbpedia> nd = db.DirectCategory(value);
                    if (nd != null && !nd.isEmpty() && nd.size() < 2) {
                        newnode.setAcademic(nd);
                    } else {
                        System.out.println("NO AVAILABLE");
                    }
                }

                /*    System.out.println (" Second Level");
                 newnode.setAcademic(  db.getNearDbpediaAca(value));
                 if( newnode.getAcademic() != null && !newnode.getAcademic().isEmpty() ){
                 for ( NodoDbpedia na : newnode.getAcademic()) {
                 System.out.print (na.getOrigin()+"-"+na.getUri()+"---");
                 System.out.print (na.getNameEn());
                 System.out.println (na.getNameEs());
                 }
                 
             
                 } else {
                 System.out.println (" Direct category");
                 newnode.setAcademic(  db.DirectCategory(value)); 
                 if (newnode.getAcademic() != null && !newnode.getAcademic().isEmpty()){
                 for ( NodoDbpedia na : newnode.getAcademic()) {
                 System.out.print (na.getOrigin()+"-"+na.getUri()+"---");
                 System.out.print (na.getNameEn());
                 System.out.println (na.getNameEs());
                 }
                   
                 } else {
                 System.out.println (" Default");  
                 System.out.println (value + "");  
                 }
                 }*/
            }

            for (NodoDbpedia na : newnode.getAcademic()) {
                System.out.print(na.getOrigin() + "-" + na.getUri() + "---");
                System.out.print(na.getNameEn());
                System.out.println(na.getNameEs());
            }
            return newnode;

            // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (RepositoryException ex) {
            Logger.getLogger(SubClassificationCorticalTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static NodoDbpedia secondchance(NodoDbpedia nd2, List<String> valid, Dbpedia db) {
        System.out.println("SECOND CHANCE");
        //System.out.println (valid);
        System.out.println(nd2.getOrigin());
        List<NodoDbpedia> candidate = new ArrayList<>();
        if (nd2.getBroader() != null && nd2.getBroader().size() < 7) {

            for (NodoDbpedia n : nd2.getBroader()) {
                System.out.println(n.getUri());
                if (valid.contains(n.getUri().trim())) {
                    System.out.print("B" + n.getUri());
                    candidate.add(n);
                }
            }
        }
        if (nd2.getNarrower() != null && nd2.getNarrower().size() < 7) {

            for (NodoDbpedia n : nd2.getNarrower()) {
                System.out.println(n.getUri());
                if (valid.contains(n.getUri().trim())) {
                    System.out.print("N" + n.getUri());
                    candidate.add(n);
                }
            }

        }

        if (candidate.isEmpty()) {
            try {
                nd2.setAcademic(db.getLabels(nd2.getUri()));
            } catch (RepositoryException ex) {
                Logger.getLogger(SubClassificationCorticalTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            nd2.setAcademic(candidate);
        }
        return nd2;

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static Map<String, NodoDbpedia> completeTopicCluster(String topics, Map<String, NodoDbpedia> finalclusters) {
        String[] topicslist = getRelevantTopics(topics.split(","));
        Map<String, NodoDbpedia> newmap = new HashMap<>();
        for (String tp : topicslist) {
            try {
                String uri = "http://ucuenca.edu.ec/resource/subcluster#" + URLEncoder.encode(tp.trim().replace(" ", "_"), "UTF-8");
                NodoDbpedia naca = new NodoDbpedia(uri);
                naca.setNameEn(tp);
                newmap.put(uri, naca);
                System.out.println("Nuevo" + uri);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(SubClassificationCorticalTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return newmap;
    }

    private static String[] getRelevantTopics(String[] split) {
        Map<String, Integer> aux = new HashMap<>();
        for (String topic : split) {
            for (String comparetopic : split) {
                if (comparetopic.contains(topic)) {
                    if (aux.containsKey(topic)) {
                        aux.put(topic, aux.get(topic) + 1);
                    } else {
                        aux.put(topic, 0);
                    }
                }
            }
        }

        List<String> ordertopic = orderMap(aux, 0);

        return ordertopic.size() > 5 ? ordertopic.subList(0, 5).toArray(new String[0]) : ordertopic.toArray(new String[0]);
    }

  private static void asignarCl( HashMap <String,HashMap<String,Integer>> hm) {
    
  }

 
    
    
     public Boolean validateSubcluster (String uri ) {
    
      return false;
      }
}

