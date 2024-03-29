package src;

import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.List;

import models.Token;
import models.TokenType;
import models.Keyword;
import static models.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokenList = new ArrayList<>();
    private int start = 0, current = 0, line = 1;

    // constructor
    Scanner(String source) {
        this.source = source;
    }

    List<Token> getListToken() {
        while (!endOfFile()) {
            start = current; // Keep track of next lexeme location
            scanToken();
        }

        tokenList.add(new Token(EOF, "", line)); // reach end of a line
        return tokenList;
    }

    /* --------- Helper function --------- */

    private boolean endOfFile() {
        return current == source.length();
        // || current == (source.length() + 1); // HACK: Phong truong hop doc lo 1 char
    }

    private boolean endOfFile(int currentOffset) {
        return (current + currentOffset) >= source.length();
    }

    private void scanToken() {
        char c = readSource();

        // Which case first and after?
        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Operator_Precedence#table
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            // POSTFIX INCREMENT, DECREMENT
            // LOGIC NOT
            // UNARY PLUS, SUBTRACT
            // PREFIX INCREMENT, DECREMENT
            // EXP
            // ARITHMETIC
            case '*':
                if (isNextChar('/')) {
                    Tan.err.report(line, " Unexpected character: " + c
                            + "/" + "\nYou may using nested block comment, which isn't supported");
                }

                addToken(MULTIPLY);
                break;
            case '/':
                if (isNextChar('/')) {
                    // Comment character, single line
                    while (!isNextChar('\n') && !endOfFile())
                        readSource();
                    // NOTE: No addToken() here because comment line don't affect to program
                } else if (isNextChar('*')) {
                    // Comment character, multiple line
                    while (!(nextChar() == '*' && nextNextChar() == '/') && !endOfFile()) {
                        if (isNextChar('\n'))
                            ++line;

                        readSource();
                    }

                    current += 2; // HACK: skip over */, ban dau cong viec nay do isNextChar() dam nhiem
                } else
                    addToken(DIVIDE);
                break;
            case '+':
                addToken(PLUS);
                break;
            case '-':
                addToken(SUBTRACT);
                break;
            // COMPARISION
            case '!':
                addToken(isNextChar('=') ? NOT_EQUAL : LOGIC_NOT);
                break;
            case '=':
                addToken(isNextChar('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(isNextChar('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(isNextChar('=') ? MORE_EQUAL : MORE);
                break;
            // LOGIC AND, OR
            // TERNARY // NOTE: Must seperate because ':' is far from '?', cause lexeme
            // between them is skipped
            case '?':
                addToken(QUESTION);
                break;
            case ':':
                addToken(COLON);
                break;

            /* --------- Others --------- */

            case ',':
                addToken(COMMA);
                break;
            case ';':
                addToken(SEMI_COLON);
                break;
            case '.':
                addToken(DOT);
                break;
            case '"':
                addString();
                break;
            // Skipped character
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                ++line;
                break;
            default:
                if (isDigit(c)) {
                    addNum();
                } else if (isIdentifier(c)) {
                    addIdentifier();
                } else {
                    // FIX: Tam thoi bo qua
                    // Tan.err.report(line, " Unexpected character: " + c);
                }
                break;
        }
    }

    private char readSource() {
        ++current;
        return source.charAt(current - 1); // NOTE: offset-by-1, due to current initial = 0
    }

    private char prevChar() {
        return (current == 0) ? source.charAt(current) : source.charAt(current - 1);
    }

    /**
     * @implNote This function same as `readSource()` except it doesn't `++current`
     *
     * @implSpec Have to being overloadded because Java not support default param
     *           value
     */
    private char nextChar() {
        return endOfFile() ? '\0' : source.charAt(current); // NOTE: current Offset-by-1 nen khong can current+1
    }

    private char nextNextChar() {
        return endOfFile(1) ? '\0' : source.charAt(current + 1);
    }

    // /**
    // *
    // * @param currentOffset - 0 = nextChar() no overload
    // */
    // private char nextChar(int currentOffset) {
    // if ((current + currentOffset) >= source.length())
    // return '\0';

    // return source.charAt(current + currentOffset); // NOTE: current Offset-by-1
    // nen khong can current+1
    // }

    /**
     * @see - Phan biet Token >< Lexeme:
     *      https://stackoverflow.com/a/14958865/12897204
     */
    private void addToken(TokenType type) {
        String lexeme = source.substring(start, current); // NOTE: Lexeme khong nhat thiet la 1 tu hoan chinh, ma co the
                                                          // la 1 chu cai thoi
        tokenList.add(new Token(type, lexeme, line));
    }

    /**
     * Override {@link #addToken(TokenType)}.
     * <p />
     * Example: {@link #addString()}
     *
     * <pre>
     * String pureValue = source.substring(start + 1, current - 1);
     * addToken(STRING, pureValue);
     * </pre>
     *
     * @param literal Store lexeme type.
     *                <p />
     *                Lexeme has type {@code string}, so even {@code number} will be
     *                scan as a {@code string}. {@code literal} has type
     *                {@code Object} so you can pass in original value and type. See
     *                example above.
     */
    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current); // NOTE: Lexeme khong nhat thiet la 1 tu hoan chinh, ma co the
                                                          // la 1 chu cai thoi
        tokenList.add(new Token(type, lexeme, literal, line));

    }

    private void addString() {
        while (!isNextChar('"')) {
            if (endOfFile())
                Tan.err.report(line, "reach EOF, unclose string block");

            if (isNextChar('\n')) // support multi-line string
                line++;

            readSource();
        }

        // Trim start, end character ""
        String pureValue = source.substring(start + 1, current - 1);
        addToken(STRING, pureValue);
    }

    private void addNum() {
        while ((isDigit(nextChar()) || isNextChar('.')) && !endOfFile())
            ++current; // Neu la digit thi moi cho doc tiep

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void addIdentifier() {
        char c = nextChar();

        while ((isIdentifier(c) || isDigit(c)) && !endOfFile()) {
            c = readSource();
        }

        // CAUTION: Hotfix - Can't get variable name which has 1 char
        // why -1 ? see caution below
        String id = (start == current - 1) ? source.substring(start, current) : source.substring(start, current - 1);
        TokenType type = (Keyword.get(id) != null) ? Keyword.get(id) : IDENTIFIER;
        // addToken(type);
        // CAUTION: Hotfix - Must get exactly lexeme for variable identifier to work
        // Currently, no need to refactor to new `addToken()`
        // because only this function used
        tokenList.add(new Token(type, id, line));

        // CAUTION: Hotfix
        // Decrement to not pass over the character in readSource()
        // which cause while loop stop
        // CAUTION: Controversy ';'
        char ch = prevChar();
        // if ((ch == '(' || ch == ')' || ch == '}' || ch == ',' || ch == ';') && nextNextChar() != '\0')

        switch (ch) {
            case '(':
            case ')':
            case '}':
            case ';':
            case ',':
            case '.':
            case '<':
                --current;
                break;
            default:
                break;
        }
    }

    /**
     *
     * @implNote If matched,
     *           <h3>that char will be skip and current also ++</h3>
     */
    private boolean isNextChar(char expected) {
        if (endOfFile() || (nextChar() != expected))
            return false;

        // else true -> Bo qua char hien tai, doc char tiep theo
        ++current;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isIdentifier(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c == '_');
    }
}
