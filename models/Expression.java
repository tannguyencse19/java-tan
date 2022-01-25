package models;

abstract class Expression {
    abstract void accept(ExpressionVisitor v);

    static class Literal extends Expression {
        final Object _value;

        Literal(Object value) {
            _value = value;
        }

        @Override
        void accept(ExpressionVisitor v) {
            v.visitLiteral(this);
        }
    }

    static class Grouping extends Expression {
        final Expression _expr;

        Grouping(Expression expr) {
            _expr = expr;
        }

        @Override
        void accept(ExpressionVisitor v) {
            v.visitGrouping(this);
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
        void accept(ExpressionVisitor v) {
            v.visitUnary(this);
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
        void accept(ExpressionVisitor v) {
            v.visitBinary(this);
        }
    }

    interface ExpressionVisitor {
        void visitLiteral(Literal instance);

        void visitGrouping(Grouping instance);

        void visitUnary(Unary instance);

        void visitBinary(Binary instance);
    }
}
