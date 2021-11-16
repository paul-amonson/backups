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
import java.nio.file.Files;
import java.time.Instant;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "do-recover",
        description = "Recover backed-up files from the index file saved with the encrypted files.",
        mixinStandardHelpOptions = true)
public class DoRecover implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        File destination = indexFile_.getParentFile();
        long start = Instant.now().getEpochSecond();
        try {
            int rv = 0;
            if(!doRecoverFiles(indexFile_, destination))
                rv = 3;
            printReport(Instant.now().getEpochSecond() - start);
            return rv;
        } catch(IllegalArgumentException e) {
            log_.error(e.getMessage());
            return 2;
        }
    }

    private boolean doRecoverFiles(File indexFile, File destination) {
        try {
            BackupIndex index = loadIndexFile(indexFile, keyFile_);
            log_.info("Starting to recover files from index file: {}", indexFile);
            for(BackupIndexEntry entry: index) {
                File src = new File(destination, entry.getId() + ".bin");
                File target = new File(chroot_, entry.getFile().toString());
                log_.debug("*** Destination Location: {}", target);
                copyFile(src, target, keyFile_);
            }
        } catch(IOException e) {
            log_.fatal("Restore failed!");
            return false;
        }
        return true;
    }

    private BackupIndex loadIndexFile(File indexFile, File keyFile) throws IOException {
        KeyData key = new Gson().fromJson(Files.readString(keyFile.toPath(), StandardCharsets.UTF_8), KeyData.class);
        String json = Copier.readStringDecrypted(indexFile, key);
        return new Gson().fromJson(json, BackupIndex.class);
    }

    private void printReport(long seconds) {
        System.out.println();
        if(dryRun_)
            System.out.println("*** DRY RUN ONLY");
        System.out.print ("+----------------------------------------+\n");
        System.out.printf("| Restored files:           %12d |\n", restoredFiles_);
        System.out.printf("| Errored files:            %12d |\n", erroredFiles_);
        System.out.print ("+----------------------------------------+\n");
        System.out.printf("| Total Time (m:ss):        %9d:%02d |\n", seconds / 60, seconds % 60);
        System.out.print ("+----------------------------------------+\n");
    }

    private void copyFile(File src, File target, File keyFile) {
        log_.info("Recovering file:\n    {}\n    {}", src, target);
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

    @CommandLine.Option(names = {"--dry-run"},
            description = "Attempt everything except the actual file recovery of files.")
    private boolean dryRun_ = false; // Assigned by picocli dynamically...
    @CommandLine.Option(names = {"--destination"}, description = "(Required) Where to store the recovered file tree.",
            required = true, paramLabel = "destination_folder")
    private File chroot_ = new File(File.separator); // Assigned by picocli dynamically...
    @CommandLine.Option(names = {"--key-file"},
            description = "(Required) Key file to use to decrypt the files to recover.", required = true,
            paramLabel = "key_file")
    private File keyFile_ = new File("."); // Assigned by picocli dynamically...

    @CommandLine.Parameters(paramLabel="index_file",
            description ="Index file to use for recovering the backed-up files.", arity = "1")
    File indexFile_;

    private final Logger log_ = LogManager.getRootLogger();
    private int restoredFiles_ = 0;
    private int erroredFiles_ = 0;
}
