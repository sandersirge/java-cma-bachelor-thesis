package cma;

public class ArgInstruction extends Instruction<ArgInstruction.Code> {

    public enum Code {
        LOADC,
        LOADA, STOREA, // TODO: separte class for address argument instruction?
    };

    private final int arg;

    public ArgInstruction(Code code, int arg) {
        super(code);
        this.arg = arg;
    }

    public int getArg() {
        return arg;
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return code.name() + " " + arg;
    }
}
