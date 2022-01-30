package src;

import java.util.HashMap;
import java.util.Map;

import models.Token;

class Environment {
    private final Map<String, Object> values = new HashMap<>();

    public void set(String identifier, Object value) {
        values.put(identifier, value);
    }

    public Object get(Token finding) {
        if (values.containsKey(finding.getLexeme()))
            return values.get(finding.getLexeme());

        throw new Interpreter().new RuntimeError(finding, "undefined variable: " + finding.getLexeme());
    }
}
