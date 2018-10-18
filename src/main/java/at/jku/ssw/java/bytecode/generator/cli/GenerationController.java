package at.jku.ssw.java.bytecode.generator.cli;

import java.util.HashMap;
import java.util.Map;

public class GenerationController {
    private final Map<CLIOptions, Integer> controlValues = new HashMap<>();

    private boolean avoidOverflows = true;
    private boolean avoidDivByZero = true;

    private String fileName;
    private String location;

    void addControlValue(CLIOptions option, int value) {
        controlValues.put(option, value);
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    void setLocation(String location) {
        this.location = location;
    }

    public boolean avoidOverflows() {
        return avoidOverflows;
    }

    public void setAvoidOverflows(boolean avoidOverflows) {
        this.avoidOverflows = avoidOverflows;
    }

    public boolean avoidDivByZero() {
        return avoidDivByZero;
    }

    public void setAvoidDivByZero(boolean avoidDivByZero) {
        this.avoidDivByZero = avoidDivByZero;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLocation() {
        return location;
    }

    public int getLocalVariableProbability() {
        return controlValues.get(CLIOptions.LV);
    }

    public int getFieldProbability() {
        return controlValues.get(CLIOptions.F);
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

    public int getOperatorStatementProbability() {
        return controlValues.get(CLIOptions.OS);
    }

    public int getArithmeticProbability() {
        return controlValues.get(CLIOptions.AS);
    }

    public int getLogicalProbability() {
        return controlValues.get(CLIOptions.LS);
    }

    public int getBitwiseProbability() {
        return controlValues.get(CLIOptions.BS);
    }

    public int getArithmeticLogicalProbability() {
        return controlValues.get(CLIOptions.ALS);
    }

    public int getArithmeticBitwiseProbability() {
        return controlValues.get(CLIOptions.ABS);
    }

    public int getLogicBitwiseProbability() {
        return controlValues.get(CLIOptions.LBS);
    }

    public int getArithmeticLogicalBitwiseProbability() {
        return controlValues.get(CLIOptions.ALBS);
    }

    public int getMaxOperators() {
        return controlValues.get(CLIOptions.MOPS);
    }

    public int executeRunXTimes() {
        return controlValues.get(CLIOptions.XRUNS);
    }

    public int getSnippetProbability() {
        return controlValues.get(CLIOptions.SNIPPET);
    }

    public int getBreakProbability() {
        return controlValues.get(CLIOptions.BREAK);
    }

    public int getPreemptiveReturnProbability() {
        return controlValues.get(CLIOptions.RETURN);
    }

    public int getPrimitiveTypesProbability() {
        return controlValues.get(CLIOptions.PRIMITIVES);
    }

    public int getObjectProbability() {
        return controlValues.get(CLIOptions.OBJECTS);
    }

    public int getArrayProbability() {
        return controlValues.get(CLIOptions.ARRAYS);
    }

    public int getVoidProbability() {
        return controlValues.get(CLIOptions.VOID);
    }

    public int getMaxArrayDimensions() {
        return controlValues.get(CLIOptions.MAXDIM);
    }

    public int getMaxArrayDimensionSize() {
        return controlValues.get(CLIOptions.MAXDIMSIZE);
    }

    public int getTypeCastProbability() {
        return controlValues.get(CLIOptions.CAST);
    }

    public int getSeedValue() {
        return controlValues.get(CLIOptions.SEED);
    }
}
