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

        /** loe m väärtust mälust aadressil S[SP], laienda stack m-1 pesa võrra */
        LOADM,

        /** kirjuta m väärtust S[SP-m..SP-1] mällu aadressile S[SP], eemalda aadress */
        STOREM,

        /** sea piirviit EP = SP + m, kontrolli EP >= HP */
        ENTER,

        /** taasta registrid ja puhasta täitmisraam, q = org. pesade + parameetrite arv */
        RETURN,
        //@formatter:on
    }

    @Override
    public String toString() {
        return code.name() + " " + arg;
    }
}
