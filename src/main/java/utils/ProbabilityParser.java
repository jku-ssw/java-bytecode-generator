package utils;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

public class ProbabilityParser {

    private static final String[] option_signatures = {"h", "f", "fa", "m", "mc"};
    private String[] args = null;
    private Options options = new Options();
    private Map<String, Integer> probabilities = new HashMap<>();


    public ProbabilityParser(String[] args) {
        this.args = args;
        this.options.addOption("h", "help", false, "Lists all options and how to use them");
        this.options.addOption("f", "field_probability", true, "Probability to generate Fields");
        this.options.addOption("fa", "field_access_probability", true, "Probability to generate access to Fields");
        this.options.addOption("m", "method_probability", true, "Probability to generate Methods");
        this.options.addOption("mc", "method_call_probability", true, "Probability to generate Method Calls");
    }

    public int[] parse() {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help();
                System.exit(0);
            }

            for (String signature : option_signatures) {
                if (cmd.hasOption(signature)) {
                    System.out.println("Using argument " + options.getOption(signature).getLongOpt() + " = " + cmd.getOptionValue(signature));
                    int value = Integer.parseInt(cmd.getOptionValue(signature));
                    probabilities.put(signature, value);
                } else {
                    probabilities.put(signature, 50);
                    System.out.println("Using default value 50 for " + options.getOption(signature).getLongOpt());
                }
            }
        } catch (ParseException e) {
            System.err.println("Failed to parse command line properties");
            e.printStackTrace();
        }
        return null;
    }

    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ByteCodeGenerator", options);
        System.exit(0);
    }

    public int getFieldProbability() {
        return probabilities.get("f");
    }

    public int getFieldAccessProbabilty() {
        return probabilities.get("fa");
    }

    public int getMethodProbability() {
        return probabilities.get("m");
    }

    public int getMethodCallProbability() {
        return probabilities.get("mc");
    }

}
