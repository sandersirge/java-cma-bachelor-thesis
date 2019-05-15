package cma;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static cma.NoArgInstruction.Code.*;
import static cma.ArgInstruction.Code.*;
import static cma.JumpInstruction.Code.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.JVM)
public class InterpreterTest {

    private ProgramWriter pw;

    @Before
    public void setUp() {
        pw = new ProgramWriter();
    }

    private void assertInterpreted(Stack<Integer> expected) {
        assertInterpreted(expected, new Stack<>());
    }

    private void assertInterpreted(Stack<Integer> expected, Stack<Integer> initial) {
        Stack<Integer> actual = Interpreter.run(pw.write(), initial);
        assertEquals(expected, actual);
    }

    @Test
    public void test1() {
        pw.visit(LOADC, 1);
        pw.visit(LOADC, 7);
        pw.visit(ADD);

        assertInterpreted(new Stack<>(8));
    }

    @Test
    public void test2() {
        int x = 0, y = 1;

        pw.visit(LOADC, y);
        pw.visit(LOAD);
        pw.visit(LOADC, 1);
        pw.visit(SUB);
        pw.visit(LOADC, x);
        pw.visit(STORE);

        assertInterpreted(new Stack<>(19, 20, 19), new Stack<>(10, 20));
    }

    @Test
    public void test2b() {
        int x = 0, y = 1;

        pw.visit(LOADA, y);;
        pw.visit(LOADC, 1);
        pw.visit(SUB);
        pw.visit(STOREA, x);

        assertInterpreted(new Stack<>(19, 20, 19), new Stack<>(10, 20));
    }

    @Test
    public void test3() {
        int x = 0, y = 1;

        pw.visit(LOADA, x);
        pw.visit(LOADA, y);
        pw.visit(GE);
        pw.visit(JUMPZ, "A");
        pw.visit(LOADA, x);
        pw.visit(LOADA, y);
        pw.visit(SUB);
        pw.visit(STOREA, x);
        pw.visit(POP);
        pw.visit(JUMP, "B");
        pw.visitLabel("A");
        pw.visit(LOADA, y);
        pw.visit(LOADA, x);
        pw.visit(SUB);
        pw.visit(STOREA, y);
        pw.visit(POP);
        pw.visitLabel("B");

        assertInterpreted(new Stack<>(15, 5), new Stack<>(15, 20));
        assertInterpreted(new Stack<>(5, 15), new Stack<>(20, 15));
    }

    @Test
    public void test4() {
        int a = 0, b = 1, c = 2;

        pw.visitLabel("A");
        pw.visit(LOADA, a);
        pw.visit(LOADC, 0);
        pw.visit(GE);
        pw.visit(JUMPZ, "B");
        pw.visit(LOADA, c);
        pw.visit(LOADC, 1);
        pw.visit(ADD);
        pw.visit(STOREA, c);
        pw.visit(POP);
        pw.visit(LOADA, a);
        pw.visit(LOADA, b);
        pw.visit(SUB);
        pw.visit(STOREA, a);
        pw.visit(POP);
        pw.visit(JUMP, "A");
        pw.visitLabel("B");

        assertInterpreted(new Stack<>(0, 5, 4), new Stack<>(20, 5, 0));
        assertInterpreted(new Stack<>(-4, 6, 4), new Stack<>(20, 6, 0));
        assertInterpreted(new Stack<>(-1, 7, 3), new Stack<>(20, 7, 0));
    }

    @Test
    public void test5() {
        int n = 0, i = 1, r = 2;

        pw.visitLabel("_while");
        pw.visit(LOADA, i);
        pw.visit(LOADA, n);
        pw.visit(LEQ);
        pw.visit(JUMPZ, "_end");

        pw.visit(LOADA, r);
        pw.visit(LOADA, i);
        pw.visit(MUL);
        pw.visit(STOREA, r);
        pw.visit(POP);

        pw.visit(LOADA, i);
        pw.visit(LOADC, 1);
        pw.visit(ADD);
        pw.visit(STOREA, i);
        pw.visit(POP);

        pw.visit(JUMP, "_while");
        pw.visitLabel("_end");
        pw.visit(HALT);

        assertInterpreted(new Stack<>(5, 6, 120), new Stack<>(5, 1, 1));
        assertInterpreted(new Stack<>(6, 7, 720), new Stack<>(6, 1, 1));
    }

    @Test
    public void test6() {
        int n = 0, x = 1, z = 2;

        pw.visit(LOADC, 1);
        pw.visit(STOREA, z);
        pw.visit(POP);

        pw.visitLabel("_while");
        pw.visit(LOADA, n);
        pw.visit(LOADC, 0);
        pw.visit(GR);
        pw.visit(JUMPZ, "_end");

        pw.visit(LOADA, n);
        pw.visit(LOADC, 1);
        pw.visit(AND);
        pw.visit(JUMPZ, "_even");

        pw.visit(LOADA, z);
        pw.visit(LOADA, x);
        pw.visit(MUL);
        pw.visit(STOREA, z);
        pw.visit(POP);

        pw.visitLabel("_even");
        pw.visit(LOADA, x);
        pw.visit(DUP);
        pw.visit(MUL);
        pw.visit(STOREA, x);
        pw.visit(POP);

        pw.visit(LOADA, n);
        pw.visit(LOADC, 2);
        pw.visit(DIV);
        pw.visit(STOREA, n);
        pw.visit(POP);

        pw.visit(JUMP, "_while");
        pw.visitLabel("_end");
        pw.visit(HALT);

        assertInterpreted(new Stack<>(0, 43046721, 177147), new Stack<>(11, 3, 0));
    }
}
