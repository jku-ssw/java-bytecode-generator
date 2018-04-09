package utils;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

public class ProbabilityParser {

    private static final String[] option_signatures = {"h", "f", "v", "ga", "la", "vtv", "m", "mc"};
    private final String[] args;
    private Options options = new Options();
    private Map<String, Integer> probabilities = new HashMap<>();


    public ProbabilityParser(String[] args) {
        this.args = args;
        this.options.addOption("h", "help", false, "Lists all options and how to use them");
        this.options.addOption("f", "field_probability", true, "Probability to generate Fields");
        this.options.addOption("v", "variable_probability", true, "Probability to generate variables");
        this.options.addOption("ga", "global_assign_probability", true, "Probability for assigning values to fields");
        this.options.addOption("la", "local_assign_probability", true, "Probability for assigning values to variables");
        this.options.addOption("vtv", "variable_to_variable_probability", true, "Probability for assigning variables to variables");
        this.options.addOption("m", "method_probability", true, "Probability to generate Methods");
        this.options.addOption("mc", "method_call_probability", true, "Probability to generate Method Calls");
    }

    /**
     * parses all input options and stores them in probabilities
     */
    public void parse() {
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
    }

    /**
     * Writes information about all Options to the Console
     */
    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ByteCodeGenerator", options);
        System.exit(0);
    }

    /**
     * @return the probability for generating Fields
     */
    public int getFieldProbability() {
        return probabilities.get("f");
    }

    /**
     * @return the probability for generating Variables
     */
    public int getVariableProbability() {
        return probabilities.get("v");
    }

    /**
     * @return the probability for generating assignments to global Variables
     */
    public int getGlobalAssignProbability() {
        return probabilities.get("ga");
    }

    /**
     * @return the probability for generating assignments to local Variables
     */
    public int getLocalAssignProbability() {
        return probabilities.get("la");
    }

    /**
     * @return the probability for generating assignments of Variables to Variables
     */
    public int getVariableToVariableAssignProbability() {
        return probabilities.get("vtv");
    }

    /**
     * @return the probability for generating Methods
     */
    public int getMethodProbability() {
        return probabilities.get("m");
    }

    /**
     * @return the probability for calling Methods
     */
    public int getMethodCallProbability() {
        return probabilities.get("mc");
    }

}
