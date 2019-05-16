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

    public NoArgInstruction(Code code) {
        super(code);
    }

    @Override
    public void accept(InstructionVisitor visitor) {
        visitor.visit(this);
    }
}
