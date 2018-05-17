import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TestGenerator {
    boolean executeAndDeleteFile(String fileName) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("java " + fileName, null, new File("src/test/generated_test_files"));
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        int exitCode = p.waitFor();
        if (exitCode != 0) {
            throw new IOException("Execution of " + fileName + " failed " + exitCode);
        } else {
            assertEquals(true, new File("src/test/generated_test_files/" + fileName + ".class").delete());
            return true;
        }
    }
}
