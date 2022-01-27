package models;

public interface Expression {
    public static class Literal implements Expression {
        public final Object _value;

        public Literal(Object value) {
            _value = value;
        }
    }

    public static class Grouping implements Expression {
        public final Expression _expr;

        public Grouping(Expression expr) {
            _expr = expr;
        }
    }

    public static class Unary implements Expression {
        public final Token _operator;
        public final Expression _expr;

        public Unary(Token operator, Expression expr) {
            _operator = operator;
            _expr = expr;
        }
    }

    public static class Binary implements Expression {
        public final Expression _lhs;
        public final Token _operator;
        public final Expression _rhs;

        public Binary(Expression lhs, Token operator, Expression rhs) {
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

        public Ternary(Expression lhs, Token operator, Expression rhs_first, Expression rhs_second) {
            _lhs = lhs;
            _operator = operator;
            _rhs_first = rhs_first;
            _rhs_second = rhs_second;
        }
    }

}
