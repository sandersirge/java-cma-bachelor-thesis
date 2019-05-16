package cma.instruction;

public class CMaIntInstruction extends CMaInstruction<CMaIntInstruction.Code> {

    public enum Code {
        LOADC,
        LOADA, STOREA,
    };

    private final int arg;

    public CMaIntInstruction(Code code, int arg) {
        super(code);
        this.arg = arg;
    }

    public int getArg() {
        return arg;
    }

    @Override
    public void accept(CMaInstructionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return code.name() + " " + arg;
    }
}
