package cli;

import org.apache.commons.cli.*;

import java.util.HashMap;
import java.util.Map;

public class ControlValueParser {

    private final String[] args;
    private final Options options = initOptions();

    private static Options initOptions() {
        Options options = new Options();
        options.addOption(CLIOptions.L.toString(), "program_length_weighting",
                true, "The maximum number of iterations for program-generation");
        options.addOption(CLIOptions.H.toString(), "help",
                false, "Lists all options and how to use them");
        options.addOption(CLIOptions.F.toString(), "field_probability",
                true, "The probability to generate fields");
        options.addOption(CLIOptions.LV.toString(), "variable_probability",
                true, "The probability to generate local variables");
        options.addOption(CLIOptions.GA.toString(), "global_assign_probability",
                true, "The probability for assigning values to fields");
        options.addOption(CLIOptions.LA.toString(), "local_assign_probability",
                true, "The probability for assigning values to variables");
        options.addOption(CLIOptions.M.toString(), "method_probability",
                true, "The probability to generate methods");
        options.addOption(CLIOptions.MC.toString(), "method_call_probability",
                true, "The probability to generate method-malls");
        options.addOption(CLIOptions.ML.toString(), "method_length_weighting",
                true, "The maximum number of iterations for method-generation");
        options.addOption(CLIOptions.MP.toString(), "maximum_parameters",
                true, "The maximum number of parameters a method can have");
        options.addOption(CLIOptions.MO.toString(), "method_overload",
                true, "The probability for overloading methods");
        options.addOption(CLIOptions.P.toString(), "print_probability",
                true, "The probability to generate print-statements");
        options.addOption(CLIOptions.JLM.toString(), "java_lang_math_probability",
                true, "The probability to call methods of java.lang.Math");
        options.addOption(CLIOptions.CF.toString(), "control_flow_probability",
                true, "the Probability to generate control flow statements");
        options.addOption(CLIOptions.CL.toString(), "control_length_weighting",
                true, "The maximum number of iterations for control-block generation");
        options.addOption(CLIOptions.CD.toString(), "control_deepness",
                true, "The maximum deepness, to which control-flow-statements can be nested");
        options.addOption(CLIOptions.MLI.toString(), "maximum_loop_iterations",
                true, "The maximum number of iterations for while-, doWhile- or for-loops");
        options.addOption(CLIOptions.WHILE.toString(), "while_probability",
                true, "The probability to generate while-loops");
        options.addOption(CLIOptions.FOR.toString(), "for_probability",
                true, "The probability to generate for-loops");
        options.addOption(CLIOptions.DOWHILE.toString(), "doWhile_probability",
                true, "The probability to generate doWhile-loops");
        options.addOption(CLIOptions.IF.toString(), "if_probability",
                true, "The probability to generate an if-statement");
        options.addOption(CLIOptions.IBF.toString(), "if_branching_factor",
                true, "The maximum branching-factor for if-statements");
        options.addOption(CLIOptions.OF.toString(), "overflow_avoidance",
                true, "The probability to avoid Overflow-Exceptions");
        return options;
    }

    private Map<String, Integer> defaultValues = initDefaultValues();

    private static Map<String, Integer> initDefaultValues() {
        Map<String, Integer> defaultValues = new HashMap<>();
        defaultValues.put(CLIOptions.L.toString(), 8);
        defaultValues.put(CLIOptions.F.toString(), 30);
        defaultValues.put(CLIOptions.LV.toString(), 10);
        defaultValues.put(CLIOptions.GA.toString(), 40);
        defaultValues.put(CLIOptions.LA.toString(), 60);
        defaultValues.put(CLIOptions.M.toString(), 30);
        defaultValues.put(CLIOptions.MC.toString(), 100);
        defaultValues.put(CLIOptions.ML.toString(), 4);
        defaultValues.put(CLIOptions.MP.toString(), 10);
        defaultValues.put(CLIOptions.MO.toString(), 70);
        defaultValues.put(CLIOptions.P.toString(), 2);
        defaultValues.put(CLIOptions.JLM.toString(), 100);
        defaultValues.put(CLIOptions.CF.toString(), 70);
        defaultValues.put(CLIOptions.CL.toString(), 2);
        defaultValues.put(CLIOptions.CD.toString(), 4);
        defaultValues.put(CLIOptions.MLI.toString(), 10);
        defaultValues.put(CLIOptions.WHILE.toString(), 100);
        defaultValues.put(CLIOptions.FOR.toString(), 100);
        defaultValues.put(CLIOptions.DOWHILE.toString(), 100);
        defaultValues.put(CLIOptions.IF.toString(), 100);
        defaultValues.put(CLIOptions.IBF.toString(), 4);
        defaultValues.put(CLIOptions.OF.toString(), 100);
        return defaultValues;
    }

    public ControlValueParser(String[] args) {
        this.args = args;
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
            if (cmd.hasOption(CLIOptions.H.toString())) {
                help();
            }
            for (CLIOptions optionKind: CLIOptions.values()) {
                if (optionKind == CLIOptions.H) continue;
                String signature = optionKind.toString();
                if (cmd.hasOption(signature)) {
                    System.out.println("Using argument " + options.getOption(signature).getLongOpt() + " = " +
                            cmd.getOptionValue(signature));
                    int value = Integer.parseInt(cmd.getOptionValue(signature));
                    generationController.addControlValue(optionKind, value);
                } else {
                    generationController.addControlValue(optionKind, defaultValues.get(signature));
                    System.out.println("Using default value " + defaultValues.get(signature) + " for " +
                            options.getOption(signature).getLongOpt());
                }
            }
        } catch (ParseException e) {
            throw new AssertionError(e);
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
