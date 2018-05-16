package utils.cli;

import java.util.HashMap;
import java.util.Map;

public class GenerationController {
    private Map<String, Integer> controlValues = new HashMap<>();

    void addControlValue(String signature, int value) {
        controlValues.put(signature, value);
    }

    /**
     * @return the probability for generating Fields
     */
    public int getFieldProbability() {
        return controlValues.get("f");
    }

    /**
     * @return the probability for generating Variables
     */
    public int getLocalVariableProbability() {
        return controlValues.get("lv");
    }

    /**
     * @return the probability for generating assignments to global Variables
     */
    public int getGlobalAssignProbability() {
        return controlValues.get("ga");
    }

    /**
     * @return the probability for generating assignments to local Variables
     */
    public int getLocalAssignProbability() {
        return controlValues.get("la");
    }

    /**
     * @return the probability for generating Methods
     */
    public int getMethodProbability() {
        return controlValues.get("m");
    }

    /**
     * @return the probability for calling Methods
     */
    public int getMethodCallProbability() {
        return controlValues.get("mc");
    }

    public int getPrintProbability() {
        return controlValues.get("p");
    }

    public int getProgramLengthWeighting() {
        return controlValues.get("l");
    }

    public int getMethodLengthWeighting() {
        return controlValues.get("ml");
    }

    public int getMaximumMethodParameters() {
        return controlValues.get("mp");
    }

    public int getMethodOverloadProbability() {
        return controlValues.get("mo");
    }

    public int getJavaLangMathProbalility() {
        return controlValues.get("jlm");
    }

    public int getControlFlowProbability() {
        return controlValues.get("cf");
    }

    public int getControlLengthWeighting() {
        return controlValues.get("cl");
    }

    public int getControlFlowDeepness() {
        return controlValues.get("cd");
    }
}
