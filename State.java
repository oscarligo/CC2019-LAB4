import java.util.ArrayList;
import java.util.List;

public class State {
    private int id;
    private boolean isAcceptance;
    private List<Transition> transitions;

    public State(int id) {
        this.id = id;
        this.isAcceptance = false;
        this.transitions = new ArrayList<>();
    }

    public void addTransition(Character symbol, State target) {
        this.transitions.add(new Transition(symbol, target));
    }

    public int getId() { return id; }
    public boolean isAcceptance() { return isAcceptance; }
    public void setAcceptance(boolean acceptance) { isAcceptance = acceptance; }
    public void setId(int id) { this.id = id; }
    public List<Transition> getTransitions() { return transitions; }
}