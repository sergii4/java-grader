package com.getman.grader;


import com.getman.grader.compiler.CharSequenceCompiler;
import com.getman.grader.compiler.CharSequenceCompilerException;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Grader {

    private static final String PACKAGE_NAME = "com.getman.grader";
    public static final String CLASS_NAME = "SolutionImpl";

    private final CharSequenceCompiler<Solution> compiler = new CharSequenceCompiler<Solution>(
            getClass().getClassLoader(), Arrays.asList(new String[] { "-target", "1.8", "-verbose", "-g" }));
    private String template;

    public static void main(String[] args) {
        Grader grader = new Grader();
        //example method body
        String code = "    String binary = Integer.toBinaryString(N);\n"
                + "        int max = 0;\n"
                + "        int temp = 0;\n"
                + "        for (char c : binary.toCharArray()) {\n"
                + "            if (c == '0') {\n"
                + "                temp++;\n"
                + "\n"
                + "            } else {\n"
                + "                max = Math.max(max, temp);\n"
                + "                temp = 0;\n"
                + "            }\n"
                + "        }\n"
                + "        return max;\n";
        Solution solution = grader.newImpl(code);
        grader.test(solution);

    }


    public Solution newImpl(String methodBody) {
        final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
        try {
            // generate semi-secure unique package and class names
            final String packageName = PACKAGE_NAME;
            final String className = CLASS_NAME;
            final String qName = packageName + '.' + className;
            // generate the source class as String
            final String source = fillTemplate(packageName, className, methodBody);
            // compile the generated Java source

            Class<Solution> compiledSolution = compiler.compile(qName, source, errs,
                    new Class<?>[] { Solution.class });
            System.out.println(errs);
            return compiledSolution.newInstance();
        } catch (CharSequenceCompilerException e) {
            System.out.println(e.getDiagnostics());
        } catch (InstantiationException e) {
            System.out.println(e.getMessage());
        } catch (IllegalAccessException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return NULL_SOLUTION;
    }

    /**
     * Return the Plotter function Java source, substituting the given package
     * name, class name, and double expression
     *
     * @param packageName
     *           a valid Java package name
     * @param className
     *           a valid Java class name
     * @param expression
     *           text for a double expression, using double x
     * @return source for the new class implementing Function interface using the
     *         expression
     * @throws IOException
     */
    private String fillTemplate(String packageName, String className, String expression)
            throws IOException {
        if (template == null)
            template = readTemplate();
        // simplest "template processor":
        String source = template.replace("$packageName", packageName)//
                .replace("$className", className)//
                .replace("$expression", expression);
        return source;
    }

    /**
     * Read the Solution source template
     *
     * @return a source template
     * @throws IOException
     */
    private String readTemplate() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("Solution.java.template");
        int size = is.available();
        byte bytes[] = new byte[size];
        if (size != is.read(bytes, 0, size))
            throw new IOException();
        return new String(bytes, "US-ASCII");
    }

    public void test(Solution solution) {
        // Run it baby

        //check 0 -> 0 -> 0 -> 0
        System.out.format("%d -> %s%n", 0,  getString(solution, 0, 0) );

        //check 1 -> 1 -> 0 -> 0
        System.out.format("%d -> %s%n", 1,  getString(solution, 1, 0) );

        //check 2 -> 10 -> 0 -> 0
        System.out.format("%d -> %s%n", 2,  getString(solution, 2, 0) );

        //check 1041 -> 10000010001 -> 5,3 -> 5
        System.out.format("%d -> %s%n", 1041,  getString(solution, 1041, 5) );

        //check 601 -> 1001011001 -> 2,1,2 -> 2
        System.out.format("%d -> %s%n", 601,  getString(solution, 601, 2) );

        //check 600 -> 1001011000 -> 2,1 -> 2
        System.out.format("%d -> %s%n", 600,  getString(solution, 600, 2) );

    }

    public String getString(Solution solution, int n, int expected) {
        return solution.solution(n) == expected ? "correct" : "incorrect";
    }

    private static final Solution NULL_SOLUTION = N -> 0;
}