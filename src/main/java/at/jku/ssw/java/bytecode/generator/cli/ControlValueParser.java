package at.jku.ssw.java.bytecode.generator.cli;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static at.jku.ssw.java.bytecode.generator.cli.CLIOptions.OF;

public class ControlValueParser {

    private static final Logger logger = LogManager.getLogger();

    private final String[] args;
    private final Options options = initOptions();
    private static final String DEFAULT_FILENAME = "MyGeneratedClass";
    private static final String DEFAULT_DIRECTORY = "Working Directory";

    private static Options initOptions() {
        Options options = new Options();
        options.addOption(CLIOptions.L.toString(), "program_length",
                true, "The maximum number of iterations for program-generation");
        options.addOption(CLIOptions.H.toString(), "help",
                false, "Lists all options and how to use them");
        options.addOption(CLIOptions.F.toString(), "field",
                true, "The probability to generate fields");
        options.addOption(CLIOptions.LV.toString(), "local_variable",
                true, "The probability to generate local variables");
        options.addOption(CLIOptions.GA.toString(), "global_assign",
                true, "The probability for assigning values to fields");
        options.addOption(CLIOptions.LA.toString(), "local_assign",
                true, "The probability for assigning values to local variables");
        options.addOption(CLIOptions.M.toString(), "method",
                true, "The probability to generate methods");
        options.addOption(CLIOptions.MC.toString(), "method_call",
                true, "The probability to generate method-calls");
        options.addOption(CLIOptions.ML.toString(), "method_length",
                true, "The maximum number of iterations for method-generation");
        options.addOption(CLIOptions.MP.toString(), "maximum_parameters",
                true, "The maximum number of parameters a method can have");
        options.addOption(CLIOptions.MO.toString(), "method_overload",
                true, "The probability for overloading methods");
        options.addOption(CLIOptions.P.toString(), "print",
                true, "The probability to generate print-statements");
        options.addOption(CLIOptions.JLM.toString(), "java_lang_math",
                true, "The probability to call methods of java.lang.Math");
        options.addOption(CLIOptions.CF.toString(), "control_flow",
                true, "the Probability to generate control flow statements");
        options.addOption(CLIOptions.CL.toString(), "control_length",
                true, "The maximum number of iterations for control-block generation");
        options.addOption(CLIOptions.CD.toString(), "control_deepness",
                true, "The maximum deepness, to which control-flow-statements can be nested");
        options.addOption(CLIOptions.MLI.toString(), "maximum_loop_iterations",
                true, "The maximum number of iterations for while-, doWhile- or for-loops");
        options.addOption(CLIOptions.WHILE.toString(), "while_loop",
                true, "The probability to generate while-loops");
        options.addOption(CLIOptions.FOR.toString(), "for_loop",
                true, "The probability to generate for-loops");
        options.addOption(CLIOptions.DOWHILE.toString(), "doWhile_loop",
                true, "The probability to generate doWhile-loops");
        options.addOption(CLIOptions.IF.toString(), "if_statement",
                true, "The probability to generate an if-statement");
        options.addOption(CLIOptions.IBF.toString(), "if_branching_factor",
                true, "The maximum branching-factor for if-statements");
        options.addOption(OF.toString(), "overflow",
                false, "Disable avoidance of overflow exceptions in the generated file");
        options.addOption(CLIOptions.DZ.toString(), "divided_by_zero",
                false, "Disable avoidance of divided_by_zero-exceptions in the generated file");
        options.addOption(CLIOptions.OS.toString(), "operator_statement",
                true, "The probability to generate statements using operators");
        options.addOption(CLIOptions.AS.toString(), "arithmetic",
                true, "The probability to generate statements using arithmetic operators");
        options.addOption(CLIOptions.LS.toString(), "logical_statement",
                true, "The probability to generate statements using logical operators");
        options.addOption(CLIOptions.BS.toString(), "bitwise_statement",
                true, "The probability to generate statements using bitwise operators");
        options.addOption(CLIOptions.ALS.toString(), "arithmetic_logical_statement",
                true, "The probability to generate statements using arithmetic and logical operators");
        options.addOption(CLIOptions.ABS.toString(), "arithmetic_bitwise_statement",
                true, "The probability to generate statements using arithmetic and bitwise operators");
        options.addOption(CLIOptions.LBS.toString(), "logical_bitwise_statement",
                true, "The probability to generate statements using logical and bitwise operators");
        options.addOption(CLIOptions.ALBS.toString(), "arithmetic_logical_bitwise_statement",
                true, "The probability to generate statements using arithmetic, logical and bitwise operators");
        options.addOption(CLIOptions.MOPS.toString(), "maximum_operators",
                true, "The maximum number of operators in an operator statement. Not exactly true with mixed operator types.");
        options.addOption(CLIOptions.XRUNS.toString(), "calls_of_run_method",
                true, "Allows to call the run method of this program additionally the provided number of times");
        options.addOption(CLIOptions.SNIPPET.toString(), "snippet_generation",
                true, "The probability to generate fixed code snippets");
        options.addOption(CLIOptions.BREAK.toString(), "break",
                true, "The probability to insert break statements");
        options.addOption(CLIOptions.RETURN.toString(), "return",
                true, "The probability to insert preemptive return statements");
        options.addOption(CLIOptions.FILENAME.toString(), "filename",
                true, "The name of the class-file generated by JBGenerator");
        options.addOption(CLIOptions.DIRECTORY.toString(), "directory",
                true, "The location to which the generated class-file is written");
        options.addOption(CLIOptions.PRIMITIVES.toString(), "primitives",
                true, "The probability to use a primitive type");
        options.addOption(CLIOptions.OBJECTS.toString(), "objects",
                true, "The probability to use a class type");
        options.addOption(CLIOptions.ARRAYS.toString(), "arrays",
                true, "The probability to use array types");
        options.addOption(CLIOptions.VOID.toString(), "void",
                true, "The probability to use void returns");
        options.addOption(CLIOptions.MAXDIM.toString(), "max_dim",
                true, "The maximum number of dimensions that created arrays are allowed to have");
        options.addOption(CLIOptions.MAXDIMSIZE.toString(), "max_dim_size",
                true, "The maximum number of elements per array dimension");
        options.addOption(CLIOptions.CAST.toString(), "cast",
                true, "The probability to insert typecasts");
        options.addOption(CLIOptions.SEED.toString(), "seed",
                true, "The seed to use for the random generation");
        return options;
    }

