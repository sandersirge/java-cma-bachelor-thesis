package cma.instruction;

public sealed interface CMaInstruction<Code> permits CMaBasicInstruction, CMaIntInstruction, CMaLabelInstruction {

    Code code();

    void accept(CMaInstructionVisitor visitor);

    @Override
    String toString();
}
