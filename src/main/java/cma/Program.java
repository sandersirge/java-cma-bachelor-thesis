package cma;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instruction = instructions.get(i);
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Label, Integer> labelEntry : labels.entrySet()) {
                if (labelEntry.getValue() == i)
                    builder.append(labelEntry.getKey()).append(": ");
            }
            builder.append(instruction.toString());
            joiner.add(builder.toString());
        }
        return joiner.toString();
    }
}
