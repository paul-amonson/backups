package backups;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "do-backup", description = "Create a new backup set file",
        mixinStandardHelpOptions = true)
public class DoBackup implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        try {
            for (int i = 0; i < setFiles_.size(); i++) {
                File setFile = setFiles_.get(i);
                if(!setFile.exists())
                    throw new IllegalArgumentException("Command line specified a missing backup set!");
                setFiles_.set(i, setFile.getAbsoluteFile());
            }
            int rv = 0;
            for(File setFile: setFiles_) {
                log_.info("*** Starting backup set: {}", setFile.getCanonicalPath());
                if(!DoBackupSet(setFile, dryRun_)) {
                    rv = 3;
                    log_.warn("*** Finished backup set with error: {}", setFile.getCanonicalPath());
                } else
                    log_.info("*** Finished backup set OK: {}", setFile.getCanonicalPath());
            }
            return rv;
        } catch(IllegalArgumentException e) {
            LogManager.getRootLogger().error(e.getMessage());
            return 2;
        }
    }

    private boolean DoBackupSet(File setFile) {
        try {
            BackupSet set = new Gson().fromJson(Files.readString(setFile.toPath(), StandardCharsets.UTF_8), BackupSet.class);
            BackupIndex index = new BackupIndex(set, dryRun_);
            index.createOrLoad();
        } catch(IOException e) {
            log_.error("");
            return false;
        }
        return dryRun_;
    }

    @CommandLine.Option(names = {"--dry-run"}, description = "Attempt everything except the actual backup of files.")
    private boolean dryRun_ = false; // Assigned by picocli dynamically...

    @CommandLine.Parameters(paramLabel="backup_set_files", description = "Backup set file(s) used to run backup(s).",
            arity = "1..*")
    List<File> setFiles_;

    private final Logger log_ = LogManager.getRootLogger();
}
