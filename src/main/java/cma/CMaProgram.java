package cma;

import cma.instruction.CMaBasicInstruction;
import cma.instruction.CMaInstruction;
import cma.instruction.CMaIntInstruction;
import cma.instruction.CMaLabelInstruction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CMaProgram {

    private final List<CMaInstruction<?>> instructions;
    private final Map<CMaLabel, Integer> labels;

    public CMaProgram(List<CMaInstruction<?>> instructions, Map<CMaLabel, Integer> labels) {
        this.instructions = instructions;
        this.labels = labels;
    }

    public List<CMaInstruction<?>> getInstructions() {
        return instructions;
    }

    public Map<CMaLabel, Integer> getLabels() {
        return labels;
    }

    @Override
    public String toString() {
        Set<Map.Entry<CMaLabel, Integer>> remainingLabelEntries = new HashMap<>(labels).entrySet();
        StringJoiner joiner = new StringJoiner("\n");

        for (int i = 0; i < instructions.size(); i++) {
            CMaInstruction<?> instruction = instructions.get(i);
            StringBuilder builder = new StringBuilder();
            for (Iterator<Map.Entry<CMaLabel, Integer>> iterator = remainingLabelEntries.iterator(); iterator.hasNext(); ) {
                Map.Entry<CMaLabel, Integer> labelEntry = iterator.next();
                if (labelEntry.getValue() == i) {
                    builder.append(labelEntry.getKey()).append(": ");
                    iterator.remove();
                }
            }
            builder.append(instruction.toString());
            joiner.add(builder.toString());
        }

        if (!remainingLabelEntries.isEmpty()) {
            // print remaining (out of bounds) labels at end, Vam can handle
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<CMaLabel, Integer> labelEntry : remainingLabelEntries) {
                builder.append(labelEntry.getKey()).append(": ");
            }
            joiner.add(builder.toString());
        }

        return joiner.toString();
    }

    public CMaProgram append(CMaProgram other) {
        List<CMaInstruction<?>> instructions = new ArrayList<>();
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

    public void toFile(Path path, CMaStack initialStack) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.writeString(path, toString(initialStack));
    }

    // TODO: 31.05.22 eraldi klassi parser
    public static CMaProgram fromFile(Path path) throws IOException {
        Map<String, CMaLabel> labelNames = new HashMap<>();
        List<String> lines = Files.readAllLines(path);
        List<String> codeLines = new ArrayList<>();

        for (String line : lines) {
            String[] split = line.split(":");
            String rest = line;
            if (split.length > 1) {
                CMaLabel label = new CMaLabel();
                String labelName = split[0].trim();
                labelNames.put(labelName, label);
                codeLines.add(labelName + ":");
                rest = split[1];
            }
            if (!rest.isBlank()) {
                codeLines.add(rest.trim());
            }
        }

        CMaProgramWriter pw = new CMaProgramWriter();

        for (String line : codeLines) {
            String[] split = line.split(" +");
            if (split.length == 1) {
                try {
                    CMaBasicInstruction.Code code = CMaBasicInstruction.Code.valueOf(line.toUpperCase());
                    pw.visit(code);
                } catch (IllegalArgumentException e) {
                    if (line.endsWith(":")) {
                        pw.visit(labelNames.get(line.substring(0, line.length() - 1)));
                    } else throw e;
                }
            } else {
                String command = split[0].toUpperCase();
                try {
                    CMaIntInstruction.Code code = CMaIntInstruction.Code.valueOf(command);
                    pw.visit(code, Integer.parseInt(split[1]));
                } catch (IllegalArgumentException e) {
                    CMaLabelInstruction.Code code = CMaLabelInstruction.Code.valueOf(command);
                    pw.visit(code, labelNames.get(split[1]));
                }
            }
        }
        return pw.toProgram();
    }
}
