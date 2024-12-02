package parser;

import constants.Status;
import grammar.Grammar;
import grammar.Production;
import util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

class LRParser extends BaseParser {

    private HashMap<Pair<Integer, String>, String> table;

    public HashMap<Pair<Integer, String>, String> getTable(){
        return table;
    }

    private HashMap<Pair<LinkedHashSet<Article>, String>, LinkedHashSet<Article>> functions;

    private static final String TERMINATOR = "$";


    /** LR(1) Parser. Takes as input the grammar to be used for parsing.
     * <p>The parser reads the <code>Action Table </code> and <code> Jump Table </code>
     * from the file <code>data</code>.</p>
     * <p>In the file, there will be a matrix with items separated by spaces.
     * The column represents a grammar symbol. By convention, the columns will look like this: </p>
     * <p><b>terminal[1] terminal[2] ..... $ nonterminal[1] nonterminal[2] ...</b></p>
     * <p>The rows will represent the current state(from 0 to the number of total states in the grammar)</p>
     * <p>Each cell can either have:</p>
     * <li><code>d[num]</code>, where num is a state, eg. <code>d2</code> This means
     * <b> push on the stack the current symbol and the <code>num</code> state, then go next symbol.</b></li>
     * <li><code>r[num]</code>, where num is a state, eg. <code>r6</code> This means
     * <b> pop from the stack based on the <code>num</code> state in reverse, then push the symbol from where
     * the popped production came from.</b></li>
     * <li><code>[num]</code>, where num is a state, eg. <code>8</code> This means
     * that the next state is <code>num</code>.</li>
 *   <li><code>acc</code>, this symbol means that the parser can accept the input.</li>
     * <li><code>x</code>, this symbol means that the parser is in an illegal state, resulting in
     * rejecting the input.</li>
     * @param grammar
     *
     *
     * @author Musoaie Pavel-Radu
     */

    public LinkedHashSet<LinkedHashSet<Article>> closures = new LinkedHashSet<>();
    protected LRParser(Grammar grammar, String data){
        super(grammar);
        table = new HashMap<>();
        functions = new HashMap<>();
        readTable(data);
    }

    protected LRParser(Grammar grammar) {
        super(grammar);
        table = new HashMap<>();
        functions = new HashMap<>();
        grammar.nonterminals.add("S'");
        Production tempProd = new Production("S'", this.grammar.start, 0);
        grammar.productions.add(new Production("S'", this.grammar.start, 0));
        collection(); //LR(1) items

        String filename = grammar.getFileName()+".out";


        File file = new File(filename);
        if(!file.exists()) {
            createTable();
            grammar.nonterminals.remove("S");
            grammar.productions.remove(tempProd);
//            writeTableToFile();
        }
        else{
            grammar.nonterminals.remove("S");
            grammar.productions.remove(tempProd);
            readTable(filename);
        }
    }

    private void readTable(String data){
        File file = new File(data);
        try{
            Scanner sc = new Scanner(file);
            int status = 0;
            String input;
            while(sc.hasNext()) {
                //Terminals columns
                Iterator<String> it = grammar.terminals.iterator();
                for (int i = 0; i < grammar.terminals.size(); i++) {
                    input = sc.next(); //get next symbol
                    table.put(new Pair<>(status, it.next()), input);
                }
                //$ column
                input = sc.next();
                table.put(new Pair<>(status, "$"), input);
                //Nonterminals columns
                it = grammar.nonterminals.iterator();
                for(int i = 0; i < grammar.nonterminals.size(); i++){
                    input = sc.next();
                    table.put(new Pair<>(status, it.next()), input);
                }
                status++;
            }
        }
        catch(FileNotFoundException f){
            System.out.println("Action/Jump tables file does not exist.");
        }

    }


    private String scan(String input, int start){
        StringBuilder terminal = new StringBuilder(); //scan for a terminal in the input
        while(input.charAt(start) == ' ') start++; //pass over white space
        while(input.charAt(start) != ' '){
            terminal.append(input.charAt(start));
            start++;
            if(isTerminal(terminal.toString()) || terminal.toString().equals("$"))
                return terminal.toString();
            if(start == input.length()) return "";
        }
        return "";
    }


    private boolean isTerminal(String test){
        return grammar.terminals.contains(test);
    }

    private boolean validatePop(String prod, String symbol, int index){
        int i = symbol.length() - 1;
        index--;
        while(prod.charAt(index) == symbol.charAt(i)){
            if(i == 0 || index == 0) return true;
            index--;
            i--;
        }
        return false;
    }


