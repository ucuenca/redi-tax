/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plublication;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import ec.edu.cedia.redi.utils.IOGraph;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.openrdf.rio.ParserConfig;
import org.slf4j.LoggerFactory;
import tinkerpop.GraphOperations;

/**
 *
 * @author joe
 */
public class Preprocessing {

    GraphOperations gp = new GraphOperations();

    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private final static String DEFAULT_CONTEXT = "http://dbpedia.org/sparql";
    private org.slf4j.Logger log = LoggerFactory.getLogger(Preprocessing.class);
    private static Preprocessing instanceService = new Preprocessing();
    private HttpClient httpClient = HttpClients.createDefault();

    private Preprocessing() {
    }

    public static Preprocessing getInstance() {
        return instanceService;
    }

    public Object detectLanguage(String text) throws UnsupportedEncodingException, IOException {
        //  Preconditions.checkNotNull(text, "It is necessary some text to detect language");
        //  Preconditions.checkArgument(!text.equals(""));

        HttpPost post = new HttpPost("http://api.cortical.io/rest/text/detect_language");

        StringEntity textEntity = new StringEntity(text);

        post.setEntity(textEntity);
        post.addHeader("api-key", "1c556a80-8595-11e6-a057-97f4c970893c");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("Cache-Control", "no-cache");
        post.addHeader("Accept", "application/json");

        // return executeService(post, "iso_tag", null);
        Object response = executeServicePath(post, "$.iso_tag");
        return response.toString();

    }

    public Object findKeywords(String text) throws UnsupportedEncodingException, IOException {

        HttpPost post = new HttpPost("http://api.cortical.io/rest/text/keywords?retina_name=en_associative");

        StringEntity textEntity = new StringEntity(text);

        post.setEntity(textEntity);
        post.addHeader("api-key", "1c556a80-8595-11e6-a057-97f4c970893c");
        post.addHeader("Content-Type", "application/x-www-form-urlencoded");
        post.addHeader("Cache-Control", "no-cache");
        post.addHeader("Accept", "application/json");

        return executeService(post, null, null);
    }

    public Object CompareText(String text1, String text2, String metric) {

        HttpPost post = new HttpPost("http://api.cortical.io/rest/compare?retina_name=en_associative");

        JSONObject json1 = new JSONObject();
        json1.put("text", text1);

        JSONObject json2 = new JSONObject();
        json2.put("text", text2);

        JSONArray jsonArr = new JSONArray();
        jsonArr.add(json1);
        jsonArr.add(json2);

        // StringEntity textEntity = new StringEntity(" { \"elements\" : [{ \"term\": \"Pablo Picasso\"  }, " +
        // "{ \"text\": \"Gustav Klimt was born in Baumgarten, near Vienna in Austria-Hungary, the second of seven children\"}]}");
//        System.out.println(jsonArr.toJSONString());
        StringEntity textEntity = new StringEntity(jsonArr.toJSONString(), Charset.defaultCharset());
        post.setEntity(textEntity);
        //  post.addHeader("api-key", "1c556a80-8595-11e6-a057-97f4c970893c");
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Cache-Control", "no-cache");
        post.addHeader("Accept", "application/json");
        post.addHeader("Accept-Encoding", "gzip, deflate");

        return executeServicePath(post, "$." + metric);
    }

    public double[] compareTextBulk(String bulk, String metric) {

        HttpPost post = new HttpPost("http://api.cortical.io/rest/compare/bulk?retina_name=en_associative");

        StringEntity textEntity = new StringEntity(bulk, Charset.defaultCharset());
        post.setEntity(textEntity);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Cache-Control", "no-cache");
        post.addHeader("Accept", "application/json");
        post.addHeader("Accept-Encoding", "gzip, deflate");

        JSONArray result = (JSONArray) executeServicePath(post, "$.[*]." + metric);
        double[] scores = new double[result.size()];
        for (int i = 0; i < result.size(); i++) {
            double score = Double.parseDouble(String.valueOf(result.get(i)));
            scores[i] = score;
        }
        return scores;
    }

