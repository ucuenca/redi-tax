/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.edu.cedia.redi.console;

import ec.edu.cedia.redi.Redi;
import ec.edu.cedia.redi.repository.RediRepository;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openrdf.repository.RepositoryException;
import subClasification.SubClassificationCortical;

/**
 *
 * @author joe
 */
public class GenerateSubClustersExec {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final CommandLineParser parser = new DefaultParser();

        final Options options = extractSubCLustersOptions();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                showHelp("Subclusters", options);
            }
            if (cmd.hasOption("delete")) {
                // showHelp("generateGroups", options); 
                RediRepository rp = RediRepository.getInstance();
                Redi r = new Redi(rp);
                r.deleteSubclusters();
            } else {
                // SubClassificationCortical sc = new SubClassificationCortical ();   
                int key = 1;
                Boolean paramfilter = false;
                if (cmd.hasOption("filter-authors")) {
                    paramfilter = true;
                }
                Boolean saved = true;
                if (cmd.hasOption("unsaved")) {
                    saved = false;
                }

                if (cmd.hasOption("keyandex")) {
                    key = Integer.parseInt(cmd.getOptionValue("keyandex").trim());
                    if (key != 2) {
                        key = 1;
                    }
                }

                SubClassificationCortical.executeSubGroup(paramfilter, saved, key);
            }
        } catch (ParseException ex) {
            showHelp("Subclusters", options);
            Logger.getLogger(GenerateSubClustersExec.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            Logger.getLogger(GenerateSubClustersExec.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void showHelp(String program, Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(program, "", opts,
                "Note: With large amount of new authors,  delete subcluster and execute the proccess",
                true);
    }

    private static Options extractSubCLustersOptions() {
        final Option filterAuthorsOption = Option.builder("f")
                .required(false)
                .longOpt("filter-authors")
                .hasArg(false)
                .desc("Filter clusters already proccessed. Default value: false.")
                .build();
        final Option saveoption = Option.builder("u")
                .required(false)
                .longOpt("unsaved")
                .hasArg(false)
                .desc("Build groups and print in stdo only. Default value: false")
                .build();
        final Option deleteOption = Option.builder("d")
                .required(false)
                .longOpt("delete")
                .hasArg(false)
                .desc("Delete subclusters.")
                .build();
        final Option keyandex = Option.builder("k")
                .required(false)
                .longOpt("keyandex")
                .hasArg(true)
                .desc("Select Yandex keyword (1 or 2).")
                .build();
        final Option help = Option.builder("help")
                .required(false)
                .longOpt("help")
                .hasArg(false)
                .desc("Show this screen.")
                .build();
        final Options options = new Options();

        options.addOption(filterAuthorsOption);
        options.addOption(saveoption);
        options.addOption(deleteOption);
        options.addOption(keyandex);
        options.addOption(help);
        return options;
    }

}
