package models;

public abstract class Expression {
    /**
     * @implNote Why param type is Visitor ? To access the interface methods and for
     *           passing nested arguments.
     */
    abstract <T> T accept(Visitor<T> v);

    static class Literal extends Expression {
        final Object _value;

        Literal(Object value) {
            _value = value;
        }

        @Override
        <T> T accept(Visitor<T> v) {
            return v.visitLiteral(this); // NOTE: this == object which call this method
                                        // try debugging ASTPrint.java 
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

    /**
     * @param T - Return type of methods.
     * @implNote
     *           Interface == Abstract class WITH ONLY method prototypes.
     *           <p />
     *           These method will be implemented by other class (i.e: ASTPrint
     *           implement Expression.Visitor<String>)
     *
     * @see https://www.w3schools.com/java/java_interface.asp
     */
    interface Visitor<T> {
        T visitLiteral(Literal instance);

        T visitGrouping(Grouping instance);

        T visitUnary(Unary instance);

        T visitBinary(Binary instance);
    }
}
