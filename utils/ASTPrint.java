package utils;

import models.Expression;
import models.Expression.Binary;
import models.Expression.Grouping;
import models.Expression.Literal;
import models.Expression.Ternary;
import models.Expression.Unary;

/**
 * @implNote Print {@code Postfix} order
 *
 */
public class ASTPrint {
    public void print(Expression AST) {
        System.out.println(switchPattern(AST));
    }

    /* --------- Helper function --------- */

    private String parenthensize(String operator, Expression... exprArr) {
        StringBuilder result = new StringBuilder(); // NOTE: https://stackoverflow.com/a/5234160/12897204

        // FOR DEBUG: First recursive run - Operator keep appear at the first
        result.append("(").append(operator);
        // position
        for (Expression e : exprArr) {
            result.append(" ");

            result.append(switchPattern(e)); // NOTE: Recursion of postfix here
        }
        result.append(")");
        return result.toString();
    }

    /**
     * @implNote Can't reduce `{ return parenthensize() }`. Stupid!
     */
    private String switchPattern(Expression e) {
        switch (e) {
            case Literal l -> {
                if (l._value == null)
                    return "nil";

                return l._value.toString();
            }
            case Grouping g -> {
                return parenthensize("group", g._expr);
            }
            case Unary u -> {
                return parenthensize(u._operator.getLexeme(), u._expr);
            }
            case Binary b -> {
                return parenthensize(b._operator.getLexeme(), b._lhs, b._rhs);
            }
            case Ternary t -> {
                // Right to left
                return parenthensize(t._operator.getLexeme(), t._rhs_second, t._rhs_first, t._lhs);
            }
            default -> {
                src.Tan.err.report(0, "Expression error");
                return "nil";
            }
        }
    }
}
