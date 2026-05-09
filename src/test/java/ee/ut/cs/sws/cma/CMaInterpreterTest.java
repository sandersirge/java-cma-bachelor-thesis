package ee.ut.cs.sws.cma;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static ee.ut.cs.sws.cma.instruction.CMaBasicInstruction.Code.*;
import static ee.ut.cs.sws.cma.instruction.CMaIntInstruction.Code.*;
import static ee.ut.cs.sws.cma.instruction.CMaLabelInstruction.Code.*;
import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.JVM)
public class CMaInterpreterTest {

    private CMaProgramWriter pw;

    @Before
    public void setUp() {
        pw = new CMaProgramWriter();
    }

    private void assertInterpreted(CMaStack expected) {
        assertInterpreted(expected, new CMaStack());
    }

    private void assertInterpreted(CMaStack expected, CMaStack initial) {
        CMaStack actual = CMaInterpreter.run(pw.toProgram(), initial);
        assertEquals(expected, actual);
    }

    @Test
    public void test1() {
        pw.visit(LOADC, 1);
        pw.visit(LOADC, 7);
        pw.visit(ADD);

        assertInterpreted(new CMaStack(8));
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

        assertInterpreted(new CMaStack(19, 20, 19), new CMaStack(10, 20));
    }

    @Test
    public void test2b() {
        int x = 0, y = 1;

        pw.visit(LOADA, y);
        pw.visit(LOADC, 1);
        pw.visit(SUB);
        pw.visit(STOREA, x);

        assertInterpreted(new CMaStack(19, 20, 19), new CMaStack(10, 20));
    }

    @Test
    public void test3() {
        int x = 0, y = 1;

        CMaLabel A = new CMaLabel();
        CMaLabel B = new CMaLabel();
        pw.visit(LOADA, x);
        pw.visit(LOADA, y);
        pw.visit(GE);
        pw.visit(JUMPZ, A);
        pw.visit(LOADA, x);
        pw.visit(LOADA, y);
        pw.visit(SUB);
        pw.visit(STOREA, x);
        pw.visit(POP);
        pw.visit(JUMP, B);
        pw.visit(A);
        pw.visit(LOADA, y);
        pw.visit(LOADA, x);
        pw.visit(SUB);
        pw.visit(STOREA, y);
        pw.visit(POP);
        pw.visit(B);

        assertInterpreted(new CMaStack(15, 5), new CMaStack(15, 20));
        assertInterpreted(new CMaStack(5, 15), new CMaStack(20, 15));
    }

    @Test
    public void test4() {
        int a = 0, b = 1, c = 2;

        CMaLabel A = new CMaLabel();
        CMaLabel B = new CMaLabel();
        pw.visit(A);
        pw.visit(LOADA, a);
        pw.visit(LOADC, 0);
        pw.visit(GE);
        pw.visit(JUMPZ, B);
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
        pw.visit(JUMP, A);
        pw.visit(B);

        assertInterpreted(new CMaStack(0, 5, 4), new CMaStack(20, 5, 0));
        assertInterpreted(new CMaStack(-4, 6, 4), new CMaStack(20, 6, 0));
        assertInterpreted(new CMaStack(-1, 7, 3), new CMaStack(20, 7, 0));
    }

    @Test
    public void test5() {
        int n = 0, i = 1, r = 2;

        CMaLabel _while = new CMaLabel();
        CMaLabel _end = new CMaLabel();
        pw.visit(_while);
        pw.visit(LOADA, i);
        pw.visit(LOADA, n);
        pw.visit(LEQ);
        pw.visit(JUMPZ, _end);

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

        pw.visit(JUMP, _while);
        pw.visit(_end);
        pw.visit(HALT);

        assertInterpreted(new CMaStack(5, 6, 120), new CMaStack(5, 1, 1));
        assertInterpreted(new CMaStack(6, 7, 720), new CMaStack(6, 1, 1));
    }

