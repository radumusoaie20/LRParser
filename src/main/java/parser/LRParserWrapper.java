package parser;

import constants.Status;
import grammar.Grammar;
import grammar.GrammarWrapper;
import util.Pair;

import java.util.ArrayList;
import java.util.HashMap;


/** Wrapper for the LRParser object, use for extracting needed data
 * (Parser Table, etc.)
 */
public class LRParserWrapper extends ParserWrapper{

    private final LRParser parser;

    public LRParserWrapper(GrammarWrapper grammar){
        parser = new LRParser(grammar.getGrammar());
    }

    /** Returns the LR Parsers Table as a Map, with the following meaning:
     * <br><br>
     * If we have for example the pair <code>[0, E] = 3</code>, that symbolizes a jump on state <b>0</b> when the input is <code>E</code> to state <code>3</code>
     *<br><br> If we have for example the pair <code>[1, F] = d5</code>, that symbolizes a shift on the Stack with the state <b>5</b>
     * <br><br> If we have for example the pair <code>[2, A] = r6</code>, that symbolizes a reduce action on the Stack with the production <b>6</b>
     *<br><br> If we have for example the pair <code>[4, B] = x</code>, that symbolizes an illegal state
     * <br><br> If we have for example the pair <code>[1, p] = acc</code>, that symbolizes the acceptance state for the input
     * @return Parser's table(Action and Jump Table's grouped together)
     */
    public HashMap<Pair<Integer, String>, String> getParserTable(){
        return parser.getTable();
    }

    /** Stores the Parser's table in the resource files, like this:<br><br>
     * If your grammar filename is something like: <code>grammar.in</code>, then the parser will generate
     * the following file <code>grammar.out</code>. <br><br> Your table will be displayed as a matrix,
     * where the number of the row marks the state(<b>Row 0 = State 0</b>) and the number of the column
     * marks either a terminal, nonterminal or '$' symbol, like this:
     * If your <code>grammar.in</code> has the following content:<br><br>
     * <code>
     *     A B C E <br>
     *     a b + *
     * </code>
     * <br> where on the first line are the nonterminals, on the second the terminals, then the columns
     * will be in the following order: <br><br>
     * <code>A B C E $ a b + *</code>
     */
    public void storeParserTable(){
        parser.writeTableToFile();
    }

    @Override
    public Status parse(String input) {
        return parser.parse(input);
    }
}
