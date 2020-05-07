package cma;

// DO NOT UPLOAD: courses lehel demo

import java.io.IOException;

// Impordime vajalike käske. Nad on erinevates klassides, sest nad vajavad
// erinev arv argumente (kompilaator hoiatab kui kasutate valesti).
import static cma.instruction.CMaBasicInstruction.Code.*;
import static cma.instruction.CMaIntInstruction.Code.*;
import static cma.instruction.CMaLabelInstruction.Code.*;

public class CMaDemo {

    public static void main(String[] args) throws IOException {

        // Magasini algväärtustamine: loadc 5; loadc 1; loadc 1
        CMaStack initialStack = new CMaStack(5,1,1);

        // Märgete jaoks kasutame CMaLabel tüüpi isendid
        CMaLabel _while = new CMaLabel();
        CMaLabel _end = new CMaLabel();

        CMaProgramWriter pw = new CMaProgramWriter();

        // while (i <= n)
        pw.visit(_while);
        pw.visit(LOADA, 1);
        pw.visit(LOADA, 0);
        pw.visit(LEQ);
        pw.visit(JUMPZ, _end);

        // r = r * i;
        pw.visit(LOADA, 2);
        pw.visit(LOADA, 1);
        pw.visit(MUL);
        pw.visit(STOREA, 2);
        pw.visit(POP);

        // i = i + 1;
        pw.visit(LOADA, 1);
        //pw.visit(LOADC, 0);
        pw.visit(LOADC, 1);
        pw.visit(ADD);
        pw.visit(STOREA, 1);
        pw.visit(POP);

        pw.visit(JUMP, _while);
        pw.visit(_end);

        // Nüüd loome programmi ja kirjutame faili
        CMaProgram program = pw.toProgram();
        program.toFile("demo.cma", initialStack);

        // Võime ka programmi käivitada ja saame tulemuseks uue magasini seisundi
        CMaStack finalStack = CMaInterpreter.run(program, initialStack);
        System.out.println(finalStack);
    }
}
