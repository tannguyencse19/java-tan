package models;

public enum TokenType {
    // Single-character tokens
    PLUS, SUBTRACT, MULTIPLY, DIVIDE, EQUAL,

    // One or two character tokens
    INCREMENT, DECREMENT,
    MORE, MORE_EQUAL, LESS, LESS_EQUAL, EQUAL_EQUAL, NOT_EQUAL,
    LOGIC_NOT, LOGIC_AND, LOGIC_OR,
    LEFT_PARAN, RIGHT_PARAN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT,

    // Literals type
    BOOLEAN, NUMBER, STRING, NIL,
    OBJECT,

    // Keywords
    // IDENTIFER: ten bien
    TRUE, FALSE,
    IDENTIFIER, VAR,
    PRINT,
    IF, ELSE,
    WHILE, FOR,
    FUNCTION, RETURN,
    CLASS,
    EOF,
}
