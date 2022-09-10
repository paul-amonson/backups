// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import com.google.gson.stream.*;
import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class BackupSet implements Iterable<File> {
    private BackupSet() {}

    BackupSet(CreateBackupSet set) {
        destinationFolder_ = set.destinationFolder_;
        setFile_ = set.setFile_;
        keyFile_ = set.keyFile_;
        dataFolders_ = set.dataFolders_;
        name_ = set.name_;
        extension_ = set.extension_;
    }

    public static TypeAdapter<BackupSet> getGSonAdapter() {
        return new TypeAdapter<>() {
            @Override public BackupSet read(JsonReader reader) throws IOException {
                BackupSet set = new BackupSet();
                reader.beginObject();
                while(reader.hasNext()) {
                    JsonToken token = reader.peek();
                    String fieldName = null;
                    if (token.equals(JsonToken.NAME)) {
                        //get the current token
                        fieldName = reader.nextName();
                    }

                    if ("name".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        set.name_ = reader.nextString();
                    }

                    if("destination".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        set.destinationFolder_ = new File(reader.nextString());
                    }
                    if("key-file".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        set.keyFile_ = new File(reader.nextString());
                    }
                    if("set-file".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        set.setFile_ = new File(reader.nextString());
                    }
                    if("extension".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        set.extension_ = reader.nextString();
                        if(set.extension_ == null || set.extension_.isBlank())
                            set.extension_ = "aes";
                    }
                    if("source-folders".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        List<File> files = new ArrayList<>();
                        reader.beginArray();
                        while(reader.hasNext()) {
                            files.add(new File(reader.nextString()));
                        }
                        reader.endArray();
                        set.dataFolders_ = files;
                    }
                }
                reader.endObject();
                return set;
            }
            @Override public void write(JsonWriter writer, BackupSet data) throws IOException {
                writer.beginObject();
                writer.name("name");
                writer.value(data.name_);
                writer.name("destination");
                writer.value(data.destinationFolder_.toString());
                writer.name("key-file");
                writer.value(data.keyFile_.toString());
                writer.name("set-file");
                writer.value(data.setFile_.toString());
                writer.name("extension");
                if(data.extension_ == null || data.extension_.isBlank())
                    writer.value("aes");
                else
                    writer.value(data.extension_);
                writer.name("source-folders");
                writer.beginArray();
                for(File file: data.dataFolders_)
                    writer.value(file.toString());
                writer.endArray();
                writer.endObject();
            }
        };
    }

    String getName() { return name_; }
    File getDestination() { return Paths.get(destinationFolder_.toString(), setFile_.getName()).toFile(); }
    File getSetFile() { return setFile_; }
    File getSetFileIndex() {
        return Paths.get(getDestination().toString(), setFile_.getName() + ".index").toFile();
    }
    File getKeyFile() { return keyFile_; }
    String getExtension() { return extension_; }

    @Override
    public Iterator<File> iterator() {
        return dataFolders_.iterator();
    }

    String name_;
    File destinationFolder_;
    File keyFile_;
    File setFile_;
    List<File> dataFolders_;
    String extension_;
}
