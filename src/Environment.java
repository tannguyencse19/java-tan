package src;

import java.util.HashMap;
import java.util.Map;

import models.Token;

class Environment {
    /**
     * Same as {@code values}
     */
    private final Map<String, Object> variable = new HashMap<>();
    /**
     * Same as {@code enclosing}
     *
     * @implNote Global scope has {@code prevEnv = null}
     */
    private final Environment prevEnv;

    /**
     * Use to create {@code Global} Scope
     * <p />
     * <b><i>Note for debugging:</i></b> {@code Global} Scope will have
     * {@code prevEnv = null}
     */
    public Environment() {
        prevEnv = null;
    }

    /**
     * Use to create new local environment
     */
    public Environment(Environment newLocalEnv) {
        prevEnv = newLocalEnv;
    }

    /**
     * Same as {@code define()}
     */
    public void defineVar(String identifier, Object value) {
        variable.put(identifier, value);
    }

    /**
     * The key difference between this function vs
     * {@link #defineVar(String, Object)} is this func not allowed to create new
     * variable
     */
    public void assign(Token identifier, Object value) {
        if (variable.containsKey(identifier.getLexeme())) {
            variable.put(identifier.getLexeme(), value);
        } else if (prevEnv != null) {
            prevEnv.assign(identifier, value);
        } else
            throw new Interpreter().new RuntimeError(identifier,
                    "assignment to undefined variable: " + identifier.getLexeme());
    }

    /**
     * Same as {@link #assign(Token, Object)} but different algorithm, using
     * {@code distance}
     * <p />
     * <b><i>Example</i></b>
     *
     * <pre>
     * Integer distance = localVar.get(a);
     * if (distance != null)
     *     env.assignAt(distance, a._identifier, rhsResult);
     * else // you must handle this by yourself
     *     globals.assign(a._identifier, rhsResult);
     * </pre>
     */
    public void assignAt(Integer distance, Token identifier, Object value) {
        Environment ancestor = ancestor(distance);

        if (ancestor != null) {
            ancestor.variable.put(identifier.getLexeme(), value);
        }
        // else no throw error because we still have globalEnv inside Interpreter
    }

    /**
     * Same as {@code get()}
     */
    public Object getValue(Token finding) {
        if (variable.containsKey(finding.getLexeme()))
            return variable.get(finding.getLexeme());

        if (prevEnv != null) {
            return prevEnv.getValue(finding);
        } else
            throw new Interpreter().new RuntimeError(finding, "undefined variable: " + finding.getLexeme());
    }

    /**
     * Often use in junction with
     * {@link Interpreter#lookUpVariable(Token, Expression)}
     */
    public Object getAt(Integer distance, String identifier) {
        Environment ancestor = ancestor(distance);

        if (ancestor != null)
            return ancestor.variable.get(identifier);
        return null;
    }

    /* ---------------- Helper function -------------------- */

    private Environment ancestor(Integer distance) {
        Environment result = this;
        for (int counter = distance; counter > 0 && result != null; counter--) {
            result = result.prevEnv;
        }

        return result;
    }
}
