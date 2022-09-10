// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package backups;

import com.amonson.crypto.KeyData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.*;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@Command(name = "generate-key", description = "Generate a new key and save to a filename or stdout (-).", mixinStandardHelpOptions = true)
public class GenerateKey implements Callable<Integer> {
    public GenerateKey() {
        log_ = LogManager.getRootLogger();
    }
    @Override
    public Integer call() throws Exception {
        if(outputFile_.exists() && !force_) {
            log_.error("Key file '{}' already exists, try using '--force' if your sure you want to over write the existing file!", outputFile_);
            return 2;
        }
        KeyData key = KeyData.newKeyData();
        String json = newGson().toJson(key);
        if(outputFile_.toString().equals("-"))
            System.out.println(json);
        else
            Files.writeString(outputFile_.toPath(), json);
        log_.info("New key generated.");
        return 0;
    }

    private Gson newGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(KeyData.class, KeyData.getGSonAdapter());
        return builder.create();
    }

    @Parameters(paramLabel="output_file", description = "Output key filename or '-' for output to console.")
    private File outputFile_; // Assigned by picocli dynamically...

    @Option(names = {"-f", "--force"}, description = "Force overwrite of the keyfile.")
    private boolean force_ = false; // Assigned by picocli dynamically...

    private final Logger log_;
}
