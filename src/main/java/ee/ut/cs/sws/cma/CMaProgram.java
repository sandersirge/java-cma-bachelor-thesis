package ee.ut.cs.sws.cma;

import ee.ut.cs.sws.cma.instruction.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public record CMaProgram(List<CMaInstruction<?>> instructions, Map<CMaLabel, Integer> labels) {

    private String render(boolean vamMode) {
        Set<Map.Entry<CMaLabel, Integer>> remainingLabelEntries = new HashMap<>(labels).entrySet();
        StringJoiner joiner = new StringJoiner("\n");

        for (int i = 0; i < instructions.size(); i++) {
            CMaInstruction<?> instruction = instructions.get(i);
            StringBuilder builder = new StringBuilder();
            for (Iterator<Map.Entry<CMaLabel, Integer>> iterator = remainingLabelEntries.iterator(); iterator.hasNext(); ) {
                Map.Entry<CMaLabel, Integer> labelEntry = iterator.next();
                if (labelEntry.getValue() == i) {
                    builder.append(labelEntry.getKey()).append(": ");
                    iterator.remove();
                }
            }
            builder.append(vamMode ? toVamInstruction(instruction) : instruction.toString());
            joiner.add(builder.toString());
        }

        if (!remainingLabelEntries.isEmpty()) {
            // print remaining (out of bounds) labels at end, Vam can handle
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<CMaLabel, Integer> labelEntry : remainingLabelEntries) {
                builder.append(labelEntry.getKey()).append(": ");
            }
            joiner.add(builder.toString());
        }

        return joiner.toString();
    }

    /**
     * Teisendab ühe käsu Vam-iga ühilduvasse formaati.
     * <ul>
     *   <li>{@code LOADLC label} → {@code LOADC <resolved address>}</li>
     *   <li>{@code RETURN q} → {@code RETURN} (Vam eeldab vaikimisi q=3)</li>
     *   <li>{@code LOADR j m}, {@code STORER j m}, {@code SLIDE q m} → ainult esimene argument</li>
     * </ul>
     */
    private String toVamInstruction(CMaInstruction<?> instruction) {
        return switch (instruction) {
            case CMaLabelInstruction(CMaLabelInstruction.Code code, CMaLabel label) -> {
                if (code == CMaLabelInstruction.Code.LOADLC) {
                    yield "LOADC " + label;
                }
                yield instruction.toString();
            }
            case CMaIntInstruction(CMaIntInstruction.Code code, int arg) -> {
                if (code == CMaIntInstruction.Code.RETURN) {
                    yield "RETURN";
                }
                yield instruction.toString();
            }
            case CMaIntIntInstruction(CMaIntIntInstruction.Code code, int arg1, int arg2) ->
                    code.name() + " " + arg1;
            default -> instruction.toString();
        };
    }

    @Override
    public String toString() {
        return render(false);
    }

    public CMaProgram append(CMaProgram other) {
        List<CMaInstruction<?>> instructions = new ArrayList<>();
        instructions.addAll(this.instructions);
        instructions.addAll(other.instructions);

        Map<CMaLabel, Integer> labels = new HashMap<>();
        labels.putAll(this.labels);
        labels.putAll(other.labels.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue() + this.instructions.size())));

        return new CMaProgram(instructions, labels);
    }

    /**
     * Tagastab programmi esituse Vam-iga ühilduvas formaadis koos algpinu laadimiskäskudega.
     *
     * <p>Teisendused:</p>
     * <ul>
     *   <li>{@code LOADLC label} → {@code LOADC <lahendatud aadress>}</li>
     *   <li>{@code RETURN q} → {@code RETURN} (Vam eeldab vaikimisi q=3)</li>
     *   <li>{@code LOADR j m}, {@code STORER j m}, {@code SLIDE q m} → ainult esimene argument</li>
     * </ul>
     *
     * <p>See on ohutu eeldusel, et kõik muutujad on ühe sõna suurused ({@code int}),
     * tagastustüübid on {@code void} või {@code int}, ning RETURN argument on alati 3.</p>
     */
    public String toString(CMaStack initialStack) {
        return initialStack.toLoadProgram().append(this).render(true);
    }

    public void toFile(Path path, CMaStack initialStack) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.writeString(path, toString(initialStack));
    }
}
