package backups;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BackupSetIndexFactory {
    BackupSetIndexFactory(BackupSet set, boolean dryRun) {
        set_ = set;
        dryRun_ = dryRun;
    }

    BackupIndex createOrLoad() throws IOException {
        File indexFile = set_.getSetFileIndex();
        if(indexFile.exists())
            return loadFile(indexFile);
        else
            return createFile(indexFile);
    }

    private BackupIndex createFile(File indexFile) throws IOException {
        BackupIndex index = new BackupIndex();
        index.saveIndex(indexFile);
        return index;
    }

    private BackupIndex loadFile(File indexFile) throws IOException {
        return new Gson().fromJson(Files.readString(indexFile.toPath(), StandardCharsets.UTF_8), BackupIndex.class);
    }

    private final BackupSet set_;
    private final boolean dryRun_;
    private final Logger log_ = LogManager.getRootLogger();
}
