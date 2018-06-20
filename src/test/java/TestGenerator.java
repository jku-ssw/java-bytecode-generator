import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TestGenerator {
    boolean executeAndDeleteFile(String fileName, String... allowedExceptions) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("java " + fileName, null, new File("src/test/generated_test_files"));
        BufferedReader brIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader brErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = brIn.readLine()) != null) {
            System.out.println(line);
        }
        while ((line = brErr.readLine()) != null) {
            if(checkIfExceptionAllowed(line, allowedExceptions)) {
                System.out.println(line);
                continue;
            } else {
                new IOException("Execution of " + fileName + " failed " + "Not allowed exception: " + line);
            }
        }
        int exitCode = p.waitFor();
        if (exitCode != 0 && allowedExceptions.length == 0) {
            throw new IOException("Execution of " + fileName + " failed " + exitCode);
        } else {
            assertEquals(true, new File("src/test/generated_test_files/" + fileName + ".class").delete());
            brIn.close();
            brErr.close();
            return true;
        }
    }

    private boolean checkIfExceptionAllowed(String line, String[] allowedExceptions) {
        for(String exception: allowedExceptions) {
            if(line.contains(exception)) {
                return true;
            }
        }
        return false;
    }
}
