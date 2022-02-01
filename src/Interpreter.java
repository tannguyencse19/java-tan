package src;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import src.Tan.TanCallable;
import src.Tan.TanFunction;
import models.Token;
import static models.TokenType.*;
import models.Expression;
import models.Expression.Literal;
import models.Expression.Grouping;
import models.Expression.Logical;
import models.Expression.Call;
import models.Expression.Unary;
import models.Expression.VarAccess;
import models.Expression.Binary;
import models.Expression.Ternary;
import models.Expression.Assign;
import models.Statement;
import models.Statement.Block;
import models.Statement.Expr;
import models.Statement.Print;
import models.Statement.VarDeclare;
import models.Statement.If;
import models.Statement.While;
import models.Statement.FuncPrototype;

public class Interpreter {
    final Environment globals = new Environment();
    private Environment env = globals;

    Interpreter() {
        globals.defineVar("clock", new TanCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000.0;
            }
        });
    }

    public void run(List<Statement> ASTList) {
        runBlock(ASTList, env);
    }

    /* --------- Helper function --------- */

    public void runBlock(List<Statement> stmtList, Environment currentEnv) {
        Environment prevEnv = this.env;
        try {
            this.env = currentEnv; // FOR DEBUG: global scope currentEnv = prevEnv

            for (Statement stmt : stmtList) {
                runStatement(stmt);
            }
        } catch (RuntimeError e) {
            Tan.err.report(e);
        } finally {
            this.env = prevEnv;
        }
    }

    /**
     * @implNote Have to code {@code Object result = switchPattern(e._expr);}
     *           because
     *           interface doesn't have fields
     */
    private void runStatement(Statement s) {
        switch (s) {
            case Block b -> {
                Environment local = new Environment(env);
                runBlock(b._stmtList, local);
            }
            case VarDeclare vd -> {
                Object result = null;
                if (vd._initializer != null) {
                    result = switchPattern(vd._initializer);
                }
                env.defineVar(vd._identifier.getLexeme(), result);
            }
            case If i -> {
                if (truthy(switchPattern(i._condition)))
                    runStatement(i._ifStmt);
                else if (i._elseStmt != null)
                    runStatement(i._elseStmt);
                else
                    return;
            }
            case While w -> {
                while (truthy(switchPattern(w._condition)))
                    runStatement(w._body);
            }
            case FuncPrototype fp -> {
                TanFunction func = new Tan().new TanFunction(fp);
                env.defineVar(fp._identifer.getLexeme(), func); // add function object
            }
            case Print p -> {
                Object result = switchPattern(p._expr);
                System.out.println(clean(result));
            }
            case Expr e -> {
                switchPattern(e._expr);
            }
            default -> {
                throwError(s, "Statement error");
            }
        }
    }

    /**
     * @implNote Can't reduce `{ return parenthensize() }`. Stupid!
     */
    private Object switchPattern(Expression e) {
        switch (e) {
            case Assign a -> {
                Object rhsResult = switchPattern(a._value);

                env.assign(a._identifier, rhsResult);

                return rhsResult;
            }
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
                            throwError(b._operator,
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
                            throwError(b._operator, "divide by 0");

                        return (double) lhs / (double) rhs;
                    default:
                        throwError(b._operator, "unexpected binary operator");
                        return null;
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
                        throwError(u._operator, "unexpected unary operator");
                        return null;
                }
            }
            case Call c -> {
                Object callee = switchPattern(c._funcName);

                List<Object> args = new ArrayList<>();
                for (Expression arg : c._arguments) {
                    args.add(switchPattern(arg));
                }

                if (!(callee instanceof TanCallable)) {
                    throwError(c._closeParen, "function name is not a callable function");
                }

                TanCallable function = (TanCallable) callee; // find the function prototype
                if (function.arity() != args.size()) {
                    throwError(c._closeParen, "Expected " +
                            function.arity() + " arguments but got " +
                            args.size());
                }

                return function.call(this, args);
            }
            case Logical l -> {
                Object lhs = switchPattern(l._lhs);

                // Short-circuit
                if (l._operator.getType() == LOGIC_AND) {
                    if (!truthy(lhs))
                        return lhs;
                } else {
                    if (truthy(lhs))
                        return lhs;
                }

                return switchPattern(l._rhs);
            }
            case VarAccess va -> {
                return env.getValue(va._identifer);
            }
            case Grouping g -> {
                return switchPattern(g._expr);
            }
            case Literal l -> {
                return l._value;
            }
            default -> {
                throwError(e, "Expression error");
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

        throwError(operator, message);
    }

    /* ---------------- Error Definition -------------------- */

    public class RuntimeError extends RuntimeException {
        final Token token;
        final Expression expr;
        final Statement stmt;

        RuntimeError(Token operator, String message) {
            super(message);
            token = operator;
            expr = null;
            stmt = null;
        }

        RuntimeError(Expression expression, String message) {
            super(message);
            token = null;
            expr = expression;
            stmt = null;
        }

        RuntimeError(Statement statement, String message) {
            super(message);
            token = null;
            expr = null;
            stmt = statement;
        }
    };

    /**
     * Wrapper for {@code throw new RuntimeError()}
     */
    private void throwError(Token token, String message) {
        throw new RuntimeError(token, message);
    }

    /**
     * Wrapper for {@code throw new RuntimeError()}
     */
    private void throwError(Expression expression, String message) {
        throw new RuntimeError(expression, message);
    }

    /**
     * Wrapper for {@code throw new RuntimeError()}
     */
    private void throwError(Statement statement, String message) {
        throw new RuntimeError(statement, message);
    }
}
