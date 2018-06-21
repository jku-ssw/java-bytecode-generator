import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TestGenerator {
    boolean executeFile(String fileName, String... allowedExceptions) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("java " + fileName, null, new File("src/test/resources/generated_test_files"));
        BufferedReader brIn = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader brErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String line;
        while ((line = brIn.readLine()) != null) {
            System.out.println(line);
        }
        while ((line = brErr.readLine()) != null) {
            if(!line.contains("Exception")) continue;
            if(checkIfExceptionAllowed(line, allowedExceptions)) {
                continue;
            } else {
                throw new IOException("Execution of " + fileName + " failed.\nNot allowed exception: " + line);
            }
        }
        int exitCode = p.waitFor();
        if (exitCode != 0 && allowedExceptions.length == 0) {
            throw new IOException("Execution of " + fileName + " failed " + exitCode);
        } else {
            //assertEquals(true, new File("src/test/generated_test_files/" + fileName + ".class").delete());
            brIn.close();
            brErr.close();
            return true;
        }
    }

    private boolean checkIfExceptionAllowed(String line, String[] allowedExceptions) {
        for(String exception: allowedExceptions) {
            System.out.println(exception);
            System.out.println(line);
            if(line.contains(exception)) {
                return true;
            }
        }
        return false;
    }
}
