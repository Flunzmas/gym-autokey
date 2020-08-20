// This file is part of KeY - Integrated Deductive Software Design
//
// Copyright (C) 2001-2011 Universitaet Karlsruhe (TH), Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
// Copyright (C) 2011-2014 Karlsruhe Institute of Technology, Germany
//                         Technical University Darmstadt, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General
// Public License. See LICENSE.TXT for details.
//

package de.uka.ilkd.key.parser;

import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.key_project.util.collection.ImmutableArray;

import de.uka.ilkd.key.java.Recoder2KeY;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.Equality;
import de.uka.ilkd.key.logic.op.Function;
import de.uka.ilkd.key.logic.op.IfThenElse;
import de.uka.ilkd.key.logic.op.Junctor;
import de.uka.ilkd.key.logic.op.LogicVariable;
import de.uka.ilkd.key.logic.op.QuantifiableVariable;
import de.uka.ilkd.key.logic.op.UpdateApplication;
import de.uka.ilkd.key.logic.op.WarySubstOp;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.pp.AbbrevMap;
import de.uka.ilkd.key.rule.TacletForTests;
import static org.junit.Assert.*;

public class TestTermParser extends AbstractTestTermParser {

    private static Sort elem,list;

    private static Function head,tail,nil,cons,isempty; 

    private static LogicVariable x,y,z,xs,ys;

    private static Term t_x,t_y,t_z,t_xs,t_ys;
    private static Term t_headxs,t_tailys,t_nil;
    
    private final Recoder2KeY r2k;

    public TestTermParser() {
        r2k = new Recoder2KeY(services, nss);
        r2k.parseSpecialClasses();
    }
    
    @Override
    protected Services getServices() {
        return TacletForTests.services();
    }

    @Before
    public void setUp() throws RecognitionException {	
        
        /*
         * Setting up only static variables here. It needs to be called only once.
         */
        if (elem != null) {
            return;
        }
        
	parseDecls("\\sorts { boolean; elem; list; int; int_sort; numbers;  }\n" +
		   "\\functions {\n" +
		   "  elem head(list);\n" +
		   "  list tail(list);\n" +
		   "  list nil;\n" +
		   "  list cons(elem,list);\n"  +
//		   "numbers #;\n"+
//		   "numbers 0 (numbers);\n"+
//		   "numbers 1 (numbers);\n"+
//		   "numbers 2 (numbers);\n"+
//		   "numbers 3 (numbers);\n"+
//		   "numbers 4 (numbers);\n"+
//		   "numbers 5 (numbers);\n"+
//		   "numbers 6 (numbers);\n"+
//		   "numbers 7 (numbers);\n"+
//		   "numbers 8 (numbers);\n"+
//		   "numbers 9 (numbers);\n"+
//		   "numbers neglit (numbers);\n"+
//                   "int Z (numbers);\n"+
//		   "int neg (int);\n"+
//		   "int add (int,int);\n"+
//		   "int sub (int,int);\n"+
//		   "int mul (int,int);\n"+
//		   "int div (int,int);\n"+
//		   "int mod (int,int);\n"+
		   "int aa ;\n"+
		   "int bb ;\n"+
		   "int cc ;\n"+
		   "int dd ;\n"+
		   "int ee ;\n"+
		   "}\n" +
		   "\\predicates {\n" +
//		   "  lt(int,int);\n" +
//                   "  leq(int,int);\n" +
		   "  isempty(list);\n" +
//		   "  p(elem,list);\n" +
		   "}\n"+
		   "\\programVariables {int globalIntPV;}"

		   );
       
        elem = lookup_sort("elem");
        assert elem != null;
	list = lookup_sort("list");

	head = lookup_func("head"); tail = lookup_func("tail");
	nil = lookup_func("nil"); cons = lookup_func("cons");
	isempty = lookup_func("isempty"); 
	
	// The declaration parser cannot parse LogicVariables; these
	// are normally declared in quantifiers, so we introduce them
	// ourselves!
	x = declareVar("x",elem);   t_x = tf.createTerm(x);  
	y = declareVar("y",elem);   t_y = tf.createTerm(y);  
	z = declareVar("z",elem);   t_z = tf.createTerm(z);  
	xs = declareVar("xs",list); t_xs = tf.createTerm(xs);
	ys = declareVar("ys",list); t_ys = tf.createTerm(ys);
	
	t_headxs = tf.createTerm(head,new Term[]{t_xs}, null, null);
	t_tailys = tf.createTerm(tail,new Term[]{t_ys}, null, null);
	t_nil = tf.createTerm(nil);
    }
    
