package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import models.Expression;
import models.Token;
import utils.ASTPrint;

/**
 * main
 */
public class Tan {
    static Error err = new Error();

    /**
     * @param args
     * @implNote If the main method won't be static, JVM would not be able to call
     *           it because there is no object of the class is present.
     * @implNote `throws IOException` because methods used inside also throw it.
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1)
            modeScript();
        else if (args.length == 1)
            modeFile(args[0]);
        else
            // modeConsole();
            // FIX: For debug
            modeFile("debug-mode");
    }

    private static void run(String content) {
        // System.out.println('\n' + content + '\n'); // test

        Scanner sc = new Scanner(content);
        List<Token> tokenList = sc.getListToken();

        // FIX: Comment out this line when finish
        // if (err.hasError())
        // System.exit(65); // FIX: Define code 65

        Parser par = new Parser(tokenList);
        Expression AST = par.getAST(); // NOTE: For debug
        new ASTPrint().print(AST);
    }

    /* --------- Helper function --------- */

    /**
     * @implNote Has to make it `static` due to being called in `static main()`
     */
    private static void modeScript() {
        System.out.println("----- tan script haven't implemented yet :( -----");
    }

    /**
     * @param file
     * @implNote Has to make it `static` due to being called in `static main()`
     * @see https://rollbar.com/blog/how-to-use-the-throws-keyword-in-java-and-when-to-use-throw
     */
    private static void modeFile(String file) throws IOException {
        System.out.println("\ntan " + file + "\n");
        byte[] content = Files.readAllBytes(Paths.get("tests/parser/mix_2.txt")); // TODO: Debug

        // System.out.println(new String(content)); // test
        run(new String(content, Charset.defaultCharset()));
    }

    /**
     * @implNote Has to make it `static` due to being called in `static main()`
     * @see https://stackoverflow.com/a/19532416/12897204
     * @see https://www.geeksforgeeks.org/difference-between-scanner-and-bufferreader-class-in-java/#:~:text=The%20Scanner%20has%20a%20little,simply%20reads%20sequence%20of%20characters.
     */
    private static void modeConsole() throws IOException {
        // Not use Scanner due to performance
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("tan> ");
            String cmdLine = buffer.readLine(); // auto detect new line

            // i.e: If user press ctrl+C, buffer.readLine() will return null
            // @see page39
            if (buffer == null)
                break; // FIX: Do muon in ra dong "tan>" truoc nen moi lam vay

            run(cmdLine);
            err.setError(false);
        }
    }

    public static void errorReport(int line, String message) {
        err.report(line, message);
    }
}