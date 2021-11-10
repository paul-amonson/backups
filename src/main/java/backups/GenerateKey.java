package backups;

import com.amonson.crypto.KeyData;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.*;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Callable;

@Command(name = "generate-key", description = "Generate a new key to a filename.", mixinStandardHelpOptions = true)
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
        String json = new Gson().toJson(key);
        if(outputFile_.toString().equals("-"))
            System.out.println(json);
        else
            Files.writeString(outputFile_.toPath(), json);
        log_.info("New key generated.");
        return 0;
    }

    @Parameters(description = "Output key filename.")
    private File outputFile_; // Assigned by picocli dynamically...

    @Option(names = {"-f", "--force"}, description = "Force overwrite of the keyfile.")
    private boolean force_ = false; // Assigned by picocli dynamically...

    private final Logger log_;
}
