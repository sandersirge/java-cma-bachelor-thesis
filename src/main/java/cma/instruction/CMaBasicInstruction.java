package cma.instruction;

public class CMaBasicInstruction extends CMaInstruction<CMaBasicInstruction.Code> {

    public enum Code {
        ADD, // binaarne +
        SUB, // binaarne -
        MUL, // binaarne *
        DIV, // binaarne /
        MOD, // binaarne %
        NEG, // unaarne -
        AND, // binaarne &
        OR,  // binaarne |
        XOR, // binaarne ^
        NOT, // unaarne !
        EQ,  // binaarne ==
        NEQ, // binaarne !=
        LE,  // binaarne <
        LEQ, // binaarne <=
        GE, GR, // binaarne >
        GEQ, // binaarne >=
        POP, // eemalda stackipealne väärtus
        DUP, // duubelda stackipealne väärtus
        LOAD, // lae väärtus stackipealselt indeksilt
        STORE, // salvesta väärtus stackipealsele indeksile
        HALT, // seiska programm
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
