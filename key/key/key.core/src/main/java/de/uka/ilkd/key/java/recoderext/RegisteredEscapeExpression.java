package de.uka.ilkd.key.java.recoderext;

import java.util.List;

import recoder.java.Expression;
import de.uka.ilkd.key.speclang.jml.translation.JMLTranslator;
import de.uka.ilkd.key.util.MiscTools;

/**
 * This class handles all escape expressions in set-statements, that are registered
 * in JMLTranslator.jml2jdl
 * 
 * 
 * @author Kai Wallisch <kai.wallisch@ira.uka.de>
 */
public class RegisteredEscapeExpression extends EscapeExpression {

    /**
     * generated UID
     */
    private static final long serialVersionUID = 5400879603292633806L;
    
    private final String mapEscape;

    RegisteredEscapeExpression(String mapEscape, List<Expression> arguments) {
        super(JMLTranslator.jml2jdl.get(mapEscape), arguments);
        this.mapEscape = mapEscape;
    }

    @Override
    public Expression deepClone() {
        return new RegisteredEscapeExpression(mapEscape, children);
    }

    @Override
    public String toSource() {
        return mapEscape + "(" + MiscTools.join(children, ",") + ")";
    }

}
