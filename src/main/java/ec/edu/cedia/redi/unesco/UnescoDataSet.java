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
package ec.edu.cedia.redi.unesco;

import ec.edu.cedia.redi.unesco.model.UnescoHierarchy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class UnescoDataSet {

    private static final Logger log = LoggerFactory.getLogger(UnescoDataSet.class);
    private static final String PATH = "unesco.bin";

    private List<UnescoHierarchy> dataset = new ArrayList<>();
    private static UnescoDataSet instance;

    public static synchronized UnescoDataSet getInstance() {
        if (instance == null) {
            instance = new UnescoDataSet();
            try {
                instance.data();
            } catch (Exception ex) {
                log.error("Cannot get Unesco dataset", ex);
            }
        }

        return instance;
    }

    public List<UnescoHierarchy> getDataset() {
        return dataset;
    }

    private void data() throws Exception {
        log.info("Getting Unesco Dataset...");
        File f = new File(PATH);
        if (f.exists()) {
            dataset = readList();
            if (dataset != null) {
                return;
            }
        }

        try (UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();) {
            final UnescoNomeclature unesco = new UnescoNomeclature(conn);
            final List<URI> twoDigit = unesco.twoDigitResources();
            for (URI lvl2 : twoDigit) {
                String lvl2Str = unesco.label(lvl2, "en").getLabel();
                List<URI> fourDigit = unesco.narrow(lvl2);
                for (URI lvl4 : fourDigit) {
                    String lvl4Str = unesco.label(lvl4, "en").getLabel();
                    String lvls6 = "";
                    if (!lvl4Str.contains("Other")) {
                        List<URI> sixDigit = unesco.narrow(lvl4);
                        for (URI lvl6 : sixDigit) {
                            String lvl6Str = unesco.label(lvl6, "en").getLabel();
                            if (!lvl6Str.contains("Other")) {
                                lvls6 += StringUtils.stripAccents(lvl6Str) + ", ";
                            }
                        }
                        UnescoHierarchy model = new UnescoHierarchy(lvl2Str, lvl4Str, lvls6, lvl2, lvl4);
                        dataset.add(model);
                        log.info(model.toString());
                    }
                }
            }
            persistList(dataset);
        }
    }

    private static void persistList(List<UnescoHierarchy> objectList) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(PATH);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(objectList);
            oos.flush();
        } catch (IOException ex) {
            log.error("Cannot store list.", ex);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                log.error("Cannot close streams.", ex);
            }
        }
    }

    private static List<UnescoHierarchy> readList() {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(PATH);
            ObjectInputStream ois = new ObjectInputStream(fin);
            return (List<UnescoHierarchy>) ois.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            log.error("Cannot read list.", ex);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException ex) {
                log.error("Cannot close stream.", ex);
            }
        }
        return null;
    }
}
