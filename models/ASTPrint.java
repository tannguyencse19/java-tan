package models;

import models.Expression.*;
import static models.TokenType.*;

/**
 * Postfix Traversal
 *
 * @implNote
 *           Why code `implements Visitor (not Expression)?`
 *           <p />
 *           The body of the interface method is provided by the "implementing"
 *           class (that's why call implements, not implemented)
 *           <p />
 *           Why Visitor<String (not TokenType)> ?
 *           <p />
 *           Type provided for Generic in this case will be the return type of
 *           the methods. See Visitor doc.
 *
 * @see https://www.w3schools.com/java/java_interface.asp
 *
 */
public class ASTPrint implements Visitor<String> {
    String print(Expression e) {
        // NOTE: https://gist.github.com/tannguyencse19/4c64c53e6f49fb368a5d1b712d1be0fe
        return e.accept(this);
    }

    @Override
    public String visitLiteral(Literal instance) {
        if (instance._value == null)
            return "nil";

        return instance._value.toString();
    }

    @Override
    public String visitGrouping(Grouping instance) {
        return parenthensize("group", instance._expr);

    }

    @Override
    public String visitUnary(Unary instance) {
        return parenthensize(instance._operator._lexeme, instance._expr);

    }

    @Override
    public String visitBinary(Binary instance) {
        return parenthensize(instance._operator._lexeme, instance._lhs, instance._rhs);
    }

    /* --------- Helper function --------- */

    private String parenthensize(String operator, Expression... exprArr) {
        StringBuilder result = new StringBuilder(); // NOTE: https://stackoverflow.com/a/5234160/12897204

        result.append("(").append(operator);
        for (Expression e : exprArr) {
            result.append(" ");

            // NOTE: Recursion of postfix traversal here
            result.append(e.accept(this));
        }
        result.append(")");
        return result.toString();
    }

    /* --------- Test --------- */
    public static void main(String[] args) {
        // NOTE: Chua dua vao 1 expression human-readable ma dang dua vao result luon
        Expression test = new Binary(new Literal(1), new Token(PLUS, "+", 1),
                new Literal(2));
        System.out.println(new ASTPrint().print(test));

        /* (* (- 123) (group 45.67)) */
        // Expression e = new Binary(
        // new Unary(
        // new Token(SUBTRACT, "-", 1),
        // new Literal(123)),
        // new Token(MULTIPLY, "*", 1),
        // new Grouping(
        // new Literal(45.67)));

        // System.out.println(new ASTPrint().print(e));
    }
}
