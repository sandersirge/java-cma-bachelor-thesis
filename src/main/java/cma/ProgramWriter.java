package cma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramWriter {

    private final List<Instruction> instructions = new ArrayList<>();
    private final Map<Label, Integer> labels = new HashMap<>();

    public void visit(Instruction instruction) {
        instructions.add(instruction);
    }

    public void visit(NoArgInstruction.Code code) {
        visit(new NoArgInstruction(code));
    }

    public void visit(ArgInstruction.Code code, int arg) {
        visit(new ArgInstruction(code, arg));
    }

    public void visit(JumpInstruction.Code code, Label label) {
        visit(new JumpInstruction(code, label));
    }

    public void visitLabel(Label label) {
        labels.put(label, instructions.size());
    }

    public Program write() {
        return new Program(instructions, labels);
    }
}
