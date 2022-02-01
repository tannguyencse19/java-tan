package src;

import java.util.HashMap;
import java.util.Map;

import models.Token;

class Environment {
    private final Map<String, Object> values = new HashMap<>();
    /**
     * Same as {@code enclosing}
     */
    private final Environment currentEnv;

    /**
     * Use to create {@code Global} Scope
     * <p />
     * <b><i>Note for debugging:</i></b> {@code Global} Scope will have
     * {@code currentEnv = null}
     */
    public Environment() {
        currentEnv = null;
    }

    /**
     * Use to create new local environment
     */
    public Environment(Environment newLocalEnv) {
        currentEnv = newLocalEnv;
    }

    /**
     * Same as {@code define()}
     */
    public void defineVar(String identifier, Object value) {
        values.put(identifier, value);
    }

    /**
     * The key difference between this function vs
     * {@link #defineVar(String, Object)} is this func not allowed to create new
     * variable
     */
    public void assign(Token identifier, Object value) {
        if (values.containsKey(identifier.getLexeme())) {
            values.put(identifier.getLexeme(), value);
        } else if (currentEnv != null) {
            currentEnv.assign(identifier, value);
        } else
            throw new Interpreter().new RuntimeError(identifier,
                    "assignement to undefined variable: " + identifier.getLexeme());
    }

    /**
     * Same as {@code get()}
     */
    public Object getValue(Token finding) {
        if (values.containsKey(finding.getLexeme()))
            return values.get(finding.getLexeme());

        if (currentEnv != null) {
            return currentEnv.getValue(finding);
        } else
            throw new Interpreter().new RuntimeError(finding, "undefined variable: " + finding.getLexeme());
    }
}
