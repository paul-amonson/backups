package backups;

import com.amonson.crypto.Copier;
import com.amonson.crypto.KeyData;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class BackupIndex implements Iterable<BackupIndexEntry> {
    boolean isFileInList(File file) {
        return entries_.containsKey(file.getAbsolutePath());
    }

    void addNewFile(File file) {
        entries_.putIfAbsent(file.getAbsolutePath(), new BackupIndexEntry(file));
    }

    void saveIndex(File indexFile, File keyFile) throws IOException {
        String json = new Gson().toJson(this, BackupIndex.class);
        KeyData key = new Gson().fromJson(Files.readString(keyFile.toPath(), StandardCharsets.UTF_8), KeyData.class);
        Copier.writeStringEncrypted(json, indexFile, key);
    }

    BackupIndexEntry getEntry(String absPath) {
        return entries_.get(absPath);
    }

    @Override  public Iterator<BackupIndexEntry> iterator() {
        return entries_.values().iterator();
    }

    int size() { return entries_.size(); }

    Iterator<String> filenameIterator() {
        return entries_.keySet().iterator();
    }

    @SerializedName(value="filesMap") private Map<String, BackupIndexEntry> entries_ = new HashMap<>();
}
