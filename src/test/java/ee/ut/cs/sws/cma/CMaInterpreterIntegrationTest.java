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
public class CMaInterpreterIntegrationTest {

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

    // Funktsioonikutsed: Samm 9 — Integratsioonitestid kõikide käskude testimiseks erinevates töövoogudes

    /**
     * Ehitab ja käivitab programmi, kus {@code run(n)} kutsub mitterekursiivset {@code inc(x)}.
     *
     * <p>C-kood:</p>
     * <pre>{@code
     * int inc(int x) { return x + 1; }
     * int run(int n) {
     *     int r = inc(n);
     *     return r;
     * }
     * run(<n>) // <n> väärtustatakse igas testis erinevalt
     * }</pre>
     *
     * <p>Oodatavad tulemused:</p>
     * <ul>
     *   <li>{@code n = 0} → {@code inc(0) = 1}</li>
     *   <li>{@code n = 9} → {@code inc(9) = 10}</li>
     *   <li>{@code n = 41} → {@code inc(41) = 42}</li>
     *   <li>{@code n = -1} → {@code inc(-1) = 0}</li>
     * </ul>
     */
    private void runNonRecursive(int n) {
        pw = new CMaProgramWriter();

        CMaLabel _run = new CMaLabel();
        CMaLabel _inc = new CMaLabel();

        /* === Peaprogramm (indeksid 0-5): kutsub run(n) === */
        pw.visit(LOADC, n);                   // 0: muutuja 'n' väärtus, run parameeter
        pw.visit(MARK);                       // 1: push EP, FP
        pw.visit(LOADC, 6);              // 2: _run aadress = 6
        pw.visit(CALL);                       // 3
        pw.visit(SLIDE, 0, 1);    // 4: tõsta tulemus
        pw.visit(HALT);                       // 5

        /*
         * === run(n) funktsioon (indeksid 6-17) ===
         * int run(int n) { int r = inc(n); return r; }
         * Lokaalsed: muutuja 'r' on FP+1
         */
        pw.visit(_run);                       // label indeksil 6
        pw.visit(ENTER, 1);              // 6: EP = SP + 1 (lokaalne muutuja 'r')
        pw.visit(ALLOC, 1);              // 7: eralda koht muutujale 'r'

        /* kutsu inc(muutuja 'n') */
        pw.visit(LOADR, -3, 1);   // 8: laadi muutuja 'n'
        pw.visit(MARK);                       // 9: push EP, FP
        pw.visit(LOADC, 18);             // 10: _inc aadress = 18
        pw.visit(CALL);                       // 11
        pw.visit(SLIDE, 0, 1);    // 12: tõsta inc tulemus

        pw.visit(STORER, 1, 1);   // 13: muutuja 'r' = inc(muutuja 'n')
        pw.visit(POP);                        // 14: eemalda STORER duplikaat

        /* tagasta muutuja 'r' */
        pw.visit(LOADR, 1, 1);    // 15: laadi muutuja 'r'
        pw.visit(STORER, -3, 1);  // 16: kirjuta tulemus parameetri pessa
        pw.visit(RETURN, 3);             // 17

        /*
         * === inc(x) funktsioon (indeksid 18-23) ===
         * int inc(int x) { return x + 1; }
         */
        pw.visit(_inc);                       // label indeksil 18
        pw.visit(ENTER, 0);              // 18: lokaalseid muutujaid pole
        pw.visit(LOADR, -3, 1);   // 19: laadi muutuja 'x'
        pw.visit(LOADC, 1);              // 20
        pw.visit(ADD);                        // 21: muutuja 'x' + 1
        pw.visit(STORER, -3, 1);  // 22: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 23

        assertInterpreted(new CMaStack(n + 1));
    }

    @Test
    public void test_non_recursive_inc_0() {
        /* run(0): inc(0) = 1 */
        runNonRecursive(0);
    }

    @Test
    public void test_non_recursive_inc_9() {
        /* run(9): inc(9) = 10 */
        runNonRecursive(9);
    }

    @Test
    public void test_non_recursive_inc_41() {
        /* run(41): inc(41) = 42 */
        runNonRecursive(41);
    }

    @Test
    public void test_non_recursive_inc_neg() {
        /* run(-1): inc(-1) = 0 */
        runNonRecursive(-1);
    }

