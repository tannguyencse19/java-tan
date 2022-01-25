package src;

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
                addToken(LEFT_PARAN);
                break;
            case ')':
                addToken(RIGHT_PARAN);
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
            case ',':
                addToken(COMMA);
                break;

            /* --------- Others --------- */

            case '.': // FIX: Not know where to put
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

    /**
     * @implNote This function same as `readSource()` except it doesn't `++current`
     *
     * @implSpec Have to being overloadded because Java not support default param
     *           value
     */
    private char nextChar() {
        if (endOfFile())
            return '\0';

        return source.charAt(current); // NOTE: current Offset-by-1 nen khong can current+1
    }

    private char nextNextChar() {
        if (endOfFile(1))
            return '\0';

        return source.charAt(current + 1);
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
     * @param type
     * @see - Phan biet Token >< Lexeme:
     *      https://stackoverflow.com/a/14958865/12897204
     */
    private void addToken(TokenType type) {
        String lexeme = source.substring(start, current); // NOTE: Lexeme khong nhat thiet la 1 tu hoan chinh, ma co the
                                                          // la 1 chu cai thoi
        tokenList.add(new Token(type, lexeme, line));
    }

    private void addToken(TokenType type, String i_lexeme) {
        tokenList.add(new Token(type, i_lexeme, line));
    }

    private void addToken(TokenType type, int i_start, int i_current) {
        String lexeme = source.substring(i_start, i_current);
        tokenList.add(new Token(type, lexeme, line));
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
        // String str = source.substring(start + 1, current - 1);
        // tokenList.add(new Token(STRING, str, line));

        addToken(STRING, start + 1, current - 1);
    }

    private void addNum() {
        while ((isDigit(nextChar()) || isNextChar('.')) && !endOfFile())
            ++current; // Neu la digit thi moi cho doc tiep

        addToken(NUMBER);
    }

    private void addIdentifier() {
        char c = nextChar();

        while ((isIdentifier(c) || isDigit(c)) && !endOfFile()) {
            c = readSource(); // NOTE: Because readSource() also ++current;
        }

        String id = source.substring(start, current - 1);
        TokenType type = (Keyword.get(id) != null) ? Keyword.get(id) : IDENTIFIER;
        addToken(type, id);
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
