package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Expression;
import models.Statement;
import models.Statement.ClassDeclare;
import models.Statement.FuncPrototype;
import src.Interpreter.ReturnException;
import src.Interpreter.RuntimeError;
import models.Token;
import utils.ASTPrint;

/**
 * main
 */
public class Tan {
    public static Error err = new Error();
    private static final Interpreter interpret = new Interpreter();

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
        List<Statement> ASTList = par.getAST(); // NOTE: For debug
        // new ASTPrint().print(AST);

        // if (err.hasError())
        // System.exit(66); // FIX: Define code 66

        Resolver res = new Resolver(interpret);
        res.run(ASTList);

        // if (err.hasError())
        // System.exit(67); // FIX: Define code 67

        interpret.run(ASTList);

        // if (err.hasRuntimeError())
        // System.exit(68); // FIX: Define code 68

        System.out.println("debug");
    }

    /* ---------------- Helper function -------------------- */

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
        byte[] content = Files.readAllBytes(Paths.get("tests/function/challenge_3.txt")); // TODO: Debug

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

    public interface TanCallable {
        int arity(); // = number of arguments pre-defined

        Object call(Interpreter interpreter, List<Object> args);
    }

    public class TanFunction implements TanCallable {
        private final FuncPrototype declaration;
        private final Environment closure;
        private final boolean isInitializer;

        TanFunction(FuncPrototype declaration, Environment closure, boolean isInitializer) {
            this.declaration = declaration;
            this.closure = closure;
            this.isInitializer = isInitializer;
        }

        /**
         * Make function/method able to access {@code this} variable like actual Java,
         * C++, Python do
         *
         * @param instance - The {@code this}
         * @return Same function with new closure which have {@code this} variable
         */
        public TanFunction bind(TanInstance instance) {
            Environment newClosure = new Environment(closure);
            newClosure.defineVar("this", instance);
            return new TanFunction(declaration, newClosure, isInitializer);
            // `get()` is constructed using TanFunction
            // so we can access it using (this.)declaration
            // instead of pass as argument
        }

        @Override
        public int arity() {
            return declaration._params.size();
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> args) {
            Environment local = new Environment(closure);

            for (int idx = 0; idx < this.arity(); idx++) {
                local.defineVar(declaration._params.get(idx).getLexeme(), args.get(idx));
            }

            try {
                interpreter.runBlock(declaration._blockStmt, local);
            } catch (ReturnException r) {
                return (isInitializer) ? closure.getAt(0, "this") : r.value;
            }

            return (isInitializer) ? closure.getAt(0, "this") : null; // For `return;` in `void` function
        }

        @Override
        public String toString() {
            return "<fn " + declaration._identifier.getLexeme() + ">";
        }
    }

    public class TanClass implements TanCallable {
        private final String identifier;
        private final TanClass superClass;
        private final Map<String, TanFunction> methods;

        TanClass(String identifier, TanClass superClass, Map<String, TanFunction> methods) {
            this.identifier = identifier;
            this.superClass = superClass;
            this.methods = methods;
        }

        @Override
        public String toString() {
            return "<class " + identifier + ">";
        }

        @Override
        public int arity() {
            TanFunction initializer = findMethod("init");
            return (initializer != null) ? initializer.arity() : 0;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> args) {
            TanInstance instance = new TanInstance(this); // FOR DEBUG: what is this?
            TanFunction initializer = findMethod("init");
            if (initializer != null)
                initializer.bind(instance).call(interpreter, args);
            // initializer.bind(...) == initializer with `this` variable pre-defined
            // initializer with `this`.call(...) == run initializer code and allow
            // access `this`

            return instance;
        }

        public TanFunction findMethod(String fieldName) {
            if (methods.containsKey(fieldName))
                return methods.get(fieldName);

            if (superClass != null)
                return superClass.findMethod(fieldName);

            return null;
        }
    }

    public class TanInstance {
        /**
         * Same as {@code klass}
         */
        private final TanClass _class;
        private final Map<String, Object> fields = new HashMap<>();

        TanInstance(TanClass _class) {
            this._class = _class;
        }

        public Object get(Token fieldName) {
            String field = fieldName.getLexeme();

            if (fields.containsKey(field)) {
                return fields.get(field);
            }
            // else if
            TanFunction method = _class.findMethod(field);
            if (method != null)
                return method.bind(this);

            // else
            // NOTE: Must throw error instead of return NULL
            // page 197
            throw new Interpreter().new RuntimeError(fieldName, "Undefined property: " + field);
        }

        public void set(Token fieldName, Object value) {
            fields.put(fieldName.getLexeme(), value);
        }

        @Override
        public String toString() {
            return "<instance of class " + _class.identifier + ">";
        }
    }
}