package models;

public class Token {
    final TokenType _type;
    final String _lexeme;
    final Number _lineID;

    /**
     *
     * @param lexeme
     * @param type
     * @param lineID
     * @implNote param 3rd - Object literal has been removed
     */
    public Token(TokenType type, String lexeme, Number lineID) {
        _type = type;
        _lexeme = lexeme;
        _lineID = lineID;
    }

    public String toString() {
        return _lexeme + ' ' + _type + ' ' + _lineID;
    }

    /* --------- Helper function --------- */
    // NOTE: There are no set methods due to variable declare as "final"

    public TokenType getType() {
        return _type;
    }

    public String getLexeme() {
        return _lexeme;
    }

    public Number getLineID() {
        return _lineID;
    }
}
