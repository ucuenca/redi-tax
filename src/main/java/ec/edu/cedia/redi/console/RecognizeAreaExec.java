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
package ec.edu.cedia.redi.console;

import ec.edu.cedia.redi.entitymanagement.RecognizeArea;
import ec.edu.cedia.redi.repository.StardogConnection;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class RecognizeAreaExec {

    private static final Logger log = LoggerFactory.getLogger(RecognizeAreaExec.class);

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        final CommandLineParser parser = new DefaultParser();

        final Options options = recognizeOptions();
        CommandLine cmd;
        try {
//        args = new String[]{"-k linked data, semantic web"};
            cmd = parser.parse(options, args);
            if (cmd.hasOption("keywords")) {
                List<String> keywords = Arrays.asList(cmd.getOptionValues("keywords"));
                for (int i = 0; i < keywords.size(); i++) {
                    keywords.set(i, keywords.get(i).trim());
                    if ("".equals(keywords.get(i))) {
                        showHelp("recognize", options);
                        System.exit(0);
                    }
                }

                if (cmd.hasOption("db")) {
                    try (Graph graph = StardogConnection.instance(cmd.getOptionValue("db")).graph()) {
                        RecognizeArea recognize = new RecognizeArea(graph);
                        URI area = recognize.recognize(keywords);
                        log.info("The keywords belong to the area {}", area);
                    }
                } else {
                    try (Graph graph = StardogConnection.instance().graph()) {
                        RecognizeArea recognize = new RecognizeArea(graph);
                        URI area = recognize.recognize(keywords);
                        log.info("The keywords belong to the area {}", area);
                    }
                }
            } else {
                showHelp("recognize", options);
            }
        } catch (MissingArgumentException ex) {
            showHelp("recognize", options);
        } catch (ParseException ex) {
            log.error("Cannot parse commandline options", ex);
        }
    }

    private static void showHelp(String program, Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(program, opts);
    }

    private static Options recognizeOptions() {
        final Option kwsOption = Option.builder("k")
                .longOpt("keywords")
                .desc("Given a list of keywords classify them under the UNESCO nomenclature.")
                .hasArgs()
                .valueSeparator(',')
                .argName("keywords")
                .required(false)
                .build();
        final Option databaseOption = Option.builder("db")
                .required(false)
                .longOpt("database")
                .hasArg()
                .argName("database")
                .desc("Specify the connection to the Stardog repository (e.g http://localhost:5820/myDB).")
                .build();
        final Options options = new Options();
        options.addOption(databaseOption);
        options.addOption(kwsOption);
        return options;
    }

}
