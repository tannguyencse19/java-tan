package src;

public class Error {
    private Boolean _errStatus;

    Error() {
        setError(false);
    }

    public void report(int line, String message) {
        report(line, "", message);
    }

    private void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
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
