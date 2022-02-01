package utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * @implNote This class will generate files Expression.java, not code inside
 *           models/Expression.java
 * @implNote Params: A path input, where to generate files. i.e:
 *           GenExpr("models")
 */
public class GenModel {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("This class require a param: Path to generate files");
            System.exit(64); // TODO: Define code
        }

        String outputDir = args[0]; // CAUTION: path can be wrong
        // NOTE: Why use Arrays.asList instead of List<>?
        // https://stackoverflow.com/a/16748184/12897204

        defineAST(outputDir, "Expression", Arrays.asList(
                // format: type (class_name): param_1_type param_1_name,
                // param_2_type param_2_name,...
                // NOTE: Case-sensitive
                "Literal: Object value", // NOTE: Object is the root of all types => Abstract type
                "VarAccess: Token identifer",
                "Logical: Expression lhs, Token operator, Expression rhs",
                "Grouping: Expression expr",
                "Call: Expression funcName, Token closeParen, List<Expression> arguments",
                "Unary: Token operator, Expression expr",
                "Assign: Token identifier, Expression value",
                "Binary: Expression lhs, Token operator, Expression rhs",
                "Ternary: Expression lhs, Token operator, Expression rhs_first, Expression rhs_second"));

        defineAST(outputDir, "Statement", Arrays.asList(
                "Block: List<Statement> stmtList",
                "Expr: Expression expr",
                "Print: Expression expr",
                "VarDeclare: Token identifier, Expression initializer",
                "If: Expression condition, Statement ifStmt, Statement elseStmt",
                "While: Expression condition, Statement body",
                "FuncPrototype: Token identifer, List<Token> params, List<Statement> blockStmt",
                "Return: Token keyword, Expression returnVal"));
    }

    /* --------- Helper function --------- */

    /**
     * @implNote Using {@code Switch} Design Pattern
     * @throws IOException For PrintWriter, write new file.
     */
    private static void defineAST(String outputDir, String filename, List<String> grammar) throws IOException {
        String path = outputDir + "/" + filename + ".java";

        // NOTE: Why PrintWriter?
        // Controversy topic
        // But the most benifit is access to `printXXX()` (i.e: println() instead of
        // print()) which make it ease to use to write a file
        PrintWriter writer = new PrintWriter(path, "utf-8"); // CAUTION: path can be wrong

        writer.println("package models;");
        writer.println("import java.util.List;"); // for Block statement

        writer.println("public interface " + filename + " {");
        /* --------- start main class --------- */
        {
            defineProduction(writer, filename, grammar);
        }
        /* --------- end main class --------- */
        writer.println("}");

        writer.close();
    }

    private static void defineProduction(PrintWriter writer, String filename, List<String> grammar)
            throws IOException {
        List<String> production_name_list = new ArrayList<>();

        /* --------- start production class --------- */
        for (String production : grammar) {
            String production_name = production.split(":")[0];
            production_name_list.add(production_name);
            String production_params = production.split(":")[1];
            // System.out.println(production_params.split(",")[0]); // TEST
            String[] split_params = production_params.split(",");
            // System.out.println(split_params[0] + split_params[1]); // TEST
            List<String> param_type = new ArrayList<>(); // NOTE: https://stackoverflow.com/a/9853116/12897204
            List<String> param_name = new ArrayList<>();
            for (String param : split_params) {
                param = param.substring(1); // NOTE: remove first whitespace
                param_type.add(param.split("\\s+")[0]);
                param_name.add(param.split("\\s+")[1]);
                // System.out.println(param_name); // TEST
            }

            /* --------- start subclass --------- */
            writer.println("public static class " + production_name + " implements " + filename + " {");
            {
                for (int idx = 0; idx < param_name.size(); idx++) {
                    writer.println("public final " + param_type.get(idx) + " _" + param_name.get(idx) + ";");
                }

                // Constructor
                writer.println("public " + production_name + "(" + production_params + ") {");
                for (int idx = 0; idx < param_name.size(); idx++) {
                    // Why use get? // NOTE: https://stackoverflow.com/a/18814651/12897204
                    writer.println("_" + param_name.get(idx) + " = " + param_name.get(idx) + ";");
                }
                writer.println("}");
            }
            writer.println("}");
            /* --------- end subclass --------- */
        }
        /* --------- end production class --------- */
    }
}
