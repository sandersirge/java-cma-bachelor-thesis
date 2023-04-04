package cma;

// DO NOT UPLOAD: pole millekski vaja?

import cma.instruction.CMaBasicInstruction;
import cma.instruction.CMaIntInstruction;
import cma.instruction.CMaLabelInstruction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMaProgramParser {

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