    @Override
    protected KeYParserF getParser(String s) {
        return new KeYParserF(ParserMode.TERM,getLexer(s),
                r2k,
                services,
                nss,
                new AbbrevMap());
    }

    @Test
    public void test1() throws Exception {
	assertEquals("parse x",t_x,parseTerm("x"));
    }

    @Test
    public void test1a() {
	boolean parsed = false;
	try { parseTerm("x()"); parsed = true;} catch (Exception e) {}
	assertFalse("parse x() should fail", parsed);
    }

    @Test
    public void test2() throws Exception {
	assertEquals("parse head(xs)",t_headxs,parseTerm("head(xs)"));
    }

    @Test
    public void test3() throws Exception {
	Term t = tf.createTerm(cons,t_headxs,t_tailys);
	
	assertEquals("parse cons(head(xs),tail(ys))",
		     t,parseTerm("cons(head(xs),tail(ys))"));
    }

    @Test
    public void test5() {
	Term t = tf.createTerm(Equality.EQUALS,
	    tf.createTerm
	     (head,
	      tf.createTerm(cons, t_x, t_xs)),
	     tf.createTerm
	     (head,
	      tf.createTerm (cons,t_x,t_nil)));
	     
	assertEquals("parse head(cons(x,xs))=head(cons(x,nil))",
		     t,parseFormula("head(cons(x,xs))=head(cons(x,nil))"));
	assertEquals("parse head(cons(x,xs))=head(cons(x,nil))",
		     t,parseFormula("head(cons(x,xs))=head(cons(x,nil()))"));
    }

    @Test
    public void testNotEqual() {
	Term t = tf.createTerm(Junctor.NOT,
	    tf.createTerm(Equality.EQUALS,
	    tf.createTerm
	     (head,
	      tf.createTerm(cons, t_x, t_xs)),
	     tf.createTerm
	     (head,
	      tf.createTerm (cons,t_x,t_nil))));
	     
	assertEquals("parse head(cons(x,xs))!=head(cons(x,nil))",
		     t,parseFormula("head(cons(x,xs))!=head(cons(x,nil))"));
    }

	@Test
    public void test6() {
	Term t = tf.createTerm
	    (Equality.EQV,
	     new Term[]{tf.createTerm(Junctor.IMP,
		     			     tf.createTerm(Junctor.OR,
		     				     		  tf.createTerm(Equality.EQUALS, t_x, t_x),
		     				     		  tf.createTerm(Equality.EQUALS, t_y,t_y)),
		     		             tf.createTerm(Junctor.AND,
		     		        	     	          tf.createTerm(Equality.EQUALS, t_z,t_z),
		     		        	     	          tf.createTerm(Equality.EQUALS, t_xs,t_xs))),
                         tf.createTerm(Junctor.NOT,
                         tf.createTerm(Equality.EQUALS, t_ys,t_ys))}, null, null);

	     
	assertEquals("parse x=x | y=y -> z=z & xs =xs <-> ! ys = ys",
		     t,parseFormula("x=x|y=y->z=z&xs=xs<->!ys=ys"));
    }

	@Test
    public void test7() {
	/** Bound variables are newly created by the parser,
	 * so we have to parse first, then extract the used variables,
	 * then build the formulae. */
	
	String s = "\\forall list x; \\forall list l1; ! x = l1";
	Term t = parseFormula(s);
	
	LogicVariable thisx = (LogicVariable) t.varsBoundHere(0)
	    .get(0);
	LogicVariable l1 = (LogicVariable) t.sub(0).varsBoundHere(0)
	    .get(0);

	Term t1 = tb.all(thisx,
	     tb.all(l1,
	      tf.createTerm
	      (Junctor.NOT,
	       tf.createTerm(Equality.EQUALS,
		             tf.createTerm(thisx),
		             tf.createTerm(l1)))));
	
	assertTrue("new variable in quantifier", thisx != x);
	assertEquals("parse \\forall list x; \\forall list l1; ! x = l1", t1,t);
	
    }

