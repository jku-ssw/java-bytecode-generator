package cli;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

public class ControlValueParser {

    private final String[] args;
    private Options options = initOptions();

    private static Options initOptions() {
        Options options = new Options();
        options.addOption("l", "program_length_weighting", true, "The maximum number of iterations for program-generation");
        options.addOption("h", "help", false, "Lists all options and how to use them");
        options.addOption("f", "field_probability", true, "The probability to generate fields");
        options.addOption("lv", "variable_probability", true, "The probability to generate local variables");
        options.addOption("ga", "global_assign_probability", true, "The probability for assigning values to fields");
        options.addOption("la", "local_assign_probability", true, "The probability for assigning values to variables");
        options.addOption("m", "method_probability", true, "The probability to generate methods");
        options.addOption("mc", "method_call_probability", true, "The probability to generate method-malls");
        options.addOption("ml", "method_length_weighting", true, "The maximum number of iterations for method-generation");
        options.addOption("mp", "maximum_parameters", true, "The maximum number of parameters a method can have");
        options.addOption("mo", "method_overload", true, "The probability for overloading methods");
        options.addOption("p", "print_probability", true, "The probability to generate print-statements");
        options.addOption("jlm", "java_lang_math_probability", true, "The probability to call methods of java.lang.Math");
        options.addOption("cf", "control_flow_probability", true, "the Probability to generate control flow statements");
        options.addOption("cl", "control_length", true, "The maximum number of iterations for control-block generation");
        options.addOption("cd", "control_deepness", true, "The maximum deepness, to which control-flow-statements can be nested");
        options.addOption("mli", "maximum_loop_iterations", true, "The maximum number of iterations for while-, doWhile- or for-loops");
        options.addOption("while", "while_probability", true, "The probability to generate while-loops");
        options.addOption("for", "for_probability", true, "The probability to generate for-loops");
        options.addOption("doWhile", "doWhile_probability", true, "The probability to generate doWhile-loops");
        options.addOption("if", "if_probability", true, "The probability to generate an if-statement");
        options.addOption("ibf", "if_branching_factor", true, "The maximum branching-factor for if-statements");
        return options;
    }

    private Map<String, Integer> defaultValues = initDefaultValues();

    private static Map<String, Integer> initDefaultValues() {
        Map<String, Integer> defaultValues = new HashMap<>();
        defaultValues.put("l", 8);
        defaultValues.put("f", 30);
        defaultValues.put("lv", 10);
        defaultValues.put("ga", 40);
        defaultValues.put("la", 60);
        defaultValues.put("m", 30);
        defaultValues.put("mc", 100);
        defaultValues.put("ml", 4);
        defaultValues.put("mp", 10);
        defaultValues.put("mo", 70);
        defaultValues.put("p", 2);
        defaultValues.put("jlm", 100);
        defaultValues.put("cf", 70);
        defaultValues.put("cl", 2);
        defaultValues.put("cd", 4);
        defaultValues.put("mli", 10);
        defaultValues.put("while", 100);
        defaultValues.put("for", 100);
        defaultValues.put("doWhile", 100);
        defaultValues.put("if", 100);
        defaultValues.put("ibf", 4);
        return defaultValues;
    }

    public ControlValueParser(String[] args) {
        this.args = args;
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
                if (signature.equals("h")) continue;
                if (cmd.hasOption(signature)) {
                    System.out.println("Using argument " + options.getOption(signature).getLongOpt() + " = " + cmd.getOptionValue(signature));
                    int value = Integer.parseInt(cmd.getOptionValue(signature));
                    generationController.addControlValue(signature, value);
                } else {
                    generationController.addControlValue(signature, defaultValues.get(signature));
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
