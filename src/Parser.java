package src;

import java.util.List;

import models.Token;
import models.TokenType;
import models.Expression;
import models.Expression.Literal;
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
        return expression();
    }

    public Expression expression() {
        return equality();
    }

    private Expression equality() {
        // i.e: equality -> comparison ( ("!=" | "==") comparison )* ;
        // Moi vao phai lay lhs comparison
        Expression lhs = comparison();

        while (matchOnce(NOT_EQUAL, EQUAL_EQUAL)) {
            // NOTE
            // Our input have form: Token comparison (Token operator Token comparison)*
            // When `matchOnce()` is called, the `current` variable is ++
            // also `matchOnce()` is called twice (one in `lhs` and one in `while`)
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

        while (matchOnce(MORE, MORE_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = prevToken();
            Expression rhs = term();
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression term() {
        Expression lhs = factor();

        while (matchOnce(PLUS, SUBTRACT)) {
            Token operator = prevToken();
            Expression rhs = factor();
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression factor() {
        Expression lhs = unary();

        while (matchOnce(DIVIDE, MULTIPLY)) {
            Token operator = prevToken();
            Expression rhs = unary();
            lhs = new Binary(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression unary() {
        while (matchOnce(LOGIC_NOT, SUBTRACT)) {
            Token operator = prevToken();
            Expression rhs = unary();
            return new Unary(operator, rhs);
        }

        return primary();
    }

    private Expression primary() {
        if (matchOnce(FALSE))
            return new Literal(false);
        else if (matchOnce(TRUE))
            return new Literal(true);
        else if (matchOnce(NIL))
            return new Literal(null);
        else if (matchOnce(NUMBER, STRING))
            return new Literal(prevToken().getLexeme());
        else if (matchOnce(LEFT_PARAN)) {
            Expression e = expression();
            panicErrHandle(RIGHT_PARAN);

            return new Grouping(e);
        } else {
            Token err = prevToken();
            // Tan.err.report(err.getLineID(), "unexpected character: " + err.getLexeme());
            // // FIX: Comment out when finish
            return new Literal(null);
        }
    }

    /* --------- Helper function --------- */

    /**
     * @implNote If match, that token will be <b>SKIP</b> due to ++current
     */
    private boolean matchOnce(TokenType... typeList) {
        for (TokenType type : typeList) {
            if (isNextToken(type) && !endOfFile()) {
                advanced(); // pass over that token
                return true; // match at least once
            }
        }

        return false;
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

    private boolean isNextToken(TokenType expected) {
        return getToken(1).getType() == expected;
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

    public ParseError panicErrHandle(TokenType expected) {
        if (isNextToken(expected))
            advanced(); // = continue parsing

        // NOTE: "return", not "throw", which will cause err.stackTrace()!
        return new ParseError(getToken(1), "expect ')' after expression");
    }

    private class ParseError extends RuntimeException {
        ParseError(Token err, String message) {
            Tan.err.report(err, message);
        }
    };

}
