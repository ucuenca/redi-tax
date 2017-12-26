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
package edu.ucuenca.taxonomy.unesco;

import edu.ucuenca.taxonomy.entitymanagement.SpotlightRecognition;
import edu.ucuenca.taxonomy.entitymanagement.api.EntityRecognition;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class UnescoPopulation {

    private GraphTraversalSource g;
    private ValueFactory vf = ValueFactoryImpl.getInstance();
    private EntityRecognition recognition = SpotlightRecognition.getInstance();

    public UnescoPopulation(GraphTraversalSource g) {
        this.g = g;
    }

    public void populate() {
        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance()) {
            UnescoNomeclature unesco = new UnescoNomeclature(conn);
            unesco.sixDigitResources().stream()
                    .filter(uri -> !unesco.code(uri).contains("99"))
                    .map(uri -> findEntities(uri, unesco))
                    //                    .map(s -> unesco.broad(field))
                    .forEach(System.out::println);
        } catch (Exception ex) {
            Logger.getLogger(UnescoPopulation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private List<URI> findEntities(URI uri, UnescoNomeclature unesco) {
        String label = unesco.label(uri, "en").getLabel();
        List<URI> entities = recognition.getEntities(label);
        if (!g.V(uri.stringValue()).hasNext()) {
//            g.addV("unesco").property(T.id, uri.stringValue(), T.label, label);
            
        }
        entities.stream().map((entity) -> {
            if (!g.V(entity.stringValue()).hasNext()) {
//                g.addV("unesco").property(T.id, uri.stringValue()).property(T.label, label);
            }
            return entity;
        }).forEach((entity) -> {
//            g.addE("same as")
//                    .from(g.V(uri.stringValue()).next())
//                    .to(entity.stringValue());
        });
        return entities;
    }

    private void a(URI unesco, String label) {
        List<URI> result = recognition.getEntities(label);
        if (result.isEmpty()) {

        }
    }

    public static void main(String[] args) {
        UnescoPopulation up = new UnescoPopulation(null);
        up.populate();
//        ValueFactory vf = ValueFactoryImpl.getInstance();
//        URI two_digit = vf.createURI("http://skos.um.es/unesco6/12");
//        URI four_digit = vf.createURI("http://skos.um.es/unesco6/1203");
//        URI six_digit = vf.createURI("http://skos.um.es/unesco6/120304");
//        EntityRecognition sp = SpotlightRecognition.getInstance();
//
//        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance()) {
//            UnescoNomeclature un = new UnescoNomeclature(conn);
//            System.out.println();
//            for (URI field : Arrays.asList(two_digit)) {//un.twoDigitResources()) {
//                String fieldStr = un.label(field, "en").getLabel();
//                System.out.print(field);
//                System.out.println("->" + fieldStr);
//
//                for (URI discipline : un.narrow(field)) {
//                    String disciplineStr = un.label(discipline, "en").getLabel();
//                    System.out.print("\t" + discipline);
//                    System.out.println("->" + disciplineStr);
//
//                    if (String.valueOf(un.code(discipline)).contains("99")) {
//                        continue;
//                    }
//                    String context = disciplineStr;
//                    for (URI entity : sp.getEntities(context, 0.15)) {
//                        System.out.println("\t\t\t" + context + "::" + entity);
//                    }
////                    for (URI subdiscipline : un.narrow(discipline)) {
////                        String subdisciplineStr = un.label(subdiscipline, "en").label();
////                        System.out.print("\t\t" + subdiscipline);
////                        System.out.println("->" + subdisciplineStr);
////                        String context = fieldStr + ", " + disciplineStr + ", " + subdisciplineStr;
////                        for (URI entity : sp.getEntities(context)) {
////                            System.out.println("\t\t\t" + entity);
////                        }
////                    }
//                }
//            }
//        } catch (Exception ex) {
//        }

    }

}
