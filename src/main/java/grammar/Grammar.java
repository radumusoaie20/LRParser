package grammar;


import util.Pair;

import javax.sound.sampled.Line;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


/** Encapsulates parts needed to make a grammar
 *
 */
public class Grammar {

    /**
     * Grammar constructor
     * <p>The file <code>filename</code> contains the following: </p>
     * <li> 1st line - Grammar's <code>nonterminals</code>, separated by <b>a space</b></li>
     * <li> 2nd line - Grammar's <code>terminals</code>, separated by <b>a space</b></li>
     * <li> <b>n-</b>th line - A grammar production set, the <b>first symbol</b> is the
     * target symbol and the other ones are a production for that symbol, separated by <b>a space</b></li>
     * <li> Last line - Grammar's <code>start symbol</code></li>
     *  @param filename
     *
     * @author Musoaie Pavel-Radu
     */


    private String shortFilename;

    public String getFileName(){
        return shortFilename;
    }

    protected Grammar(String filename) {
        shortFilename = filename.split("\\.")[0];
        File file = new File(filename);
        nonterminals = new LinkedHashSet<>();
        terminals = new LinkedHashSet<>();
        productions = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file);
            String buffer;
            //Reading nonterminals
            buffer = sc.nextLine();
            nonterminals =   new LinkedHashSet<>(Arrays.stream(buffer.split(" ")).toList());
            //Reading terminals
            buffer = sc.nextLine();
            terminals = new LinkedHashSet<>(Arrays.stream(buffer.split(" ")).toList());

            int state = 1;
            //Reading productions
            while(sc.hasNextLine()) {
                buffer = sc.nextLine();
                String[] splitter = buffer.split(":");
                if (splitter.length == 1) {
                    break; // We found the start symbol at the end
                }
                else{
                    String begin = splitter[0];
                    begin = begin.trim();
                    splitter[1] = splitter[1].trim();
                    String[] prods = splitter[1].split("\\|", -1);
                    for(int i = 0; i < prods.length; i++){
                        prods[i] = prods[i].trim();
                        productions.add(new Production(begin, prods[i], state++));
                    }
                }
            }
            start = buffer;

            firstSet = new HashMap<>();
            followSet = new HashMap<>();

            computeFirstSets();