    @Override
    public Status parse(String input) {
        if(input == null) return Status.REJECTED; //reject if null parameter
        input += TERMINATOR; //append end symbol
        input = input.trim();
        input = input.replaceAll("\\s{1,}",""); //remove whitespaces
        Stack<Pair<Integer, String>> stack = new Stack<>();
        stack.push(new Pair<>(0, TERMINATOR));
        String terminal;
        for(int i = 0; i < input.length(); i++){
                int copy = i - 1;
                terminal = scan(input, i);
                //move i by the terminal size
                if(!terminal.isEmpty())
                    i += terminal.length() - 1;
                else return Status.REJECTED;
                Pair<Integer, String> pair = new Pair<>(stack.peek().first, terminal);
                String action;
                if(table.containsKey(pair)) {
                    action = table.get(pair);
                }
                else action = "x";
                if(action.charAt(0) == 'd'){
                    Pair<Integer, String> newPair = new Pair<>();
                    newPair.first = Integer.parseInt(action.substring(1));
                    newPair.second = terminal;
                    stack.push(newPair);
                }
                else if(action.charAt(0) == 'r'){
                    int productionIndex = Integer.parseInt(action.substring(1)) - 1;
                    Production production = grammar.productions.get(productionIndex);
                    int index = production.result.length();
                    String symbol = stack.peek().second;
                    while(index != 0){
                        if(validatePop(production.result, symbol, index)){
                            index -= symbol.length();
                            stack.pop();
                            symbol = stack.peek().second;
                        }
                        else return Status.REJECTED;
                    }
                    Pair<Integer, String> newPair = new Pair<>();
                    newPair.second = production.start;
                    int status = stack.peek().first;
                    newPair.first = Integer.parseInt(table.get(new Pair<>(status, newPair.second)));
                    stack.push(newPair);
                    i = copy; // stay on the same symbol
                }
                else if(action.equals("acc"))
                    return Status.ACCEPTED;
                else return Status.REJECTED;
        }
        return Status.REJECTED;
    }


    private LinkedHashSet<Article> closure(LinkedHashSet<Article> input){
        LinkedHashSet<Article> copy = new LinkedHashSet<>(input);
        LinkedHashSet<Article> result;
        LinkedHashSet<String> visitedNonTerminals = new LinkedHashSet<>();
        do{
            result = new LinkedHashSet<>(copy);
            HashSet<Article> temp = new HashSet<>(copy);


            Iterator<Article> iterator = temp.iterator();
            while(iterator.hasNext()){
                Article article = iterator.next();
                if(canBeExtended(article)){
                    String nonterminal = findSymbolAfterDot(article);
                    if(!visitedNonTerminals.contains(nonterminal) && grammar.nonterminals.contains(nonterminal)) {
                        ArrayList<Production> productions = this.grammar.getListOfProductionForNonTerminal(nonterminal);
                        productions.forEach(
                                production -> copy.add(new Article(production))
                        );
                        visitedNonTerminals.add(nonterminal);
                    }
                }
                iterator.remove();
            }
        }
        while(!result.equals(copy));
        return result;
    }


    private LinkedHashSet<Article> jump(LinkedHashSet<Article> articles, String symbol){

        LinkedHashSet<Article> newArticles = new LinkedHashSet<>();

        for(Article article: articles){
            if(findSymbolAfterDot(article).equals(symbol)) {
                Article article1 = new Article(article.production);
                article1.dot = article.dot + symbol.length();
                newArticles.add(article1);
            }
        }
        return closure(newArticles);
    }

    private void collection(){

        LinkedHashSet<Article> temp = new LinkedHashSet<>();
        temp.add(new Article(grammar.productions.get(grammar.productions.size()-1)));
        closures.add(closure(temp));
        ArrayList<Boolean> found = new ArrayList<>();
        found.add(false);
        int index = 0;

        while(!isDone(found)){
            int i = 0;
            while(i < found.size() && found.get(i))
                i++;
            index = i;
            LinkedHashSet<Article> articles;
            Iterator<LinkedHashSet<Article>> iterator = closures.iterator();
            for(int j = 0; j < index && iterator.hasNext(); j++) iterator.next();
            articles = iterator.next();

            LinkedHashSet<String> symbols = getSymbolsAfterDot(articles);
            found.set(index, true);
            for(String symbol: symbols){
                LinkedHashSet<Article> temp1 = jump(articles, symbol);
                functions.put(new Pair<>(articles, symbol), temp1);
                if(closures.add(temp1))
                    found.add(false);
            }
        }
    }

