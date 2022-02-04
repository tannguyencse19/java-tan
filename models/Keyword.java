package models;

import java.util.HashMap;
import java.util.Map;

import static models.TokenType.*;

public final class Keyword {
    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("nil", NIL);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("var", VAR);
        keywords.put("print", PRINT);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("and", LOGIC_AND);
        keywords.put("or", LOGIC_OR);
        keywords.put("while", WHILE);
        keywords.put("for", FOR);
        keywords.put("function", FUNCTION);
        keywords.put("return", RETURN);
        keywords.put("class", CLASS);
        keywords.put("this", THIS);
        keywords.put("super", SUPER);
    }

    /**
     *
     * @return If has, return keyword's TokenType; Else return `IDENTIFER`
     */
    public static TokenType get(String expected) {
        return keywords.get(expected);
    }
}
