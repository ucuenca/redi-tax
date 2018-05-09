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

import corticalClasification.KnowledgeAreas;
import ec.edu.cedia.redi.Author;
import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.RediRepository;
import ec.edu.cedia.redi.unesco.UnescoNomeclature;
import ec.edu.cedia.redi.unesco.UnescoNomeclatureConnection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe
 */
public class GenerateGroups {

    private static final Logger log = LoggerFactory.getLogger(GenerateGroups.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        final CommandLineParser parser = new DefaultParser();

        final Options options = extractAreasOptions();
        CommandLine cmd;
        try {
            args = new String[]{""};
            cmd = parser.parse(options, args);
            if (cmd.hasOption("threads")) {
                int threads = Integer.parseInt(cmd.getOptionValue("threads").trim());
                extractAreas(threads);
            } else {
                showHelp("generateGroups", options);
            }
        } catch (MissingArgumentException ex) {
            showHelp("generateGroups", options);
        } catch (ParseException ex) {
            showHelp("generateGroups", options);
        }

    }

    private static void showHelp(String program, Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(program, opts, true);
    }

    private static Options extractAreasOptions() {
        final Option threadsOption = Option.builder("t")
                .required(true)
                .longOpt("threads")
                .hasArg(true)
                .desc("Number of threads to execute in a single machine. \nThe execution is not thread-safe.")
                .build();
        final Options options = new Options();
        options.addOption(threadsOption);
        return options;
    }

    private static void extractAreas(int threads) throws Exception {
        try (RediRepository rediRepository = RediRepository.getInstance();
                UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();) {
            final Redi redi = new Redi(rediRepository);
            final UnescoNomeclature unesco = new UnescoNomeclature(conn);

            final List<URI> twoDigit = unesco.twoDigitResources();
            final Iterator<Author> authors = redi.getAuthors().iterator();
            KnowledgeAreas areas = new KnowledgeAreas(redi, unesco, twoDigit);
            do {
                for (int i = 0; i < threads; i++) {
                    if (authors.hasNext()) {
                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    Author author = authors.next();
                                    areas.extractArea(author);
                                    log.info(authors.next().getURI().toString());
                                } catch (Exception ex) {
                                    log.error("Cannot extract area", ex);
                                }
                            }
                        };
                        thread.start();
                    }
                }

            } while (authors.hasNext());
        }
    }

}
