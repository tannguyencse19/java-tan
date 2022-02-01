package src;

import models.Token;
import models.TokenType;
import src.Interpreter.RuntimeError;

public class Error {
    private Boolean _errStatus;
    private Boolean runtimeError;

    Error() {
        setError(false);
    }

    public void report(int line, String message) {
        report(line, "", message); // FIX: where is being skipped
    }

    public void report(Token token, String message) {
        // NOTE: Don't use tenary to pass argument
        // NOTE: https://stackoverflow.com/a/40521314/12897204
        if (token.getType() != TokenType.EOF)
            report(token.getLineID(), token.getLexeme(), message);
        else
            report(token.getLineID(), "", message);
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

    public void report(RuntimeError err) {
        if (err.token != null) {
            System.err.println(err.getMessage() +
                    "\n[line " + err.token.getLineID() + "]");
        } else if (err.expr != null) {
            System.err.println(err.getMessage() +
                    "\n[expression " + err.expr + "]");
        } else if (err.stmt != null) {
            System.err.println(err.getMessage() +
                    "\n[statement " + err.stmt + "]");
        }
        setRuntimeError(true);
    }

    /* --------- Helper function ---------- */
    public void setError(Boolean status) {
        _errStatus = status;
    }

    public Boolean hasError() {
        return _errStatus;
    }

    public void setRuntimeError(Boolean status) {
        runtimeError = status;
    }

    public Boolean hasRuntimeError() {
        return runtimeError;
    }

    /* ---------------- Error Type -------------------- */

}
