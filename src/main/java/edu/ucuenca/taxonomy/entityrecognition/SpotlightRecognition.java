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
package edu.ucuenca.taxonomy.entityrecognition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns resources based in some string using the
 * <a href="https://github.com/dbpedia-spotlight/dbpedia-spotlight/wiki/Web-service">Spotlight
 * web service</a>.
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class SpotlightRecognition implements EntityRecognition {

    private final Logger log = LoggerFactory.getLogger(SpotlightRecognition.class);
    private final HttpClient httpclient = HttpClients.createDefault();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ValueFactory vf = ValueFactoryImpl.getInstance();
    private final static String DBPEDIA_RESOURCE = "http://dbpedia.org/resource/";
    private final static String SPOTLIGHT_SERVICE = "http://model.dbpedia-spotlight.org/en/candidates";
    private static SpotlightRecognition instance;

    private SpotlightRecognition() {
    }

    public static SpotlightRecognition getInstance() {
        if (instance == null) {
            instance = new SpotlightRecognition();
        }
        return instance;
    }

    @Override
    public List<URI> getEntities(String context) {
        return getEntities(context, 0.1);
    }

    @Override
    public List<URI> getEntities(String context, double confidence) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkArgument(!"".equals(context));
        Preconditions.checkState(confidence >= 0 && confidence <= 1);

        try {
            URIBuilder builder = new URIBuilder(SPOTLIGHT_SERVICE);
            builder.setParameter("text", context);
            builder.setParameter("confidence", String.valueOf(confidence));
            builder.setParameter("support", "0");
            builder.setParameter("spotter", "Default");
            builder.setParameter("disambiguator", "Default");
            builder.setParameter("policy", "whitelist");
            builder.setParameter("types", "");
            builder.setParameter("sparql", "");

            java.net.URI uri = builder.build();

            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader("Accept", "application/json");

            // Request response
            HttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                ObjectNode root = (ObjectNode) mapper.readTree(result);
                return root.findValues("@uri").stream()
                        .map(element -> vf.createURI(DBPEDIA_RESOURCE, element.asText()))
                        .collect(Collectors.toList());
            }
        } catch (UnsupportedEncodingException ex) {
            log.error("Cannot build request URL for spotlight service", ex);
        } catch (IOException | ParseException | URISyntaxException ex) {
            log.error("Cannot build request URL for spotlight service", ex);
        }
        return Collections.emptyList();
    }

}