            computeFollowSets();
        }
        catch(FileNotFoundException f){
            System.out.println("The file doesn't exist.");
        }
    }

    private void computeFollowSets(){

        HashMap<String, Boolean> scouted = new HashMap<>();

        for(String symbol: nonterminals){
            scouted.put(symbol, false);
            followSet.put(symbol, new LinkedHashSet<>());
        }

        followSet.put(start, findFollow(start, scouted));

        for(String symbol: nonterminals) {
            findFollow(symbol, scouted);
        }

    }

    private LinkedHashSet<String> findFollow(String symbol, HashMap<String, Boolean> scouted){

        LinkedHashSet<String> result = new LinkedHashSet<>();


        if(symbol.equals(start)){
            result.add("$");
        }

        if(nonterminals.contains(symbol)){
            if(scouted.get(symbol))
                return followSet.get(symbol);
            scouted.put(symbol, true);
            ArrayList<Production> productions = getListOfProductionWithNonTerminal(symbol);
            for(Production prod: productions){
                int index = prod.result.indexOf(symbol) + symbol.length();
                if(index == prod.result.length()){
                    if(!prod.start.equals(symbol))
                        result.addAll(findFollow(prod.start, scouted));
                }
                else{
                    Pair<String, Integer> terminal = firstTerminal(prod.result, index);
                    if(terminal.second > index){
                        String nonTerminal = firstNonTerminal(prod.result, index).first;
                        LinkedHashSet<String> newSet = new LinkedHashSet<>(firstSet.get(nonTerminal));
                        boolean hasVoid = newSet.contains("");
                        newSet.remove("");
                        result.addAll(newSet);
                        if(hasVoid){
                            if(index + nonTerminal.length() == prod.result.length()) {
                                if (!prod.start.equals(symbol))
                                    result.addAll(findFollow(prod.start, scouted));
                            }
                            else{
                              String rightSide = prod.result.substring(index + nonTerminal.length());
                              boolean repeat = true;
                              int start = 0;
                              while(repeat){
                                  Pair<String, Integer> terminal1 = firstTerminal(rightSide,start);
                                  if(start != terminal1.second){
                                      String nonterminal1 = firstNonTerminal(rightSide, start).first;
                                      start += nonterminal1.length();
                                      if(nonterminal1.isEmpty())
                                          break;
                                      newSet = new LinkedHashSet<>(firstSet.get(nonterminal1));
                                      if (!newSet.contains(""))
                                          repeat = false;
                                      if(start == rightSide.length() && newSet.contains("")){
                                          result.addAll(findFollow(prod.start, scouted));
                                      }
                                      newSet.remove("");
                                      result.addAll(newSet);
                                      }
                                  else{
                                      start += terminal1.first.length();
                                      result.add(terminal1.first);
                                      repeat = false;
                                  }
                              }
                            }
                        }
                    }
                    else{
                        LinkedHashSet<String> newSet  = new LinkedHashSet<>();
                        newSet.add(terminal.first);
                        result.addAll(newSet);
                    }
                }
            }
        }
        if(nonterminals.contains(symbol))
            followSet.put(symbol, result);
        return result;
    }

    private void computeFirstSets(){
        HashMap<String, Boolean> scouted = new HashMap<>();
        HashMap<String, Boolean> finished = new HashMap<>();
        for(String terminal: this.terminals) {
            LinkedHashSet<String> first = new LinkedHashSet<>();
            first.add(terminal);
            firstSet.put(terminal, first);
        }
        for(String symbol : nonterminals) {
            scouted.put(symbol, false);
            finished.put(symbol, false);
            firstSet.put(symbol, new LinkedHashSet<>());
        }

        for(String symbol: nonterminals) {
            if(!finished.get(symbol))
                firstSet.put(symbol, findFirst(symbol, scouted, finished));
        }
    }

    private LinkedHashSet<String> findFirst(String input, HashMap<String, Boolean> scouted, HashMap<String, Boolean> finished){

        LinkedHashSet<String> result = new LinkedHashSet<>();

        if(terminals.contains(input))
            return firstSet.get(input);

        else if(nonterminals.contains(input)){
            scouted.put(input, true);
            //get left-recursive and non-left recursive productions
            ArrayList<Production> nonRecursiveProd = new ArrayList<>();
            ArrayList<Production> recursiveProd = new ArrayList<>();
            for(Production prod: getListOfProductionForNonTerminal(input)) {
                if (isProductionLeftRecursive(input, prod.result))
                    recursiveProd.add(prod);
                else nonRecursiveProd.add(prod);
            }
            for(Production prod: nonRecursiveProd){
                result.addAll(findFirst(prod.result, scouted, finished));
            }
            for(Production prod: recursiveProd){
                int position = prod.result.indexOf(input);
                if(position != 0){
                    String before = prod.result.substring(0, position);
                    LinkedHashSet<String> beforeSet = new LinkedHashSet<>();
                    beforeSet.addAll(findFirst(before, scouted, finished));

                    if(firstSet.get(input).contains("")){
                        String after = prod.result.substring(position+1, prod.result.length() - 1);
                        LinkedHashSet<String> afterSet = new LinkedHashSet<>();
                        afterSet.addAll(findFirst(after, scouted, finished));

                        result.addAll(afterSet);
                    }

                    result.addAll(beforeSet);
                }
                else if(position != prod.result.length() - 1){
                    if(firstSet.get(input).contains("")){
                        String after = prod.result.substring(position+1, prod.result.length() - 1);
                        LinkedHashSet<String> afterSet = new LinkedHashSet<>();
                        afterSet.addAll(findFirst(after, scouted, finished));

                        result.addAll(afterSet);
                    }
                }
            }
            firstSet.put(input, result);
            finished.put(input, true);
        }
        else if(input.isEmpty()){
            result.add("");
        }
        else{
            int start = 0;
            while(start < input.length()){
                Pair<String, Integer> terminal = firstTerminal(input, start);
                if(terminal.second == start){
                    result.add(terminal.first);
                    break;
                }
                else{
                    String nonterminal = firstNonTerminal(input, start).first;
                    start += nonterminal.length();
                    LinkedHashSet<String> before;
                    if(finished.get(nonterminal)) {
                        before = firstSet.get(nonterminal);
                        result.addAll(before);
                    }
                    else{
                        before = findFirst(nonterminal, scouted, finished);
                        result.addAll(before);
                    }

                    if(!before.contains(""))
                        break;
                }
            }
        }
        return result;
    }



    public boolean isProductionLeftRecursive(String symbol, String production){
        return production.contains(symbol);
    }

    public Pair<String, Integer> firstTerminal(String input, Integer start){
        String result = "";
        int lowestIndex = Integer.MAX_VALUE;
        for(String terminal: terminals){
            int index = input.indexOf(terminal, start);
            if(index != -1 && index < lowestIndex){
                result = terminal;
                lowestIndex = index;
            }
        }
        return new Pair<>(result, lowestIndex);
    }
    public Pair<String, Integer> firstNonTerminal(String input, Integer start){
        String result = "";
        int lowestIndex = Integer.MAX_VALUE;
        for(String nonterminal: nonterminals){
            int index = input.indexOf(nonterminal, start);
            if(index != -1 && index < lowestIndex){
                result = nonterminal;
                lowestIndex = index;
            }
        }
        return new Pair<>(result, lowestIndex);
    }

    public ArrayList<Production> getListOfProductionForNonTerminal(String nonterminal){
        return
                (ArrayList<Production>) productions
                        .stream()
                        .filter(production -> production.start.equals(nonterminal))
                        .collect(Collectors.toList());
    }

    public ArrayList<Production> getListOfProductionWithNonTerminal(String nonterminal){
        return
                (ArrayList<Production>) productions
                        .stream()
                        .filter(production -> production.result.contains(nonterminal))
                        .collect(Collectors.toList());
    }


    public LinkedHashSet<String> nonterminals;
    public LinkedHashSet<String> terminals;
    public String start;
    public ArrayList<Production> productions;

    public HashMap<String, LinkedHashSet<String>> firstSet;

    public HashMap<String, LinkedHashSet<String>> followSet;
}
