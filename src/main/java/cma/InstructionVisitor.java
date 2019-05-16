package cma;

public abstract class InstructionVisitor {

    protected abstract void visit(NoArgInstruction noArgInstruction);
    protected abstract void visit(ArgInstruction argInstruction);
    protected abstract void visit(JumpInstruction jumpInstruction);

    public void visit(Instruction<?> instruction) {
        instruction.accept(this);
    }
}
