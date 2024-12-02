package grammar;

import java.util.Objects;

public class Production {
    public int state;
    public String start;
    public String result;

    public Production(String start, String result, int state){
        this.start = start;
        this.result = result;
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Production that = (Production) o;
        return state == that.state && Objects.equals(start, that.start) && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return state * 31 + 17 * result.hashCode() + 13 * start.hashCode();
    }

    @Override
    public String toString(){
        String output =  state + ". " + start + " -> ";
        if(!result.isEmpty())
            output += result;
        else output += "ε";
        return output;
    }

}
