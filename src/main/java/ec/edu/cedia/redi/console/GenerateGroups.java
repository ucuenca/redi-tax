/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package corticalClasification;
import com.google.common.base.Joiner;
import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.RediRepository;
import ec.edu.cedia.redi.unesco.UnescoNomeclature;
import ec.edu.cedia.redi.unesco.UnescoNomeclatureConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.URI;
import plublication.Preprocessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe
 */
public class GenerateGroups {

    /**
     * @param args the command line arguments
     */
    private static final Logger log = LoggerFactory.getLogger(GenerateGroups.class);
    
   /* public static void main(String[] args) {
    
            
            
    }*/
    
    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */




/**
 *
 * @author joe
 */


    private static List<AreaUnesco> filterAreas(List<AreaUnesco> l, int n, double porcentage) {

        Collections.sort(l, new AreaUnesco().reversed());

        if (n >= l.size()) {
            return l;
        }

        Double min = 0.0;
        for (int i = 0; i < l.size(); i++) {

            if (i == 0) {
                min = l.get(i).getScore() - l.get(i).getScore() * porcentage;
            } else if (l.get(i).getScore() < min || n < i + 1) {
                return l.subList(0, i);
            }

        }

        return l;
    }


    /**
     * @param args the command line arguments
     */
    private Preprocessing processing = Preprocessing.getInstance();
    private static Preprocessing p = Preprocessing.getInstance();

    public static void main(String[] args) throws Exception {

        UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();
        UnescoNomeclature unesco = new UnescoNomeclature(conn);
        List<URI> listUris = unesco.twoDigitResources();

        Preprocessing p = Preprocessing.getInstance();
        RediRepository rp = RediRepository.getInstance();
        
        Redi r = new Redi(rp);
        Author actual = null;
        List<Author> authors = r.getAuthors();
        Map<String, String[]> myHashMap = new HashMap<>();
        Map<String, List<AreaUnesco>> authorAreas = new HashMap<>();


        File f = new File();

        for (Author a : authors) {
            actual = a;
            System.out.println("AUTOR: " + actual.getURI());

            if (r.isAuthorInCluster(a.getURI())) {
                System.out.println(actual.getURI() + " Procesado");
                continue;
            }

            List<AreaUnesco> areasList = new ArrayList();
         
            String userKeywords = actual.getKeywords();

            String bestCategory = "";
            URI bestCategoryURI = null;
            Double bestScore = 0.0;
            for (URI l : listUris) {
                String arrayLabels = "";
                System.out.println("*********");
                String label2 = unesco.label(l, "en").getLabel();
                System.out.println(label2);
                List<URI> listN = unesco.narrow(l);
                for (URI nl : listN) {
                    String label4 = unesco.label(nl, "en").getLabel();
                    System.out.println(label4);
                    // arrayLabels = arrayLabels + ", " + label4;
                    String arrayLabels4 = label4;
                    if (!label4.contains("Other")) {
                        //  if (listN.size() < 30) {
                        List<URI> listNl = unesco.narrow(nl);
                        int count = 0;
                        for (URI nnl : listNl) {
                            String label6 = unesco.label(nnl, "en").getLabel();
                            if (!label6.contains("Other")) {
                                //   System.out.println(label6);
                                // arrayLabels = arrayLabels + ", " + label6.replace(".", "");
                                arrayLabels4 = arrayLabels4 + ", " + StringUtils.stripAccents(label6);
                                count++;

                                //     }
                            }
                        }

                        Double val;
                        Object number = p.CompareText(arrayLabels4, StringUtils.stripAccents(userKeywords), "weightedScoring");
                        if (number instanceof Double) {
                            val = (Double) number;
                        } else {
                            val = ((BigDecimal) number).doubleValue();
                        }

                        AreaUnesco area = new AreaUnesco(label4, nl, val);
                        areasList.add(area);

                        if (bestScore.doubleValue() < val.doubleValue()) {
                            bestScore = val;
                            bestCategory = label4;
                            bestCategoryURI = nl;
                        }
                        System.out.println(actual.getURI() + " Score : " + label4 + "-" + val);

                    }
                }

            }

           
            r.store(actual.getURI(), filterAreas(areasList, 2, 0.1));
     

        }
 

        conn.close();
    }


    private static final HttpClient httpclient = HttpClients.createDefault();

    public static void getEntities(String json) {
        try {
            URIBuilder builder = new URIBuilder("http://api.cortical.io/rest/compare");
            builder.setParameter("retina_name", "en_associative");
            builder.setParameter("api-key", "1c556a80-8595-11e6-a057-97f4c970893c");
            builder.setParameter("Content-Type", "application/json");

            java.net.URI uri = builder.build();

            HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("Accept", "application/json");
            StringEntity textEntity = new StringEntity(json);
            httpPost.setEntity(textEntity);
            // Request response
            HttpResponse response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                System.out.println(result);
//                ObjectNode root = (ObjectNode) mapper.readTree(result);
            }
        } catch (UnsupportedEncodingException ex) {
        } catch (IOException | ParseException | URISyntaxException ex) {
        }
    }

    public Map<URI, Map<URI, Double>> getWeights(Map<URI, String> taxonomy, List<Author> authors) throws IOException, InterruptedException {
        Map<URI, Map<URI, Double>> authorScores = new HashMap<>();
        for (Author author : authors) {
            Map<URI, Double> scores = new HashMap<>();
            for (Map.Entry<URI, String> tax : taxonomy.entrySet()) {
                String keywords = StringUtils.stripAccents(author.getKeywords());
                String unescoWords = StringUtils.stripAccents(tax.getValue());
                int wordsAuthor = keywords.split(";").length;
                int wordsUnesco = unescoWords.split(";").length;
                System.out.println("[Unesco:" + wordsUnesco + ", Author:" + wordsAuthor + "]");
                double score = (double) processing.CompareText(keywords, unescoWords, "");
                scores.put(tax.getKey(), score);
            }
            authorScores.put(author.getURI(), scores);
        }
        return authorScores;
    }

    public Map<URI, String> getWords(int limit) {
        Map<URI, String> unescoTax = new HashMap<>();
        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance()) {
            UnescoNomeclature unesco = new UnescoNomeclature(conn);
            for (URI twoDigitResource : unesco.twoDigitResources()) {
                List<URI> fourDigitResources = unesco.narrow(twoDigitResource).stream()
                        .filter(uri -> !uri.stringValue().contains("99"))
                        .collect(Collectors.toList());
                List<URI> sixDigitResources = fourDigitResources.stream()
                        .map(fourDigit -> unesco.narrow(fourDigit))
                        .flatMap(List::stream)
                        .filter(uri -> !uri.stringValue().contains("99"))
                        .collect(Collectors.toList());

                List<URI> all = new ArrayList<>();
                all.addAll(fourDigitResources);
                all.addAll(sixDigitResources);
                List<String> words;

                if (limit == -1) {
                    words = all.stream().map(uri -> unesco.label(uri, "en").getLabel())
                            .collect(Collectors.toList());
                } else {
                    words = all.stream().map(uri -> unesco.label(uri, "en").getLabel())
                            .limit(limit).collect(Collectors.toList());
                }
                unescoTax.put(twoDigitResource, Joiner.on(";").join(words));
            }
        } catch (Exception ex) {
        }
        return unescoTax;
    }

    public List<Author> getAuthors() {
        try (RediRepository r = RediRepository.getInstance()) {
            Redi redi = new Redi(r);
            return redi.getAuthors();
        } catch (Exception ex) {
        }
        return Collections.emptyList();
    }


}

    

