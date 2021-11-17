// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.UUID;

class BackupIndexEntry {
    // Entry point for new file...
    BackupIndexEntry(File newFile) {
        file_ = newFile.getAbsoluteFile();
        fileId_ = UUID.randomUUID().toString();
        lastModified_ = file_.lastModified();
        lastSize_ = file_.length();
        needsBackup_ = true;
        checked_ = true;
    }

    // Called when index is read from disk...
    void determineNeedsBackup() {
        needsBackup_ = false;
        if(getLastModifiedTs() != file_.lastModified())
            needsBackup_ = true;
        else if(getLastSize() != file_.length())
            needsBackup_ = true;
        checked_ = true;
    }

    void updateAfterBackedUp() {
        lastModified_ = file_.lastModified();
        lastSize_ = file_.length();
    }

    void resetFileTime() {
        lastModified_ = 0L;
    }

    File getFile() { return file_; }
    String getId() { return fileId_; }
    long getLastSize() { return lastSize_; }
    long getLastModifiedTs() { return lastModified_; }
    boolean needsBackup() { return needsBackup_; }
    boolean wasChecked() { return checked_; }

    @SerializedName(value="file")
    private File file_;
    @SerializedName(value="id")
    private String fileId_;
    @SerializedName(value="last-modified")
    private long lastModified_;
    @SerializedName(value="last-size")
    private long lastSize_;

    @Expose(serialize = false, deserialize = false)
    private boolean needsBackup_ = false;
    @Expose(serialize = false, deserialize = false)
    private boolean checked_ = false;

}