    private Map<String, Integer> defaultValues = initDefaultValues();

    private static Map<String, Integer> initDefaultValues() {
        Map<String, Integer> defaultValues = new HashMap<>();
        defaultValues.put(CLIOptions.L.toString(), 3);
        defaultValues.put(CLIOptions.F.toString(), 50);
        defaultValues.put(CLIOptions.LV.toString(), 50);
        defaultValues.put(CLIOptions.GA.toString(), 30);
        defaultValues.put(CLIOptions.LA.toString(), 30);
        defaultValues.put(CLIOptions.M.toString(), 60);
        defaultValues.put(CLIOptions.MC.toString(), 100);
        defaultValues.put(CLIOptions.ML.toString(), 3);
        defaultValues.put(CLIOptions.MP.toString(), 5);
        defaultValues.put(CLIOptions.MO.toString(), 0);
        defaultValues.put(CLIOptions.P.toString(), 5);
        defaultValues.put(CLIOptions.JLM.toString(), 100);
        defaultValues.put(CLIOptions.CF.toString(), 40);
        defaultValues.put(CLIOptions.CL.toString(), 2);
        defaultValues.put(CLIOptions.CD.toString(), 4);
        defaultValues.put(CLIOptions.MLI.toString(), 1);
        defaultValues.put(CLIOptions.WHILE.toString(), 100);
        defaultValues.put(CLIOptions.FOR.toString(), 100);
        defaultValues.put(CLIOptions.DOWHILE.toString(), 100);
        defaultValues.put(CLIOptions.IF.toString(), 100);
        defaultValues.put(CLIOptions.IBF.toString(), 3);
        defaultValues.put(CLIOptions.OS.toString(), 50);
        defaultValues.put(CLIOptions.AS.toString(), 100);
        defaultValues.put(CLIOptions.LS.toString(), 100);
        defaultValues.put(CLIOptions.BS.toString(), 100);
        defaultValues.put(CLIOptions.ALS.toString(), 100);
        defaultValues.put(CLIOptions.ABS.toString(), 100);
        defaultValues.put(CLIOptions.LBS.toString(), 100);
        defaultValues.put(CLIOptions.ALBS.toString(), 100);
        defaultValues.put(CLIOptions.MOPS.toString(), 7);
        defaultValues.put(CLIOptions.XRUNS.toString(), 1);
        defaultValues.put(CLIOptions.SNIPPET.toString(), 5);
        defaultValues.put(CLIOptions.BREAK.toString(), 5);
        defaultValues.put(CLIOptions.RETURN.toString(), 5);
        defaultValues.put(CLIOptions.PRIMITIVES.toString(), 60);
        defaultValues.put(CLIOptions.OBJECTS.toString(), 20);
        defaultValues.put(CLIOptions.ARRAYS.toString(), 20);
        defaultValues.put(CLIOptions.VOID.toString(), 20);
        defaultValues.put(CLIOptions.MAXDIM.toString(), 3);
        defaultValues.put(CLIOptions.MAXDIMSIZE.toString(), 1_000);
        defaultValues.put(CLIOptions.CAST.toString(), 10);
        defaultValues.put(CLIOptions.SEED.toString(), new Random().nextInt());
        return defaultValues;
    }

