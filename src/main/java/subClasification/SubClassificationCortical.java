/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package subClasification;

import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.Dbpedia;
import ec.edu.cedia.redi.DbpediaRepository;
import ec.edu.cedia.redi.NodoDbpedia;
import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.RediRepository;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.repository.RepositoryException;
import org.slf4j.LoggerFactory;
import plublication.Preprocessing;

/**
 *
 * @author joe
 */
public class SubClassificationCortical {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SubClassificationCortical.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        executeSubGroup(true, true , 1);
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

    public static void executeSubGroup(Boolean filter, Boolean Save , int key) {
        try {
            RediRepository rp = RediRepository.getInstance();
            Redi r = new Redi(rp);

            DbpediaRepository drep = DbpediaRepository.getInstance();
            Dbpedia db = new Dbpedia(drep);
            Preprocessing p = Preprocessing.getInstance();

            List<String> clusters = r.getclustersAvailable();
            for (String cl : clusters) {
                log.info("Cluster: " + cl);
                // if (true){
                if (!filter || !r.askSubCluster(cl)) {
                    List<String> total = new ArrayList();
                    // List<Author> authors = r.getAuthorsbyCluster("http://skos.um.es/unesco6/1203");
                    List<Author> authors = r.getAuthorsbyCluster(cl);
                    System.out.print(authors);
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

                    System.out.println("FINISH PREPRO");

                    /*    for (String k  :topkeyword (total)) {
                     System.out.println (k);
            
                     }*/
                    List<String> topk = topkeyword(total, 1);

                    int num = 0;
                    if (topk.size() > 50) {
                        num = 50;
                    } else {
                        num = topk.size();
                    }
                    System.out.print("Dbpedia Entities");
                    Object objdbpedia = p.detectDbpediaEntitiestoArray(String.join(", ", topk.subList(0, num)));
                    if (objdbpedia != null) {
                        Map<String, String> entities = (Map<String, String>) objdbpedia;
                        System.out.print(entities);
                        // Map <String,String> valid = new HashMap ();
                        if (!entities.isEmpty()) {
                            List<String> valid = new ArrayList();
                            List<NodoDbpedia> nd = new ArrayList();
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

                            subClusterAuthor(authors, nd, cl, r, Save);
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

            Logger.getLogger(SubClassificationCortical.class.getName()).log(Level.SEVERE, null, ex);

        }
    }

    public static List<String> preProccesingKeywords(String key , int k) {
        try {
            Preprocessing p = Preprocessing.getInstance();
           // System.out.println ( a.getURI());
            //  Object r = p.detectLanguage(a.getKeywords());
            //  System.out.println (r.toString());
            //System.out.println (a.getKeywords());
            String all = key;

            String prek = all.replace("null", "").replaceAll("[\"-\']", "").trim();
            if (prek.length() > 3000) {
                prek = prek.substring(0, 3000);
            }
            List<String> kt = Arrays.asList(p.traductor(prek , k).toString().replace("[\"", "").replace("\"]", "").replace("context,", "").toLowerCase().split(";"));
            kt = kt.stream().map(s -> s.trim()).filter(s -> s.length() > 0).collect(Collectors.toList());
            List<String> ktnd = deleteDuplicate(kt);
            System.out.println(ktnd);

          //  System.out.println (s);
            //List<String> deleteDuplicate = deleteDuplicate (trad);
            // topkeyword (ktnd)
            System.out.println("**********************");

            return ktnd;

        } catch (IOException ex) {
            Logger.getLogger(SubClassificationCortical.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static List<String> deleteDuplicate(List<String> list) {

        return new ArrayList<>(new HashSet<>(list));
    }

    public static List<String> topkeyword(List<String> list, int min) {
        Map<String, Integer> mp = new HashMap<>();
        list.stream().forEach((l) -> {
            if (mp.containsKey(l.trim())) {

                mp.put(l.trim(), ((Integer) mp.get(l)) + 1);
            } else {
                mp.put(l.trim(), 0);
            }
        });
        return orderMap(mp, min);

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
        List<String> auxlist = new ArrayList();
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
            Map<String, NodoDbpedia> finalclusters = new HashMap();
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
        List<String> mostrelevantk = new ArrayList();
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

                Logger.getLogger(SubClassificationCortical.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(SubClassificationCortical.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static NodoDbpedia secondchance(NodoDbpedia nd2, List<String> valid, Dbpedia db) {
        System.out.println("SECOND CHANCE");
        //System.out.println (valid);
        System.out.println(nd2.getOrigin());
        List<NodoDbpedia> candidate = new ArrayList();
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
                Logger.getLogger(SubClassificationCortical.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            nd2.setAcademic(candidate);
        }
        return nd2;

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static Map<String, NodoDbpedia> completeTopicCluster(String topics, Map<String, NodoDbpedia> finalclusters) {
        String[] topicslist = getRelevantTopics(topics.split(","));
        Map<String, NodoDbpedia> newmap = new HashMap();
        for (String tp : topicslist) {
            try {
                String uri = "http://ucuenca.edu.ec/resource/subcluster#" + URLEncoder.encode(tp.trim().replace(" ", "_"), "UTF-8");
                NodoDbpedia naca = new NodoDbpedia(uri);
                naca.setNameEn(tp);
                newmap.put(uri, naca);
                System.out.println("Nuevo" + uri);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(SubClassificationCortical.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return newmap;
    }

    private static String[] getRelevantTopics(String[] split) {
        Map<String, Integer> aux = new HashMap();
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
    
    
     public Boolean validateSubcluster (String uri ) {
    
      return false;
      }
}

