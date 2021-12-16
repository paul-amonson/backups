// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

class BackupSet implements Iterable<File> {
    BackupSet(CreateBackupSet set) {
        destinationFolder_ = set.destinationFolder_;
        setFile_ = set.setFile_;
        keyFile_ = set.keyFile_;
        dataFolders_ = set.dataFolders_;
        name_ = set.name_;
        extension_ = set.extension_;
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

    @SerializedName(value="name")           private String name_;
    @SerializedName(value="destination")    private File destinationFolder_;
    @SerializedName(value="key-file")       private File keyFile_;
    @SerializedName(value="set-file")       private File setFile_;
    @SerializedName(value="source-folders") private List<File> dataFolders_;
    @SerializedName(value="extension")      private String extension_;
}
