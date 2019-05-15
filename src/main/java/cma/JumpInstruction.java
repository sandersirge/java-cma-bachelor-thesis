package cma;

public class JumpInstruction extends Instruction<JumpInstruction.Code> {

    public enum Code {
        JUMP, JUMPZ,
    };

    private final Code code;
    private final String label;

    public JumpInstruction(Code code, String label) {
        this.code = code;
        this.label = label;
    }

    @Override
    public Code getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
