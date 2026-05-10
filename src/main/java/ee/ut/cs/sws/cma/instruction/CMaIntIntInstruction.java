package ee.ut.cs.sws.cma.instruction;

public record CMaIntIntInstruction(Code code, int arg1, int arg2) implements CMaInstruction<CMaIntIntInstruction.Code> {

    public enum Code {
        //@formatter:off
        /** nihuta m pealmist väärtust q positsiooni võrra allapoole */
        SLIDE,
        //@formatter:on
    }

    @Override
    public String toString() {
        return code.name() + " " + arg1 + " " + arg2;
    }
}

