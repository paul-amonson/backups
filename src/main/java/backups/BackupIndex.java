package backups;

import com.google.gson.annotations.Expose;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class BackupIndex {
    BackupIndex(BackupSet set, boolean dryRun) {
        set_ = set;
        dryRun_ = dryRun;
    }

    void createOrLoad() {
    }

    @Expose(serialize = false, deserialize = false) private final BackupSet set_;
    @Expose(serialize = false, deserialize = false) private final boolean dryRun_;
    @Expose(serialize = false, deserialize = false) private final Logger log_ = LogManager.getRootLogger();
}
