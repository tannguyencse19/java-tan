package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.Token;
import models.TokenType;
import static models.TokenType.*;
import models.Expression;
import models.Expression.Literal;
import models.Expression.This;
import models.Expression.VarAccess;
import models.Expression.Logical;
import models.Expression.Ternary;
import models.Expression.Call;
import models.Expression.Get;
import models.Expression.Unary;
import models.Expression.Assign;
import models.Expression.Set;
import models.Expression.Binary;
import models.Expression.Grouping;
import models.Statement;
import models.Statement.Block;
import models.Statement.Expr;
import models.Statement.Print;
import models.Statement.Return;
import models.Statement.VarDeclare;
import models.Statement.While;
import models.Statement.If;
import models.Statement.FuncPrototype;
import models.Statement.ClassDeclare;

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
            // CAUTION: Hotfix - Pass over SEMI_COLON of current statement after finish
            // if match, then ++current; else, no
            matchAtLeast(SEMI_COLON, RIGHT_BRACE);
        }

        return stmtList;
    }

    private List<Statement> block() {
        List<Statement> stmtList = new ArrayList<>();

        while (!isNextToken(RIGHT_BRACE)) {
            stmtList.add(declaration());
            // CAUTION: Hotfix - Same as `program()`
            // if match, then ++current; else, no
            matchAtLeast(SEMI_COLON, RIGHT_BRACE);
        }

        // CAUTION: Hot-fix - RIGHT_BRACE is the condition to exit while loop
        // CAUTION: so panicError will never happended except it is endOfFile
        // panicError(RIGHT_BRACE, "expected '}' to close block");
        if (endOfFile())
            panicErrorCustom(prevToken(), "expected '}' to close block");

        return stmtList;
    }

    private Statement declaration() {
        try {
            if (matchAtLeast(VAR))
                return varStatement();
            else if (matchAtLeast(FUNCTION))
                return funcStatement("function");
            else if (matchAtLeast(CLASS))
                return classStatement();

            return statement();
        } catch (ParseError err) {
            synchronize();
            return null;
        }
    }

    private Statement varStatement() {
        panicError(IDENTIFIER, "expect variable name");
        Token identifier = prevToken();

        Expression initializer = null; // NOTE: Initial no value
        if (matchAtLeast(EQUAL)) {
            initializer = expression();
        }

        return new VarDeclare(identifier, initializer);
    }

    private Statement funcStatement(String kind) {
        // NOTE: Differentiate between function and class function (method)
        panicError(IDENTIFIER, "expect " + kind + " name");
        Token identifier = prevToken();

        panicError(LEFT_PAREN, "expect " + kind + " parameters");
        List<Token> params = new ArrayList<>(); // NOTE: Initial no argument
        if (!isNextToken(RIGHT_PAREN)) { // handle no argument edge case
            do {
                if (params.size() >= 255)
                    panicErrorCustom(nextToken(), "can't have more than 255 parameters");

                panicError(IDENTIFIER, "expect parameter name");
                Token nextParam = prevToken();
                params.add(nextParam);
            } while (matchAtLeast(COMMA));
        }
        panicError(RIGHT_PAREN, "expect ')' after define " + kind + " parameters");

        List<Statement> body = new ArrayList<>();
        if (matchAtLeast(LEFT_BRACE))
            body = block();
        else
            body = Arrays.asList(statement());

        // NOTE: No need panicError block syntax as it's handled inside `block()`
        if (getToken(1).getType() != RIGHT_BRACE) {
            panicError(SEMI_COLON, "expect ';' at the end of " + kind + " body");
        }

        return new FuncPrototype(identifier, params, body);
    }

    private Statement classStatement() {
        panicError(IDENTIFIER, "expect class name");
        Token name = prevToken();

        VarAccess superClass = null;
        if (isNextToken(LESS)) {
            panicError(IDENTIFIER, "expect superclass name");
            superClass = new VarAccess(prevToken());
        }

        panicError(LEFT_BRACE, "expect '{' before class body");
        List<FuncPrototype> methods = new ArrayList<>();
        while (!isNextToken(RIGHT_BRACE) && !endOfFile()) {
            FuncPrototype parseMethod = (FuncPrototype) funcStatement("method");
            methods.add(parseMethod);
        }
        panicError(RIGHT_BRACE, "expect '}' after class body");

        return new ClassDeclare(name, superClass, methods);
    }

    private Statement statement() {
        if (matchAtLeast(LEFT_BRACE)) {
            return new Block(block());
        } else if (matchAtLeast(IF)) {
            panicError(LEFT_PAREN, "if statement missing '(' for condition");
            Expression condition = expression();
            panicError(RIGHT_PAREN, "if statement missing ')' for condition");

            Statement ifStmt = statement();
            // NOTE: No need panicError block syntax as it's handled inside `block()`
            // NOTE: Check if "pass over SEMI_COLON" is handled by different function
            if (getToken(1).getType() != RIGHT_BRACE) {
                // CAUTION: Hotfix - Pass over SEMI_COLON if statement is not a block
                // ++current;
                panicError(SEMI_COLON, "expect ';' at the end of statement");
            }
            Statement elseStmt = null;
            if (matchAtLeast(ELSE)) {
                elseStmt = statement();
            }
            return new If(condition, ifStmt, elseStmt);
        } else if (matchAtLeast(WHILE)) {
            panicError(LEFT_PAREN, "while loop missing '(' at condition");
            Expression condition = expression();
            panicError(RIGHT_PAREN, "while loop missing ')' at condition");

            Statement body = statement();
            // NOTE: No need to Hotfix ++current due to
            // `program()`, `block()`, `matchAtLeast(IF)` already handle
            return new While(condition, body);
        } else if (matchAtLeast(FOR)) {
            panicError(LEFT_PAREN, "for loop missing '(' at the beginning of initializer");
            Statement initializer = null;
            if (!matchAtLeast(SEMI_COLON)) {
                initializer = declaration();
                panicError(SEMI_COLON, "for loop missing ';' at initializer");
            }
            Expression condition = null;
            if (!matchAtLeast(SEMI_COLON)) {
                condition = expression();
                panicError(SEMI_COLON, "for loop missing ';' at condition");
            }
            Expression iterate = null;
            if (!matchAtLeast(SEMI_COLON)) {
                iterate = expression();
                panicError(RIGHT_PAREN, "for loop missing ')' at the end of iterate");
            }

            Statement body = null;
            if (!matchAtLeast(SEMI_COLON)) {
                body = statement();
            }

            // Syntactic Sugar-ize
            if (condition == null) {
                condition = new Literal(true);
            }
            if (iterate != null) {
                // NOTE: If body == null
                // No need a block, also that will cause Interpreter create new environment
                // which means empty body can't access outer scope variable
                // See testcase `for_2.txt`
                body = (body == null) ? new Expr(iterate)
                        : new Block(Arrays.asList(body, new Expr(iterate)));
            }
            body = new While(condition, body);
            if (initializer != null) {
                body = new Block(Arrays.asList(initializer, body));
            }

            return body;
        } else if (matchAtLeast(PRINT)) {
            Expression expr = expression(); // CAUTION: Hot-fix - With PrintStatement, need to pass over token Print
            return new Print(expr);
        } else if (matchAtLeast(RETURN)) {
            Token keyword = prevToken(); // Use only to keep track return error
            Expression returnVal = null;
            if (!isNextToken(SEMI_COLON)) {// Handle no return value edge case
                returnVal = expression();
            }

            // CAUTION: Hotfix - Wrapper function already deal with this
            // This line is just "short-circuit"
            panicError(SEMI_COLON, "return missing ';' at the end");
            --current;
            return new Return(keyword, returnVal);
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
            Token equal = prevToken(); // only for `panicErrorCustom()`
            Expression rhs = ternary(); // also value

            if (lhs instanceof VarAccess) {
                Token identifier = ((VarAccess) lhs)._identifer;
                return new Assign(identifier, rhs);
            } else if (lhs instanceof Get) {
                Get getExpr = (Get) lhs;

                return new Set(getExpr._object, getExpr._propName, rhs);
            }

            panicErrorCustom(equal, "Invalid assignment identiifier");
        }

        // NOTE: It means assignment expr is a ternary expr
        // NOTE: when `matchAtLeast(EQUAL)` false
        return lhs;
    }

    private Expression ternary() {
        Expression lhs = logicOr();

        while (matchAtLeast(QUESTION, COLON)) {
            Expression first = logicOr();
            panicError(COLON, "expect ':' ");
            Expression second = logicOr();

            lhs = new Ternary(lhs, new Token(TERNARY, "?:", prevToken().getLineID()), first, second);
        }

        return lhs;
    }

    private Expression logicOr() {
        Expression lhs = logicAnd();

        while (matchAtLeast(LOGIC_OR)) {
            Token operator = prevToken();
            Expression rhs = logicAnd();

            lhs = new Logical(lhs, operator, rhs);
        }

        return lhs;
    }

    private Expression logicAnd() {
        Expression lhs = equality();

        while (matchAtLeast(LOGIC_AND)) {
            Token operator = prevToken();
            Expression rhs = equality();

            lhs = new Logical(lhs, operator, rhs);
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
                panicErrorCustom(nextToken(), "Unary '+'expressions are not supported");

            Token operator = prevToken();
            Expression rhs = unary();
            return new Unary(operator, rhs);
        }

        return call();
    }

    private Expression call() {
        /**
         * It's the funcName. A.k.a {@code expr}
         */
        Expression curry = primary();

        while (true) {
            if (matchAtLeast(LEFT_PAREN))
                curry = finishCall(curry);
            else if (matchAtLeast(DOT)) {
                panicError(IDENTIFIER, "Expect property name after '.'");
                Token propName = prevToken();

                curry = new Get(curry, propName);
            } else
                break;
        }

        return curry;
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
        else if (matchAtLeast(LEFT_PAREN)) {
            Expression e = expression();
            panicError(RIGHT_PAREN, "expect ')' after expression");

            return new Grouping(e);
        } else if (matchAtLeast(IDENTIFIER))
            return new VarAccess(prevToken());
        else if (matchAtLeast(THIS))
            return new This(prevToken());
        else {
            panicErrorCustom(nextToken(), "Expect expression");
            // FIX: Potential erase current parse result, which isn't Panic Error Handling
            // page 91
            return new Literal(null);
        }
    }

    /* -------------------- Helper function --------------------- */

    /**
     * Match at least one type in `typeList`
     * <p />
     * Same as {@code match()}
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
     *              if (matchResult == PLUS) panicErrorCustom(...);
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
        int temp = (current == 0) ? 0 : current;

        TokenType nextToken = tokenList.get(temp).getType();

        // CAUTION: Don't use do-while
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
     * Same as {@code peek()}
     *
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
        // CAUTION: Don't use do-while
        // Edge case: When current == list.size => do before while cause
        // current out-of-bound
        advance(); // pass over the token which cause ParseError
        while (!endOfFile()) {
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
            advance();
        }
    }

    private Expression finishCall(Expression funcName) {
        List<Expression> args = new ArrayList<>();

        if (!isNextToken(RIGHT_PAREN)) { // handle no argument edge case
            do {
                if (args.size() >= 255)
                    panicErrorCustom(nextToken(), "can't have more than 255 arguments");

                Expression nextArg = expression();
                args.add(nextArg);
            } while (matchAtLeast(COMMA));
        }

        panicError(RIGHT_PAREN, "expect ')' closing arguments of function call");
        // Why put closeParen after panicError?
        // panicError check nextToken. If nextToken == ')', then it pass over
        // then definitely prevToken is ')'.
        // So if there is any error, check panicError instead of prevToken.
        Token closeParen = prevToken();
        return new Call(funcName, closeParen, args);
    }

    /* ---------------- Error handle -------------------- */

    /**
     * Same as {@code error()}
     *
     * @param token - Custom token
     */
    private ParseError panicErrorCustom(Token token, String message) {
        throw new ParseError(token, message);
    }

    /**
     * Same as {@code consume()}
     *
     * @implNote Copy of {@link #panicErrorCustom(Token, String)}
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
