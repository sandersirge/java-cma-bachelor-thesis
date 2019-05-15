package cma;

import java.util.List;
import java.util.Map;

public class Program {

    private final List<Instruction> instructions;
    private final Map<String, Integer> labels;

    public Program(List<Instruction> instructions, Map<String, Integer> labels) {
        this.instructions = instructions;
        this.labels = labels;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public Map<String, Integer> getLabels() {
        return labels;
    }
}
