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
package corticalClasification;

import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.unesco.UnescoNomeclature;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plublication.Preprocessing;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class KnowledgeAreas {

    private final Redi redi;
    private final UnescoNomeclature unesco;
    private final List<URI> twoDigit;
    private final Preprocessing cortical = Preprocessing.getInstance();

    private static final Logger log = LoggerFactory.getLogger(KnowledgeAreas.class);

    public KnowledgeAreas(Redi redi, UnescoNomeclature unesco, List<URI> twoDigit) throws RepositoryException {
        this.redi = redi;
        this.unesco = unesco;
        this.twoDigit = twoDigit;
    }

    public void extractArea(Author author) throws RepositoryException, Exception {
        if (redi.isAuthorInCluster(author.getURI())) {
            log.info("Author done! {}", author.getURI());
            return;
        }

        log.info("Author processing {}", author.getURI());
        List<AreaUnesco> areasList = new ArrayList();
        String userKeywords = author.getKeywords();

        String bestCategory = "";
        URI bestCategoryURI = null;
        double bestScore = 0.0;
        for (URI l : twoDigit) {
            String label2 = unesco.label(l, "en").getLabel();
            List<URI> listN = unesco.narrow(l);
            for (URI nl : listN) {
                String label4 = unesco.label(nl, "en").getLabel();
                String arrayLabels4 = label4;
                if (!label4.contains("Other")) {
                    List<URI> listNl = unesco.narrow(nl);
                    int count = 0;
                    for (URI nnl : listNl) {
                        String label6 = unesco.label(nnl, "en").getLabel();
                        if (!label6.contains("Other")) {
                            arrayLabels4 = arrayLabels4 + ", " + StringUtils.stripAccents(label6);
                            count++;
                        }
                    }

                    double val;
                    Object number = cortical.CompareText(arrayLabels4, StringUtils.stripAccents(userKeywords), "weightedScoring");
                    if (number instanceof Double) {
                        val = (Double) number;
                    } else {
                        val = ((BigDecimal) number).doubleValue();
                    }

                    AreaUnesco area = new AreaUnesco(label4, nl, val);
                    areasList.add(area);

                    if (bestScore < val) {
                        bestScore = val;
                        bestCategory = label4;
                        bestCategoryURI = nl;
                    }
                    log.info("\n\tAuthor: {}\n\tArea: {} -> {}\n\tKeywords: {}\n\tScore: {}",
                            new String[]{author.getURI().stringValue(), label2, label4, userKeywords, String.valueOf(val)});
                }
            }
        }
        redi.store(author.getURI(), filterAreas(areasList, 2, 0.1));
    }

    private List<AreaUnesco> filterAreas(List<AreaUnesco> l, int n, double porcentage) {
        Collections.sort(l, new AreaUnesco().reversed());

        if (n >= l.size()) {
            return l;
        }
        double min = 0.0;
        for (int i = 0; i < l.size(); i++) {
            if (i == 0) {
                min = l.get(i).getScore() - l.get(i).getScore() * porcentage;
            } else if (l.get(i).getScore() < min || n < i + 1) {
                return l.subList(0, i);
            }
        }
        return l;
    }
}
