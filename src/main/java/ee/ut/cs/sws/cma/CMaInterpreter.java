package ee.ut.cs.sws.cma;

import ee.ut.cs.sws.cma.instruction.*;

import static ee.ut.cs.sws.cma.instruction.CMaBasicInstruction.Code.LOAD;
import static ee.ut.cs.sws.cma.instruction.CMaBasicInstruction.Code.STORE;
import static ee.ut.cs.sws.cma.instruction.CMaIntInstruction.Code.LOADC;
import static ee.ut.cs.sws.cma.instruction.CMaIntInstruction.Code.LOADM;
import static ee.ut.cs.sws.cma.instruction.CMaIntInstruction.Code.STOREM;
import static ee.ut.cs.sws.cma.instruction.CMaIntInstruction.Code.LOADRC;

public class CMaInterpreter {

    private final CMaProgram program;
    private int pc = 0;
    private final CMaStack stack;
    private int fp = 0;  // Frame Pointer (raamiviit)
    private int ep = 0;  // Extreme Pointer (piiriviit)
    private int hp = Integer.MAX_VALUE;  // Heap Pointer (kuhjaviit)

    private CMaInterpreter(CMaProgram program, CMaStack initialStack) {
        this.program = program;
        this.stack = new CMaStack(initialStack);
    }

    public static CMaStack run(CMaProgram program) {
        return run(program, new CMaStack());
    }

    public static CMaStack run(CMaProgram program, CMaStack initialStack) {
        CMaInterpreter interpreter = new CMaInterpreter(program, initialStack);
        return interpreter.execute();
    }

    private CMaStack execute() {
        while (0 <= pc && pc < program.instructions().size()) {
            CMaInstruction<?> instruction = program.instructions().get(pc);
            pc++;
            execute(instruction);
        }
        return stack;
    }

