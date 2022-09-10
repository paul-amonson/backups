// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import com.amonson.crypto.Copier;
import com.amonson.crypto.KeyData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

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
        String json = newGson().toJson(this, BackupIndex.class);
        KeyData key = newGson().fromJson(Files.readString(keyFile.toPath(), StandardCharsets.UTF_8), KeyData.class);
        Copier.writeStringEncrypted(json, indexFile, key);
    }

    private Gson newGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(BackupSet.class, BackupSet.getGSonAdapter());
        builder.registerTypeAdapter(KeyData.class, KeyData.getGSonAdapter());
        builder.registerTypeAdapter(BackupIndex.class, BackupIndex.getGSonAdapter());
        return builder.create();
    }

    public static TypeAdapter<BackupIndex> getGSonAdapter() {
        return new TypeAdapter<>() {
            @Override public BackupIndex read(JsonReader reader) throws IOException {
                BackupIndex index = new BackupIndex();
                reader.beginArray();
                while(reader.hasNext()) {
                    reader.beginObject();
                    String path = null;
                    BackupIndexEntry entry = new BackupIndexEntry();
                    while(reader.hasNext()) {
                        JsonToken token = reader.peek();
                        String fieldName = null;
                        if (token.equals(JsonToken.NAME)) {
                            //get the current token
                            fieldName = reader.nextName();
                        }
                        if ("full-name".equals(fieldName)) {
                            //move to next token
                            token = reader.peek();
                            path = reader.nextString();
                        }
                        if ("file".equals(fieldName)) {
                            //move to next token
                            token = reader.peek();
                            entry.file_ = new File(reader.nextString());
                        }
                        if ("id".equals(fieldName)) {
                            //move to next token
                            token = reader.peek();
                            entry.fileId_ = reader.nextString();
                        }
                        if ("last-modified".equals(fieldName)) {
                            //move to next token
                            token = reader.peek();
                            entry.lastModified_ = reader.nextLong();
                        }
                        if ("last-size".equals(fieldName)) {
                            //move to next token
                            token = reader.peek();
                            entry.lastSize_ = reader.nextLong();
                        }
                    }
                    reader.endObject();
                    index.entries_.put(path, entry);
                }
                reader.endArray();
                return index;
            }
            @Override public void write(JsonWriter writer, BackupIndex data) throws IOException {
                writer.beginArray();
                for(Map.Entry<String,BackupIndexEntry> entry: data.entries_.entrySet()) {
                    writer.beginObject();
                    writer.name("full-name");
                    writer.value(entry.getKey());
                    writer.name("file");
                    writer.value(entry.getValue().file_.toString());
                    writer.name("id");
                    writer.value(entry.getValue().fileId_);
                    writer.name("last-modified");
                    writer.value(entry.getValue().lastModified_);
                    writer.name("last-size");
                    writer.value(entry.getValue().lastSize_);
                    writer.endObject();
                }
                writer.endArray();
            }
        };
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
