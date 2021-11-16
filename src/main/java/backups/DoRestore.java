package backups;

import com.amonson.crypto.Copier;
import com.amonson.crypto.KeyData;
import com.google.gson.Gson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "do-restore", description = "Restore a backup set",
        mixinStandardHelpOptions = true)
public class DoRestore implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        long start = Instant.now().getEpochSecond();
        chroot_ = chroot_.getAbsoluteFile();
        try {
            for (int i = 0; i < setFiles_.size(); i++) {
                File setFile = setFiles_.get(i);
                if(!setFile.exists())
                    throw new IllegalArgumentException("Command line specified a missing backup set!");
                setFiles_.set(i, setFile.getAbsoluteFile());
            }
            int rv = 0;
            for(File setFile: setFiles_) {
                if(!doRestoreSet(setFile)) {
                    rv = 3;
                    log_.warn("*** Finished restore set with error: {}", setFile.getCanonicalPath());
                } else
                    log_.info("*** Finished restore set OK: {}", setFile.getCanonicalPath());
            }
            printReport(Instant.now().getEpochSecond() - start);
            return rv;
        } catch(IllegalArgumentException e) {
            log_.error(e.getMessage());
            return 2;
        }
    }

    private boolean doRestoreSet(File setFile) {
        try {
            BackupSet set = new Gson().fromJson(Files.readString(setFile.toPath(), StandardCharsets.UTF_8),
                    BackupSet.class);
            System.out.println("\n==========================================================================================");
            System.out.printf("====  %s  ====\n", set.getName());
            log_.info("*** Starting to restore backup set: {}", setFile.getCanonicalPath());
            BackupSetIndexFactory factory = new BackupSetIndexFactory(set, dryRun_);
            BackupIndex index = factory.loadOnly(set.getKeyFile());
            totalFiles_ = index.size();
            for(BackupIndexEntry entry: index) {
                File src = new File(set.getDestination(), entry.getId() + ".bin");
                File target = new File(chroot_, entry.getFile().toString());
                log_.debug("*** Destination Location: {}", target);
                if(force_ || checkDoCopy(src, target)) {
                    copyFile(src, target, set.getKeyFile());
                } else {
                    log_.info("Skipping restoring older file:\n    {}", target);
                    skippedFiles_ += 1;
                }
            }
        } catch(IOException e) {
            log_.fatal("Restore failed!");
            return false;
        }
        return true;
    }

    private void printReport(long seconds) {
        System.out.println();
        if(dryRun_)
            System.out.println("*** DRY RUN ONLY");
        System.out.print ("+----------------------------------------+\n");
        System.out.printf("| Restored files:           %12d |\n", restoredFiles_);
        System.out.printf("| Skipped files:            %12d |\n", skippedFiles_);
        System.out.printf("| Errored files:            %12d |\n", erroredFiles_);
        System.out.printf("| Total processed files:    %12d |\n", totalFiles_);
        System.out.print ("+----------------------------------------+\n");
        System.out.printf("| Total Time (m:ss):        %9d:%02d |\n", seconds / 60, seconds % 60);
        System.out.print ("+----------------------------------------+\n");
    }

    private boolean checkDoCopy(File src, File target) {
        return !target.exists() || src.lastModified() > target.lastModified();
    }

    private void copyFile(File src, File target, File keyFile) {
        log_.info("Restoring file:\n    {}\n    {}", src, target);
        try {
            KeyData key = null;
            if(keyFile != null)
                key = new Gson().fromJson(Files.readString(keyFile.toPath(), StandardCharsets.UTF_8), KeyData.class);
            if(!dryRun_) {
                target.getParentFile().mkdirs();
                Copier.copyFile(src, target, key, Copier.Direction.Decryption);
            }
            restoredFiles_ += 1;
        } catch(SecurityException | IOException e) {
            log_.error("Failed to restore file:\n    {}!", src);
            log_.catching(Level.DEBUG, e);
            erroredFiles_ += 1;
        }
    }

    @CommandLine.Option(names = {"--dry-run"}, description = "Attempt everything except the actual restore of files.")
    private boolean dryRun_ = false; // Assigned by picocli dynamically...
    @CommandLine.Option(names = {"--force"}, description = "Force overwrite of all existing files in the destination.")
    private boolean force_ = false; // Assigned by picocli dynamically...
    @CommandLine.Option(names = {"--chroot"}, description = "Change to a root other than '/' for the restored files.",
            paramLabel = "new-root-folder")
    private File chroot_ = new File(File.separator); // Assigned by picocli dynamically...

    @CommandLine.Parameters(paramLabel="backup_set_files", description =
            "Backup set file(s) used to run restore from backups.",
            arity = "1..*")
    List<File> setFiles_;

    private final Logger log_ = LogManager.getRootLogger();
    private int restoredFiles_ = 0;
    private int skippedFiles_ = 0;
    private int totalFiles_ = 0;
    private int erroredFiles_ = 0;
}
