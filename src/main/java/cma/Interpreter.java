package cma;

import static cma.ArgInstruction.Code.LOADC;
import static cma.NoArgInstruction.Code.LOAD;
import static cma.NoArgInstruction.Code.STORE;

public class Interpreter {

    private final Program program;
    private int pc = 0;
    private final Stack<Integer> stack;

    private Interpreter(Program program, Stack<Integer> stack) {
        this.program = program;
        this.stack = new Stack<>(stack);
    }

    private Stack<Integer> execute() {
        while (0 <= pc && pc < program.getInstructions().size()) {
            Instruction instruction = program.getInstructions().get(pc);
            pc++;
            execute(instruction);
        }
        return stack;
    }

    private void execute(Instruction instruction) {
        // TODO: not use instanceof
        if (instruction instanceof NoArgInstruction) {
            NoArgInstruction noArgInstruction = (NoArgInstruction) instruction;
            int arg, lhs, rhs;
            switch (noArgInstruction.getCode()) {
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MOD:
                case AND:
                case OR:
                case XOR:
                case EQ:
                case NEQ:
                case LE:
                case LEQ:
                case GE:
                case GR:
                case GEQ:
                    rhs = stack.pop();
                    lhs = stack.pop();
                    switch (noArgInstruction.getCode()) {
                        case ADD:
                            stack.push(lhs + rhs);
                            break;
                        case SUB:
                            stack.push(lhs - rhs);
                            break;
                        case MUL:
                            stack.push(lhs * rhs);
                            break;
                        case DIV:
                            stack.push(lhs / rhs);
                            break;
                        case MOD:
                            stack.push(lhs % rhs);
                            break;
                        case AND:
                            stack.push(lhs & rhs);
                            break;
                        case OR:
                            stack.push(lhs | rhs);
                            break;
                        case XOR:
                            stack.push(lhs ^ rhs);
                            break;
                        case EQ:
                            stack.push(bool2Integer(lhs == rhs));
                            break;
                        case NEQ:
                            stack.push(bool2Integer(lhs != rhs));
                            break;
                        case LE:
                            stack.push(bool2Integer(lhs < rhs));
                            break;
                        case LEQ:
                            stack.push(bool2Integer(lhs <= rhs));
                            break;
                        case GE:
                        case GR:
                            stack.push(bool2Integer(lhs > rhs));
                            break;
                        case GEQ:
                            stack.push(bool2Integer(lhs >= rhs));
                            break;
                    }
                    break;
                case NEG:
                    arg = stack.pop();
                    stack.push(-arg);
                    break;
                case NOT:
                    arg = stack.pop();
                    stack.push(bool2Integer(!integer2Bool(arg)));
                    break;
                case POP:
                    stack.pop();
                    break;
                case DUP:
                    stack.push(stack.peek());
                    break;
                case LOAD:
                    arg = stack.pop();
                    stack.push(stack.get(arg));
                    break;
                case STORE:
                    arg = stack.pop();
                    stack.set(arg, stack.peek());
                    break;
                case HALT:
                    pc = -1;
                    break;
            }
        }
        else if (instruction instanceof ArgInstruction) {
            ArgInstruction argInstruction = (ArgInstruction) instruction;
            switch (argInstruction.getCode()) {
                case LOADC:
                    stack.push(argInstruction.getArg());
                    break;
                case LOADA:
                    execute(new ArgInstruction(LOADC, argInstruction.getArg()));
                    execute(new NoArgInstruction(LOAD));
                    break;
                case STOREA:
                    execute(new ArgInstruction(LOADC, argInstruction.getArg()));
                    execute(new NoArgInstruction(STORE));
                    break;
            }
        }
        else if (instruction instanceof JumpInstruction) {
            JumpInstruction jumpInstruction = (JumpInstruction) instruction;
            switch (jumpInstruction.getCode()) {
                case JUMP:
                    pc = program.getLabels().get(jumpInstruction.getLabel());
                    break;
                case JUMPZ:
                    if (!integer2Bool(stack.pop()))
                        pc = program.getLabels().get(jumpInstruction.getLabel());
                    break;
            }
        }
        else
            throw new UnsupportedOperationException("Unknown instruction");
    }

    private boolean integer2Bool(int i) {
        return i != 0;
    }

    private Integer bool2Integer(boolean b) {
        return b ? 1 : 0;
    }

    public static Stack<Integer> run(Program program) {
        return run(program, new Stack<>());
    }

    public static Stack<Integer> run(Program program, Stack<Integer> stack) {
        Interpreter interpreter = new Interpreter(program, stack);
        return interpreter.execute();
    }
}