    @Test
    public void test8() throws Exception {
	/** A bit like test7, but for a substitution term */

	{
	    String s = "{\\subst elem xs; head(xs)} cons(xs,ys)";
	    Term t = parseTerm(s);

	    LogicVariable thisxs = (LogicVariable) t.varsBoundHere(1)
		.get(0);
	
	    Term t1 = tf.createTerm
		(WarySubstOp.SUBST,
		 new Term[]{t_headxs, tf.createTerm
        		 (cons, 
        		  new Term[]{tf.createTerm(thisxs), t_ys},
        		  	     null,
        		  	     null)},
		new ImmutableArray<QuantifiableVariable>(thisxs),
		null);

	    assertTrue("new variable in subst term", thisxs != xs);
	    assertEquals("parse {xs:elem head(xs)} cons(xs,ys)",t1,t);
	}
    }

	@Test
    public void test9() {
	/* Try something with a prediate */
	
	String s = "\\exists list x; !isempty(x)";
	Term t = parseFormula(s);
	
	LogicVariable thisx = (LogicVariable) t.varsBoundHere(0)
	    .get(0);

	Term t1 = tb.ex(thisx,
	     tf.createTerm
	     (Junctor.NOT,
	      tf.createTerm(isempty,new Term[]{tf.createTerm(thisx)}, null, null)));
	      
	assertTrue("new variable in quantifier", thisx != x);
	assertEquals("parse \\forall list x; \\forall list l1; ! x = l1", t1,t);
	
    }

	@Test
    public void test10() throws Exception {
	// Unquoted, this is
	// <{ int x = 2; {String x = "\"}";} }> true
	//	String s = "< { int x = 1; {String s = \"\\\"}\";} } > true";
	String s = "\\<{ int x = 1; {int s = 2;} }\\> x=x";
	Term t = parseTerm(s);
	
	// for now, just check that the parser doesn't crash
	
	//	 System.out.println(t);

	// Same with a box
	s = "\\[{ int x = 2; {String s = \"\\\"}\";} }\\] true";
	//s = "< { int x = 1; {int s = 2;} } > true";
	t = parseTerm(s);
	//System.out.println(t);
    }



    @Ignore("weigl: #1506")
	@Test
    public void test12() throws Exception {
	    String s="\\<{int i; i=0;}\\> \\<{ while (i>0) ;}\\>true";
	    Term t = parseTerm(s);
    }

	@Test
    public void test13() throws Exception{
       Term t1 = parseTerm("\\exists elem x; \\forall list ys; \\forall list xs; ( xs ="
		                    			+" cons(x,ys))");
	Term t2 = parseTerm("\\exists elem y; \\forall list xs; \\forall list ys; ( ys ="
                                        +" cons(y,xs))");
        Term t3 = parseTerm("\\exists int_sort bi; (\\<{ int p_x = 1;"
                            +" {int s = 2;} }\\>"
                            +" true ->"
                            +"\\<{ int p_x = 1;boolean p_y=2<1;"
                            +" while(p_y){ int s=3 ;} }\\>"
                            +" true)");
        Term t4 = parseTerm("\\exists int_sort ci; (\\<{ int p_y = 1;"
                            +" {int s = 2;} }\\>"
                            +" true ->"
                            +"\\<{ int p_y = 1;boolean p_x = 2<1;"
                            +"while(p_x){ int s=3 ;} }\\>"
                            +" true)");
        assertTrue("Terms should be equalModRenaming", t3.equalsModRenaming(t4));
     }

	@Test
    public void test14() throws Exception {
	String s="\\<{int[] i;i=new int[5];}\\>true";
	Term t=parseTerm(s);
	s="\\<{int[] i;}\\>\\<{}\\>true";
	t=parseTerm(s);
    }

