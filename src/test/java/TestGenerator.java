import java.io.*;

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

    public int run(String clazz) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("java", clazz);
        pb.redirectError();
        pb.directory(new File("src"));
        Process p = pb.start();
        InputStreamConsumer consumer = new InputStreamConsumer(p.getInputStream());
        consumer.start();

        int result = p.waitFor();

        consumer.join();

        System.out.println(consumer.getOutput());

        return result;
    }

    public class InputStreamConsumer extends Thread {

        private InputStream is;
        private IOException exp;
        private StringBuilder output;

        public InputStreamConsumer(InputStream is) {
            this.is = is;
        }

        @Override
        public void run() {
            int in = -1;
            output = new StringBuilder(64);
            try {
                while ((in = is.read()) != -1) {
                    output.append((char) in);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                exp = ex;
            }
        }

        public StringBuilder getOutput() {
            return output;
        }

        public IOException getException() {
            return exp;
        }
    }
}
