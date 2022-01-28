package models;

public class Token {
    final TokenType _type;
    final String _lexeme;
    final Object _literal;
    final int _lineID;

    public Token(TokenType type, String lexeme, int lineID) {
        _type = type;
        _lexeme = lexeme;
        _lineID = lineID;
        _literal = null;
    }

    /**
     * @param literal - Only for {@code TokenType: NUMBER, STRING}
     */
    public Token(TokenType type, String lexeme, Object literal, int lineID) {
        _type = type;
        _lexeme = lexeme;
        _lineID = lineID;
        _literal = literal;
    }

    /* --------- Helper function --------- */
    // NOTE: There are no set methods due to variable declare as "final"

    public TokenType getType() {
        return _type;
    }

    public String getLexeme() {
        return _lexeme;
    }

    public Object getLiteral() {
        return _literal;
    }

    public int getLineID() {
        return _lineID;
    }
}
