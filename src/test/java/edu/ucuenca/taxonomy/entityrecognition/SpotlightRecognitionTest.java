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

import ec.edu.cedia.redi.entitymanagement.SpotlightRecognition;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class SpotlightRecognitionTest {

    private static SpotlightRecognition spotlight;

    @BeforeClass
    public static void setUpClass() {
        spotlight = SpotlightRecognition.getInstance();

    }

    /**
     * Test of getEntities method, of class SpotlightRecognition.
     */
    @Test
    @Ignore
    public void testGetEntities() {
        List<URI> result = spotlight.getEntities("Logic, Methodology, Scientific");
        assertEquals(4, result.size());
        result = spotlight.getEntities("Logic, Methodology, Scientific", 0.2);
        assertEquals(3, result.size());
    }

    /**
     * Test of getEntities method, of class SpotlightRecognition.
     */
    @Test
    @Ignore
    public void testGetEntitiesNotResults() {
        List<URI> result = spotlight.getEntities("programming");
        assertEquals(0, result.size());
    }

    /**
     * Test of getEntities method, of class SpotlightRecognition.
     */
    @Test
    public void testGetEntitiesOneResult() {
        List<URI> result = spotlight.getEntities("Linked Data");
        assertEquals(1, result.size());
    }

}
