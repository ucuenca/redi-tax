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
import ec.edu.cedia.redi.unesco.model.UnescoHierarchy;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
    private final List<UnescoHierarchy> unescoDataset;
    private final Preprocessing cortical = Preprocessing.getInstance();
    private final JSONArray comparations = new JSONArray();
    private boolean inspect = false;

    private static final Logger log = LoggerFactory.getLogger(KnowledgeAreas.class);

    public KnowledgeAreas(Redi redi, List<UnescoHierarchy> unescoDataset) {
        this.redi = redi;
        this.unescoDataset = unescoDataset;
    }

    public void extractArea(Author author) throws RepositoryException, Exception {
        if (redi.isAuthorInCluster(author.getURI()) && !inspect) {
            log.info("Author done! {}", author.getURI());
            return;
        }

        log.info("Author processing {}", author.getURI());
        LinkedList<AreaUnesco> areasList = new LinkedList<>();
        String authorKeywords = author.getKeywords();

        for (UnescoHierarchy h : unescoDataset) {
            String labels = h.getLevel4() + ", " + h.getLevels6();
            addComparation(labels, StringUtils.stripAccents(authorKeywords));
            AreaUnesco area = new AreaUnesco(h.getLevel4(), h.getLevel4Uri());
            areasList.add(area);
            if (inspect) {
                log.info("\n\tAuthor: {}\n\tArea: {} -> {}\n\tKeywords: {}",
                        new String[]{author.getURI().stringValue(), h.getLevel2(), h.getLevel4(), authorKeywords});
            }
        }
        double[] scores = cortical.compareTextBulk(getComparationsString(), "weightedScoring");
        if (scores.length != areasList.size()) {
            throw new Exception(String.format("scores lenght (%s) must be equal to areas length (%s)", scores.length, areasList.size()));
        }
        for (int i = 0; i < areasList.size(); i++) {
            areasList.get(i).setScore(scores[i]);
        }
        List<AreaUnesco> selected = filterAreas(areasList, 2, 0.1);
        if (inspect) {
            log.info("\n\nResults:");
            for (AreaUnesco area : areasList) {
                log.info("{} ({})", area.getLabel(), area.getScore());
            }
            log.info("\n\nAreas Selected:");
            for (AreaUnesco areaUnesco : selected) {
                log.info("{} ({})", areaUnesco.getLabel(), areaUnesco.getScore());
            }
        } else {
            redi.store(author.getURI(), selected);
        }
    }

    public boolean isInspect() {
        return inspect;
    }

    public void setInspect(boolean inspect) {
        this.inspect = inspect;
    }

    private List<AreaUnesco> filterAreas(List<AreaUnesco> l, int size, double porcentage) {
        Collections.sort(l, new AreaUnesco().reversed());

        if (size >= l.size()) {
            return l;
        }
        double min = 0.0;
        for (int i = 0; i < l.size(); i++) {
            if (i == 0) {
                min = l.get(i).getScore() - l.get(i).getScore() * porcentage;
            } else if (l.get(i).getScore() < min || size < i + 1) {
                return l.subList(0, i);
            }
        }
        return l;
    }

    private void addComparation(String t1, String t2) {
        JSONObject json1 = new JSONObject();
        json1.put("text", t1);
        JSONObject json2 = new JSONObject();
        json2.put("text", t2);

        JSONArray jsonArr = new JSONArray();
        jsonArr.add(json1);
        jsonArr.add(json2);
        comparations.add(jsonArr);
    }

    private String getComparationsString() {
        return comparations.toJSONString();
    }
}
