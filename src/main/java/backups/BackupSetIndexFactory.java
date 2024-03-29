// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import com.amonson.crypto.Copier;
import com.amonson.crypto.KeyData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BackupSetIndexFactory {
    BackupSetIndexFactory(BackupSet set, boolean dryRun) {
        set_ = set;
        dryRun_ = dryRun;
    }

    BackupIndex createOrLoad(File keyFile) throws IOException {
        File indexFile = set_.getSetFileIndex();
        if(indexFile.exists())
            return loadFile(indexFile, keyFile);
        else
            return createFile(indexFile, keyFile);
    }

    BackupIndex loadOnly(File keyFile) throws IOException {
        File indexFile = set_.getSetFileIndex();
        if(indexFile.exists())
            return loadFile(indexFile, keyFile);
        throw new FileNotFoundException("Missing index file: " + indexFile);
    }

    private Gson newGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(BackupSet.class, BackupSet.getGSonAdapter());
        builder.registerTypeAdapter(KeyData.class, KeyData.getGSonAdapter());
        builder.registerTypeAdapter(BackupIndex.class, BackupIndex.getGSonAdapter());
        return builder.create();
    }

    private BackupIndex createFile(File indexFile, File keyFile) throws IOException {
        indexFile.getParentFile().mkdirs();
        BackupIndex index = new BackupIndex();
        index.saveIndex(indexFile, keyFile);
        return index;
    }

    private BackupIndex loadFile(File indexFile, File keyFile) throws IOException {
        KeyData key = newGson().fromJson(Files.readString(keyFile.toPath(), StandardCharsets.UTF_8), KeyData.class);
        String json = Copier.readStringDecrypted(indexFile, key);
        return newGson().fromJson(json, BackupIndex.class);
    }

    private final BackupSet set_;
    private final boolean dryRun_;
    private final Logger log_ = LogManager.getRootLogger();
}
