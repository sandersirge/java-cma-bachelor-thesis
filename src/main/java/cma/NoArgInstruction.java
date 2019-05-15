package cma;

public class NoArgInstruction extends Instruction<NoArgInstruction.Code> {

    public enum Code {
        ADD, SUB, MUL, DIV, MOD,
        NEG,
        AND, OR, XOR, NOT,
        EQ, NEQ, LE, LEQ, GE, GR, GEQ,
        POP, DUP,
        LOAD, STORE,
        HALT,
    };

    private final Code code;

    public NoArgInstruction(Code code) {
        this.code = code;
    }

    @Override
    public Code getCode() {
        return code;
    }
}
