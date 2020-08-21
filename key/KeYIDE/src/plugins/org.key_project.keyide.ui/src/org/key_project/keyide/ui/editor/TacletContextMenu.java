/*******************************************************************************
 * Copyright (c) 2014 Karlsruhe Institute of Technology, Germany
 *                    Technical University Darmstadt, Germany
 *                    Chalmers University of Technology, Sweden
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Technical University Darmstadt - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.key_project.keyide.ui.editor;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;
import org.key_project.keyide.ui.util.KeYIDEUtil;
import org.key_project.util.collection.ImmutableList;
import org.key_project.util.eclipse.WorkbenchUtil;

import de.uka.ilkd.key.gui.ProofMacroMenu;
import de.uka.ilkd.key.macros.ProofMacro;
import de.uka.ilkd.key.pp.PosInSequent;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.rule.BuiltInRule;
import de.uka.ilkd.key.rule.TacletApp;

/**
 * A ContextMenu for the applicable {@link TacletApp}s of the selected Term.
 * 
 * @author Christoph Schneider, Niklas Bunzel, Stefan K�sdorf, Marco Drebing
 */
public class TacletContextMenu extends ExtensionContributionFactory {
   /**
    * Creates a ContextMenu for all applicable {@link TacletApp}s by creating and adding {@link TacletCommandContributionItem}s to a {@link IContributionRoot}.
    * @param serviceLocator The given {@link IServiceLocator}.
    * @param additions The {@link IContributionRoot} the {@link TacletApp}s will be added.
    */
   @Override
   public void createContributionItems(IServiceLocator serviceLocator, IContributionRoot additions) {
      IEditorPart activeEditor = WorkbenchUtil.getActiveEditor();
      if (activeEditor instanceof KeYEditor) {
         KeYEditor keyEditor = (KeYEditor)activeEditor;
         Goal goal = keyEditor.getSelectionModel().getSelectedGoal();
         if (goal != null && !keyEditor.getProofControl().isInAutoMode()) {
            PosInSequent pos = keyEditor.getSelectedPosInSequent();
            // Add taclet rules
            ImmutableList<TacletApp> appList = KeYIDEUtil.findTaclets(keyEditor.getUI(), goal, pos);
            if (appList != null) {
               Iterator<TacletApp> it = appList.iterator();
               while (it.hasNext()) {
                  TacletApp app = it.next();
                  CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, "", "org.key_project.keyide.ui.commands.applyrule", SWT.PUSH);
                  p.label = app.rule().displayName();
                  TacletCommandContributionItem item = new TacletCommandContributionItem(p, goal, app, keyEditor.getUI(), pos);
                  item.setVisible(true);
                  additions.addContributionItem(item, null);
               }
            }
            // Add built in rules
            ImmutableList<BuiltInRule> builtInRules = KeYIDEUtil.findBuiltInRules(keyEditor.getUI(), goal, pos);
            for (BuiltInRule rule : builtInRules) {
               CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, "", "org.key_project.keyide.ui.commands.applyrule", SWT.PUSH);
               p.label = rule.displayName();
               BuiltInRuleCommandContributionItem item = new BuiltInRuleCommandContributionItem(p, goal, rule, keyEditor.getUI(), pos);
               item.setVisible(true);
               additions.addContributionItem(item, null);
            }
            // Add macros
            MenuManager macroMenu = new MenuManager("Strategy macros");
            HashMap<String, MenuManager> subMenus = new HashMap<String, MenuManager>();
            Iterable<ProofMacro> allMacros = ProofMacroMenu.REGISTERED_MACROS;
            for (ProofMacro macro : allMacros) {
            	if (pos != null) {
                    if (macro.canApplyTo(goal.node(), pos.getPosInOccurrence())) {
                        CommandContributionItemParameter p = new CommandContributionItemParameter(serviceLocator, "", "org.key_project.keyide.ui.commands.applyrule", SWT.PUSH);
                        p.label = macro.getName();
                        MacroCommandContributionItem item = new MacroCommandContributionItem(p, goal.node(), macro, keyEditor.getUI(), pos);
                        item.setVisible(true);
                        // sort macros into submenus depending on their category
                        String cat = macro.getCategory();
                        if (cat == null) {
                      	  macroMenu.add(item);
                        } else if (subMenus.containsKey(cat)) {
                      	  subMenus.get(cat).add(item);
                        } else {
                      	  MenuManager subMenu = new MenuManager(cat);
                      	  subMenu.add(item);
                      	  subMenus.put(cat, subMenu);
                        }
                     }
            	}
            }
            // add all submenus to the main menu
            for (String category : subMenus.keySet())  {
            	macroMenu.add(subMenus.get(category));
            }
            additions.addContributionItem(macroMenu, null); 
         }
      }
   }
}