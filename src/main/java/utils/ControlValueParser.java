package utils;

import org.apache.commons.cli.*;

public class ControlValueParser {

    private final String[] args;
    private Options options = new Options();


    public ControlValueParser(String[] args) {
        this.args = args;
        this.options.addOption("l", "program_length_weighting", true, "The maximum number of Iterations for Program-Generation");
        this.options.addOption("h", "help", false, "Lists all options and how to use them");
        this.options.addOption("f", "field_probability", true, "The Probability to generate Fields");
        this.options.addOption("v", "variable_probability", true, "The Probability to generate variables");
        this.options.addOption("ga", "global_assign_probability", true, "The Probability for assigning values to fields");
        this.options.addOption("la", "local_assign_probability", true, "The Probability for assigning values to variables");
        this.options.addOption("vtv", "variable_to_variable_probability", true, "The Probability for assigning variables to variables");
        this.options.addOption("m", "method_probability", true, "THe Probability to generate Methods");
        this.options.addOption("mc", "method_call_probability", true, "The Probability to generate Method Calls");
        this.options.addOption("ml", "method_length_weighting", true, "The maximum number of Iterations for Method-Generation");
        this.options.addOption("mp", "maximum_parameters", true, "The maximum number of parameters a Method can have");
        this.options.addOption("mo", "method_overload", true, "The Probability for overloading methods");
        this.options.addOption("p", "print_probability", true, "The Probability to generate Print-Statements");
        this.options.addOption("jlm", "java_lang_math_probability", true, "The Probability to call methods of java.lang.Math");
        this.parse();
    }

    /**
     * parses all Input-Options and stores them in generationController
     */
    public GenerationController parse() {
        GenerationController generationController = new GenerationController();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                help();
                System.exit(0);
            }
            for (Option option : this.options.getOptions()) {
                String signature = option.getOpt();
                if (cmd.hasOption(signature)) {
                    System.out.println("Using argument " + options.getOption(signature).getLongOpt() + " = " + cmd.getOptionValue(signature));
                    int value = Integer.parseInt(cmd.getOptionValue(signature));
                    generationController.addControlValue(signature, value);
                } else {
                    generationController.addControlValue(signature, 50);
                    System.out.println("Using default value 50 for " + options.getOption(signature).getLongOpt());
                }
            }
        } catch (ParseException e) {
            System.err.println("Failed to parse command line properties");
            e.printStackTrace();
        }
        return generationController;
    }

    /**
     * Writes information about all Options to the Console
     */
    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ByteCodeGenerator", options);
        System.exit(0);
    }
}
