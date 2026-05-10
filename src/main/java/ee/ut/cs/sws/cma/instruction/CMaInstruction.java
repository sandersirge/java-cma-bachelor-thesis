package ee.ut.cs.sws.cma.instruction;

public sealed interface CMaInstruction<Code> permits CMaBasicInstruction, CMaIntInstruction, CMaIntIntInstruction, CMaLabelInstruction {

    Code code();

    @Override
    String toString();
}
