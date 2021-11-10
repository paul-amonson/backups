package backups;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "create-backup-set", description = "Create a new backup set file",
        mixinStandardHelpOptions = true)
public class CreateBackupSet implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        try {
            checkFolderWrite(destinationFolder_);
            destinationFolder_ = destinationFolder_.getAbsoluteFile();
            for (int i = 0; i < dataFolders_.size(); i++) {
                File source = dataFolders_.get(i);
                checkFolderRead(source);
                dataFolders_.set(i, source.getAbsoluteFile());
            }
            checkFileRead(keyFile_);
            keyFile_ = keyFile_.getAbsoluteFile();
            if(!setFile_.createNewFile() && !force_) // can I create the file?
                throw new IllegalAccessException(String.format("Failed to create the set file '%s'!", setFile_)); // No
            setFile_ = setFile_.getAbsoluteFile();
            BackupSet set = new BackupSet(this);
            String contents = new Gson().toJson(set);
            LogManager.getRootLogger().info(contents);
            Files.writeString(setFile_.toPath(), contents);
            return 0;
        } catch(IllegalAccessException e) {
            LogManager.getRootLogger().error(e.getMessage());
            return 2;
        }
    }

    private void checkFolderWrite(File folder) throws IllegalAccessException {
        if(!folder.isDirectory() || !folder.canWrite())
            throw new IllegalAccessException(String.format("Folder '%s' is either not a directory or is now writable!",
                    folder));
    }

    private void checkFolderRead(File folder) throws IllegalAccessException {
        if(!folder.isDirectory() || !folder.canRead())
            throw new IllegalAccessException(String.format("Folder '%s' is either not a directory or is now readable!",
                    folder));
    }

    private void checkFileRead(File file) throws IllegalAccessException {
        if(!file.isFile() || !file.canRead())
            throw new IllegalAccessException(String.format("Folder '%s' is either not a directory or is now readable!",
                    file));
    }

    @CommandLine.Option(names = {"-f", "--force"}, description = "Overwrite an existing backup set file.")
    private boolean force_ = false; // Assigned by picocli dynamically...

    @CommandLine.Option(paramLabel = "backup_set_name", names = {"-n", "--name"}, description = "Name of the backup set.",
            required = true)
    String name_;

    @CommandLine.Option(paramLabel = "destination_folder",names = {"-d", "--destination"},
            description = "Folder where the back will be placed.", required = true)
    File destinationFolder_;

    @CommandLine.Option(paramLabel = "key_file",names = {"-k", "--key-file"},
            description = "The encryption/decryption key used in the backup set.", required = true)
    File keyFile_;

    @CommandLine.Option(paramLabel = "set_file",names = {"-s", "--set-file"},
            description = "The encryption/decryption key used in the backup set.", required = true)
    File setFile_;

    @CommandLine.Parameters(paramLabel="source_folders", description = "Folder(s) to include in the backup set.",
            arity = "1..*")
    List<File> dataFolders_;
}