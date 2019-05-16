package cma;

import cma.instruction.CMaBasicInstruction;
import cma.instruction.CMaInstruction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static cma.instruction.CMaBasicInstruction.Code.HALT;

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
        List<CMaInstruction> printInstructions = new ArrayList<>(instructions);
        printInstructions.add(new CMaBasicInstruction(HALT)); // add HALT in case program ends with label
        // TODO: instead of HALT, just print (out of bounds) labels at end, Vam can handle

        StringJoiner joiner = new StringJoiner("\n");
        for (int i = 0; i < printInstructions.size(); i++) {
            CMaInstruction instruction = printInstructions.get(i);
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

    public CMaProgram append(CMaProgram other) {
        List<CMaInstruction> instructions = new ArrayList<>();
        instructions.addAll(this.instructions);
        instructions.addAll(other.instructions);

        Map<CMaLabel, Integer> labels = new HashMap<>();
        labels.putAll(this.labels);
        labels.putAll(other.labels.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() + this.instructions.size())));

        return new CMaProgram(instructions, labels);
    }

    public String toString(CMaStack initialStack) {
        return initialStack.toLoadProgram().append(this).toString();
    }

    public void toFile(String filename, CMaStack initialStack) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8)) {
            writer.write(toString(initialStack));
        }
    }
}