    /**
     * Rekursiivne {@code fac(n)} funktsioon, mida kutsub {@code run(n)}.
     *
     * <p>C-kood:</p>
     * <pre>{@code
     * int fac(int n) {
     *     if (n <= 0) return 1;
     *     return n * fac(n - 1);
     * }
     * int run(int n) {
     *     int r = fac(n);
     *     return r;
     * }
     * run(<n>) // <n> väärtustatakse igas testis erinevalt
     * }</pre>
     *
     * <p>Oodatavad tulemused:</p>
     * <ul>
     *   <li>{@code n = 0} → {@code fac(0) = 1}</li>
     *   <li>{@code n = 1} → {@code fac(1) = 1}</li>
     *   <li>{@code n = 5} → {@code fac(5) = 120}</li>
     *   <li>{@code n = 10} → {@code fac(10) = 3628800}</li>
     * </ul>
     */
    private CMaStack runFac(int n) {
        pw = new CMaProgramWriter();

        CMaLabel _fac = new CMaLabel();
        CMaLabel _recurse = new CMaLabel();
        CMaLabel _run = new CMaLabel();

        /* === Peaprogramm (indeksid 0-5): kutsub run(n) === */
        pw.visit(LOADC, n);                   // 0: muutuja 'n' väärtus, run parameetriks
        pw.visit(MARK);                       // 1: push EP, FP
        pw.visit(LOADC, 6);              // 2: _run aadress = 6
        pw.visit(CALL);                       // 3: FP=3, PC=6, S[3]=4
        pw.visit(SLIDE, 0, 1);    // 4: tõsta tulemus
        pw.visit(HALT);                       // 5

        /*
         * === run(n) funktsioon (indeksid 6-17) ===
         * int run(int n) { int r = fac(n); return r; }
         * Lokaalsed: muutuja 'r' on FP+1
         */
        pw.visit(_run);                       // label indeksil 6
        pw.visit(ENTER, 1);              // 6: EP = SP + 1 (lokaalne muutuja 'r')
        pw.visit(ALLOC, 1);              // 7: eralda koht muutujale 'r'

        /* kutsu fac(muutuja 'n'): muutuja 'n' asub FP-3 */
        pw.visit(LOADR, -3, 1);   // 8: laadi muutuja 'n'
        pw.visit(MARK);                       // 9: push EP, FP
        pw.visit(LOADC, 18);             // 10: _fac aadress = 18
        pw.visit(CALL);                       // 11
        pw.visit(SLIDE, 0, 1);    // 12: tõsta fac tulemus

        pw.visit(STORER, 1, 1);   // 13: muutuja 'r' = fac(muutuja 'n')
        pw.visit(POP);                        // 14: eemalda STORER duplikaat

        /* tagasta muutuja 'r': kopeeri muutuja 'r' parameetri pessa */
        pw.visit(LOADR, 1, 1);    // 15: laadi muutuja 'r'
        pw.visit(STORER, -3, 1);  // 16: kirjuta tulemus parameetri pessa
        pw.visit(RETURN, 3);             // 17

        /* === fac(n) funktsioon (indeksid 18-...) === */
        pw.visit(_fac);                       // label indeksil 18
        pw.visit(ENTER, 0);              // 18: lokaalseid muutujaid pole

        /* kui muutuja 'n' <= 0, tagasta 1 */
        pw.visit(LOADR, -3, 1);   // 19: laadi muutuja 'n'
        pw.visit(LOADC, 0);              // 20
        pw.visit(LEQ);                        // 21: muutuja 'n' <= 0?
        pw.visit(JUMPZ, _recurse);            // 22: kui n > 0, hüppa rekursiivsesse harusse
        pw.visit(LOADC, 1);              // 23: baassjuht: tulemus = 1
        pw.visit(STORER, -3, 1);  // 24: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 25

        /* rekursiivne juht: tagasta muutuja 'n' * fac(muutuja 'n' - 1) */
        pw.visit(_recurse);                   // label indeksil 26
        pw.visit(LOADR, -3, 1);   // 26: laadi muutuja 'n' (korrutamiseks)
        pw.visit(LOADR, -3, 1);   // 27: laadi muutuja 'n' (fac argumendiks)
        pw.visit(LOADC, 1);              // 28
        pw.visit(SUB);                        // 29: muutuja 'n' - 1
        pw.visit(MARK);                       // 30: push EP, FP
        pw.visit(LOADC, 18);             // 31: _fac aadress = 18
        pw.visit(CALL);                       // 32
        pw.visit(SLIDE, 0, 1);    // 33: tõsta fac tulemus
        pw.visit(MUL);                        // 34: muutuja 'n' * fac(muutuja 'n' - 1)
        pw.visit(STORER, -3, 1);  // 35: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 36

        return CMaInterpreter.run(pw.toProgram());
    }

