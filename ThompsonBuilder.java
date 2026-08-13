import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
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
    public AFN build(String postfix) {
        Deque<AFNFragment> stack = new ArrayDeque<>();
        Set<Character> alphabet = new HashSet<>();

        for (int i = 0; i < postfix.length(); i++) {
            char symbol = postfix.charAt(i);

            switch (symbol) {
                case '*': 
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '*'");
                    }
                    stack.push(buildKleene(stack.pop()));
                    break;

                case '.': 
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '.'");
                    }
                    AFNFragment rightConcat = stack.pop();
                    AFNFragment leftConcat = stack.pop();
                    stack.push(buildConcat(leftConcat, rightConcat));
                    break;

                case '|': 
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Expresión posfija inválida cerca de '|'");
                    }
                    AFNFragment rightUnion = stack.pop();
                    AFNFragment leftUnion = stack.pop();
                    stack.push(buildUnion(leftUnion, rightUnion));
                    break;

                default:
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

    
    // Builds a basic AFN for a single character
    private AFNFragment buildBasic(char symbol) {
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

    private static class AFNFragment {
        State start;
        State end;

        AFNFragment(State start, State end) {
            this.start = start;
            this.end = end;
        }
    }
}