import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class GraphvizExporter {

    /**
     * Generates a Graphviz DOT file representing the given AFN 
     * @param afn The AFN instance to be exported
     * @param file The path where the DOT file will be saved
     */
    public static Path exportToDot(AFN afn, Path file) throws IOException {
        Path output = file.toAbsolutePath();
        Files.createDirectories(output.getParent());

        try (PrintWriter writer = new PrintWriter(
                Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
            writer.println("digraph AFN {");
            writer.println("    rankdir=LR;"); 
            writer.println("    node [shape = circle];");
            writer.println();

            writer.println("    // Initial State");
            writer.println("    start [shape = point, style = invis];");
            writer.println("    start -> " + afn.getStart().getId() + ";");
            writer.println();

            writer.println("    // Final States");
            Set<State> visited = new HashSet<>();
            markAcceptanceNodes(afn.getStart(), visited, writer);
            writer.println();

            writer.println("    // Transitions");
            visited.clear();
            writeTransitions(afn.getStart(), visited, writer);

            writer.println("}");
        }
        return output;
    }

    private static void markAcceptanceNodes(State current, Set<State> visited, PrintWriter writer) {
        if (current == null || visited.contains(current)) return;
        visited.add(current);

        if (current.isAcceptance()) {
            writer.println("    " + current.getId() + " [shape = doublecircle];");
        }

        for (Transition t : current.getTransitions()) {
            markAcceptanceNodes(t.getTarget(), visited, writer);
        }
    }

    private static void writeTransitions(State current, Set<State> visited, PrintWriter writer) {
        if (current == null || visited.contains(current)) return;
        visited.add(current);

        for (Transition t : current.getTransitions()) {
            String label = t.isEpsilon() ? "ε" : String.valueOf(t.getSymbol());
            writer.println("    " + current.getId() + " -> " + t.getTarget().getId()
                    + " [label=\"" + label.replace("\\", "\\\\").replace("\"", "\\\"") + "\"];");
            writeTransitions(t.getTarget(), visited, writer);
        }
    }
}
