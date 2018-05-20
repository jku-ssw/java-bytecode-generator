package cli;

import org.apache.commons.cli.*;

public class ControlValueParser {

    private final String[] args;
    private Options options = new Options();


    public ControlValueParser(String[] args) {
        this.args = args;
        this.options.addOption("l", "program_length_weighting", true, "The maximum number of iterations for program-Generation");
        this.options.addOption("h", "help", false, "Lists all options and how to use them");
        this.options.addOption("f", "field_probability", true, "The probability to generate fields");
        this.options.addOption("lv", "variable_probability", true, "The probability to generate variables");
        this.options.addOption("ga", "global_assign_probability", true, "The probability for assigning values to fields");
        this.options.addOption("la", "local_assign_probability", true, "The probability for assigning values to variables");
        this.options.addOption("m", "method_probability", true, "The probability to generate methods");
        this.options.addOption("mc", "method_call_probability", true, "The probability to generate method-malls");
        this.options.addOption("ml", "method_length_weighting", true, "The maximum number of iterations for method-generation");
        this.options.addOption("mp", "maximum_parameters", true, "The maximum number of parameters a method can have");
        this.options.addOption("mo", "method_overload", true, "The probability for overloading methods");
        this.options.addOption("p", "print_probability", true, "The probability to generate print-Statements");
        this.options.addOption("jlm", "java_lang_math_probability", true, "The probability to call methods of java.lang.Math");
        this.options.addOption("cf", "control_flow_probability", true, "the Probability to generate control flow statements");
        this.options.addOption("cl", "control_length", true, "The maximum number of iterations for control-block generation");
        this.options.addOption("cd", "control_deepness", true, "The maximum deepness, to which control-flow-statements can be nested");
        this.options.addOption("ibf", "if_branching_factor", true, "The maximum branching_factor for if-statements");
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
                if(signature.equals("h")) continue;
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
     * Writes information about all Options to the console
     */
    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("jb_generator", options);
        System.exit(0);
    }
}