    @Test
    public void test_recursive_fac_0() {
        /* run(0): fac(0) = 1 */
        assertEquals(new CMaStack(1), runFac(0));
    }

    @Test
    public void test_recursive_fac_1() {
        /* run(1): fac(1) = 1 */
        assertEquals(new CMaStack(1), runFac(1));
    }

    @Test
    public void test_recursive_fac_5() {
        /* run(5): fac(5) = 120 */
        assertEquals(new CMaStack(120), runFac(5));
    }

    @Test
    public void test_recursive_fac_10() {
        /* run(10): fac(10) = 3628800 */
        assertEquals(new CMaStack(3628800), runFac(10));
    }

    /**
     * {@code inc(x, step)} funktsioon, mida kutsub lülituslausega {@code run(op)}.
     *
     * <p>C-kood:</p>
     * <pre>{@code
     * int inc(int x, int step) { return x + step; }
     * int run(int op) {
     *     int n = 5;
     *     int r;
     *     switch (op) {
     *         case 0: r = inc(n, 1); break;  // LOADRC, MARK, CALL, SLIDE 0 1
     *         case 1: r = n * 2;     break;  // LOADR, MUL, STORER
     *     }
     *     return r;
     * }
     * run(<op>) // <op> väärtustatakse igas testis erinevalt
     * }</pre>
     *
     * <p>Oodatavad tulemused:</p>
     * <ul>
     *   <li>{@code op=0} → {@code inc(5, 1) = 6}</li>
     *   <li>{@code op=1} → {@code 5 * 2 = 10}</li>
     * </ul>
     */
    private CMaStack runDispatch(int op) {
        pw = new CMaProgramWriter();

        CMaLabel _inc   = new CMaLabel();
        CMaLabel _run   = new CMaLabel();
        CMaLabel _table = new CMaLabel();
        CMaLabel _case0 = new CMaLabel();
        CMaLabel _case1 = new CMaLabel();
        CMaLabel _end   = new CMaLabel();

        /* Peaprogramm (0–5): kutsub run(op). */
        pw.visit(LOADC, op);                  // 0:  muutuja 'op' → parameeter run-ile
        pw.visit(MARK);                       // 1:  push EP(0), FP(0)
        pw.visit(LOADC, 12);             // 2:  _run aadress = 12
        pw.visit(CALL);                       // 3:  FP=3, PC=12, S[3]=4
        pw.visit(SLIDE, 0, 1);    // 4:  tõsta tulemus parameetri kohalt
        pw.visit(HALT);                       // 5:  peaprogramm lõpeb

        /*
         * inc(muutuja 'x', muutuja 'step') (6–11): tagastab muutuja 'x' + muutuja 'step'.
         * FP-4: muutuja 'x' (esimene arg), FP-3: muutuja 'step' (teine arg).
         * RETURN 4 eemaldab muutuja 'step' pesa ja org-pesad (EP, FP, PC).
         * Käsud: ENTER, LOADR, ADD, STORER, RETURN.
         */
        pw.visit(_inc);
        pw.visit(ENTER, 0);              // 6:  lokaalseid muutujaid pole; EP = SP+0
        pw.visit(LOADR, -4, 1);   // 7:  laadi muutuja 'x' (FP-4, esimene arg)
        pw.visit(LOADR, -3, 1);   // 8:  laadi muutuja 'step' (FP-3, teine arg)
        pw.visit(ADD);                        // 9:  muutuja 'x' + muutuja 'step'
        pw.visit(STORER, -4, 1);  // 10: tulemus → muutuja 'x' pessa (FP-4)
        pw.visit(RETURN, 4);             // 11: eemaldab muutuja 'step' ja org-pesad

        /*
         * run(muutuja 'op') (12–18): eraldab muutuja 'n'=5 ja muutuja 'r', valib haru JUMPI abil.
         * Käsud: ENTER, ALLOC, STORER, LOADR, JUMPI.
         */
        pw.visit(_run);
        pw.visit(ENTER, 2);              // 12: EP = SP+2 (2 lokaalset: n, r)
        pw.visit(ALLOC, 2);              // 13: eralda koht muutujatele 'n' ja 'r'
        pw.visit(LOADC, 5);              // 14: push 5
        pw.visit(STORER, 1, 1);   // 15: muutuja 'n' = 5 (FP+1)
        pw.visit(POP);                        // 16: eemalda STORER duplikaat
        pw.visit(LOADR, -3, 1);   // 17: laadi muutuja 'op' (FP-3) — JUMPI indeks
        pw.visit(JUMPI, _table);              // 18: PC = target(_table) + op

        /*
         * Lülituslause harud (19–20): JUMPI sihtmärgid.
         * muutuja 'op'=0 → _case0 (21), muutuja 'op'=1 → _case1 (31).
         */
        pw.visit(_table);
        pw.visit(JUMP, _case0);               // 19: op=0 → kutsu inc(n, 1)
        pw.visit(JUMP, _case1);               // 20: op=1 → arvuta n*2

        /*
         * case 0 (21–30): muutuja 'r' = inc(muutuja 'n', 1).
         * Laadi muutuja 'n' (LOADRC+LOAD) ja muutuja 'step'=1, kutsu inc.
         * SLIDE 0 1 on no-op — tulemus on pärast RETURN 4 juba pinul.
         * Käsud: LOADRC, LOAD, LOADC, MARK, CALL, SLIDE 0 1, STORER.
         */
        pw.visit(_case0);
        pw.visit(LOADRC, 1);             // 21: laadi muutuja 'n' aadress (FP+1) — LOADRC otse
        pw.visit(LOAD);                       // 22: loe muutuja 'n' väärtus
        pw.visit(LOADC, 1);              // 23: muutuja 'step'=1 (teine arg inc-ile)
        pw.visit(MARK);                       // 24: push EP, FP
        pw.visit(LOADC, 6);              // 25: _inc aadress = 6
        pw.visit(CALL);                       // 26: FP=10, PC=6, S[10]=27
        pw.visit(SLIDE, 0, 1);    // 27: tulemus juba pinul (no-op)
        pw.visit(STORER, 2, 1);   // 28: muutuja 'r' = tulemus (FP+2)
        pw.visit(POP);                        // 29: eemalda STORER duplikaat
        pw.visit(JUMP, _end);                 // 30: hüppa tagastusele

        /*
         * case 1 (31–35): muutuja 'r' = muutuja 'n' * 2.
         * Käsud: LOADR, LOADC, MUL, STORER.
         */
        pw.visit(_case1);
        pw.visit(LOADR, 1, 1);    // 31: laadi muutuja 'n' (FP+1)
        pw.visit(LOADC, 2);              // 32: push 2
        pw.visit(MUL);                        // 33: muutuja 'n' * 2 = 10
        pw.visit(STORER, 2, 1);   // 34: muutuja 'r' = muutuja 'n'*2 (FP+2)
        pw.visit(POP);                        // 35: eemalda STORER duplikaat

        /*
         * Tagastus (36–38): return r.
         * Käsud: LOADR, STORER, RETURN.
         */
        pw.visit(_end);
        pw.visit(LOADR, 2, 1);    // 36: laadi muutuja 'r' (FP+2)
        pw.visit(STORER, -3, 1);  // 37: tulemus → parameetri pessa (FP-3)
        pw.visit(RETURN, 3);             // 38: tagasta

        return CMaInterpreter.run(pw.toProgram());
    }

