package models;
import java.util.List;
import models.Expression.VarAccess;
public interface Expression {
public static class Literal implements Expression {
public final Object _value;
public Literal( Object value) {
_value = value;
}
}
public static class This implements Expression {
public final Token _keyword;
public This( Token keyword) {
_keyword = keyword;
}
}
public static class Super implements Expression {
public final Token _keyword;
public final Token _methodName;
public Super( Token keyword, Token methodName) {
_keyword = keyword;
_methodName = methodName;
}
}
public static class VarAccess implements Expression {
public final Token _identifer;
public VarAccess( Token identifer) {
_identifer = identifer;
}
}
public static class Logical implements Expression {
public final Expression _lhs;
public final Token _operator;
public final Expression _rhs;
public Logical( Expression lhs, Token operator, Expression rhs) {
_lhs = lhs;
_operator = operator;
_rhs = rhs;
}
}
public static class Grouping implements Expression {
public final Expression _expr;
public Grouping( Expression expr) {
_expr = expr;
}
}
public static class Call implements Expression {
public final Expression _funcName;
public final Token _closeParen;
public final List<Expression> _arguments;
public Call( Expression funcName, Token closeParen, List<Expression> arguments) {
_funcName = funcName;
_closeParen = closeParen;
_arguments = arguments;
}
}
public static class Get implements Expression {
public final Expression _object;
public final Token _propName;
public Get( Expression object, Token propName) {
_object = object;
_propName = propName;
}
}
public static class Unary implements Expression {
public final Token _operator;
public final Expression _expr;
public Unary( Token operator, Expression expr) {
_operator = operator;
_expr = expr;
}
}
public static class Assign implements Expression {
public final Token _identifier;
public final Expression _value;
public Assign( Token identifier, Expression value) {
_identifier = identifier;
_value = value;
}
}
public static class Set implements Expression {
public final Expression _object;
public final Token _propName;
public final Expression _value;
public Set( Expression object, Token propName, Expression value) {
_object = object;
_propName = propName;
_value = value;
}
}
public static class Binary implements Expression {
public final Expression _lhs;
public final Token _operator;
public final Expression _rhs;
public Binary( Expression lhs, Token operator, Expression rhs) {
_lhs = lhs;
_operator = operator;
_rhs = rhs;
}
}
public static class Ternary implements Expression {
public final Expression _lhs;
public final Token _operator;
public final Expression _rhs_first;
public final Expression _rhs_second;
public Ternary( Expression lhs, Token operator, Expression rhs_first, Expression rhs_second) {
_lhs = lhs;
_operator = operator;
_rhs_first = rhs_first;
_rhs_second = rhs_second;
}
}
}
