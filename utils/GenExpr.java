package utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * @implNote This class will generate files Expression.java, not code inside
 *           models/Expression.java
 * @implNote Params: A path input, where to generate files. i.e:
 *           GenExpr("models")
 */
public class GenExpr {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("This class require a param: Path to generate files");
            System.exit(64); // TODO: Define code
        }

        String outputDir = args[0]; // CAUTION: path can be wrong
        // NOTE: Why use Arrays.asList instead of List<>?
        // https://stackoverflow.com/a/16748184/12897204
        defineAST(outputDir, "Expression", Arrays.asList(
            
        ));

    }

    /* --------- Helper function --------- */

    /**
     *
     * @throws IOException For PrintWriter, write new file.
     */
    private static void defineAST(String outputDir, String filename, List<String> content) throws IOException {
        String path = outputDir + "/" + filename + ".java";

        // NOTE: Why PrintWriter?
        // Controversy topic
        // But the most benifit is access to `printXXX()` (i.e: println() instead of
        // print()) which make it ease to use to write a file
        PrintWriter writer = new PrintWriter(path, "utf-8"); // CAUTION: path can be wrong

        writer.println("package models;\n");
        writer.println("abstract class " + filename + " {\n");
        writer.println("}");

        writer.close();
    }
}