    @Test
    public void test_dispatch_inc() {
        /* run(0): inc(5, 1) = 6 — kasutab LOADRC, MARK, CALL, SLIDE 0 1 */
        assertEquals(new CMaStack(6), runDispatch(0));
    }

    @Test
    public void test_dispatch_double() {
        /* run(1): 5 * 2 = 10 — kasutab LOADR, MUL, STORER */
        assertEquals(new CMaStack(10), runDispatch(1));
    }

    /**
     * {@code void compute(x)} funktsioon lokaalsete muutujatega, mida kutsub {@code run(n)}.
     *
     * <p>C-kood:</p>
     * <pre>{@code
     * void compute(int x) {
     *     int a = x * 2;
     *     int b = a + 1;
     *     return;
     * }
     * int run(int n) {
     *     compute(n);
     *     return n * 2;
     * }
     * run(<n>) // <n> väärtustatakse igas testis erinevalt
     * }</pre>
     *
     * <p>Oodatavad tulemused:</p>
     * <ul>
     *   <li>{@code n = 0} → {@code 0 * 2 = 0}</li>
     *   <li>{@code n = 3} → {@code 3 * 2 = 6}</li>
     *   <li>{@code n = -4} → {@code -4 * 2 = -8}</li>
     * </ul>
     *
     * <p>Programmi aadressid:</p>
     * <ul>
     *   <li>{@code 0–5}: peaprogramm</li>
     *   <li>{@code 6–18}: {@code compute(x)}</li>
     *   <li>{@code 19–28}: {@code run(n)}</li>
     * </ul>
     */
    private void runVoidReturn(int n) {
        pw = new CMaProgramWriter();

        CMaLabel _compute = new CMaLabel();
        CMaLabel _run     = new CMaLabel();

        /* === Peaprogramm (indeksid 0-5): kutsub run(n) === */
        pw.visit(LOADC, n);                   // 0: muutuja 'n' väärtus, run parameeter
        pw.visit(MARK);                       // 1: push EP, FP
        pw.visit(LOADC, 19);             // 2: _run aadress = 19
        pw.visit(CALL);                       // 3
        pw.visit(SLIDE, 0, 1);    // 4: tõsta tulemus
        pw.visit(HALT);                       // 5

        /*
         * === compute(x) funktsioon (indeksid 6-18) ===
         * void compute(int x) {
         *     int a = x * 2;
         *     int b = a + 1;
         *     return;
         * }
         * Lokaalsed: muutuja 'a' on FP+1, muutuja 'b' on FP+2.
         * Tagastustüüp void: RETURN 4 (q = 1 + 3 - 0 = 4).
         */
        pw.visit(_compute);                   // label indeksil 6
        pw.visit(ENTER, 2);              // 6:  EP = SP + 2 (2 lokaalset: a, b)
        pw.visit(ALLOC, 2);              // 7:  eralda koht muutujatele 'a' ja 'b'

        /* muutuja 'a' = muutuja 'x' * 2 */
        pw.visit(LOADR, -3, 1);   // 8:  laadi muutuja 'x' (FP-3)
        pw.visit(LOADC, 2);              // 9
        pw.visit(MUL);                        // 10: muutuja 'x' * 2
        pw.visit(STORER, 1, 1);   // 11: muutuja 'a' = muutuja 'x' * 2 (FP+1)
        pw.visit(POP);                        // 12: eemalda STORER duplikaat

        /* muutuja 'b' = muutuja 'a' + 1 */
        pw.visit(LOADR, 1, 1);    // 13: laadi muutuja 'a' (FP+1)
        pw.visit(LOADC, 1);              // 14
        pw.visit(ADD);                        // 15: muutuja 'a' + 1
        pw.visit(STORER, 2, 1);   // 16: muutuja 'b' = muutuja 'a' + 1 (FP+2)
        pw.visit(POP);                        // 17: eemalda STORER duplikaat

        pw.visit(RETURN, 4);             // 18: void tagastus, eemaldab param + lokaalsed + org-pesad

        /*
         * === run(n) funktsioon (indeksid 19-28) ===
         * int run(int n) { compute(n); return n * 2; }
         * Lokaalseid muutujaid pole.
         */
        pw.visit(_run);                       // label indeksil 19
        pw.visit(ENTER, 0);              // 19: lokaalseid muutujaid pole

        /* kutsu compute(muutuja 'n') */
        pw.visit(LOADR, -3, 1);   // 20: laadi muutuja 'n'
        pw.visit(MARK);                       // 21: push EP, FP
        pw.visit(LOADC, 6);              // 22: _compute aadress = 6
        pw.visit(CALL);                       // 23
        /* void tagastus: pinu on taastatud, SLIDE pole vaja */

        /* tagasta muutuja 'n' * 2 */
        pw.visit(LOADR, -3, 1);   // 24: laadi muutuja 'n'
        pw.visit(LOADC, 2);              // 25
        pw.visit(MUL);                        // 26: muutuja 'n' * 2
        pw.visit(STORER, -3, 1);  // 27: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 28

        assertInterpreted(new CMaStack(n * 2));
    }

