package backups;

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

    void saveIndex(File indexFile) throws IOException {
        String json = new Gson().toJson(this, BackupIndex.class);
        Files.writeString(indexFile.toPath(), json, StandardCharsets.UTF_8);
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
