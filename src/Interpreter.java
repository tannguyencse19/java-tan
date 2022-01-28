package src;

import java.util.Objects;

import models.Expression;
import models.Token;
import models.Expression.Literal;
import models.Expression.Grouping;
import models.Expression.Unary;
import models.Expression.Binary;
import models.Expression.Ternary;

public class Interpreter {
    public void run(Expression AST) {
        try {
            Object result = switchPattern(AST);
            System.out.println(clean(result));
        } catch (RuntimeError e) {
            Tan.err.report(e);
        }
    }

    /* --------- Helper function --------- */

    /**
     * @implNote Can't reduce `{ return parenthensize() }`. Stupid!
     */
    private Object switchPattern(Expression e) {
        switch (e) {
            case Ternary t -> {
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
                        verifyNumber(b._operator, "exist an operand of '>' is not a number", lhs, rhs);
                        return (double) lhs > (double) rhs;
                    case MORE_EQUAL:
                        verifyNumber(b._operator, "exist an operand of '>=' is not a number", lhs, rhs);
                        return (double) lhs >= (double) rhs;
                    case LESS:
                        verifyNumber(b._operator, "exist an operand of '<' is not a number", lhs, rhs);
                        return (double) lhs < (double) rhs;
                    case LESS_EQUAL:
                        verifyNumber(b._operator, "exist an operand of '<=' is not a number", lhs, rhs);
                        return (double) lhs <= (double) rhs;
                    case PLUS:
                        if (lhs instanceof Double && rhs instanceof Double)
                            return (double) lhs + (double) rhs;
                        else if (lhs instanceof String || rhs instanceof String)
                            return (String) lhs + (String) rhs;
                        else {
                            throw new RuntimeError(b._operator,
                                    "two operand of '+' are not type number or string");
                        }
                    case SUBTRACT:
                        verifyNumber(b._operator, "exist an operand of '-' is not a number", lhs, rhs);
                        return (double) lhs - (double) rhs;
                    case MULTIPLY:
                        verifyNumber(b._operator, "exist an operand of '*' is not a number", lhs, rhs);
                        return (double) lhs * (double) rhs;
                    case DIVIDE:
                        verifyNumber(b._operator, "exist an operand of '/' is not a number", lhs, rhs);
                        if ((double) rhs == 0)
                            throw new RuntimeError(b._operator, "divide by 0");

                        return (double) lhs / (double) rhs;
                    default:
                        throw new RuntimeError(b._operator, "unexpected binary operator");
                }
            }
            case Unary u -> {
                Object rhs = switchPattern(u._expr);

                switch (u._operator.getType()) {
                    case LOGIC_NOT:
                        return !truthy(rhs);
                    case SUBTRACT:
                        verifyNumber(u._operator, "operand of unary '-' is not a number", rhs);
                        return -((double) rhs);
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

    private String clean(Object obj) {
        String result = Objects.toString(obj, null);

        if (result == null)
            return "nil";
        else if (obj instanceof Double) {
            if (result.endsWith(".0"))
                result = result.substring(0, result.length() - 2);
        }

        return result;
    }

    private void verifyNumber(Token operator, String message, Object... args) {
        if (args.length == 1 && args[0] instanceof Double)
            return;
        else if (args.length == 2 && args[0] instanceof Double && args[1] instanceof Double)
            return;

        throw new RuntimeError(operator, message);
    }

    public class RuntimeError extends RuntimeException {
        final Token token;

        RuntimeError(Token operator, String message) {
            super(message);
            token = operator;
        }
    };
}