    @Test
    public void test_void_return_0() {
        /* run(0): compute(0); return 0*2 = 0 */
        runVoidReturn(0);
    }

    @Test
    public void test_void_return_3() {
        /* run(3): compute(3); return 3*2 = 6 */
        runVoidReturn(3);
    }

    @Test
    public void test_void_return_neg() {
        /* run(-4): compute(-4); return -4*2 = -8 */
        runVoidReturn(-4);
    }

    private static int fibExpected(int n) {
        if (n < 0) return -1;
        if (n <= 1) return n;
        int a = 0, b = 1;
        for (int i = 2; i <= n; i++) { int c = a + b; a = b; b = c; }
        return b;
    }

    /**
     * Rekursiivne {@code fibo(n)} funktsioon koos lülituslausega, mida kutsub {@code run(n)}.
     *
     * <p>C-kood (vt joonis 2.x):</p>
     * <pre>{@code
     * int fibo(int n) {
     *     if (n < 0) return -1;
     *     switch (n) {
     *         case 0:  return 0; break;
     *         case 1:  return 1; break;
     *         default: return fibo(n - 1) + fibo(n - 2);
     *     }
     * }
     * int run(int n) {
     *     int r = fibo(n);
     *     return r;
     * }
     * run(<n>) // <n> väärtustatakse igas testis erinevalt
     * }</pre>
     *
     * <p>Oodatavad tulemused:</p>
     * <ul>
     *   <li>{@code n < 0} → {@code -1}</li>
     *   <li>{@code n = 0} → {@code 0}</li>
     *   <li>{@code n = 1} → {@code 1}</li>
     *   <li>{@code n = 5} → {@code 5}</li>
     *   <li>{@code n = 7} → {@code 13}</li>
     * </ul>
     */
    private void runFibo(int n) {
        pw = new CMaProgramWriter();

        CMaLabel _fibo     = new CMaLabel();
        CMaLabel _run      = new CMaLabel();
        CMaLabel _non_neg  = new CMaLabel();
        CMaLabel _table    = new CMaLabel();
        CMaLabel _case0    = new CMaLabel();
        CMaLabel _case1    = new CMaLabel();
        CMaLabel _recurse  = new CMaLabel();

        /* === Peaprogramm (indeksid 0-5): kutsub run(n) === */
        pw.visit(LOADC, n);                   // 0: muutuja 'n' väärtus, run parameeter
        pw.visit(MARK);                       // 1: push EP, FP
        pw.visit(LOADC, 6);              // 2: _run aadress = 6
        pw.visit(CALL);                       // 3
        pw.visit(SLIDE, 0, 1);    // 4: tõsta tulemus
        pw.visit(HALT);                       // 5

        /*
         * === run(n) funktsioon (indeksid 6-17) ===
         * int run(int n) { int r = fibo(n); return r; }
         * Lokaalsed: muutuja 'r' on FP+1
         */
        pw.visit(_run);                       // label indeksil 6
        pw.visit(ENTER, 1);              // 6: EP = SP + 1 (lokaalne muutuja 'r')
        pw.visit(ALLOC, 1);              // 7: eralda koht muutujale 'r'

        /* kutsu fibo(muutuja 'n') */
        pw.visit(LOADR, -3, 1);   // 8: laadi muutuja 'n'
        pw.visit(MARK);                       // 9: push EP, FP
        pw.visit(LOADC, 18);             // 10: _fibo aadress = 18
        pw.visit(CALL);                       // 11
        pw.visit(SLIDE, 0, 1);    // 12: tõsta fibo tulemus

        pw.visit(STORER, 1, 1);   // 13: muutuja 'r' = fibo(muutuja 'n')
        pw.visit(POP);                        // 14: eemalda STORER duplikaat

        /* tagasta muutuja 'r' */
        pw.visit(LOADR, 1, 1);    // 15: laadi muutuja 'r'
        pw.visit(STORER, -3, 1);  // 16: kirjuta tulemus parameetri pessa
        pw.visit(RETURN, 3);             // 17

        /*
         * === fibo(n) funktsioon (indeksid 18-56) ===
         * Baassjuhud n<0, n=0, n=1 — rekursiivne juht: fibo(n-1) + fibo(n-2).
         */
        pw.visit(_fibo);                      // label indeksil 18
        pw.visit(ENTER, 0);              // 18: lokaalseid muutujaid pole

        /* kui n < 0, tagasta -1 */
        pw.visit(LOADC, 0);              // 19: push 0
        pw.visit(LOADR, -3, 1);   // 20: laadi muutuja 'n'
        pw.visit(GR);                         // 21: 0 > n, ehk n < 0?
        pw.visit(JUMPZ, _non_neg);            // 22: kui n >= 0, hüppa edasi
        pw.visit(LOADC, -1);             // 23: tulemus = -1
        pw.visit(STORER, -3, 1);  // 24: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 25

        /* lülituslause: n=0, n=1 → baassjuhud; n>1 → rekursiivne juht */
        pw.visit(_non_neg);                   // label indeksil 26
        pw.visit(LOADR, -3, 1);   // 26: laadi muutuja 'n'
        pw.visit(LOADC, 1);              // 27
        pw.visit(LEQ);                        // 28: n <= 1?
        pw.visit(JUMPZ, _recurse);            // 29: kui n > 1, hüppa rekursiivsesse harusse

        /* JUMPI lülituslause harud: n=0 → _case0, n=1 → _case1 */
        pw.visit(LOADR, -3, 1);   // 30: laadi muutuja 'n' (JUMPI indeks)
        pw.visit(JUMPI, _table);              // 31: PC = target(_table) + n

        pw.visit(_table);                     // label indeksil 32
        pw.visit(JUMP, _case0);               // 32: n=0 → tagasta 0
        pw.visit(JUMP, _case1);               // 33: n=1 → tagasta 1

        pw.visit(_case0);                     // label indeksil 34
        pw.visit(LOADC, 0);              // 34: tulemus = 0
        pw.visit(STORER, -3, 1);  // 35: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 36

        pw.visit(_case1);                     // label indeksil 37
        pw.visit(LOADC, 1);              // 37: tulemus = 1
        pw.visit(STORER, -3, 1);  // 38: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 39

        /* rekursiivne juht: fibo(n-1) + fibo(n-2) */
        pw.visit(_recurse);                   // label indeksil 40
        pw.visit(LOADR, -3, 1);   // 40: laadi muutuja 'n' (fibo(n-1) argumendiks)
        pw.visit(LOADC, 1);              // 41
        pw.visit(SUB);                        // 42: muutuja 'n' - 1
        pw.visit(MARK);                       // 43: push EP, FP
        pw.visit(LOADC, 18);             // 44: _fibo aadress = 18
        pw.visit(CALL);                       // 45
        pw.visit(SLIDE, 0, 1);    // 46: fibo(n-1) tulemus pinul

        /* fibo(n-1) on pinul — arvuta fibo(n-2) */
        pw.visit(LOADR, -3, 1);   // 47: laadi muutuja 'n' (FP taastatud)
        pw.visit(LOADC, 2);              // 48
        pw.visit(SUB);                        // 49: muutuja 'n' - 2
        pw.visit(MARK);                       // 50: push EP, FP
        pw.visit(LOADC, 18);             // 51: _fibo aadress = 18
        pw.visit(CALL);                       // 52
        pw.visit(SLIDE, 0, 1);    // 53: fibo(n-2) tulemus pinul

        pw.visit(ADD);                        // 54: fibo(n-1) + fibo(n-2)
        pw.visit(STORER, -3, 1);  // 55: tulemus → parameetri pessa
        pw.visit(RETURN, 3);             // 56

        assertInterpreted(new CMaStack(fibExpected(n)));
    }

    @Test
    public void test_fibo_neg() {
        /* run(-1): fibo(-1) = -1 */
        runFibo(-1);
    }

    @Test
    public void test_fibo_0() {
        /* run(0): fibo(0) = 0 */
        runFibo(0);
    }

    @Test
    public void test_fibo_1() {
        /* run(1): fibo(1) = 1 */
        runFibo(1);
    }

    @Test
    public void test_fibo_5() {
        /* run(5): fibo(5) = 5 */
        runFibo(5);
    }

    @Test
    public void test_fibo_7() {
        /* run(7): fibo(7) = 13 */
        runFibo(7);
    }
}
