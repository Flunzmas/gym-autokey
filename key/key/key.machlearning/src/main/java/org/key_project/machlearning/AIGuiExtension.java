package org.key_project.machlearning;

import de.uka.ilkd.key.core.KeYMediator;
import de.uka.ilkd.key.gui.MainWindow;
import de.uka.ilkd.key.gui.WindowUserInterfaceControl;
import de.uka.ilkd.key.gui.extension.api.KeYGuiExtension;
import de.uka.ilkd.key.gui.extension.api.KeYGuiExtension.Info;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.proof.ProofEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map.Entry;

@Info(experimental = false)
public class AIGuiExtension implements KeYGuiExtension, KeYGuiExtension.Toolbar {

    @Override
    public @NotNull JToolBar getToolbar(MainWindow mainWindow) {

        JToolBar result = new JToolBar();

        for (Entry<String, Tactic> entry : KeYConnection.TACTICS.entrySet()) {
            JButton button = new JButton(entry.getKey());
            button.addActionListener(e -> {
                KeYMediator mediator = mainWindow.getMediator();
                Proof proof = mediator.getSelectedProof();
                Goal goal = mediator.getSelectedGoal();
                WindowUserInterfaceControl ui = mainWindow.getUserInterface();
                try {
                    ui.getProofControl().fireAutoModeStarted(new ProofEvent(proof));
                    entry.getValue().apply(ui, proof, goal, null);
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    ui.getProofControl().fireAutoModeStopped(new ProofEvent(proof));
                }
            });
            result.add(button);
        }

        return result;
    }
}