    public ControlValueParser(String[] args) {
        this.args = args;
    }

    public GenerationController parse() {
        GenerationController generationController = new GenerationController();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption(CLIOptions.H.toString())) {
                help();
            }
            for (CLIOptions optionKind : CLIOptions.values()) {
                if (optionKind == CLIOptions.H) continue;
                String signature = optionKind.toString();
                if (cmd.hasOption(signature)) {
                    switch (optionKind) {
                        case FILENAME:
                            generationController.setFileName(cmd.getOptionValue(signature));
                            logger.debug("FILENAME: {}", cmd.getOptionValue(signature));
                            break;
                        case DIRECTORY:
                            generationController.setLocation(cmd.getOptionValue(signature));
                            logger.debug("Writing file to directory: {}", cmd.getOptionValue(signature));
                            break;
                        case OF:
                            generationController.setAvoidOverflows(false);
                            logger.debug("Not avoiding Overflows");
                            break;
                        case DZ:
                            generationController.setAvoidDivByZero(false);
                            logger.debug("Not avoiding Divisions by zero");
                            break;
                        default:
                            int value = Integer.parseInt(cmd.getOptionValue(signature));
                            generationController.addControlValue(optionKind, value);
                            logger.debug(
                                    "Using argument {} = {}",
                                    options.getOption(signature).getLongOpt(),
                                    cmd.getOptionValue(signature));
                    }
                } else {
                    switch (optionKind) {
                        case FILENAME:
                            generationController.setFileName(DEFAULT_FILENAME);
                            logger.debug("Default filename: {}", DEFAULT_FILENAME);
                            break;
                        case DIRECTORY:
                            logger.debug("Default directory: {}", DEFAULT_DIRECTORY);
                            break;
                        case OF:
                            logger.debug("Avoiding Overflows (use -of to disable)");
                            break;
                        case DZ:
                            logger.debug("Avoiding Divisions by zero (use -dz to disable)");
                            break;
                        default:
                            generationController.addControlValue(optionKind, defaultValues.get(signature));
                            logger.debug(
                                    "Using default value {} for {}",
                                    defaultValues.get(signature),
                                    options.getOption(signature).getLongOpt());
                    }
                }
            }
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
        return generationController;
    }


    private void help() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("jb_generator", options);
        System.exit(0);
    }
}