    private void createTable(){
            int l = closures.size();
            Iterator<LinkedHashSet<Article>> iterator = closures.iterator();
            for(int i = 0; i < l; i++){
                LinkedHashSet<Article> articles = iterator.next();
                LinkedHashSet<Article> functionArticle;
                for(Article article: articles){
                    if(article.dot == article.production.result.length()){ //reduce
                        boolean isStartSymbol = article.production.result.equals(grammar.start);

                        if(!isStartSymbol) {
                            for (String urmator : grammar.followSet.get(article.production.start)) {
                                Pair<Integer, String> pair = new Pair<>(i, urmator);
                                table.put(pair, "r" + article.production.state);
                            }
                        }
                        else{
                            table.put(new Pair<>(i, "$"), "acc");
                        }
                    }
                    else {
                        Pair<String, Integer> terminal = grammar.firstTerminal(article.production.result,
                                article.dot);
                        if (terminal.second == article.dot) { //shift
                            Pair<LinkedHashSet<Article>, String> argument =
                                    new Pair<>(articles, terminal.first);
                            functionArticle = functions.get(argument);
                            Integer j = findIndexOfClosure(functionArticle);
                            table.put(new Pair<>(i, terminal.first), "d" + j);
                        }
                        else{  //jump
                            String nonterminal = grammar.firstNonTerminal(article.production.result,
                                    article.dot).first;

                            Pair<LinkedHashSet<Article>, String> argument =
                                    new Pair<>(articles, nonterminal);

                            functionArticle = functions.get(argument);
                            Integer j = findIndexOfClosure(functionArticle);
                            table.put(new Pair<>(i, nonterminal), j.toString());
                        }
                    }
                }
            }
    }


    //UTIL
    private String findSymbolAfterDot(Article article){
        String result = "";
        Pair<String, Integer> terminal = this.grammar.firstTerminal(article.production.result, article.dot);
        if(terminal.second != article.dot)
            return this.grammar.firstNonTerminal(article.production.result, article.dot).first;
        return terminal.first;
    }



    private LinkedHashSet<String> getSymbolsAfterDot(LinkedHashSet<Article> articles){
        LinkedHashSet<String> result = new LinkedHashSet<>();
        articles.forEach(
                article -> {
                    if(!findSymbolAfterDot(article).isEmpty()) result.add(findSymbolAfterDot(article));
                }
        );
        return result;
    }

    private boolean canBeExtended(Article article){
        return grammar.nonterminals.contains(findSymbolAfterDot(article));
    }

    private boolean isDone(ArrayList<Boolean> found){
        for(Boolean b: found)
            if(!b) return false;
        return true;
    }

    private int findIndexOfClosure(LinkedHashSet<Article> closure){
        Iterator<LinkedHashSet<Article>> iterator = closures.iterator();
        int index = 0;
        while(iterator.hasNext()){
            LinkedHashSet<Article> next = iterator.next();
            if(next.equals(closure))
                return index;
            index++;
        }
        return -1;
    }

    protected void writeTableToFile(){
        String filename = grammar.getFileName()+".out";

        try {
            FileWriter file = new FileWriter(filename);
            int l = closures.size();
            for (int i = 0; i < l; i++) {
                Pair<Integer, String> tableInput;
                for (String symbol : grammar.terminals) {
                    tableInput = new Pair<>(i, symbol);
                    if (table.containsKey(tableInput)){
                        file.write(table.get(tableInput) + " ");
                    }
                    else{
                        file.write("x ");
                    }
                }
                tableInput = new Pair<>(i, "$");
                if(table.containsKey(tableInput)){
                    file.write(table.get(tableInput) + " ");
                }
                else{
                    file.write("x ");
                }

                int k = grammar.nonterminals.size();
                Iterator<String> nonterminals = grammar.nonterminals.iterator();
                String nonterminal;
                for(int j = 0; j < k - 1; j++){
                    nonterminal  = nonterminals.next();
                    tableInput = new Pair<>(i, nonterminal);
                    if(table.containsKey(tableInput)){
                        file.write(table.get(tableInput) + " ");
                    }
                    else file.write("x ");
                }
                nonterminal = nonterminals.next();
                tableInput = new Pair<>(i, nonterminal);
                if(table.containsKey(tableInput))
                    file.write(table.get(tableInput));
                else file.write("x");

                file.write("\n");
            }
            file.close();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}
