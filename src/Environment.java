package src;

import java.util.HashMap;
import java.util.Map;

import models.Token;

class Environment {
    private final Map<String, Object> values = new HashMap<>();

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
            return;
        }

        throw new Interpreter().new RuntimeError(identifier,
                "assignement to undefined variable: " + identifier.getLexeme());
    }

    public Object getValue(Token finding) {
        if (values.containsKey(finding.getLexeme()))
            return values.get(finding.getLexeme());

        throw new Interpreter().new RuntimeError(finding, "undefined variable: " + finding.getLexeme());
    }
}