    public Object executeService(HttpUriRequest request, @Nullable String key, @Nullable String Secondkey) {
        while (true) {
            try {
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity != null && response.getStatusLine().getStatusCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"))) {
                        String jsonResult = reader.readLine();
                        Object parser = new JsonParser().parse(jsonResult);
                        //   System.out.print (parser);
                        if (parser instanceof JsonObject) {
                            System.out.print(parser);
                            if (((JsonObject) parser).get(key) instanceof JsonArray) {
                                JsonArray ja = (JsonArray) ((JsonObject) parser).get(key);
                                List auxl = new ArrayList();

                                for (JsonElement a : ja) {
                                    if (a instanceof JsonObject) {
                                        JsonObject jo = a.getAsJsonObject();
                                        auxl.add(jo.get(Secondkey).toString());
                                    } else {
                                        auxl.add(a.getAsString());
                                    }
                                }

                                return auxl.toString();
                            } else {
                                return ((JsonObject) parser).get(key).getAsString();
                            }
                        } else if (parser instanceof JsonArray) {
                            return ((JsonArray) parser).toString();
                        } else {
                            return "";
                        }
                    } catch (IOException ex) {
                        log.error("Problems reading response", ex);
                    }
                } else {
                    System.out.print(response);
                }
            } catch (UnknownHostException e) {
                log.error("Can't reach host in service: Detect Language", e);
            } catch (IOException ex) {
                log.error("Cannot make request", ex);
            }
        }
    }

    public Object executeServicePath(HttpUriRequest request, @Nullable String path) {
        while (true) {
            try {
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity != null && response.getStatusLine().getStatusCode() == 200) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"))) {
                        String jsonResult = reader.readLine();
                        // Object parserresponse = new JsonParser().parse(jsonResult);

                        return JsonPath.read(jsonResult, path);
                    }
                } else {
                    log.error(response.toString());
                }
            } catch (UnknownHostException e) {
                log.error("Can't reach host in service: Detect Language", e);
            } catch (IOException ex) {
                log.error("Cannot make request", ex);
            }
        }
    }

    public Object traductor(String palabras) throws IOException {
        String contextEs = "contexto, ";
        String contextEn = "context, ";

        String urlbase = "https://translate.yandex.net/api/v1.5/tr.json/translate";
        /* HashMap <String, String> param = new HashMap<>();
         param.put("key", "trnsl.1.1.20160321T160516Z.43cfb95e23a69315.6c0a2ae19f56388c134615f4740fbb1d400f15d3");
         param.put("lang", "es-en");
         param.put("text", contextEs + palabras);
         param.put("options", "1");*/

        List<NameValuePair> list = new ArrayList();

//        NameValuePair nv1 = new BasicNameValuePair("key", "trnsl.1.1.20180515T220323Z.a01167a60fd15c32.e1f33475375f91802f0e1da270a94bd99b412521");
        NameValuePair nv1 = new BasicNameValuePair("key", "trnsl.1.1.20160321T160516Z.43cfb95e23a69315.6c0a2ae19f56388c134615f4740fbb1d400f15d3");
        list.add(nv1);
        NameValuePair nv2 = new BasicNameValuePair("lang", "es-en");
        list.add(nv2);
        NameValuePair nv3 = new BasicNameValuePair("text", contextEs + palabras);
        list.add(nv3);
        NameValuePair nv4 = new BasicNameValuePair("options", "1");
        list.add(nv4);

        try {

            URIBuilder builder = new URIBuilder("https://translate.yandex.net/api/v1.5/tr.json/translate").addParameters(list);
            //  System.out.print("URI"+builder.build());
            HttpGet request = new HttpGet(builder.build());
            // return executeService (request , "text" , null);
            return executeServicePath(request, "$.text");
        } catch (URISyntaxException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "FAIL";
    }

    public Object detectDbpediaEntities(String text) {

        List<NameValuePair> list = new ArrayList();
        NameValuePair nv1 = new BasicNameValuePair("confidence", "0.5");
        list.add(nv1);
        NameValuePair nv2 = new BasicNameValuePair("support", "0");
        list.add(nv2);
        NameValuePair nv3 = new BasicNameValuePair("text", text);
        list.add(nv3);

        try {
            URIBuilder builder;
            builder = new URIBuilder("http://model.dbpedia-spotlight.org/en/annotate").addParameters(list);
            HttpPost post = new HttpPost(builder.build());
            System.out.println(builder.build());
            post.addHeader("Accept", "application/json");
            post.setEntity(new UrlEncodedFormEntity(list, HTTP.UTF_8));

            //return executeService(post, "Resources", "@URI");
            return executeServicePath(post, " $.Resources[*].@URI");
        } catch (URISyntaxException | IOException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    public void queryDbpedia(String URI, int level) throws RepositoryException {

        if (level < 1) {
            return;
        }

        String query2 = "Describe    <" + URI + "> ";

        SPARQLRepository repository = new SPARQLRepository(DEFAULT_CONTEXT);
        Map<String, String> additionalHttpHeaders = new HashMap<>();
//        additionalHttpHeaders.put("Accept", "application/rdf+xml");
//        additionalHttpHeaders.put("Accept", "application/n-triples");
        additionalHttpHeaders.put("Accept", "application/ld+json");
        repository.setAdditionalHttpHeaders(additionalHttpHeaders);

        try {
            //  repository = new SPARQLRepository(DEFAULT_CONTEXT);

            repository.initialize();

            RepositoryConnection connection = repository.getConnection();
            ParserConfig config = new ParserConfig();
//            config = config.useDefaults();
//            config.addNonFatalError(BasicParserSettings.NORMALIZE_LANGUAGE_TAGS);
//            config.addNonFatalError(BasicParserSettings.FAIL_ON_UNKNOWN_LANGUAGES);
//
//            connection.setParserConfig(config);
            //  String query = "select distinct ?Concept where {[] a ?Concept} LIMIT 100";
            GraphQuery q = connection.prepareGraphQuery(QueryLanguage.SPARQL, query2, DEFAULT_CONTEXT);
            GraphQueryResult result = q.evaluate();
//            q.evaluate(rdhl);
//            Iterations.asList(result);

            while (result.hasNext()) {
                System.out.println("Clase:" + result.getClass());
                Statement val = result.next();
                System.out.println(val);
                System.out.println("Actual" + URI);
                System.out.println(val.getSubject() + " - " + val.getPredicate() + " - " + val.getObject());
                gp.RDF2Graph(val.getSubject().stringValue(), val.getPredicate().stringValue(), val.getObject().stringValue());
                if (URI.equals(val.getObject().stringValue())) {
                    continue;
                }
                if (val.getPredicate().toString().equals("http://purl.org/dc/terms/subject") || val.getPredicate().toString().equals("http://www.w3.org/2004/02/skos/core#broader")) {
                    System.out.println("Cambio" + val.getObject());
                    queryDbpedia(val.getObject().stringValue(), level - 1);
                }

            }
        } catch (RepositoryException | MalformedQueryException | QueryEvaluationException ex) {
            Logger.getLogger(Preprocessing.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            repository.shutDown();

        }

    }

    public void entitiesEnrichment(String uri) throws RepositoryException {

//        queryDbpedia("http://dbpedia.org/resource/Programming_language", 3);
//        queryDbpedia("http://dbpedia.org/resource/Computer_science", 4);
        queryDbpedia(uri, 3);
        IOGraph.write(gp.getGraph(), "coco2.graphml");
    }

    public void entitiesEnrichment(String uri, Graph graph) throws RepositoryException {

//        queryDbpedia("http://dbpedia.org/resource/Programming_language", 3);
//        queryDbpedia("http://dbpedia.org/resource/Computer_science", 4);
        gp.setGraph(graph);
        gp.setG(graph.traversal());
        queryDbpedia(uri, 3);
        // IOGraph.write(gp.getGraph(), "coco2.graphml");
    }

}
