// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import com.amonson.crypto.KeyData;
import org.apache.logging.log4j.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * Do backups and restores.
 *
 * generate-key keyfile
 * create-backup-set folder(s) setfile
 * backup set-files(s) [--key=keyfile] (default ./backups.key)
 * restore set-file(s) [alternate-root-folder] [--key=keyfile] (default ./backups.key)
 */
@Command(name = "backups", versionProvider=Backup.Version.class, description = "Tool to perform a compressed and encrypted backup.")
public class Backup {
    public static void main(String[] args) {
        Logger logger = LogManager.getRootLogger();
        ((org.apache.logging.log4j.core.Logger)logger).setLevel(Level.DEBUG); // Set level.
        int exitCode = 0;

        try {
            Backup app = new Backup(logger);
            app.initialize();
            exitCode = app.runApp(args);
        } catch(Exception e) {
            logger.fatal("Unknown error occurred!");
            logger.catching(Level.DEBUG, e);
            exitCode = 2;
        }
        finally {
            System.exit(exitCode);
        }
    }

    private Backup(Logger logger) {
        log_ = logger;
    }

    private void initialize() {
        // Setup sub-command dispatch
        dispatch_.put("generate-key", this::doGenerateKey);

        // Setup CLI parser
    }

    private int runApp(String[] args) {
        try {
            CommandLine commandLine = cliParser_.parse(options_, args);
            if(commandLine.hasOption('h')) {
                System.out.println("HELP");
                return 0;
            }
            String subCommand = commandLine.getArgList().get(0);
            return dispatch_.get(subCommand).handle(commandLine);
        } catch(ParseException | IndexOutOfBoundsException e) {
            log_.fatal("Commandline parsing failed!");
            log_.catching(Level.DEBUG, e);
            return 1;
        }
    }

    private int doGenerateKey(CommandLine commandLine) {
        return 0;
    }

    private final Logger log_;
    private       KeyData key_;

    // Dispatcher Code
    @FunctionalInterface private interface CommandHandler { int handle(CommandLine cl); }
    private final Map<String,CommandHandler> dispatch_ = new HashMap<>();

    public static class Version implements CommandLine.IVersionProvider {
        @Override public String[] getVersion() throws Exception {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            if(resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                String name = manifest.getMainAttributes().getValue("Implementation-Title");
                String version = manifest.getMainAttributes().getValue("Implementation-Version");
                return new String[] {name + " " + version};
            }
            return new String[] {"FAILED"};
        }
    }
}
