package src;

import java.util.ArrayList;
import java.util.List;

import models.Token;
import static models.TokenType.*;

public class Parser {
    /**
     * @implNote tokenList from Tan.java (using Scanner.java)
     * @see {@link src.Tan#run}
     */
    private final List<Token> tokenList;

    // constructor
    Parser(List<Token> tokenList) {
        this.tokenList = tokenList;

        // for (Token token : tokenList) { // TEST
        //     System.out.println(token);
        // }
    }

}
