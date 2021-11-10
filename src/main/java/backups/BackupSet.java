package backups;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Iterator;
import java.util.List;

class BackupSet implements Iterable<File> {
    BackupSet(CreateBackupSet set) {
        destinationFolder_ = set.destinationFolder_;
        setFile_ = set.setFile_;
        keyFile_ = set.keyFile_;
        dataFolders_ = set.dataFolders_;
        name_ = set.name_;
    }

    String getName() { return name_; }
    File getDestination() { return destinationFolder_; }
    File getSetFile() { return setFile_; }
    File getKeyFile() { return keyFile_; }

    @Override
    public Iterator<File> iterator() {
        return dataFolders_.iterator();
    }

    @SerializedName(value="name")           private String name_;
    @SerializedName(value="destination")    private File destinationFolder_;
    @SerializedName(value="key-file")       private File keyFile_;
    @SerializedName(value="set-file")       private File setFile_;
    @SerializedName(value="source-folders") private List<File> dataFolders_;
}
