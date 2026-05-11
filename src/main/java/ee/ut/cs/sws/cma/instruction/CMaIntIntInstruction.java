package ee.ut.cs.sws.cma.instruction;

public record CMaIntIntInstruction(Code code, int arg1, int arg2) implements CMaInstruction<CMaIntIntInstruction.Code> {

    public enum Code {
        //@formatter:off
        /** nihuta m pealmist väärtust q positsiooni võrra allapoole */
        SLIDE,

        /** loe m väärtust FP-suhteliselt aadressilt j: LOADRC j; LOADM m */
        LOADR,

        /** salvesta m väärtust FP-suhtelisele aadressile j: LOADRC j; STOREM m */
        STORER,
        //@formatter:on
    }

    @Override
    public String toString() {
        return code.name() + " " + arg1 + " " + arg2;
    }
}

