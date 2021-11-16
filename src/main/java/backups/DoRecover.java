package backups;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "do-recover",
        description = "Recover backup-ed files from the index file saved with the encrypted files.",
        mixinStandardHelpOptions = true)
public class DoRecover implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return null;
    }
}
