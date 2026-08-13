import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class GraphvizExporter {

    /**
     * Generates a Graphviz DOT file representing the given AFN 
     * @param afn The AFN instance to be exported
     * @param filePath The path where the DOT file will be saved
     */
    public static void exportToDot(AFN afn, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
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
            System.out.println("DOT file generated: " + filePath);
        } catch (IOException e) {
            System.err.println("Error generating DOT file: " + e.getMessage());
        }
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
            writer.println("    " + current.getId() + " -> " + t.getTarget().getId() + " [label=\"" + label + "\"];");
            writeTransitions(t.getTarget(), visited, writer);
        }
    }
}