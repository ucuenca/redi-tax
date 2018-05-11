/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plublication.Preprocessing;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public final class StringUtils {

    private static final Preprocessing cortical = Preprocessing.getInstance();
    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);

    public static String keepUniqueTopics(String str) {
        LinkedList<String> keywords = new LinkedList<>(Arrays.asList(str.split(";")));

        str = keywords.stream()
                .map(StringUtils::cleanString)
                .distinct()
                .collect(Collectors.joining(";"));

        return str;
    }

    public static String cleanString(String s) {
        s = s.toLowerCase();
        // clean everything in parenthesis
        s = s.replaceAll("\\(.*\\)", "");
        s = s.trim();

        return s;
    }

    public static String processTopics(String topics) {
        String result = topics;
        try {
            String lang = String.valueOf(cortical.detectLanguage(topics));
            if ("es".equals(lang)) {
                result = String.valueOf(cortical.traductor(topics));
                result = JSONAraytoString(result);
            }
        } catch (IOException ex) {
            log.error("Cannot translate/detect language for '" + topics + "'", ex);
        }
        return keepUniqueTopics(result);
    }

    private static String JSONAraytoString(String json) {
//        JSONArray array;
        String result = "";
//        try {
//            array = new JSONArray(json);
//            for (int i = 1; i < array.length(); i++) {
//                result += array.getString(i) + ";";
//            }
//        } catch (JSONException ex) {
//            log.error("I don't understand the json document: " + json, ex);
//        }
        // TODO: parse json correctly.
        result = json.replaceFirst("\\[\"context, ", "").replace("\"]", "");
        return result;
    }
}
