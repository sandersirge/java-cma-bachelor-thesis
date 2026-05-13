package ee.ut.cs.sws.cma;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static ee.ut.cs.sws.cma.instruction.CMaBasicInstruction.Code.*;
import static ee.ut.cs.sws.cma.instruction.CMaIntInstruction.Code.*;
import static ee.ut.cs.sws.cma.instruction.CMaIntIntInstruction.Code.*;
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

    // Funktsioonikutsed: Samm 3 — LOADRC, LOADR, STORER, LOADM ja STOREM käskude testimine

    @Test
    public void test_loadrc() {
        /* fp = 0 (vaikeväärtus), LOADRC 3 → push(0 + 3) = 3 */
        pw.visit(LOADRC, 3);

        assertInterpreted(new CMaStack(3));
    }

    @Test
    public void test_loadr() {
        /*
         * stack: [10, 20, 30], fp = 0 (vaikeväärtus)
         * LOADR 2 1 → LOADRC 2; LOADM 1 → push(fp + 2) = push(2), load 1 väärtus: push(stack[2]) = 30
         */
        pw.visit(LOADR, 2, 1);

        assertInterpreted(new CMaStack(10, 20, 30, 30), new CMaStack(10, 20, 30));
    }

    @Test
    public void test_storer() {
        /*
         * stack: [10, 20, 30], fp = 0 (vaikeväärtus)
         * LOADC 99; STORER 1 1 → LOADRC 1; STOREM 1
         *   LOADRC 1: push(fp + 1) = push(1) → [10, 20, 30, 99, 1]
         *   STOREM 1: stack[1] = stack[SP-1] = 99, SP ei muutu → [10, 99, 30, 99, 1]
         */
        pw.visit(LOADC, 99);
        pw.visit(STORER, 1, 1);

        assertInterpreted(new CMaStack(10, 99, 30, 99, 1), new CMaStack(10, 20, 30));
    }

    @Test
    public void test_loadm() {
        /*
         * LOADM 2: lae 2 väärtust aadressilt S[SP]
         * stack enne: [10, 20, 30, 1]   (addr = 1, loeb stack[1] ja stack[2])
         * Tsükkel tagurpidi:
         *   i=1: S[SP+1] ← S[S[SP]+1] → S[4] ← S[2] = 30  (laiendab stacki)
         *   i=0: S[SP+0] ← S[S[SP]+0] → S[3] ← S[1] = 20  (kirjutab üle aadressi)
         * SP ← SP + 2 - 1 = SP + 1
         * stack pärast: [10, 20, 30, 20, 30]
         */
        pw.visit(LOADC, 10);
        pw.visit(LOADC, 20);
        pw.visit(LOADC, 30);
        pw.visit(LOADC, 1);              // aadress: stack[1..2] = [20, 30]
        pw.visit(LOADM, 2);

        assertInterpreted(new CMaStack(10, 20, 30, 20, 30));
    }

    @Test
    public void test_storem() {
        /*
         * STOREM 2: kirjuta 2 väärtust aadressile S[SP], SP ei muutu
         * stack enne: [0, 0, 7, 8, 0]  (addr = 0, kirjutab stack[2] ja stack[3] → stack[0] ja stack[1])
         *   i=0: S[S[SP]+0] ← S[SP-2+0] → S[0] ← S[2] = 7
         *   i=1: S[S[SP]+1] ← S[SP-2+1] → S[1] ← S[3] = 8
         * aadress (0) jääb pinule → SP ei muutu
         * stack pärast: [7, 8, 7, 8, 0]
         */
        pw.visit(STOREM, 2);

        assertInterpreted(new CMaStack(7, 8, 7, 8, 0), new CMaStack(0, 0, 7, 8, 0));
    }

    // Funktsioonikutsed: Samm 4 — MARK ja CALL käskude testimine

    @Test
    public void test_mark_call() {
        /*
         * Stack enne: []
         * LOADC 42  → [42]                    (parameeter)
         * MARK      → [42, 0, 0]              (push EP=0, push FP=0)
         * LOADC 5   → [42, 0, 0, 5]           (funktsiooni aadress)
         * CALL      → FP=3, PC=5, S[3]=4      → stack: [42, 0, 0, 4]
         * indeks 4: HALT (tagastuspunkt, siia ei jõua)
         * indeks 5: HALT (funktsioon peatub kohe)
         */
        CMaLabel _func = new CMaLabel();

        pw.visit(LOADC, 42);             // 0: parameeter
        pw.visit(MARK);                       // 1: push EP(0), push FP(0)
        pw.visit(LOADC, 5);              // 2: funktsiooni aadress (indeks 5)
        pw.visit(CALL);                       // 3: FP=3, PC=5, S[3]=4 (tagastusaadress)
        pw.visit(HALT);                       // 4: tagastuspunkt
        pw.visit(_func);                      // 5: funktsiooni algus
        pw.visit(HALT);                       // 5: funktsioon peatub kohe

        /* Stack pärast: [42, 0, 0, 4] — parameeter, salv. EP, FP, tagastusaadress */
        assertInterpreted(new CMaStack(42, 0, 0, 4));
    }

    // Funktsioonikutsed: Samm 5 — ENTER käsu testimine

    @Test
    public void test_enter() {
        /*
         * stack: [10, 20], ENTER 5 → EP = (2-1) + 5 = 6
         * MARK → push EP(6), push FP(0) → stack: [10, 20, 6, 0]
         * Kontrollime EP väärtust läbi MARK käsu
         */
        pw.visit(ENTER, 5);
        pw.visit(MARK);

        assertInterpreted(new CMaStack(10, 20, 6, 0), new CMaStack(10, 20));
    }

    @Test
    public void test_enter_in_function() {
        /*
         * MARK/CALL/ENTER tsükkel: funktsioon kutsub ja ENTER seab EP
         * LOADC 42 → [42]
         * MARK     → [42, 0, 0]
         * LOADC 5  → [42, 0, 0, 5]    (funktsiooni aadress, _func label indeksil 5)
         * CALL     → FP=3, PC=5, S[3]=4 → [42, 0, 0, 4]
         * HALT     (tagastuspunkt, indeks 4)
         * _func (indeks 5): ENTER 3 → EP = (4-1) + 3 = 6
         * MARK → push EP(6), push FP(3) → [42, 0, 0, 4, 6, 3]
         * HALT
         */
        CMaLabel _func = new CMaLabel();

        pw.visit(LOADC, 42);             // 0: parameeter
        pw.visit(MARK);                       // 1: push EP(0), push FP(0)
        pw.visit(LOADC, 5);              // 2: funktsiooni aadress (indeks 5)
        pw.visit(CALL);                       // 3: FP=3, PC=5, S[3]=4
        pw.visit(HALT);                       // 4: tagastuspunkt

        pw.visit(_func);                      // label indeksil 5
        pw.visit(ENTER, 3);              // 5: EP = (4-1) + 3 = 6
        pw.visit(MARK);                       // 6: push EP(6), push FP(3)
        pw.visit(HALT);                       // 7: funktsioon peatub

        /* Stack pärast: [42, 0, 0, 4, 6, 3] */
        assertInterpreted(new CMaStack(42, 0, 0, 4, 6, 3));
    }

    // Funktsioonikutsed: Samm 6 — RETURN käsu testimine

    @Test
    public void test_call_return() {
        /*
         * Täielik kutse-tagastus tsükkel:
         * Peaprogramm kutsub funktsiooni, mis kohe tagastab.
         *
         * 0: LOADC 42      → [42]                    (parameeter)
         * 1: MARK           → [42, 0, 0]              (push EP=0, FP=0)
         * 2: LOADC 6        → [42, 0, 0, 6]           (funktsiooni aadress)
         * 3: CALL           → FP=3, PC=6, S[3]=4      → [42, 0, 0, 4]
         * 4: HALT           (tagastuspunkt)
         *
         * _func (indeks 5):
         * 5: ENTER 0        → EP = (4-1)+0 = 3
         * 6: RETURN 3       → PC=S[3]=4, EP=S[1]=0, FP=S[2]=0
         *                     SP = 3-3 = 0, truncate(1) → [42]
         *                     PC=4 → jõuab HALT-i
         */
        CMaLabel _func = new CMaLabel();

        pw.visit(LOADC, 42);             // 0: parameeter
        pw.visit(MARK);                       // 1: push EP(0), push FP(0)
        pw.visit(LOADC, 6);              // 2: funktsiooni aadress
        pw.visit(CALL);                       // 3: FP=3, PC=5, S[3]=4
        pw.visit(HALT);                       // 4: tagastuspunkt

        pw.visit(_func);                      // label indeksil 5
        pw.visit(ENTER, 0);              // 5: EP = 3
        pw.visit(RETURN, 3);             // 6: taasta ja kärbi, q=3 (EP+FP+PC)

        /* Pärast RETURN: stack = [42], PC=4 → HALT */
        assertInterpreted(new CMaStack(42));
    }

    @Test
    public void test_call_return_with_locals() {
        /*
         * Funktsioon eraldab lokaalse muutuja, kirjutab sinna ja tagastab.
         *
         * 0: LOADC 10       → [10]                    (parameeter)
         * 1: MARK           → [10, 0, 0]
         * 2: LOADC 5        → [10, 0, 0, 5]
         * 3: CALL           → FP=3, PC=5, S[3]=4      → [10, 0, 0, 4]
         * 4: HALT
         *
         * _func (indeks 5):
         * 5: ENTER 1        → EP = (4-1)+1 = 4
         * 6: ALLOC 1        → [10, 0, 0, 4, 0]        (lokaalne muutuja indeksil 4)
         * 7: LOADC 99
         * 8: STORER 1 1     → stack[FP+1]=stack[4]=99  → [10, 0, 0, 4, 99, 99]
         * 9: POP            → [10, 0, 0, 4, 99]
         * 10: RETURN 3      → PC=4, EP=0, SP=3-3=0, FP=0, truncate(1) → [10]
         */
        CMaLabel _func = new CMaLabel();

        pw.visit(LOADC, 10);             // 0: parameeter
        pw.visit(MARK);                       // 1
        pw.visit(LOADC, 5);              // 2: funktsiooni aadress
        pw.visit(CALL);                       // 3
        pw.visit(HALT);                       // 4

        pw.visit(_func);                      // label indeksil 5
        pw.visit(ENTER, 1);              // 5
        pw.visit(ALLOC, 1);              // 6: lokaalne muutuja
        pw.visit(LOADC, 99);             // 7
        pw.visit(STORER, 1, 1);   // 8: kirjuta lokaalsesse muutujasse
        pw.visit(POP);                        // 9
        pw.visit(RETURN, 3);             // 10: tagasta, q=3

        /* Pärast RETURN: stack = [10], PC=4 → HALT */
        assertInterpreted(new CMaStack(10));
    }

    // Funktsioonikutsed: Samm 7 — SLIDE käsu testimine

    @Test
    public void test_slide() {
        /*
         * stack: [10, 20, 30], SLIDE 2 1 → kopeeri 1 väärtus (30) 2 positsiooni allapoole
         * S[SP-2-1+1] = S[SP-1+1] → S[0] = S[2] = 30 → stack: [30, 20, 30]
         * truncate(SP-2+1) = truncate(1) → stack: [30]
         */
        pw.visit(LOADC, 10);
        pw.visit(LOADC, 20);
        pw.visit(LOADC, 30);
        pw.visit(SLIDE, 2, 1);

        assertInterpreted(new CMaStack(30));
    }

    @Test
    public void test_slide_multiple_values() {
        /*
         * stack: [1, 2, 3, 4, 5], SLIDE 2 2 → kopeeri 2 väärtust (4, 5) 2 positsiooni allapoole
         * S[SP-2-2+1] = S[SP-2+1] → S[1] = S[3] = 4
         * S[SP-2-2+2] = S[SP-2+2] → S[2] = S[4] = 5
         * truncate(SP-2+1) = truncate(3) → stack: [1, 4, 5]
         */
        pw.visit(LOADC, 1);
        pw.visit(LOADC, 2);
        pw.visit(LOADC, 3);
        pw.visit(LOADC, 4);
        pw.visit(LOADC, 5);
        pw.visit(SLIDE, 2, 2);

        assertInterpreted(new CMaStack(1, 4, 5));
    }

    @Test
    public void test_slide_zero() {
        /* stack: [10, 20], SLIDE 0 1 → q=0, nihkumist pole, truncate(SP+1) → muutumatu */
        pw.visit(LOADC, 10);
        pw.visit(LOADC, 20);
        pw.visit(SLIDE, 0, 1);

        assertInterpreted(new CMaStack(10, 20));
    }

    @Test
    public void test_slide_m_zero() {
        /*
         * stack: [10, 20, 30], SLIDE 2 0 → m=0, tagastusväärtust pole, kärbi q pesa
         * SP = SP - q = 2 - 2 = 0 → truncate(1) → [10]
         */
        pw.visit(LOADC, 10);
        pw.visit(LOADC, 20);
        pw.visit(LOADC, 30);
        pw.visit(SLIDE, 2, 0);

        assertInterpreted(new CMaStack(10));
    }

    // Funktsioonikutsed: Samm 8 — JUMPI käsu testimine

    @Test
    public void test_jumpi() {
        /*
         * Lülituslause harud:
         *      väärtus 0 → case0
         *      väärtus 1 → case1
         *      väärtus 2 → case2
         * Igale lülituslause harule vastavad väärtused laetakse stackile.
         */
        CMaLabel _table = new CMaLabel();
        CMaLabel _case0 = new CMaLabel();
        CMaLabel _case1 = new CMaLabel();
        CMaLabel _case2 = new CMaLabel();

        /* Programm: LOADC index; JUMPI _table; _table: JUMP _case0; JUMP _case1; JUMP _case2 */
        pw.visit(LOADC, 0);              // 0: indeks
        pw.visit(JUMPI, _table);              // 1: PC = target(_table) + 0 = 2
        pw.visit(_table);                     // indeks 2
        pw.visit(JUMP, _case0);               // 2: hüppa case0-le
        pw.visit(JUMP, _case1);               // 3: hüppa case1-le
        pw.visit(JUMP, _case2);               // 4: hüppa case2-le
        pw.visit(_case0);
        pw.visit(LOADC, 100);
        pw.visit(HALT);
        pw.visit(_case1);
        pw.visit(LOADC, 200);
        pw.visit(HALT);
        pw.visit(_case2);
        pw.visit(LOADC, 300);
        pw.visit(HALT);

        /* Indeks 0 → case0 → stack: [100] */
        assertInterpreted(new CMaStack(100));
    }

    @Test
    public void test_jumpi_index1() {
        CMaLabel _table = new CMaLabel();
        CMaLabel _case0 = new CMaLabel();
        CMaLabel _case1 = new CMaLabel();
        CMaLabel _case2 = new CMaLabel();

        pw.visit(LOADC, 1);              // 0: indeks
        pw.visit(JUMPI, _table);              // 1: PC = target(_table) + 1 = 3
        pw.visit(_table);
        pw.visit(JUMP, _case0);               // 2: hüppa case0-le
        pw.visit(JUMP, _case1);               // 3: hüppa case1-le
        pw.visit(JUMP, _case2);               // 4: hüppa case2-le
        pw.visit(_case0);
        pw.visit(LOADC, 100);
        pw.visit(HALT);
        pw.visit(_case1);
        pw.visit(LOADC, 200);
        pw.visit(HALT);
        pw.visit(_case2);
        pw.visit(LOADC, 300);
        pw.visit(HALT);

        /* Indeks 1 → case1 → stack: [200] */
        assertInterpreted(new CMaStack(200));
    }

    @Test
    public void test_jumpi_index2() {
        CMaLabel _table = new CMaLabel();
        CMaLabel _case0 = new CMaLabel();
        CMaLabel _case1 = new CMaLabel();
        CMaLabel _case2 = new CMaLabel();

        pw.visit(LOADC, 2);              // 0: indeks
        pw.visit(JUMPI, _table);              // 1: PC = target(_table) + 2 = 4
        pw.visit(_table);
        pw.visit(JUMP, _case0);               // 2: hüppa case0-le
        pw.visit(JUMP, _case1);               // 3: hüppa case1-le
        pw.visit(JUMP, _case2);               // 4: hüppa case2-le
        pw.visit(_case0);
        pw.visit(LOADC, 100);
        pw.visit(HALT);
        pw.visit(_case1);
        pw.visit(LOADC, 200);
        pw.visit(HALT);
        pw.visit(_case2);
        pw.visit(LOADC, 300);
        pw.visit(HALT);

        /* Indeks 2 → case2 → stack: [300] */
        assertInterpreted(new CMaStack(300));
    }
}
