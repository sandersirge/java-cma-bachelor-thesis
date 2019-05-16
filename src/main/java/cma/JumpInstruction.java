package cma;

public class JumpInstruction extends Instruction<JumpInstruction.Code> {

    public enum Code {
        JUMP, JUMPZ,
    };

    private final String label;

    public JumpInstruction(Code code, String label) {
        super(code);
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visit(this);
    }
}
