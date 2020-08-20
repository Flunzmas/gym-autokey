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

import java.util.HashMap;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.key_project.util.collection.ImmutableList;

import de.uka.ilkd.key.java.JavaReader;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.NamespaceSet;
import de.uka.ilkd.key.pp.AbbrevMap;
import de.uka.ilkd.key.rule.Taclet;
import de.uka.ilkd.key.rule.tacletbuilder.TacletBuilder;

/**
 * Extends generated class {@link KeYParser} with custom constructors.
 */
public class KeYParserF extends KeYParser {

    public KeYParserF(ParserMode mode, KeYLexerF keYLexerF,
            ParserConfig schemaConfig, ParserConfig normalConfig,
            HashMap<Taclet, TacletBuilder<? extends Taclet>> taclet2Builder, ImmutableList<Taclet> immutableList) {
        super(mode, new CommonTokenStream(keYLexerF), schemaConfig,
                normalConfig, taclet2Builder, immutableList);
    }

    public KeYParserF(ParserMode mode, TokenStream lexer,
            ParserConfig schemaConfig, ParserConfig normalConfig,
            HashMap<Taclet, TacletBuilder<? extends Taclet>> taclet2Builder, ImmutableList<Taclet> taclets) {
        super(mode, lexer, schemaConfig, normalConfig, taclet2Builder, taclets);
    }

    public KeYParserF(ParserMode mode, KeYLexerF keYLexerF) {
        super(mode, new CommonTokenStream(keYLexerF));
    }

    public KeYParserF(ParserMode mode, KeYLexerF keYLexerF,
            JavaReader jr, Services services, NamespaceSet nss, AbbrevMap scm) {
        super(mode, new CommonTokenStream(keYLexerF), jr, services, nss, scm);
    }

    public KeYParserF(ParserMode mode, KeYLexerF keYLexerF, Services services, NamespaceSet nss) {
        super(mode, new CommonTokenStream(keYLexerF), services, nss);
    }

    /**
     * Gets a better error message for a recognition exception from the parser.
     *
     * {@link #getErrorMessage(RecognitionException, String[])} is used for
     * that.
     *
     * @param e
     *            the raised exception, not <code>null</code>
     * @return an error message for that exception
     */
    public String getErrorMessage(RecognitionException e) {
        return getErrorMessage(e, KeYLexerTokens.getTokennames());
    }

    public boolean isAtProofScript() {
        return input.LA(1) == PROOFSCRIPT;
    }

}