    @Test
    public void test6() {
        int n = 0, x = 1, z = 2;

        CMaLabel _while = new CMaLabel();
        CMaLabel _even = new CMaLabel();
        CMaLabel _end = new CMaLabel();
        pw.visit(LOADC, 1);
        pw.visit(STOREA, z);
        pw.visit(POP);

        pw.visit(_while);
        pw.visit(LOADA, n);
        pw.visit(LOADC, 0);
        pw.visit(GR);
        pw.visit(JUMPZ, _end);

        pw.visit(LOADA, n);
        pw.visit(LOADC, 1);
        pw.visit(AND);
        pw.visit(JUMPZ, _even);

        pw.visit(LOADA, z);
        pw.visit(LOADA, x);
        pw.visit(MUL);
        pw.visit(STOREA, z);
        pw.visit(POP);

        pw.visit(_even);
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

        pw.visit(JUMP, _while);
        pw.visit(_end);
        pw.visit(HALT);

        assertInterpreted(new CMaStack(0, 43046721, 177147), new CMaStack(11, 3, 0));
    }

    @Test(expected = CMaException.class)
    public void test_missing_label() {
        CMaLabel _label = new CMaLabel();
        pw.visit(JUMP, _label);

        CMaInterpreter.run(pw.toProgram(), new CMaStack());
    }

    @Test(expected = CMaException.class)
    public void test_multiple_label() {
        CMaLabel _label = new CMaLabel();
        pw.visit(_label);
        pw.visit(JUMP, _label);
        pw.visit(_label);
    }

    // Funktsioonikutsed: Samm 1 — ALLOC käsu testimine

    @Test
    public void test_alloc_empty() {
        pw.visit(ALLOC, 3);

        assertInterpreted(new CMaStack(0, 0, 0));
    }

    @Test
    public void test_alloc_after_loadc() {
        pw.visit(LOADC, 5);
        pw.visit(ALLOC, 2);

        assertInterpreted(new CMaStack(5, 0, 0));
    }

    // Funktsioonikutsed: Samm 3 — LOADRC, LOADR ja STORER käskude testimine

    @Test
    public void test_loadrc() {
        // fp = 0 (vaikeväärtus), LOADRC 3 → push(0 + 3) = 3
        pw.visit(LOADRC, 3);

        assertInterpreted(new CMaStack(3));
    }

    @Test
    public void test_loadr() {
        // stack: [10, 20, 30], fp = 0 (vaikeväärtus)
        // LOADR 2 → push(fp + 2) = push(2), LOAD → push(stack[2]) = 30
        pw.visit(LOADR, 2);

        assertInterpreted(new CMaStack(10, 20, 30, 30), new CMaStack(10, 20, 30));
    }

    @Test
    public void test_storer() {
        // stack: [10, 20, 30], fp = 0 (vaikeväärtus)
        // LOADC 99; STORER 1 → push(fp + 1) = push(1), STORE → stack[1] = 99
        pw.visit(LOADC, 99);
        pw.visit(STORER, 1);

        assertInterpreted(new CMaStack(10, 99, 30, 99), new CMaStack(10, 20, 30));
    }

    // Funktsioonikutsed: Samm 4 — MARK ja CALL käskude testimine

    @Test
    public void test_mark_call() {
        // Stack enne: []
        // LOADC 42  → [42]                    (parameeter)
        // MARK      → [42, 0, 0]              (push EP=0, push FP=0)
        // LOADC 5   → [42, 0, 0, 5]           (funktsiooni aadress)
        // CALL      → FP=3, PC=5, S[3]=4      → stack: [42, 0, 0, 4]
        // indeks 4: HALT (tagastuspunkt, siia ei jõua)
        // indeks 5: HALT (funktsioon peatub kohe)
        CMaLabel _func = new CMaLabel();

        pw.visit(LOADC, 42);      // 0: parameeter
        pw.visit(MARK);           // 1: push EP(0), push FP(0)
        pw.visit(LOADC, 5);       // 2: funktsiooni aadress (indeks 5)
        pw.visit(CALL);           // 3: FP=3, PC=5, S[3]=4 (tagastusaadress)
        pw.visit(HALT);           // 4: tagastuspunkt
        pw.visit(_func);          // 5: funktsiooni algus
        pw.visit(HALT);           // 5: funktsioon peatub kohe

        // Stack pärast: [42, 0, 0, 4] — parameeter, salvestatud EP, FP, tagastusaadress
        assertInterpreted(new CMaStack(42, 0, 0, 4));
    }
}
