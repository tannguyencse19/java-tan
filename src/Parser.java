package src;

import java.util.ArrayList;
import java.util.List;

import models.Token;
import models.TokenType;
import models.Expression;
import models.Expression.VarAccess;
import models.Expression.Literal;
import models.Expression.Ternary;
import models.Expression.Unary;
import models.Expression.Assign;
import models.Expression.Binary;
import models.Expression.Grouping;
import models.Statement;
import models.Statement.Block;
import models.Statement.Expr;
import models.Statement.Print;
import models.Statement.VarDeclare;
import models.Statement.If;

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

    public List<Statement> getAST() {
        // NOTE: Panic Error Handling
        try {
            List<Statement> ASTList = program();

            return ASTList;
        } catch (ParseError e) {
            // FOR DEBUG: This shouldn't appear because ParseError is compile error
            System.err.println(e);
            return null;
        }
    }

    /* ----------------- Backbone function ------------------- */

    private List<Statement> program() {
        List<Statement> stmtList = new ArrayList<>();

        // NOTE: matchPeek already check EOF
        while (matchPeek(SEMI_COLON)) {
            stmtList.add(declaration());
            // CAUTION: Hot-fix - Due to current++ at different place, current might be ==
            current++; // CAUTION: Hot-fix - Pass over SEMI_COLON of current statement after finish
        }

        return stmtList;
    }

    private List<Statement> block() {
        List<Statement> stmtList = new ArrayList<>();

        while (!isNextToken(RIGHT_BRACE)) {
            stmtList.add(declaration());
            current++; // CAUTION: Same reason like `program()`
        }

        // CAUTION: Hot-fix - RIGHT_BRACE is the condition to exit while loop
        // CAUTION: so panicError will never happended except it is endOfFile
        // panicError(RIGHT_BRACE, "expected '}' to close block");
        if (endOfFile())
            throwError(prevToken(), "expected '}' to close block");

        return stmtList;
    }

    private Statement declaration() {
        try {
            if (matchAtLeast(VAR))
                return varStatement();
            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Statement varStatement() {
        panicError(IDENTIFIER, "identifier need to be initialized before used");
        Token identifier = prevToken();

        Expression initializer = null; // NOTE: No default value
        if (matchAtLeast(EQUAL)) {
            initializer = expression();
        }

        return new VarDeclare(identifier, initializer);
    }

    private Statement statement() {
        if (matchAtLeast(LEFT_BRACE)) {
            return new Block(block());
        } else if (matchAtLeast(IF)) {
            panicError(LEFT_PARAN, "if statement missing '(' for condition");
            Expression condition = expression();
            panicError(RIGHT_PARAN, "if statement missing ')' for condition");

            Statement ifStmt = statement();
            ++current; // CAUTION: Hotfix - Pass over SEMI_COLON
            Statement elseStmt = null;
            if (matchAtLeast(ELSE)) {
                elseStmt = statement();
            }
            return new If(condition, ifStmt, elseStmt);
        } else if (matchAtLeast(PRINT)) {
            Expression expr = expression(); // CAUTION: Hot-fix - With PrintStatement, need to pass over token Print
            return new Print(expr);
        }

        Expression expr = expression();
        return new Expr(expr);
    }

    private Expression expression() {
        return assignment();
    }

    /**
     * How to deal with {@link #assignment()} -> {@code page 122}
     *
     * @return
     */
    private Expression assignment() {
        Expression lhs = ternary(); // also identifier

        if (matchAtLeast(EQUAL)) {
            Token equal = prevToken(); // only for `throwError()`
            Expression rhs = ternary(); // also value

            if (lhs instanceof VarAccess) {
                Token identifier = ((VarAccess) lhs)._identifer;
                return new Assign(identifier, rhs);
            }

            throwError(equal, "Invalid assignment identiifier");
        }

        // NOTE: It means assignment expr is a ternary expr
        // NOTE: when `matchAtLeast(EQUAL)` false
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
            return new Literal(prevToken().getLiteral());
        else if (matchAtLeast(LEFT_PARAN)) {
            Expression e = expression();
            panicError(RIGHT_PARAN, "expect ')' after expression");

            return new Grouping(e);
        } else if (matchAtLeast(IDENTIFIER))
            return new VarAccess(prevToken());
        else {
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
     *           <p />
     *           So it's often use in conjuction with {@link #prevToken()}
     */
    private boolean matchAtLeast(TokenType... typeList) {
        for (TokenType type : typeList) {
            if (isNextToken(type)) {
                advance(); // pass over that token
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
                advance(); // pass over that token
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
        int temp = (current == 0) ? 0 : current - 1; // NOTE: Offset to get nextToken below

        TokenType nextToken = tokenList.get(temp).getType();

        // Shouldn't use do-while
        // Edge case: When current == list.size => do before while cause
        // current out-of-bound
        while (nextToken != EOF && nextToken != type) {
            ++temp;
            nextToken = tokenList.get(temp).getType();
        }

        return nextToken != EOF;
    }

    /**
     * @implNote Often use in conjuction with {@link #nextToken()}
     */
    private Token advance() {
        if (!endOfFile())
            current++; // NOTE: This cause coupling between advance() and getToken()
        return prevToken();
    }

    /**
     * @implNote See {@link #getToken(int)} for "Why name prevToken?"
     */
    private Token prevToken() {
        return getToken(0);
    }

    /**
     * @implNote See {@link #getToken(int)} for "Why name nextToken although it get
     *           current token?"
     *
     * @implNote Often use in conjuction with {@link #advance()}
     */
    private Token nextToken() {
        return getToken(1);
    }

    /**
     * Same as {@code check()}
     */
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

    private void synchronize() {
        do {
            advance(); // pass over the token which cause ParseError
            switch (nextToken().getType()) {
                case SEMI_COLON:
                case CLASS:
                case FUNCTION:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }
        } while (!endOfFile());
    }

    /* ---------------- Error handle -------------------- */

    /**
     * @param token - Custom token
     */
    private ParseError throwError(Token token, String message) {
        throw new ParseError(token, message);
    }

    /**
     * Same as {@code consume()}
     *
     * @implNote Copy of {@link #throwError(Token, String)}
     * @implNote If match, current++;
     * @param expected - Give second-chance. If the next token match, then it won't
     *                 throw.
     *
     */
    private void panicError(TokenType expected, String message) {
        if (prevToken().getType() == expected || matchPeek(expected)) // NOTE: Use `prevToken` for case `advance()` run
                                                                      // before
            advance(); // = continue parsing
        else
            throw new ParseError(nextToken(), message); // Point error location to next token
    }

    /**
     * Diffrent are in <b><i>When</i></b> it throws.
     * <p />
     * Checked exceptions (i.e: every exception except {@code RuntimeException})
     * happens at compile time. An easy example is your IDE warning, underline error
     * code.
     * <p />
     * Unchecked exceptions are happens at runtime. It's when the compiler print
     * error stack trace.
     */
    private class ParseError extends RuntimeException {
        ParseError(Token token, String message) {
            Tan.err.report(token, message);
        }
    };

}
