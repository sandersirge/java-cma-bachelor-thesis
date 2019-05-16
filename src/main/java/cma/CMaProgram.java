package cma;

import cma.instruction.CMaInstruction;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class CMaProgram {

    private final List<CMaInstruction> instructions;
    private final Map<CMaLabel, Integer> labels;

    public CMaProgram(List<CMaInstruction> instructions, Map<CMaLabel, Integer> labels) {
        this.instructions = instructions;
        this.labels = labels;
    }

    public List<CMaInstruction> getInstructions() {
        return instructions;
    }

    public Map<CMaLabel, Integer> getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < instructions.size(); i++) {
            CMaInstruction instruction = instructions.get(i);
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<CMaLabel, Integer> labelEntry : labels.entrySet()) {
                if (labelEntry.getValue() == i)
                    builder.append(labelEntry.getKey()).append(": ");
            }
            builder.append(instruction.toString());
            joiner.add(builder.toString());
        }
        return joiner.toString();
    }
}
