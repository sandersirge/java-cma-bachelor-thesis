package cma;

public class ArgInstruction extends Instruction<ArgInstruction.Code> {

    public enum Code {
        LOADC,
        LOADA, STOREA, // TODO: separte class for address argument instruction?
    };

    private final Code code;
    private final int arg;

    public ArgInstruction(Code code, int arg) {
        this.code = code;
        this.arg = arg;
    }

    @Override
    public Code getCode() {
        return code;
    }

    public int getArg() {
        return arg;
    }
}
