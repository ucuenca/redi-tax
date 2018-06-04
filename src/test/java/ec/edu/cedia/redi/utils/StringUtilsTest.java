/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.utils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class StringUtilsTest {

    /**
     * Test of keepUniqueTopics method, of class StringUtils.
     */
    @Test
    public void testKeepUniqueTopics() {
        String str = "Linked Data;Linked data;semantic web;linked data";
        String expResult = "linked data;semantic web";
        String result = StringUtils.keepUniqueTopics(str);
        assertEquals(expResult, result);
    }

    /**
     * Test of cleanString method, of class StringUtils.
     */
    @Test
    public void testCleanString() {
        String str = "Linked Data;Linked data (All);semantic web;linked data";
        String expResult = "linked data;linked data ;semantic web;linked data";
        String result = StringUtils.cleanString(str);
        assertEquals(expResult, result);
    }

    /**
     * Test of processTopics method, of class StringUtils.
     */
    @Test
    public void testProcessTopics() {
        String str = "lenguajes de programacion;datos enlazados;ecuador;ciencias de la computacion;"
                + "medicina;principios de hardware;inteligencia artificial;ecuador";
        String expResult = "programming languages;linked data;ecuador;computer science;medicine;"
                + "principles of hardware;artificial intelligence";
        String result = StringUtils.processTopics(str);
        assertEquals(expResult, result);
    }
}
