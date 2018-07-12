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
import ec.edu.cedia.redi.unesco.UnescoDataSet;
import ec.edu.cedia.redi.unesco.UnescoNomeclatureConnection;
import ec.edu.cedia.redi.unesco.model.UnescoHierarchy;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author joe
 */
public class GenerateGroups {

    private static final Logger log = LoggerFactory.getLogger(GenerateGroups.class);
    private static int offset = -1, limit = -1;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        final CommandLineParser parser = new DefaultParser();

        final Options options = extractAreasOptions();
        CommandLine cmd;
        try {
//            args = new String[]{"-t=4", "-o=118", "-l=100", "-f"};
//            args = new String[]{"-t=3", "f"};
//            args = new String[]{"f"};
//            args = new String[]{"-t=3"};
//            args = new String[]{"-a=https://redi.cedia.edu.ec/resource/authors/UCUENCA/oai-pmh/SAQUICELA_GALARZA__VICTOR_HUGO"};
            cmd = parser.parse(options, args);

            if ((cmd.hasOption("offset") && !cmd.hasOption("limit")) || cmd.hasOption("limit") && !cmd.hasOption("offset")) {
                showHelp("generateGroups", options);
                return;
            }

            if (cmd.hasOption("author")) {
                String authorURI = cmd.getOptionValue("author");
                inspectAuthor(authorURI);
            } else if (cmd.hasOption("threads")) {
                int threads = Integer.parseInt(cmd.getOptionValue("threads").trim());
                boolean filter = cmd.hasOption("filter-authors");
                if (cmd.hasOption("offset") && cmd.hasOption("limit")) {
                    offset = Integer.parseInt(cmd.getOptionValue("offset").trim());
                    limit = Integer.parseInt(cmd.getOptionValue("limit").trim());
                }
                extractAreas(threads, filter);
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
        formatter.printHelp(program, "", opts,
                "Note: in order to process a small batch use both offset and limit options."
                + "\nthreads option is mandatory only to start process.",
                true);
    }

    private static Options extractAreasOptions() {
        final Option threadsOption = Option.builder("t")
                .required(false)
                .longOpt("threads")
                .hasArg(true)
                .valueSeparator('=')
                .desc("Number of threads to execute in a single machine. \nThe execution is not thread-safe.")
                .build();
        final Option offsetOption = Option.builder("o")
                .required(false)
                .longOpt("offset")
                .hasArg(true)
                .valueSeparator('=')
                .desc("Offset value to skip authors in the SPARQL query.")
                .build();
        final Option limitOption = Option.builder("l")
                .required(false)
                .longOpt("limit")
                .hasArg(true)
                .valueSeparator('=')
                .desc("Limit number of authors in a the SPARQL query.")
                .build();
        final Option filterAuthorsOption = Option.builder("f")
                .required(false)
                .longOpt("filter-authors")
                .hasArg(false)
                .desc("Filter authors already proccessed. Default value: false.")
                .build();
        final Option authorOption = Option.builder("a")
                .required(false)
                .longOpt("author")
                .hasArg(true)
                .desc("Build groups for author and print in stdo.")
                .build();
        final Options options = new Options();
        options.addOption(threadsOption);
        options.addOption(offsetOption);
        options.addOption(limitOption);
        options.addOption(filterAuthorsOption);
        options.addOption(authorOption);
        return options;
    }

    private static void extractAreas(int threads, boolean filterAuthorsProcessed) throws Exception {
        try (RediRepository rediRepository = RediRepository.getInstance();) {
            final Redi redi = new Redi(rediRepository);
            final Iterator<Author> authors;
            if (offset != -1 && limit != -1) {
                authors = redi.getAuthors(offset, limit, filterAuthorsProcessed).iterator();
            } else {
                authors = redi.getAuthors(filterAuthorsProcessed).iterator();
            }
            final List<UnescoHierarchy> dataset = UnescoDataSet.getInstance().getDataset();

            ExecutorService pool = Executors.newFixedThreadPool(threads);
            while (authors.hasNext()) {
                Author author = authors.next();
                pool.execute(new AreasExtractor(author, dataset));
            }
            pool.shutdown();
        }
    }

    private static void inspectAuthor(String authorUri) throws Exception {
        try (RediRepository rediRepository = RediRepository.getInstance();) {
            final Redi redi = new Redi(rediRepository);
            final Author author = redi.getAuthor(authorUri);
            final List<UnescoHierarchy> dataset = UnescoDataSet.getInstance().getDataset();

            ExecutorService pool = Executors.newFixedThreadPool(1);
            pool.execute(new AreasExtractor(author, dataset, true));
            pool.shutdown();
        }
    }

    private static class AreasExtractor implements Runnable {

        private Author author;
        private List<UnescoHierarchy> unescoDataset;
        private boolean inspect = false;

        public AreasExtractor(Author author, List<UnescoHierarchy> unescoDataset) {
            this.author = author;
            this.unescoDataset = unescoDataset;
        }

        public AreasExtractor(Author author, List<UnescoHierarchy> unescoDataset, boolean inspect) {
            this(author, unescoDataset);
            this.inspect = inspect;
        }

        @Override
        public void run() {
            log.info("Executing author {} in thread {}",
                    new String[]{author.getURI().stringValue()}, Thread.currentThread().getName());
            try (RediRepository rediRepository = RediRepository.getInstance();
                    UnescoNomeclatureConnection conn = UnescoNomeclatureConnection.getInstance();) {
                Redi redi = new Redi(rediRepository);

                KnowledgeAreas areas = new KnowledgeAreas(redi, unescoDataset);
                areas.setInspect(inspect);
                areas.extractArea(author);
            } catch (Exception ex) {
                log.error("Cannot extract area", ex);
            }
        }

    }

}
