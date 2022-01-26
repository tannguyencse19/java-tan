package src;

import models.Token;
import models.TokenType;

public class Error {
    private Boolean _errStatus;

    Error() {
        setError(false);
    }

    public void report(int line, String message) {
        report(line, "", message); // FIX: where is being skipped
    }

    public void report(Token err, String message) {
        // NOTE: Don't use tenary to pass argument
        // NOTE: https://stackoverflow.com/a/40521314/12897204
        if (err.getType() != TokenType.EOF)
            report(err.getLineID(), err.getLexeme(), message);
        else
            report(err.getLineID(), "", message);
    }

    private void report(int line, String where,
            String message) {
        if (where.isEmpty())
            System.err.println(
                    "[line " + line + "] Error: " + message);
        else
            System.err.println(
                    "[line " + line + "] Error at '" + where + "': " + message);

        setError(true);
    }

    /* --------- Helper function ---------- */
    public void setError(Boolean status) {
        _errStatus = status;
    }

    public Boolean hasError() {
        return _errStatus;
    }
}
