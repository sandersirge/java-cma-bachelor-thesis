package ee.ut.cs.sws.cma.instruction;

public record CMaIntInstruction(Code code, int arg) implements CMaInstruction<CMaIntInstruction.Code> {

    public enum Code {
        //@formatter:off
        /** lisa stackile konstant */
        LOADC,

        /** loe väärtus indeksilt */
        LOADA,

        /** salvesta väärtus indeksile */
        STOREA,

        /** eralda m nulliga algväärtustatud pesa */
        ALLOC,

        /** lisa stackile FP + j (suhteline aadress) */
        LOADRC,

        /** loe väärtus FP-suhteliselt indeksilt */
        LOADR,

        /** salvesta väärtus FP-suhtelisele indeksile */
        STORER,

        /** sea piirviit EP = SP + m, kontrolli EP >= HP */
        ENTER,
        //@formatter:on
    }

    @Override
    public String toString() {
        return code.name() + " " + arg;
    }
}
