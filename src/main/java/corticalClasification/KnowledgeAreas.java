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
//    private final UnescoNomeclature unesco;
    private final List<UnescoHierarchy> unescoDataset;
    private final Preprocessing cortical = Preprocessing.getInstance();
    private JSONArray comparations = new JSONArray();

    private static final Logger log = LoggerFactory.getLogger(KnowledgeAreas.class);

    public KnowledgeAreas(Redi redi, List<UnescoHierarchy> unescoDataset) {
        this.redi = redi;
        this.unescoDataset = unescoDataset;
    }

    public void extractArea(Author author) throws RepositoryException, Exception {
        if (redi.isAuthorInCluster(author.getURI())) {
            log.info("Author done! {}", author.getURI());
            return;
        }

        log.info("Author processing {}", author.getURI());
        LinkedList<AreaUnesco> areasList = new LinkedList();
        String userKeywords = author.getKeywords();

        for (UnescoHierarchy h : unescoDataset) {
            String arrayLabels4 = h.getLevel4() + ", " + h.getLevels6();
            addComparation(arrayLabels4, StringUtils.stripAccents(userKeywords));
            AreaUnesco area = new AreaUnesco(h.getLevel4(), h.getLevel4Uri());
            areasList.add(area);
            log.info("\n\tAuthor: {}\n\tArea: {} -> {}\n\tKeywords: {}\n\tScore: {}",
                    new String[]{author.getURI().stringValue(), h.getLevel2(), h.getLevel4(), userKeywords, String.valueOf(0.0)});
        }
        double[] scores = cortical.compareTextBulk(getComparationsString(), "weightedScoring");
        if (scores.length != areasList.size()) {
            throw new Exception(String.format("scores lenght (%s) must be equal to areas length (%s)", scores.length, areasList.size()));
        }
        for (int i = 0; i < areasList.size(); i++) {
            areasList.get(i).setScore(scores[i]);
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

    private JSONArray getComparations() {
        return comparations;
    }

    private String getComparationsString() {
        return comparations.toJSONString();
    }
}