    private void execute(CMaInstruction<?> instruction) {
        switch (instruction) {
            case CMaBasicInstruction(CMaBasicInstruction.Code code) -> {
                int arg, lhs, rhs;
                switch (code) {
                    case ADD, SUB, MUL, DIV, MOD, AND, OR, XOR, EQ, NEQ, LE, LEQ, GE, GR, GEQ -> {
                        rhs = stack.pop();
                        lhs = stack.pop();
                        switch (code) {
                            case ADD -> stack.push(lhs + rhs);
                            case SUB -> stack.push(lhs - rhs);
                            case MUL -> stack.push(lhs * rhs);
                            case DIV -> stack.push(lhs / rhs);
                            case MOD -> stack.push(lhs % rhs);
                            case AND -> stack.push(lhs & rhs);
                            case OR -> stack.push(lhs | rhs);
                            case XOR -> stack.push(lhs ^ rhs);
                            case EQ -> stack.push(CMaUtils.bool2int(lhs == rhs));
                            case NEQ -> stack.push(CMaUtils.bool2int(lhs != rhs));
                            case LE -> stack.push(CMaUtils.bool2int(lhs < rhs));
                            case LEQ -> stack.push(CMaUtils.bool2int(lhs <= rhs));
                            case GE, GR -> stack.push(CMaUtils.bool2int(lhs > rhs));
                            case GEQ -> stack.push(CMaUtils.bool2int(lhs >= rhs));
                        }
                    }
                    case NEG -> {
                        arg = stack.pop();
                        stack.push(-arg);
                    }
                    case NOT -> {
                        arg = stack.pop();
                        stack.push(CMaUtils.bool2int(!CMaUtils.int2bool(arg)));
                    }
                    case POP -> stack.pop();
                    case DUP -> stack.push(stack.peek());
                    case LOAD -> {
                        arg = stack.pop();
                        stack.push(stack.get(arg));
                    }
                    case STORE -> {
                        arg = stack.pop();
                        stack.set(arg, stack.peek());
                    }
                    case HALT -> pc = -1; // out of range pc halts
                    case MARK -> {
                        // S[SP+1] = EP; S[SP+2] = FP; SP += 2
                        stack.push(ep);
                        stack.push(fp);
                    }
                    case CALL -> {
                        // FP = SP; tmp = PC; PC = S[FP]; S[FP] = tmp
                        fp = stack.size() - 1;  // SP = stack.size() - 1
                        int tmp = pc;
                        pc = stack.get(fp);
                        stack.set(fp, tmp);
                    }
                }
            }
            case CMaIntInstruction(CMaIntInstruction.Code code, int arg) -> {
                switch (code) {
                    case LOADC -> stack.push(arg);
                    case LOADA -> {
                        execute(new CMaIntInstruction(LOADC, arg));
                        execute(new CMaBasicInstruction(LOAD));
                    }
                    case STOREA -> {
                        execute(new CMaIntInstruction(LOADC, arg));
                        execute(new CMaBasicInstruction(STORE));
                    }
                    case ALLOC -> stack.allocate(arg);
                    case LOADRC -> stack.push(fp + arg);
                    case LOADM -> {
                        // LOADM m: S[SP+i] ← S[S[SP]+i] for i=m-1..0; SP ← SP + m - 1
                        int sp = stack.size() - 1;
                        int target = stack.get(sp);                         // S[SP] = base address
                        stack.allocate(arg - 1);                        // expand: new SP = sp + arg - 1
                        for (int i = arg - 1; i >= 0; i--) {
                            stack.set(sp + i, stack.get(target + i));       // S[SP+i] ← S[target+i]
                        }
                    }
                    case STOREM -> {
                        // STOREM m: S[S[SP]+i] ← S[SP-m+i] for i=0..m-1; eemalda aadress
                        int sp = stack.size() - 1;
                        int target = stack.get(sp);                         // S[SP] = destination address
                        for (int i = 0; i < arg; i++) {
                            stack.set(target + i, stack.get(sp - arg + i)); // S[target+i] ← S[SP-m+i]
                        }
                    }
                    case ENTER -> {
                        // EP = SP + m; kui EP >= HP, siis viga
                        ep = stack.size() - 1 + arg;
                        if (ep >= hp)
                            throw new CMaException("Stack Overflow: EP(%d) >= HP(%d)".formatted(ep, hp));
                    }
                    case RETURN -> {
                        // PC = S[FP]; EP = S[FP-2]; kontrolli EP >= HP;
                        // SP = FP - q (truncate(FP - q + 1)); FP = S[FP-1]
                        pc = stack.get(fp);                    // taasta tagastusaadress
                        ep = stack.get(fp - 2);                // taasta vana EP
                        if (ep >= hp)
                            throw new CMaException("Stack Overflow: EP(%d) >= HP(%d)".formatted(ep, hp));
                        int newSp = fp - arg;                  // SP = FP - q
                        int newFp = stack.get(fp - 1);         // taasta vana FP
                        stack.truncate(newSp + 1);    // kärbi stack: size = SP + 1
                        fp = newFp;
                    }
                }

            }
            case CMaIntIntInstruction(CMaIntIntInstruction.Code code, int arg1, int arg2) -> {
                switch (code) {
                    case SLIDE -> {
                        // SLIDE q m: nihuta m pealmist väärtust q positsiooni allapoole
                        //   if (q > 0)
                        //     if (m = 0) SP ← SP - q;
                        //     else { SP ← SP-q-m; for (i←0; i<m; i++) { SP++; S[SP]←S[SP+q]; } }
                        if (arg1 > 0) {
                            int sp = stack.size() - 1;
                            if (arg2 == 0) {
                                // m = 0: lihtsalt kärbi q pesa
                                stack.truncate(sp - arg1 + 1);  // SP = SP - q, size = SP - q + 1
                            } else {
                                // kopeeri m väärtust q positsiooni allapoole, siis kärbi
                                sp = sp - arg1 - arg2;
                                for (int i = 0; i < arg2; i++) {
                                    sp++;
                                    stack.set(sp, stack.get(sp + arg1)); // S[SP] ← S[SP+q]
                                }
                                stack.truncate(sp + 1);         // SP = SP - q, size = SP - q + 1
                            }
                        }
                    }
                    case LOADR -> {
                        // LOADR j m = LOADRC j; LOADM m
                        execute(new CMaIntInstruction(LOADRC, arg1));
                        execute(new CMaIntInstruction(LOADM, arg2));
                    }
                    case STORER -> {
                        // STORER j m = LOADRC j; STOREM m
                        execute(new CMaIntInstruction(LOADRC, arg1));
                        execute(new CMaIntInstruction(STOREM, arg2));
                    }
                }
            }
            case CMaLabelInstruction(CMaLabelInstruction.Code code, CMaLabel label) -> {
                switch (code) {
                    case JUMP -> pc = getLabelTarget(label);
                    case JUMPZ -> {
                        if (!CMaUtils.int2bool(stack.pop()))
                            pc = getLabelTarget(label);
                    }
                    case JUMPI -> {
                        // PC = target(label) + S[SP]; SP--
                        pc = getLabelTarget(label) + stack.pop();
                    }
                }
            }
        }
    }

    private int getLabelTarget(CMaLabel label) {
        Integer target = program.labels().get(label);
        if (target != null)
            return target;
        else
            throw new CMaException("label '%s' not placed".formatted(label));
    }
}
