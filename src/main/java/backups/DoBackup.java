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
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "do-backup", description = "Backup files in a backup set",
        mixinStandardHelpOptions = true)
public class DoBackup implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        long start = Instant.now().getEpochSecond();
        try {
            for (int i = 0; i < setFiles_.size(); i++) {
                File setFile = setFiles_.get(i);
                if(!setFile.exists())
                    throw new IllegalArgumentException("Command line specified a missing backup set!");
                setFiles_.set(i, setFile.getAbsoluteFile());
            }
            int rv = 0;
            for(File setFile: setFiles_) {
                if(!DoBackupSet(setFile)) {
                    rv = 3;
                    log_.warn("*** Finished backup set with error: {}", setFile.getCanonicalPath());
                } else
                    log_.info("*** Finished backup set OK: {}", setFile.getCanonicalPath());
            }
            printReport(Instant.now().getEpochSecond() - start);
            return rv;
        } catch(IllegalArgumentException e) {
            LogManager.getRootLogger().error(e.getMessage());
            return 2;
        }
    }

    private boolean DoBackupSet(File setFile) {
        try {
            BackupSet set = new Gson().fromJson(Files.readString(setFile.toPath(), StandardCharsets.UTF_8),
                    BackupSet.class);
            System.out.println("\n==========================================================================================");
            System.out.printf("====  %s  ====\n", set.getName());
            log_.info("*** Starting backup set: {}", setFile.getCanonicalPath());
            BackupSetIndexFactory factory = new BackupSetIndexFactory(set, dryRun_);
            BackupIndex index = factory.createOrLoad(set.getKeyFile());
            walkFileTrees(set, index);
            backupIndex(set, index);
            index.saveIndex(set.getSetFileIndex(), set.getKeyFile());
        } catch(IOException e) {
            log_.fatal("Backup failed!");
            return false;
        }
        return true;
    }

    private void printReport(long seconds) {
        System.out.println("");
        if(dryRun_)
            System.out.println("*** DRY RUN ONLY");
        System.out.print ("+----------------------------------------+\n");
        System.out.printf("| Backed up files:          %12d |\n", backupedFiles_);
        System.out.printf("| Skipped files:            %12d |\n", skippedFiles_);
        System.out.printf("| New files backed up:      %12d |\n", newFiles_);
        System.out.printf("| Apparently Deleted files: %12d |\n", deletedFiles_);
        System.out.printf("| Errored files:            %12d |\n", erroredFiles_);
        System.out.printf("| Total processed files:    %12d |\n", totalFiles_);
        System.out.print ("+----------------------------------------+\n");
        System.out.printf("| Total Time (m:ss):        %9d:%02d |\n", seconds / 60, seconds % 60);
        System.out.print ("+----------------------------------------+\n");
    }

    private void walkFileTrees(BackupSet set, BackupIndex index) throws IOException {
        for(File folder: set) {
            Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<>() {
                @Override public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes)
                        throws IOException {
                    File file = path.toFile().getAbsoluteFile();
                    if(!file.isDirectory())
                        checkRealFile(index, file);
                    return FileVisitResult.CONTINUE;
                }

                @Override public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                    log_.warn("Failed to check file:\n    {}!", path.toFile().getCanonicalPath());
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    void checkRealFile(BackupIndex index, File file) {
        if(index.isFileInList(file)) {
            index.getEntry(file.toString()).determineNeedsBackup();
            if(index.getEntry(file.toString()).needsBackup()) {
                log_.debug("Previously backed up file needs backing up:\n    {}", file);
            } else {
                log_.debug("Previously backed up file not changed:\n    {}", file);
            }
        } else {
            index.addNewFile(file);
            log_.debug("New file to backup:\n    {}", file);
            newFiles_ += 1;
        }
    }

    void backupIndex(BackupSet set, BackupIndex index) throws IOException {
        if(set.getDestination().exists() && !set.getDestination().isDirectory())
            throw new IOException("Destination path exists and is not a folder: " + set.getDestination());
        if(!set.getDestination().exists())
            Files.createDirectories(set.getDestination().toPath());
        totalFiles_ = index.size();
        for(BackupIndexEntry entry: index) {
            if(entry.wasChecked()) {
                if (entry.needsBackup()) {
                    if (backupEntry(set.getDestination(), entry, set.getKeyFile())) {
                        entry.updateAfterBackedUp();
                        backupedFiles_ += 1;
                    } else
                        entry.resetFileTime();
                } else {
                    log_.info("Skipped backing up file:\n    {}", entry.getFile());
                    skippedFiles_ += 1;
                }
            } else {
                log_.info("Apparently deleted file in source:\n    {}", entry.getFile());
                deletedFiles_ += 1;
            }
        }
    }

    private boolean backupEntry(File folder, BackupIndexEntry entry, File keyFile) throws IOException {
        File target = new File(folder, entry.getId() + ".bin");
        return copyFile(entry.getFile(), target, keyFile);
    }

    private boolean copyFile(File src, File target, File keyFile) {
        log_.info("Backing up:\n    {}\n    {}", src, target);
        try {
            KeyData key = null;
            if(keyFile != null)
                key = new Gson().fromJson(Files.readString(keyFile.toPath(), StandardCharsets.UTF_8), KeyData.class);
            if(!dryRun_)
                Copier.copyFile(src, target, key, Copier.Direction.Encryption);
            return true;
        } catch(IOException e) {
            log_.error("Failed to backup file:\n    {}!", src);
            log_.catching(Level.DEBUG, e);
            return false;
        }
    }

    @CommandLine.Option(names = {"--dry-run"}, description = "Attempt everything except the actual backup of files.")
    private boolean dryRun_ = false; // Assigned by picocli dynamically...

    @CommandLine.Parameters(paramLabel="backup_set_files", description = "Backup set file(s) used to run backup(s).",
            arity = "1..*")
    List<File> setFiles_;

    private final Logger log_ = LogManager.getRootLogger();
    private int backupedFiles_ = 0;
    private int newFiles_ = 0;
    private int skippedFiles_ = 0;
    private int totalFiles_ = 0;
    private int erroredFiles_ = 0;
    private int deletedFiles_ = 0;
}
