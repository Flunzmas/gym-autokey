package de.uka.ilkd.key.parser;

import de.uka.ilkd.key.java.Recoder2KeY;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.*;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.pp.LogicPrinter;
import de.uka.ilkd.key.pp.NotationInfo;
import de.uka.ilkd.key.pp.ProgramPrinter;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.TacletForTests;
import de.uka.ilkd.key.util.HelperClassForTests;
import org.antlr.runtime.RecognitionException;
import org.key_project.util.collection.ImmutableSLList;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

/**
 * Class providing methods for parser tests.
 *
 * @author Kai Wallisch <kai.wallisch@ira.uka.de>
 */
public class AbstractTestTermParser {

    protected final TermFactory tf;
    protected final TermBuilder tb;
    protected final NamespaceSet nss;
    protected final Services services;

    AbstractTestTermParser() {
        services = getServices();
        tb = services.getTermBuilder();
        tf = tb.tf();
        nss = services.getNamespaces();
    }

    Sort lookup_sort(String name) {
        return nss.sorts().lookup(new Name(name));
    }

    Function lookup_func(String name) {
        return nss.functions().lookup(new Name(name));
    }

    LogicVariable declareVar(String name, Sort sort) {
        LogicVariable v = new LogicVariable(new Name(name), sort);
        nss.variables().add(v);
        return v;
    }

    private KeYParserF stringDeclParser(String s) {
        // fills namespaces 
        new Recoder2KeY(services, nss).parseSpecialClasses();
        return new KeYParserF(ParserMode.DECLARATION,
                new KeYLexerF(s,
                        "No file. Call of parser from " + this.getClass().getSimpleName()),
                services, nss);
    }

    public void parseDecls(String s) throws RecognitionException {
        KeYParserF stringDeclParser = stringDeclParser(s);
        stringDeclParser.decls();
    }

    public Term parseProblem(String s) {
        try {
            new Recoder2KeY(TacletForTests.services(),
                    nss).parseSpecialClasses();
            KeYLexerF lexer = new KeYLexerF(s,
                    "No file. Call of parser from " + this.getClass().getSimpleName());
            return new KeYParserF(ParserMode.PROBLEM,
                    lexer,
                    new ParserConfig(services, nss),
                    new ParserConfig(services, nss),
                    null,
                    ImmutableSLList.<Taclet>nil()).problem();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new RuntimeException("Exc while Parsing:\n" + sw);
        }
    }

    protected KeYLexerF getLexer(String s) {
        return new KeYLexerF(s,
                "No file. Call of parser from parser/" + getClass().getSimpleName());
    }

    protected KeYParserF getParser(String s) {
        return new KeYParserF(ParserMode.TERM, getLexer(s), services, nss);
    }

    public Term parseTerm(String s) throws Exception {
        return getParser(s).term();
    }

    public Term parseFormula(String s) {
        try {
            return getParser(s).formula();
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            throw new RuntimeException("Exc while Parsing:\n" + sw);
        }
    }

    /**
     * Convert a {@link Term} into a {@link String}.
     *
     * @param t The {@link Term} that will be converted.
     */
    protected String printTerm(Term t) throws IOException {
        LogicPrinter lp = new LogicPrinter(new ProgramPrinter(), new NotationInfo(), services);
        lp.getNotationInfo().setHidePackagePrefix(false);
        lp.printTerm(t);
        return lp.toString();
    }

    /**
     * Remove whitespaces before executing
     * {@link junit.framework.TestCase#assertEquals(java.lang.String, java.lang.String)}.
     */
    protected void assertEqualsIgnoreWhitespaces(String expected, String actual) {
        assertEquals(expected.replaceAll("\\s+", ""), actual.replaceAll("\\s+", ""));
    }

    protected void assertEqualsIgnoreWhitespaces(String message, String expected, String actual) {
        assertEquals(message, expected.replaceAll("\\s+", ""), actual.replaceAll("\\s+", ""));
    }

    protected void verifyPrettyPrinting(String expectedPrettySyntax, Term expectedParseResult) throws IOException {
        // check whether pretty-printing the parsed term yields the original pretty syntax again
        String printedSyntax = printTerm(expectedParseResult);
        String message = "\nAssertion failed while pretty-printing a term:\n"
                + expectedParseResult
                + "\nExpected pretty-syntax is: \"" + expectedPrettySyntax
                + "\"\nBut pretty-printing resulted in: \"" + printedSyntax
                + "\"\n(whitespaces are ignored during comparison of the above strings)\n";
        assertEqualsIgnoreWhitespaces(message, expectedPrettySyntax, printedSyntax);
    }

    protected void verifyParsing(Term expectedParseResult, String expectedPrettySyntax) throws Exception {
        // check whether parsing pretty-syntax produces the correct term
        Term parsedPrettySyntax = parseTerm(expectedPrettySyntax);
        String message = "\nAssertion failed while parsing pretty syntax. "
                + "Parsed string \"" + expectedPrettySyntax + "\", which results in term:\n"
                + parsedPrettySyntax + "\nBut expected parse result is:\n"
                + expectedParseResult + "\n";
        assertEquals(message, expectedParseResult, parsedPrettySyntax);
    }

    /**
     * Takes two different String representations for the same term and checks
     * whether they result in the same {@link Term} after parsing. Subsequently,
     * the {@link Term} is printed back to a {@link String} and compared with
     * the first argument. The first argument is expected to be in
     * pretty-syntax.
     *
     * @param prettySyntax {@link Term} representation in pretty-syntax.
     * @param verboseSyntax {@link Term} in verbose syntax.
     * @param optionalStringRepresentations Optionally, additional String
     * representations will be tested for correct parsing.
     * @throws IOException
     */
    protected void comparePrettySyntaxAgainstVerboseSyntax(String prettySyntax, String verboseSyntax,
            String... optionalStringRepresentations) throws Exception {
        Term expectedParseResult = parseTerm(verboseSyntax);
        compareStringRepresentationAgainstTermRepresentation(prettySyntax, expectedParseResult, optionalStringRepresentations);
    }

    /**
     * Takes a {@link String} and a {@link Term} and checks whether they can be
     * transformed into each other by the operations parsing and printing.
     *
     * @param prettySyntax Expected result after pretty-printing
     * {@code expectedParseResult}.
     * @param expectedParseResult Expected result after parsing
     * {@code expectedPrettySyntax}.
     * @param optionalStringRepresentations Optionally, additional String
     * representations will be tested for correct parsing.
     * @throws IOException
     */
    protected void compareStringRepresentationAgainstTermRepresentation(String prettySyntax, Term expectedParseResult,
            String... optionalStringRepresentations) throws Exception {

        verifyParsing(expectedParseResult, prettySyntax);
        verifyPrettyPrinting(prettySyntax, expectedParseResult);

        /*
         * Optionally, further string representations of the same term will be parsed here.
         */
        for (int i = 0; i < optionalStringRepresentations.length; i++) {
            assertEquals(expectedParseResult, parseTerm(optionalStringRepresentations[i]));
        }
    }

   protected Services getServices() {
      File keyFile = new File(HelperClassForTests.TESTCASE_DIRECTORY
            + File.separator + "termParser" + File.separator + "parserTest.key");
      return HelperClassForTests.createServices(keyFile);
   }

}
