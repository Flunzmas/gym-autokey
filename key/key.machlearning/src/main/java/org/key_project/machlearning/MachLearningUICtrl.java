package org.key_project.machlearning;

import de.uka.ilkd.key.macros.ProofMacro;
import de.uka.ilkd.key.macros.ProofMacroFinishedInfo;
import de.uka.ilkd.key.macros.SkipMacro;
import de.uka.ilkd.key.macros.scripts.ProofScriptEngine;
import de.uka.ilkd.key.parser.Location;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.io.ProblemLoader;
import de.uka.ilkd.key.prover.ProverCore;
import de.uka.ilkd.key.prover.TaskFinishedInfo;
import de.uka.ilkd.key.prover.TaskStartedInfo;
import de.uka.ilkd.key.prover.TaskStartedInfo.TaskKind;
import de.uka.ilkd.key.prover.impl.DefaultTaskStartedInfo;
import de.uka.ilkd.key.ui.ConsoleUserInterfaceControl;
import de.uka.ilkd.key.ui.Verbosity;
import de.uka.ilkd.key.util.Pair;

public class MachLearningUICtrl extends ConsoleUserInterfaceControl {

    public MachLearningUICtrl(byte verbosity) {
        super(verbosity, true);
    }

    @Override
    public void taskFinished(TaskFinishedInfo info) {
        if(info.getResult() != null) {
            throw new RuntimeException((Throwable)info.getResult());
        }

        if (info.getSource() instanceof ProblemLoader) {
            System.err.println("... Finished loading");
            fireTaskFinished(info);
            return;
        }

        super.taskFinished(info);
    }
}
