package ee.ut.cs.sws.cma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static ee.ut.cs.sws.cma.instruction.CMaIntInstruction.Code.LOADC;

public class CMaStack {

    private final List<Integer> data;

    public CMaStack() {
        data = new ArrayList<>();
    }

    public CMaStack(List<Integer> data) {
        this.data = new ArrayList<>(data);
    }

    public CMaStack(CMaStack stack) {
        this(stack.data);
    }

    public CMaStack(int... data) {
        this(Arrays.stream(data).boxed().toList());
    }

    public void push(int value) {
        data.add(value);
    }

    public int peek() {
        return data.getLast();
    }

    public int pop() {
        int value = peek();
        data.removeLast();
        return value;
    }

    public void set(int index, int value) {
        data.set(index, value);
    }

    public int get(int index) {
        return data.get(index);
    }

    public int size() {
        return data.size();
    }

    /** Kasvata stacki m nulliga algväärtustatud pesa võrra. SP += m */
    public void allocate(int m) {
        for (int i = 0; i < m; i++)
            data.add(0);
    }

    /** Kärbi stack uuele suurusele. SP = newSize - 1 */
    public void truncate(int newSize) {
        data.subList(newSize, data.size()).clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CMaStack stack = (CMaStack) o;
        return Objects.equals(data, stack.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public String toString() {
        return data.toString();
    }

    public CMaProgram toLoadProgram() {
        CMaProgramWriter pw = new CMaProgramWriter();
        for (int value : data)
            pw.visit(LOADC, value);
        return pw.toProgram();
    }
}
