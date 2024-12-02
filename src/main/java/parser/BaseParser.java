package parser;

import constants.Status;
import grammar.Grammar;
import grammar.Production;
import util.Pair;

import java.util.*;
import java.util.stream.Collectors;


/** Base Class for parsers <br /> <br />
 * Implements Parser, it's parse method always returns <code>Status.REJECTED</code>.
 * <br /> <br />
 * Provides the <code>first</code> and <code>follow</code> methods
 *<br /> <br />
 * Inherit this class for Parsers implementation
 * @author Musoaie Pavel-Radu
 */
class BaseParser implements Parser{

    protected Grammar grammar;

    protected BaseParser(Grammar grammar){
        this.grammar = grammar;
    }


    @Override
    public Status parse(String input){
        return Status.ACCEPTED;
    }
}
