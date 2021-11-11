// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import org.apache.logging.log4j.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;

/**
 * Do backups and restores.
 *
 * generate-key {keyfile | -} [--force]
 * create-backup-set --key=keyfile --destination=dest-folder {folder [folder [...]]} {setfile}
 * backup set-files(s)
 * restore set-file(s) [alternate-root-folder]
 */
@Command(subcommands = {GenerateKey.class, CreateBackupSet.class, DoBackup.class, DoRestore.class}, name = "backups",
        versionProvider=Backup.Version.class, mixinStandardHelpOptions = true,
        description = "Tool to perform a compressed and encrypted backup.")
public class Backup implements Callable<Integer> {
    public static void main(String[] args) {
        String logLevel = "INFO";
        if(System.getenv().containsKey("LOG_LEVEL"))
            logLevel = System.getenv("LOG_LEVEL");
        logLevel = System.getProperty("logLevel", logLevel);
        ((org.apache.logging.log4j.core.Logger)LogManager.getRootLogger()).setLevel(Level.getLevel(logLevel));

        System.exit(new CommandLine(new Backup()).execute(args));
    }

    private Backup() { log_ = LogManager.getRootLogger(); }

    @Override public Integer call() throws Exception {
        log_.error("Bad command line (no options or parameters)!");
        return new CommandLine(new Backup()).execute("-h");
    }

    private final Logger log_;

    // Version provider...
    public static class Version implements CommandLine.IVersionProvider {
        @Override public String[] getVersion() throws Exception {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            if(resources.hasMoreElements()) {
                Manifest manifest = new Manifest(resources.nextElement().openStream());
                String name = manifest.getMainAttributes().getValue("Implementation-Title");
                String version = manifest.getMainAttributes().getValue("Implementation-Version");
                return new String[] {name + " " + version};
            }
            return new String[] {"No Version"};
        }
    }
}
