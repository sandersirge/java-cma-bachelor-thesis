package cma;

public class JumpInstruction extends Instruction<JumpInstruction.Code> {

    public enum Code {
        JUMP, JUMPZ,
    };

    private final Label label;

    public JumpInstruction(Code code, Label label) {
        super(code);
        this.label = label;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return code.name() + " " + label;
    }
}
