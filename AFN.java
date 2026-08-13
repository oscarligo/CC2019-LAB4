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
}