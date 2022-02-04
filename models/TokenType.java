package models;

public enum TokenType {
    // Single-character tokens
    PLUS, SUBTRACT, MULTIPLY, DIVIDE, EQUAL,
    COMMA, DOT, QUESTION, COLON, SEMI_COLON,

    // One or two character tokens
    INCREMENT, DECREMENT,
    MORE, MORE_EQUAL, LESS, LESS_EQUAL, EQUAL_EQUAL, NOT_EQUAL,
    LOGIC_NOT, LOGIC_AND, LOGIC_OR,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    TERNARY,

    // Literals type
    BOOLEAN, NUMBER, STRING, NIL,
    OBJECT,

    // Keywords
    // IDENTIFIER: ten bien
    TRUE, FALSE,
    IDENTIFIER, VAR,
    PRINT,
    IF, ELSE,
    WHILE, FOR,
    FUNCTION, RETURN,
    CLASS, THIS, SUPER,
    EOF,

    // Others
    /**
     * Example:
     *
     * <pre>
     * private TokenType match(TokenType type) {
     *     if (isNextToken(type)) {
     *         return type;
     *     }
     *
     *     return NONE;
     * }
     * </pre>
     */
    NONE,
}
