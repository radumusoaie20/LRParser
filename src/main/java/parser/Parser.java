package parser;

import constants.Status;

public interface Parser {


    /** Method that parses the string input. Expects only terminals
     *
     * @param input
     * @return Either <code>Status.ACCEPTED</code>, or <code>Status.REJECTED</code> based on the <code>input</code>
     */

    Status parse(String input);

}
