package ee.ut.cs.sws.cma.instruction;

public record CMaBasicInstruction(Code code) implements CMaInstruction<CMaBasicInstruction.Code> {

    public enum Code {
        //@formatter:off
        /** binaarne + */ ADD,
        /** binaarne - */ SUB,
        /** binaarne * */ MUL,
        /** binaarne / */ DIV,
        /** binaarne % */ MOD,
        /** unaarne -  */ NEG,

        /** binaarne & */ AND,
        /** binaarne | */ OR,
        /** binaarne ^ */ XOR,
        /** unaarne !  */ NOT,

        /** binaarne == */ EQ,
        /** binaarne != */ NEQ,
        /** binaarne <  */ LE,
        /** binaarne <= */ LEQ,
        /** binaarne >  */ GE, GR,
        /** binaarne >= */ GEQ,

        /** eemalda stackipealne väärtus */
        POP,

        /** duubelda stackipealne väärtus */
        DUP,

        /** lae väärtus stackipealselt indeksilt */
        LOAD,

        /** salvesta väärtus stackipealsele indeksile */
        STORE,

        /** seiska programm */
        HALT,

        /** salvesta EP ja FP stackile (täitmisraami ettevalmistus) */
        MARK,

        /** kutsu funktsioon: FP=SP, vaheta PC ja S[FP] */
        CALL,
        //@formatter:on
    }

    @Override
    public String toString() {
        return code.name();
    }
}