	@Test@Ignore
	public void xtestBindingUpdateTermOldBindingAlternative() throws Exception {
	String s="\\<{int i,j;}\\> {i:=j} i = j";
	Term t = parseTerm(s);
	assertTrue("expected {i:=j}(i=j) but is ({i:=j}i)=j)", 
		t.sub(0).op() instanceof UpdateApplication);
    }

	@Test
    public void testBindingUpdateTerm() throws Exception {
	String s="\\forall int j; {globalIntPV:=j} globalIntPV = j";
	Term t = parseTerm(s);
	assertFalse("expected ({globalIntPV:=j}globalIntPV)=j) but is {globalIntPV:=j}(globalIntPV=j)", 
		t.sub(0).op() instanceof UpdateApplication);
    }

	@Test
    public void testParsingArray() throws Exception {
	String s="\\forall int[][] i; \\forall int j; i[j][j] = j";
	Term t = parseTerm(s);
    }


	@Test@Ignore
    public void xtestParsingArrayWithSpaces() throws Exception {
	String s="\\<{int[][] i; int j;}\\> i[ j ][ j ] = j";
	Term t = parseTerm(s);
    }

    @Test
    public void testParsingArrayCombination() throws Exception {
	String s="\\forall int[][] i; \\forall int j; i [i[i[j][j]][i[j][j]]][i[j][i[j][j]]] = j";
	Term t = parseTerm(s);
    }


	@Test
	public void testAmbigiousFuncVarPred() {
	// tests bug id 216
	String s = "\\functions {} \\predicates{mypred(int, int);}"+
	    "\n\\problem {\\forall int x; mypred(x, 0)}\n \\proof {\n"+
	    "(branch \"dummy ID\""+
	    "(opengoal \"  ==> true  -> true \") ) }";
	try {
	    parseProblem(s);
	} catch (RuntimeException re) {
	    System.out.println(re);
	    assertTrue("Fixed bug 216 occured again. The original bug "+
		       "was due to ambigious rules using semantic "+
		       "predicates in a 'wrong' way", false);
	}
    }

	@Test
	public void testParseQueriesAndAttributes() throws Exception {
	TacletForTests.getJavaInfo().readJavaBlock("{}");
	r2k.readCompilationUnit("public class T extends "
				+"java.lang.Object{ "
				+"private T a;"
				+"private static T b;"
				+"T c;"
				+"static T d;"
				+"public T e;"
				+"public static T f;"
				+"protected T g;"
				+"protected T h;"
				+"public T query(){} "
				+"public static T staticQ(T p){} "
				+"public static T staticQ() {}}");
	String s = "\\forall T t;( (t.query()=t & t.(T::query)()=t & T.staticQ()=t "
	    +"& T.staticQ(t)=t & T.b=t.(T::a) & T.d=t.(T::c) & t.(T::e)=T.f & t.(T::g)=t.(T::h)))";
	parseTerm(s);
    }

	@Test
	public void testProgramVariables() {
	TacletForTests.getJavaInfo().readJavaBlock("{}");
	r2k.readCompilationUnit("public class T0 extends "
				+"java.lang.Object{} ");
	String s = "\\<{T0 t;}\\> t(1,2) = t()";
	boolean parsed=false;
	try {
	    parseTerm(s);
	    parsed = true;
	} catch (Exception e) {}
	assertFalse("Program variables should not have arguments", parsed);
    }

	@Test
	public void testNegativeLiteralParsing() {
	String s1 = "-1234";
	Term t = null;
	try {
	    t = parseTerm(s1);
	} catch (Exception e) {fail();}
	assertTrue("Failed parsing negative literal", 
		   (""+t.op().name()).equals("Z") && 
		   (""+t.sub(0).op().name()).equals("neglit"));


	String s2 = "-(1) ";
	try {
	    t = parseTerm(s2);
	} catch (Exception e) {fail();}
	assertTrue("Failed parsing negative complex term", 
		   (""+t.op().name()).equals("neg") && 
		   (""+t.sub(0).op().name()).equals("Z"));

	String s3 = "\\forall int i; -i = 0 ";
	try {
	    t = parseTerm(s3);
	} catch (Exception e) {fail();}
	assertTrue("Failed parsing negative variable", 
		   (""+t.sub(0).sub(0).op().name()).equals("neg"));

	
    }

