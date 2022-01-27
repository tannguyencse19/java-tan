package src;

import java.util.List;

import models.Token;
import models.TokenType;
import models.Expression;
import models.Expression.Literal;
import models.Expression.Ternary;
import models.Expression.Unary;
import models.Expression.Binary;
import models.Expression.Grouping;

import static models.TokenType.*;

public class Parser {
    /**
     * @implNote tokenList from Tan.java (using Scanner.java)
     * @see {@link src.Tan#run}
     */
    private final List<Token> tokenList;
    private int current = 0;

    // constructor
    Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
        // for (Token token : tokenList) { // TEST
        // System.out.println(token);
        // }
    }

    public Expression getAST() {
        // NOTE: Panic Error Handling
        try {
            return expression();
        } catch (Exception e) {
            return null;
        }
    }

    /* ----------------- Backbone function ------------------- */

    private Expression expression() {
        // return logicOR(commaOperator(), equality());
        return commaOperator();
    }

    private Expression commaOperator() {
        Expression lhs = ternary();

        while (matchAtLeast(COMMA)) {
            Token operator = prevToken();
            Expression rhs = ternary();
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression ternary() {
        Expression lhs = equality();

        while (matchAtLeast(QUESTION, COLON)) {
            Expression first = equality();
            panicError(COLON, "expect ':' ");
            Expression second = equality();

            lhs = new Ternary(lhs, new Token(TERNARY, "?:", prevToken().getLineID()), first, second);
        }

        return lhs;
    }

    // while (matchAtLeast(LOGIC_NOT, SUBTRACT)) {
    // Token operator = prevToken();
    // Expression rhs = unary();
    // return new Unary(operator, rhs);
    // }

    // return primary();

    private Expression equality() {
        // i.e: equality -> comparison ( ("!=" | "==") comparison )* ;
        // Moi vao phai lay lhs comparison
        Expression lhs = comparison();

        while (matchAtLeast(NOT_EQUAL, EQUAL_EQUAL)) {
            // NOTE
            // Our input have form: Token comparison (Token operator Token comparison)*
            // When `matchAtLeast()` is called, the `current` variable is ++
            // also `matchAtLeast()` is called twice (one in `lhs` and one in `while`)
            // which make `current` += 2 => getToken() return Token comparison
            // instead of Token operator. That's why call prevToken() here
            Token operator = prevToken();
            Expression rhs = comparison();

            // Chuan bi cho vong recurse tiep theo: Moi vao phai lay lhs comparison
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression comparison() {
        Expression lhs = term();

        while (matchAtLeast(MORE, MORE_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = prevToken();
            Expression rhs = term();
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression term() {
        Expression lhs = factor();

        while (matchAtLeast(PLUS, SUBTRACT)) {
            Token operator = prevToken();
            Expression rhs = factor();
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression factor() {
        Expression lhs = unary();

        while (matchAtLeast(DIVIDE, MULTIPLY)) {
            Token operator = prevToken();
            Expression rhs = unary();
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression unary() {
        TokenType matchResult = matchAtLeastWithResult(LOGIC_NOT, SUBTRACT, PLUS); // NOTE: Example from page 88

        while (matchResult != NONE) {
            if (matchResult == PLUS)
                throwError(nextToken(), "Unary '+'expressions are not supported");

            Token operator = prevToken();
            Expression rhs = unary();
            return new Unary(operator, rhs);
        }

        return primary();
    }

    private Expression primary() {
        if (matchAtLeast(FALSE))
            return new Literal(false);
        else if (matchAtLeast(TRUE))
            return new Literal(true);
        else if (matchAtLeast(NIL))
            return new Literal(null);
        else if (matchAtLeast(NUMBER, STRING))
            return new Literal(prevToken().getLexeme());
        else if (matchAtLeast(LEFT_PARAN)) {
            Expression e = expression();
            panicError(RIGHT_PARAN, "expect ')' after expression");

            return new Grouping(e);
        } else {
            throwError(nextToken(), "Expect expression");
            // FIX: Potential erase current parse result, which isn't Panic Error Handling
            // page 91
            return new Literal(null);
        }
    }

    /* -------------------- Helper function --------------------- */

    /**
     * Match at least one type in `typeList`
     *
     * @implNote If match, that token will be <b>SKIP</b> due to ++current
     */
    private boolean matchAtLeast(TokenType... typeList) {
        for (TokenType type : typeList) {
            if (isNextToken(type)) {
                advanced(); // pass over that token
                return true; // match at least once
            }
        }

        return false;
    }

    /**
     * @implNote Can't overload {@link #matchAtLeast(TokenType...)} due to same
     *           params type. See: https://stackoverflow.com/a/16377981/12897204
     * @return NIL - Only use as condition to exit.
     *
     *         <pre>
     *         TokenType matchResult = matchAtLeastWithResult(SUBTRACT, PLUS);
     *
     *         while (matchResult != NIL) {
     *              if (matchResult == PLUS) throwError(...);
     *         }
     *         </pre>
     */
    private TokenType matchAtLeastWithResult(TokenType... typeList) {
        for (TokenType type : typeList) {
            if (isNextToken(type)) {
                advanced(); // pass over that token
                return type; // match at least once
            }
        }

        return NONE;
    }

    /**
     * Peek forward {@link #tokenList} to see if there exists a token
     *
     * @implNote This function is totally different from
     *           {@link #matchAtLeast(TokenType...)}
     */
    private boolean matchPeek(TokenType type) {
        int temp = current - 1; // NOTE: Offset to get nextToken below

        TokenType nextToken;

        do {
            ++temp;
            nextToken = tokenList.get(temp).getType();
        } while (nextToken != EOF && nextToken != type);

        return nextToken != EOF;
    }

    private Token advanced() {
        if (!endOfFile())
            current++; // NOTE: This cause coupling between advanced() and getToken()
        return prevToken();
    }

    /**
     * @implNote See {@link #getToken(int)} for "Why name prevToken?"
     */
    private Token prevToken() {
        return getToken(0);
    }

    private Token nextToken() {
        return getToken(1);
    }

    private boolean isNextToken(TokenType expected) {
        return !endOfFile() ? nextToken().getType() == expected : false;
    }

    /**
     *
     * @param offset - for currentToken, offset = 0 due to in {@link #advance()}
     *               current++ before {@link #getToken(int)}
     */
    private Token getToken(int offset) {
        if (current == 0)
            return tokenList.get(0);

        return tokenList.get(current - 1 + offset);
    }

    /**
     * @implNote Must seperate from {@link #getToken} because function can only
     *           return 1 type of value
     */
    private boolean endOfFile() {
        // return current == tokenList.size();
        return getToken(0).getType() == EOF;
    }

    /* --------- Error handle --------- */

    /**
     * @param token - Custom token
     */
    private ParseError throwError(Token token, String message) {
        throw new ParseError(token, message);
    }

    /**
     * @implNote Copy of {@link #throwError(Token, String)}
     * @param expected - Give second-chance. If the next token match, then it won't
     *                 throw.
     */
    private void panicError(TokenType expected, String message) {
        if (prevToken().getType() == expected || matchPeek(expected)) // NOTE: Use `prevToken` for case `advanced()` run
                                                                      // before
            advanced(); // = continue parsing
        else
            throw new ParseError(nextToken(), message); // Point error location to next token
    }

    private class ParseError extends RuntimeException {
        ParseError(Token token, String message) {
            Tan.err.report(token, message);
        }
    };

}
