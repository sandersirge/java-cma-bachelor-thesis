package ee.ut.cs.sws.cma;

import ee.ut.cs.sws.cma.instruction.CMaBasicInstruction;
import ee.ut.cs.sws.cma.instruction.CMaInstruction;
import ee.ut.cs.sws.cma.instruction.CMaIntInstruction;
import ee.ut.cs.sws.cma.instruction.CMaIntIntInstruction;
import ee.ut.cs.sws.cma.instruction.CMaLabelInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CMaProgramWriter {

    private final List<CMaInstruction<?>> instructions = new ArrayList<>();
    private final Map<CMaLabel, Integer> labels = new HashMap<>();

    public void visit(CMaInstruction<?> instruction) {
        instructions.add(instruction);
    }

    public void visit(CMaBasicInstruction.Code code) {
        visit(new CMaBasicInstruction(code));
    }

    public void visit(CMaIntInstruction.Code code, int arg) {
        visit(new CMaIntInstruction(code, arg));
    }

    public void visit(CMaIntIntInstruction.Code code, int arg1, int arg2) {
        visit(new CMaIntIntInstruction(code, arg1, arg2));
    }

    public void visit(CMaLabelInstruction.Code code, CMaLabel label) {
        visit(new CMaLabelInstruction(code, label));
    }

    public void visit(CMaLabel label) {
        if (labels.put(label, instructions.size()) != null)
            throw new CMaException("label '%s' placed multiple times".formatted(label));
    }

    public CMaProgram toProgram() {
        return new CMaProgram(instructions, labels);
    }
}
