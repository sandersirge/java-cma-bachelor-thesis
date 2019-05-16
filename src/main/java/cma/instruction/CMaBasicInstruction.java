package cma.instruction;

public class CMaBasicInstruction extends CMaInstruction<CMaBasicInstruction.Code> {

    public enum Code {
        ADD, SUB, MUL, DIV, MOD,
        NEG,
        AND, OR, XOR, NOT,
        EQ, NEQ, LE, LEQ, GE, GR, GEQ,
        POP, DUP,
        LOAD, STORE,
        HALT,
    };

    public CMaBasicInstruction(Code code) {
        super(code);
    }

    @Override
    public void accept(CMaInstructionVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return code.name();
    }
}
