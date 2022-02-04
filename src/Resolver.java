package src;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import models.Token;
import models.Expression;
import models.Expression.Literal;
import models.Expression.This;
import models.Expression.Grouping;
import models.Expression.Logical;
import models.Expression.Call;
import models.Expression.Get;
import models.Expression.Unary;
import models.Expression.VarAccess;
import models.Expression.Binary;
import models.Expression.Ternary;
import models.Expression.Assign;
import models.Expression.Set;
import models.Statement;
import models.Statement.Block;
import models.Statement.ClassDeclare;
import models.Statement.Expr;
import models.Statement.Print;
import models.Statement.Return;
import models.Statement.VarDeclare;
import models.Statement.If;
import models.Statement.While;
import models.Statement.FuncPrototype;

public class Resolver {
    private final Interpreter interpreter;
    /**
     * <i>implNote</i>:
     * <p />
     * The second field of {@code Map}, {@code Boolean} is a flag.
     * {@code false} when the variable just <b><i>declared</i></b>.
     * <p />
     * Must use {@code Boolean} primitive instead of {@code boolean}
     * because {@code Map} don't accept {@code boolean} as a generic
     */
    private final Stack<Map<String, Boolean>> scopeStack = new Stack<>();
    /**
     * Invalid return handling. See page 187.
     */
    private FuncType currentFunction = FuncType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void run(List<Statement> ASTList) {
        try {
            resolve(ASTList);
        } catch (ResolveError e) {
            Tan.err.report(e.identifier, e.getMessage());
        }
    }

    /* ---------------- Helper function -------------------- */

    private void runStatement(Statement s) {
        switch (s) {
            case Block b -> {
                beginScope();
                resolve(b._stmtList);
                endScope();
            }
            case VarDeclare vd -> {
                // Why split to 2 steps: declare -> define?
                // Consider this case
                /*
                 * var a = "outer";
                 * {
                 * var a = a;
                 * }
                 */
                declare(vd._identifier); // == prototype

                if (vd._initializer != null) {
                    resolve(vd._initializer);
                }

                define(vd._identifier);
            }
            case FuncPrototype fp -> {
                declare(fp._identifier); // == prototype
                define(fp._identifier);

                resolveFunction(fp, FuncType.FUNCTION);
            }
            case ClassDeclare cd -> {
                ClassType enclosingClass = currentClass; // for case nested classes
                currentClass = ClassType.CLASS;

                declare(cd._identifier);
                define(cd._identifier);

                beginScope();
                scopeStack.peek().put("this", true);

                for (FuncPrototype method : cd._methods) {
                    resolveFunction(method, FuncType.METHOD);
                }

                endScope();
                currentClass = enclosingClass;
            }
            case If i -> {
                resolve(i._condition);
                resolve(i._ifStmt);
                if (i._elseStmt != null)
                    resolve(i._elseStmt);
            }
            case While w -> {
                resolve(w._condition);
                resolve(w._body);
            }
            case Return r -> {
                if (currentFunction == FuncType.NONE) {
                    throwError(r._keyword, "Can't return from top-level code.");
                }

                if (r._returnVal != null)
                    resolve(r._returnVal);
            }
            case Print p -> {
                resolve(p._expr);
            }
            case Expr e -> {
                resolve(e._expr);
            }
            default -> {
            }
        }
    }

    /**
     * Same as {@link Interpreter#switchPattern(Expression)}.
     * Just change the name
     */

    private void runExpression(Expression e) {
        switch (e) {
            case Assign a -> {
                // Right-to-left
                resolve(a._value);
                resolveVariable(a, a._identifier);
            }
            case Set s -> {
                // Right-to-left
                resolve(s._value);
                resolve(s._object);
                // Since properties are looked up dynamically, they don’t get resolved
            }
            case Ternary t -> {
                resolve(t._rhs_second);
                resolve(t._rhs_first);
                resolve(t._lhs);
            }
            case Binary b -> {
                resolve(b._lhs);
                resolve(b._rhs);
            }
            case Unary u -> {
                resolve(u._expr);
            }
            case Call c -> {
                resolve(c._funcName);
                for (Expression arg : c._arguments) {
                    resolve(arg);
                }
            }
            case Get g -> {
                resolve(g._object);
                // Since properties are looked up dynamically, they don’t get resolved
            }
            case Logical l -> {
                resolve(l._lhs);
                resolve(l._rhs);
            }
            case VarAccess va -> {
                Token identifier = va._identifer;
                if (!scopeStack.isEmpty() && scopeStack.peek().get(identifier.getLexeme()) == Boolean.FALSE) {
                    throwError(identifier, "Can't read local variable in its own initializer");
                }

                resolveVariable(va, identifier);
            }
            case Grouping g -> {
                resolve(g._expr);
            }
            case This th -> {
                if (currentClass == ClassType.NONE) {
                    throwError(th._keyword, "Can't use this outside class");
                }

                resolveVariable(th, th._keyword);
            }
            case Literal l -> {
                // nothing
            }
            default -> {

            }
        }
    }

    /* ---------------- Scope Helper function -------------------- */

    private void beginScope() {
        scopeStack.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopeStack.pop();
    }

    /**
     * Mark variable as {@code undefined}
     */
    private void declare(Token identifier) {
        if (scopeStack.isEmpty())
            return;

        Map<String, Boolean> currentScope = scopeStack.peek();
        if (currentScope.containsKey(identifier.getLexeme())) {
            throwError(identifier,
                    "Already variable with this name in this scope.");
        }

        currentScope.put(identifier.getLexeme(), false);
    }

    /**
     * Mark variable as {@code defined}
     */
    private void define(Token identifier) {
        if (scopeStack.isEmpty())
            return;
        // scopeStack.peek() = currentScope
        scopeStack.peek().put(identifier.getLexeme(), true);
    }

    /* ---------------- Switch Pattern Helper function -------------------- */

    /**
     * Same as {@code resolveLocal()}
     */
    private void resolveVariable(Expression expr, Token identifier) {
        String id = identifier.getLexeme();

        for (int idx = scopeStack.size() - 1; idx >= 0; idx--) {
            if (scopeStack.get(idx).containsKey(id)) {
                interpreter.storeResolve(expr, scopeStack.size() - idx - 1);
                return;
            }
        }
    }

    private void resolve(Statement stmt) {
        runStatement(stmt);
    }

    private void resolve(List<Statement> stmtList) {
        for (Statement stmt : stmtList) {
            resolve(stmt);
        }
    }

    private void resolve(Expression expr) {
        runExpression(expr);
    }

    private void resolveFunction(FuncPrototype function, FuncType type) {
        FuncType enclosingFunction = currentFunction; // stash currentFunction status
        currentFunction = type;

        beginScope();
        for (Token param : function._params) {
            declare(param);
            define(param);
        }
        resolve(function._blockStmt);
        endScope();

        currentFunction = enclosingFunction;
    }

    /* ---------------- Error Define -------------------- */

    public class ResolveError extends RuntimeException {
        final Token identifier;

        ResolveError(Token identifier, String message) {
            super(message);
            this.identifier = identifier;
        }
    }

    private void throwError(Token identifier, String message) {
        throw new ResolveError(identifier, message);
    }

    private enum FuncType {
        NONE,
        FUNCTION,
        METHOD
    }

    /**
     * Use {@code enum} for inheritance
     */
    private enum ClassType {
        NONE,
        CLASS
    }
}
