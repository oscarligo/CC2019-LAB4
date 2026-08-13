import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThompsonBuilder {

    // ID counter for generating unique state IDs
    private int stateCounter = 0;

    /**
     * Builds an AFN from a postfix expression.
     * 
     * @param postfix The postfix expression representing the regular expression
     * @return The complete AFN instance generated
     */
    public AFN build(List<String> postfix) {
        Deque<AFNFragment> stack = new ArrayDeque<>();
        Set<Character> alphabet = new HashSet<>();

        for (String token : postfix) {
            switch (token) {
                case "*":
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '*'");
                    }
                    stack.push(buildKleene(stack.pop()));
                    break;

                case "+":
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '+'");
                    }
                    stack.push(buildPlus(stack.pop()));
                    break;

                case "?":
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '?'");
                    }
                    stack.push(buildOptional(stack.pop()));
                    break;

                case "·":
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '·'");
                    }
                    AFNFragment rightConcat = stack.pop();
                    AFNFragment leftConcat = stack.pop();
                    stack.push(buildConcat(leftConcat, rightConcat));
                    break;

                case "|":
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '|'");
                    }
                    AFNFragment rightUnion = stack.pop();
                    AFNFragment leftUnion = stack.pop();
                    stack.push(buildUnion(leftUnion, rightUnion));
                    break;

                default:
                    if (token.equals("^")) {
                        throw new IllegalArgumentException("El operador '^' no está soportado por Thompson");
                    }
                    char symbol = literal(token);
                    if (token.equals("ε")) {
                        stack.push(buildBasic(null));
                        break;
                    }
                    alphabet.add(symbol);
                    stack.push(buildBasic(symbol));
                    break;
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("La expresión posfija no está bien formada.");
        }

        // Final AFN construction
        AFNFragment finalFragment = stack.pop();
        
        // Mark the end state as an acceptance state
        finalFragment.end.setAcceptance(true);

        return new AFN(finalFragment.start, finalFragment.end, alphabet);
    }

    public AFN build(String postfix) {
        List<String> tokens = new ArrayList<>();
        for (char token : postfix.toCharArray()) {
            tokens.add(token == '.' ? "·" : String.valueOf(token));
        }
        return build(tokens);
    }

    private char literal(String token) {
        if (token.length() == 1) {
            return token.charAt(0);
        }
        if (token.length() == 2 && token.charAt(0) == '\\') {
            return token.charAt(1);
        }
        throw new IllegalArgumentException("Símbolo inválido: " + token);
    }

    
    // Builds a basic AFN for a single character
    private AFNFragment buildBasic(Character symbol) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(symbol, end);
        return new AFNFragment(start, end);
    }

    // Builds a concatenation of two AFN fragments
    private AFNFragment buildConcat(AFNFragment left, AFNFragment right) {
        left.end.addTransition(null, right.start);
        return new AFNFragment(left.start, right.end);
    }

    // Builds a union of two AFN fragments
    private AFNFragment buildUnion(AFNFragment left, AFNFragment right) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition(null, left.start);  // ε to N1
        start.addTransition(null, right.start); // ε to N2

        left.end.addTransition(null, end);  // ε from N1 to new final
        right.end.addTransition(null, end); // ε from N2 to new final

        return new AFNFragment(start, end);
    }

    private AFNFragment buildKleene(AFNFragment fragment) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);

        start.addTransition(null, fragment.start); // ε to N
        start.addTransition(null, end);            // ε to new final
        
        fragment.end.addTransition(null, fragment.start); // ε to loop back to N
        fragment.end.addTransition(null, end);            // ε to new final

        return new AFNFragment(start, end);
    }

    private AFNFragment buildPlus(AFNFragment fragment) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(null, fragment.start);
        fragment.end.addTransition(null, fragment.start);
        fragment.end.addTransition(null, end);
        return new AFNFragment(start, end);
    }

    private AFNFragment buildOptional(AFNFragment fragment) {
        State start = new State(stateCounter++);
        State end = new State(stateCounter++);
        start.addTransition(null, fragment.start);
        start.addTransition(null, end);
        fragment.end.addTransition(null, end);
        return new AFNFragment(start, end);
    }

    public static void selfTest() {
        ThompsonBuilder builder = new ThompsonBuilder();
        AFN plus = builder.build(Arrays.asList("a", "+"));
        AFN optional = builder.build(Arrays.asList("a", "?", "b", "·"));
        AFN epsilon = builder.build(Arrays.asList("ε"));

        if (!plus.accepts("a") || !plus.accepts("aaaa") || plus.accepts("")
                || !optional.accepts("b") || !optional.accepts("ab") || optional.accepts("a")
                || !epsilon.accepts("") || epsilon.accepts("ε")) {
            throw new AssertionError("La construcción o evaluación de AFN falló");
        }
        System.out.println("Test passed: Thompson AFN evaluation");
    }

    private static class AFNFragment {
        State start;
        State end;

        AFNFragment(State start, State end) {
            this.start = start;
            this.end = end;
        }
    }
}
