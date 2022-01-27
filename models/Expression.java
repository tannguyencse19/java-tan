package models;

public abstract class Expression {
    public abstract <T> T accept(Visitor<T> v);

    public static class Literal extends Expression {
        public final Object _value;

        public Literal(Object value) {
            _value = value;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitLiteral(this);
        }
    }

    public static class Grouping extends Expression {
        public final Expression _expr;

        public Grouping(Expression expr) {
            _expr = expr;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitGrouping(this);
        }
    }

    public static class Unary extends Expression {
        public final Token _operator;
        public final Expression _expr;

        public Unary(Token operator, Expression expr) {
            _operator = operator;
            _expr = expr;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitUnary(this);
        }
    }

    public static class Binary extends Expression {
        public final Expression _lhs;
        public final Token _operator;
        public final Expression _rhs;

        public Binary(Expression lhs, Token operator, Expression rhs) {
            _lhs = lhs;
            _operator = operator;
            _rhs = rhs;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitBinary(this);
        }
    }

    public static class Ternary extends Expression {
        public final Expression _lhs;
        public final Token _operator;
        public final Expression _rhs_first;
        public final Expression _rhs_second;

        public Ternary(Expression lhs, Token operator, Expression rhs_first, Expression rhs_second) {
            _lhs = lhs;
            _operator = operator;
            _rhs_first = rhs_first;
            _rhs_second = rhs_second;
        }

        @Override
        public <T> T accept(Visitor<T> v) {
            return v.visitTernary(this);
        }
    }

    public interface Visitor<T> {
        T visitLiteral(Literal instance);

        T visitGrouping(Grouping instance);

        T visitUnary(Unary instance);

        T visitBinary(Binary instance);

        T visitTernary(Ternary instance);
    }
}
