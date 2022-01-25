package models;

abstract class Expression {
    abstract <T> T accept(Visitor<T> v);

    static class Literal extends Expression {
        final Object _value;

        Literal(Object value) {
            _value = value;
        }

        @Override
        <T> T accept(Visitor<T> v) {
            return v.visitLiteral(this);
        }
    }

    static class Grouping extends Expression {
        final Expression _expr;

        Grouping(Expression expr) {
            _expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> v) {
            return v.visitGrouping(this);
        }
    }

    static class Unary extends Expression {
        final Token _operator;
        final Expression _expr;

        Unary(Token operator, Expression expr) {
            _operator = operator;
            _expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> v) {
            return v.visitUnary(this);
        }
    }

    static class Binary extends Expression {
        final Expression _lhs;
        final Token _operator;
        final Expression _rhs;

        Binary(Expression lhs, Token operator, Expression rhs) {
            _lhs = lhs;
            _operator = operator;
            _rhs = rhs;
        }

        @Override
        <T> T accept(Visitor<T> v) {
            return v.visitBinary(this);
        }
    }

    interface Visitor<T> {
        T visitLiteral(Literal instance);

        T visitGrouping(Grouping instance);

        T visitUnary(Unary instance);

        T visitBinary(Binary instance);
    }
}
