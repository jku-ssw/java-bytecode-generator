package cli;

import java.util.HashMap;
import java.util.Map;

public class GenerationController {
    private final Map<CLIOptions, Integer> controlValues = new HashMap<>();

    void addControlValue(CLIOptions option, int value) {
        controlValues.put(option, value);
    }

    public int getFieldProbability() {
        return controlValues.get(CLIOptions.F);
    }

    public int getLocalVariableProbability() {
        return controlValues.get(CLIOptions.LV);
    }

    public int getGlobalAssignProbability() {
        return controlValues.get(CLIOptions.GA);
    }

    public int getLocalAssignProbability() {
        return controlValues.get(CLIOptions.LA);
    }

    public int getMethodProbability() {
        return controlValues.get(CLIOptions.M);
    }

    public int getMethodCallProbability() {
        return controlValues.get(CLIOptions.MC);
    }

    public int getPrintProbability() {
        return controlValues.get(CLIOptions.P);
    }

    public int getProgramLengthWeighting() {
        return controlValues.get(CLIOptions.L);
    }

    public int getMethodLengthWeighting() {
        return controlValues.get(CLIOptions.ML);
    }

    public int getMaximumMethodParameters() {
        return controlValues.get(CLIOptions.MP);
    }

    public int getMethodOverloadProbability() {
        return controlValues.get(CLIOptions.MO);
    }

    public int getJavaLangMathProbability() {
        return controlValues.get(CLIOptions.JLM);
    }

    public int getControlFlowProbability() {
        return controlValues.get(CLIOptions.CF);
    }

    public int getControlLengthWeighting() {
        return controlValues.get(CLIOptions.CL);
    }

    public int getControlFlowDeepness() {
        return controlValues.get(CLIOptions.CD);
    }

    public int getIfBranchingFactor() {
        return controlValues.get(CLIOptions.IBF);
    }

    public int getMaxLoopIterations() {
        return controlValues.get(CLIOptions.MLI);
    }

    public int getWhileProbability() {
        return controlValues.get(CLIOptions.WHILE);
    }

    public int getForProbability() {
        return controlValues.get(CLIOptions.FOR);
    }

    public int getDoWhileProbability() {
        return controlValues.get(CLIOptions.DOWHILE);
    }

    public int getIfProbability() {
        return controlValues.get(CLIOptions.IF);
    }

    public int getAvoidOverFlowProbability() {
        return controlValues.get(CLIOptions.OF);
    }
}
