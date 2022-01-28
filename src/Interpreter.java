package src;

import models.Expression;
import models.Expression.Literal;
import models.Expression.Grouping;
import models.Expression.Unary;
import models.Expression.Binary;
import models.Expression.Ternary;

public class Interpreter {
    public void run(Expression AST) {
        switchPattern(AST);
    }

    /* --------- Helper function --------- */

    /**
     * @implNote Can't reduce `{ return parenthensize() }`. Stupid!
     */
    private Object switchPattern(Expression e) {
        switch (e) {
            case Ternary t -> {
                // Right to left
                Object rhs_second = switchPattern(t._rhs_second);
                Object rhs_first = switchPattern(t._rhs_first);
                Object lhs = switchPattern(t._lhs);

                return truthy(lhs) ? rhs_first : rhs_second;
            }
            case Binary b -> {
                Object lhs = switchPattern(b._lhs);
                Object rhs = switchPattern(b._rhs);

                switch (b._operator.getType()) {
                    case COMMA:
                        return rhs; // discard lhs
                    case NOT_EQUAL:
                        return !isEqual(lhs, rhs);
                    case EQUAL_EQUAL:
                        return isEqual(lhs, rhs);
                    case MORE:
                        return (double) lhs > (double) rhs;
                    case MORE_EQUAL:
                        return (double) lhs >= (double) rhs;
                    case LESS:
                        return (double) lhs < (double) rhs;
                    case LESS_EQUAL:
                        return (double) lhs <= (double) rhs;
                    case PLUS:
                        if (lhs instanceof Double && rhs instanceof Double)
                            return (double) lhs + (double) rhs;
                        else if (lhs instanceof String && rhs instanceof String)
                            return (String) lhs + (String) rhs;
                        else {
                            Tan.err.report(b._operator, "operator '+' receive unexpected operand");
                            return null;
                        }
                    case SUBTRACT:
                        return (double) lhs - (double) rhs;
                    case MULTIPLY:
                        return (double) lhs * (double) rhs;
                    case DIVIDE:
                        if ((double) rhs == 0)
                            Tan.err.report(b._operator, "divide by 0");

                        return (double) lhs / (double) rhs;
                    default:
                        Tan.err.report(b._operator, "unexpected operator");
                        return null;
                }
            }
            case Unary u -> {
                Object rhs = switchPattern(u._expr);

                switch (u._operator.getType()) {
                    case LOGIC_NOT:
                        return !truthy(rhs);
                    case SUBTRACT:
                        return -(double) rhs;
                    default:
                        Tan.err.report(u._operator, "unexpected unary operator");
                        return null;
                }
            }
            case Grouping g -> {
                return switchPattern(g._expr);
            }
            case Literal l -> {
                return l._value;
            }
            default -> {
                src.Tan.err.report(0, "Expression error");
                return null;
            }
        }
    }

    private boolean isEqual(Object lhs, Object rhs) {
        if (lhs == null && rhs == null)
            return true;
        else if (lhs == null) // (rhs != null) or (lhs != null && rhs == null)
            return false;
        else
            return lhs.equals(rhs);
    }

    /**
     * Lox follows Ruby’s rule:
     * - {@code false} and {@code nil} are falsey
     * - everything else is truthy
     */
    private boolean truthy(Object value) {
        if (value == null)
            return false;
        else if (value instanceof Boolean)
            return (boolean) value;
        else
            return true;
    }
}
