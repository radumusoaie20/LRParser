Welcome! This package gives you an API for working with Grammars and LR Parsers(For now only those with a one token lookahead). <br>

These are the following entities you can use:

1. <code>GrammarWrapper</code>
   1. <b>getFirstSet(Nonterminal)</b>
            
      Returns FIRST(Nonterminal)
   
   2. <b>getFollowSet(Nonterminal)</b>
       
        Returns FOLLOW(Nonterminal)
2. <code>LRParserWrapper</code>
   1. <b>getParserTable()</b>
        
       Returns the LR Parser Table as a <code>HashMap</code>(See Documentation)
    2. <b>storeParserTable()</b>
    
       Stores the LR Parser Table in an <code>.out</code> file


## Input Data

* The input for the grammar shall be in a <code>.in</code> file and is as follows:

  
        [List of nonterminals, separated by space]
  
        [List of terminals, separated by space]

        [List of productions]

        [Productions of the form: Sym : M0 | M1 | ...]

        [Start Symbol]

Here is an example:

        E T F
        id + * ( )
        E : E+T | T | E-T
        T : T*F | F | T/F
        F : id | (E) | -(E)
        E

The parser parsing table is represented as a <code>HashMap</code> with the following meaning for it's key values:
    
        If we have the string value `d5`, that represents a shift with the state 5
        If we have the string value `r6`, that represents a reduce with the production 6
        If we have the string value `acc`, that represents acceptance of the input
        If we have the string value `x`, that represents an illegal value, leading to rejection of the input

Hope this helps when using this LR Parser Implementation!
