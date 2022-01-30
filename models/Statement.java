package models;
import java.util.List;
public interface Statement {
public static class Block implements Statement {
public final List<Statement> _stmtList;
public Block( List<Statement> stmtList) {
_stmtList = stmtList;
}
}
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
public static class If implements Statement {
public final Expression _condition;
public final Statement _ifStmt;
public final Statement _elseStmt;
public If( Expression condition, Statement ifStmt, Statement elseStmt) {
_condition = condition;
_ifStmt = ifStmt;
_elseStmt = elseStmt;
}
}
}
