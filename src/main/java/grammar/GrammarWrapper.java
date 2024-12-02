package grammar;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class GrammarWrapper{

    Grammar grammar;

    public GrammarWrapper(String grammarData){
        this.grammar = new Grammar(grammarData);
    }

    public Grammar getGrammar(){
        return grammar;
    }

    public ArrayList<String> getFirstSet(String nonterminal){
        return (ArrayList<String>) grammar.firstSet.get(nonterminal).stream().toList();
    }

    public ArrayList<String> getFollowSet(String nonterminal){
        return (ArrayList<String>) grammar.followSet.get(nonterminal).stream().toList();
    }

}
