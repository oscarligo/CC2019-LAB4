import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class AFN {
    private State start;
    private State end;
    
    private Set<Character> alphabet;
    
    private Set<State> states;

    public AFN(State start, State end, Set<Character> alphabet) {
        this.start = start;
        this.end = end;
        this.alphabet = alphabet;
        this.states = collectStates();
    }

    private Set<State> collectStates() {
        Set<State> visited = new HashSet<>();
        dfs(this.start, visited);
        return visited;
    }

    private void dfs(State current, Set<State> visited) {
        if (current == null || visited.contains(current)) return;
        visited.add(current);
        
        for (Transition t : current.getTransitions()) {
            dfs(t.getTarget(), visited);
        }
    }

    public State getStart() { return start; }
    public State getEnd() { return end; }
    public Set<Character> getAlphabet() { return alphabet; }
    public Set<State> getStates() { return states; }

    public boolean accepts(String input) {
        Set<State> current = epsilonClosure(Collections.singleton(start));

        for (char symbol : input.toCharArray()) {
            Set<State> next = new HashSet<>();
            for (State state : current) {
                for (Transition transition : state.getTransitions()) {
                    if (!transition.isEpsilon() && transition.getSymbol() == symbol) {
                        next.add(transition.getTarget());
                    }
                }
            }
            current = epsilonClosure(next);
        }

        return current.stream().anyMatch(State::isAcceptance);
    }

    private Set<State> epsilonClosure(Set<State> initial) {
        Set<State> closure = new HashSet<>(initial);
        Deque<State> pending = new ArrayDeque<>(initial);

        while (!pending.isEmpty()) {
            for (Transition transition : pending.remove().getTransitions()) {
                if (transition.isEpsilon() && closure.add(transition.getTarget())) {
                    pending.add(transition.getTarget());
                }
            }
        }
        return closure;
    }
}
