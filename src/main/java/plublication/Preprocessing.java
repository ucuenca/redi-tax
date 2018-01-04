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
import tinkerpop.GraphOperations;
import ec.edu.cedia.redi.utils.IOGraph;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
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

/**
 *
 * @author joe
 */
public class Preprocessing {

    GraphOperations gp = new GraphOperations();

    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private final static String DEFAULT_CONTEXT = "http://dbpedia.org/sparql";
    // private Logger log = Logger.getLogger(Writer.class.getName());
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

    public Object executeService(HttpUriRequest request, @Nullable String key, @Nullable String Secondkey) throws IOException {
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
                    }
                } else {
                    System.out.print(response);
                }
            } catch (UnknownHostException e) {
                System.out.printf(e + "Can't reach host in service: Detect Language");
                // log.log(Priority.WARN, "Can"Can't reach host in service: Detect Language"reach host in service: Detect Language");
            }
        }
    }

    public Object executeServicePath(HttpUriRequest request, @Nullable String path) throws IOException {
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
                    System.out.print(response);
                }
            } catch (UnknownHostException e) {
                System.out.printf(e + "Can't reach host in service: Detect Language");
                // log.log(Priority.WARN, "Can"Can't reach host in service: Detect Language"reach host in service: Detect Language");
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
