public class Transition {
    private Character symbol; 
    private State target;

    public Transition(Character symbol, State target) {
        this.symbol = symbol;
        this.target = target;
    }

    public boolean isEpsilon() {
        return this.symbol == null || this.symbol == '\0';
    }

    public Character getSymbol() { return symbol; }
    public State getTarget() { return target; }
}