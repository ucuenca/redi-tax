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

import ec.edu.cedia.redi.repository.StardogConnection;
import ec.edu.cedia.redi.unesco.UnescoPopulation;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Unesco {

    private static final Logger log = LoggerFactory.getLogger(Unesco.class);

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        final CommandLineParser parser = new DefaultParser();

        final Options options = unescoOptions();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("populate")) {
                if (cmd.hasOption("db")) {
                    try (Graph graph = StardogConnection.instance(cmd.getOptionValue("db")).graph()) {
                        UnescoPopulation unesco = new UnescoPopulation(graph.traversal());
                        unesco.populate();
                    }
                } else {
                    try (Graph graph = StardogConnection.instance().graph()) {
                        UnescoPopulation unesco = new UnescoPopulation(graph.traversal());
                        unesco.populate();
                    }
                }
            } else {
                showHelp("unesco", options);
            }
        } catch (ParseException ex) {
            log.error("Cannot parse commandline options", ex);
        }
    }

    private static void showHelp(String program, Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(program, opts);
    }

    private static Options unescoOptions() {
        final Option populateOption = Option.builder("p")
                .required(false)
                .longOpt("populate")
                .hasArg(false)
                .desc("Populate and expand Stardog repository with UNESCO nomenclature.")
                .build();
        final Option databaseOption = Option.builder("db")
                .required(false)
                .longOpt("database")
                .hasArg(false)
                .desc("Specify the connection to the Stardog repository (e.g http://localhost:5820/myDB).")
                .build();
        final Options options = new Options();
        options.addOption(populateOption);
        options.addOption(databaseOption);
        return options;
    }
}
