package models;

abstract class Expression {
    static class Binary extends Expression {
        final Expression _lhs;
        final Token _operator;
        final Expression _rhs;

        Binary(Expression lhs, Token operator, Expression rhs) {
            _lhs = lhs;
            _operator = operator;
            _rhs = rhs;
        }
    }
}
