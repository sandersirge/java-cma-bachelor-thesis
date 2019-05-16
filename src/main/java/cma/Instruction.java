package cma;

public abstract class Instruction<Code> {

    protected final Code code;

    protected Instruction(Code code) {
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public abstract void accept(InstructionVisitor visitor);
}
