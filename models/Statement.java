package models;

public interface Statement {
public static class Expr implements Statement {
public final Expression _expr;
public Expr( Expression expr) {
_expr = expr;
}
}
public static class Print implements Statement {
public final Expression _expr;
public Print( Expression expr) {
_expr = expr;
}
}
public static class VarDeclare implements Statement {
public final Token _identifier;
public final Expression _initializer;
public VarDeclare( Token identifier, Expression initializer) {
_identifier = identifier;
_initializer = initializer;
}
}
}
