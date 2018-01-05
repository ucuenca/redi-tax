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

import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
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
            String query = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                    + "PREFIX dct: <http://purl.org/dc/terms/> "
                    + "CONSTRUCT {?a rdfs:label ?kws} WHERE {"
                    + "SELECT ?a (group_concat(DISTINCT ?kw ; separator=\";\") as ?kws) "
                    + "WHERE { GRAPH ?graph {"
                    + "  ?a a foaf:Person;"
                    + "    foaf:publications ?p."
                    + "  ?p dct:subject [rdfs:label ?kw]"
                    + "}} GROUP BY ?a}";
            GraphQuery q = connection.prepareGraphQuery(QueryLanguage.SPARQL, query);
            q.setBinding("graph", vf.createIRI(RediRepository.DEFAULT_CONTEXT));
            GraphQueryResult result = q.evaluate();
            while (result.hasNext()) {
                Statement stmt = result.next();
                authors.add(new Author((URI) stmt.getSubject(), stmt.getObject().stringValue()));
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
}