	@Test
	public void testIfThenElse () throws Exception {
        Term t=null, t2=null;
        
        String s1 = "\\if (3=4) \\then (1) \\else (2)";
        try {
            t = parseTerm ( s1 );
        } catch ( Exception e ) {
            fail ();
        }
        assertTrue ( "Failed parsing integer if-then-else term",
                     t.op () == IfThenElse.IF_THEN_ELSE
                     && t.sub ( 0 ).equals ( parseTerm ( "3=4" ) )
                     && t.sub ( 1 ).equals ( parseTerm ( "1" ) )
                     && t.sub ( 2 ).equals ( parseTerm ( "2" ) ) );

        String s2 = "\\if (3=4 & 1=1) \\then (\\if (3=4) \\then (1) \\else (2)) \\else (2)";
        try {
            t2 = parseTerm ( s2 );
        } catch ( Exception e ) {
            fail ();
        }
        assertTrue ( "Failed parsing nested integer if-then-else term",
                     t2.op () == IfThenElse.IF_THEN_ELSE
                     && t2.sub ( 0 ).equals ( parseTerm ( "3=4 & 1=1" ) )
                     && t2.sub ( 1 ).equals ( t )
                     && t2.sub ( 2 ).equals ( parseTerm ( "2" ) ) );

        String s3 = "\\if (3=4) \\then (1=2) \\else (2=3)";
        try {
            t = parseTerm ( s3 );
        } catch ( Exception e ) {
            fail ();
        }
        assertTrue ( "Failed parsing propositional if-then-else term",
                     t.op () == IfThenElse.IF_THEN_ELSE
                     && t.sub ( 0 ).equals ( parseTerm ( "3=4" ) )
                     && t.sub ( 1 ).equals ( parseTerm ( "1=2" ) )
                     && t.sub ( 2 ).equals ( parseTerm ( "2=3" ) ) );

    }

	@Test
	public void testInfix1() throws Exception {
	assertEquals("infix1",parseTerm("aa + bb"),
	                      parseTerm("add(aa,bb)"));
    }

	@Test
	public void testInfix2() throws Exception {
	assertEquals("infix2",parseTerm("aa + bb*cc"),
	                      parseTerm("add(aa,mul(bb,cc))"));
    }

	@Test
	public void testInfix3() throws Exception {
	assertEquals("infix3",parseTerm("aa + bb*cc < 123 + -90"),
	                      parseTerm("lt(add(aa,mul(bb,cc)),add(123,-90))"));
    }

	@Test
	public void testInfix4() throws Exception {
	assertEquals("infix4",parseTerm("aa%bb*cc < -123"),
	                      parseTerm("lt(mul(mod(aa,bb),cc),-123)"));
    }

	@Test
	public void testCast() throws Exception {
        assertEquals("cast stronger than plus", parseTerm("(int)3+2"), 
                parseTerm("((int)3)+2"));
     }
    
//    public void testParseTermsWithLabels() {
//        // First register the labels ...
//        TermLabels.registerSymbolicExecutionTermLabels(serv.getProfile().getTermLabelManager());
//
//        Term t = parseTerm("(3 + 2)<<" + SimpleTermLabel.LOOP_BODY_LABEL_NAME + ">>");
//        assertTrue(t.hasLabels());
//        t = parseTerm("3 + 2<<" + SimpleTermLabel.LOOP_BODY_LABEL_NAME + ">>");
//        assertFalse(t.hasLabels());
//        assertTrue(t.sub(1).hasLabels());
//
//        try {
//            t = parseTerm("(3 + 2)<<unknownLabel>>");
//            fail("Term " + t + " should not have been parsed");
//        } catch(Exception ex) {
//            // expected
//        }
//    }
}