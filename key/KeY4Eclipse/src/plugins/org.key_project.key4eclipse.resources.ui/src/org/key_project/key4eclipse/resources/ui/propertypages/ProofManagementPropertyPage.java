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

package org.key_project.key4eclipse.resources.ui.propertypages;

import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.key_project.key4eclipse.common.ui.property.AbstractProjectPropertyPage;
import org.key_project.key4eclipse.resources.builder.KeYProjectBuildJob;
import org.key_project.key4eclipse.resources.builder.KeYProjectBuildMutexRule;
import org.key_project.key4eclipse.resources.property.KeYProjectBuildProperties;
import org.key_project.key4eclipse.resources.property.KeYProjectProperties;
import org.key_project.key4eclipse.resources.ui.util.LogUtil;
import org.key_project.key4eclipse.resources.util.KeYResourcesUtil;

import de.uka.ilkd.key.smt.SolverType;

public class ProofManagementPropertyPage extends AbstractProjectPropertyPage {

    private Button enableKeYResourcesBuildsButton;

    private Button enableBuildOnStartupButton;

    private Button enableBuildRequiredProofsOnlyButton;

    private Spinner numberOfThreadsSpinner;

    private Text numberOfThreadsText;

    private Button enableMultiThreadingButton;

    private Button autoDeleteProofFilesButton;

    private Button generateTestCasesButton;

    private Button generateCounterExamplesButton;

    private Button autoDeleteTestCasesButton;

    private Text fillText;

    private static final String generateTestCasesButtonText = "Generate test cases";
    private static final String generateCounterExamplesButtonText = "Generate counter exampels";


    private SelectionListener buttonSelectionListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            update();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }
    };


    private SelectionListener enableMultiThreadingButtonSelectionListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            update();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }
    };


    private SelectionListener generateTestCasesButtonSelectionListener = new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if(generateTestCasesButton.getSelection() && !KeYProjectProperties.TEST_CASE_GENERATION_SUPPORTS_MULTITHREADING){
                enableMultiThreadingButton.setSelection(false);
            }
            update();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }
    };

    private List excludedJavaFilesList;


    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createContents(Composite parent) {
        initializeDialogUnits(parent);
        Composite root = new Composite(parent, SWT.NONE);
        root.setLayout(new GridLayout(1, false));    

        enableKeYResourcesBuildsButton = new Button(root, SWT.CHECK);
        enableKeYResourcesBuildsButton.setText("Enable KeY Resources builds");
        enableKeYResourcesBuildsButton.addSelectionListener(buttonSelectionListener);
        enableKeYResourcesBuildsButton.setSelection(KeYProjectProperties.isEnableKeYResourcesBuilds(getProject()));

        Group builderSettings = new Group(root, SWT.NONE);
        builderSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        builderSettings.setLayout(new GridLayout(1, false));
        builderSettings.setText("Builder settings");
        Composite builderSettingsComposite = new Composite(builderSettings, SWT.NONE);
        builderSettingsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        builderSettingsComposite.setLayout(new GridLayout(1, false));

        enableBuildOnStartupButton = new Button(builderSettingsComposite, SWT.CHECK);
        enableBuildOnStartupButton.setText("Continue build on startup");
        enableBuildOnStartupButton.setSelection(KeYProjectProperties.isEnableBuildOnStartup(getProject()));

        enableBuildRequiredProofsOnlyButton = new Button(builderSettingsComposite, SWT.CHECK);
        enableBuildRequiredProofsOnlyButton.setText("Build required proofs only");
        enableBuildRequiredProofsOnlyButton.setSelection(KeYProjectProperties.isEnableBuildRequiredProofsOnly(getProject()));

        autoDeleteProofFilesButton = new Button(builderSettingsComposite, SWT.CHECK);
        autoDeleteProofFilesButton.setText("Delete unnecessary proof files automatically");
        autoDeleteProofFilesButton.setSelection(KeYProjectProperties.isAutoDeleteProofFiles(getProject()));

        Group multiThreadingSettings = new Group(builderSettingsComposite, SWT.NONE);
        multiThreadingSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        multiThreadingSettings.setLayout(new GridLayout(1, false));
        multiThreadingSettings.setText("Multi Threading");
        Composite multiThreadingComposite = new Composite(multiThreadingSettings, SWT.NONE);
        multiThreadingComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        multiThreadingComposite.setLayout(new GridLayout(2, false));

        enableMultiThreadingButton = new Button(multiThreadingComposite, SWT.CHECK);
        enableMultiThreadingButton.setText("Enable multithreading");
        enableMultiThreadingButton.addSelectionListener(enableMultiThreadingButtonSelectionListener);
        enableMultiThreadingButton.setSelection(KeYProjectProperties.isEnableMultiThreading(getProject()));

        fillText = new Text(multiThreadingComposite, SWT.SINGLE);
        fillText.setText("");
        Color backgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        fillText.setBackground(backgroundColor);
        fillText.setEnabled(false);

        numberOfThreadsText = new Text(multiThreadingComposite, SWT.SINGLE);
        numberOfThreadsText.setText("Number of threads:");
        numberOfThreadsText.setBackground(backgroundColor);

        numberOfThreadsSpinner = new Spinner(multiThreadingComposite, SWT.NONE);
        numberOfThreadsSpinner.setMinimum(1);
        numberOfThreadsSpinner.setMaximum(100);
        numberOfThreadsSpinner.setIncrement(1);
        setSelectionForSetNumberOfThreads();

        Group testCaseGenSettings = new Group(builderSettingsComposite, SWT.NONE);
        testCaseGenSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        testCaseGenSettings.setLayout(new GridLayout(1, false));
        testCaseGenSettings.setText("Test Case Generation");
        Composite testCaseGenComposite = new Composite(testCaseGenSettings, SWT.NONE);
        testCaseGenComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        testCaseGenComposite.setLayout(new GridLayout(1, false));

        generateTestCasesButton = new Button(testCaseGenComposite, SWT.CHECK);
        generateTestCasesButton.setText(generateTestCasesButtonText);
        generateTestCasesButton.addSelectionListener(generateTestCasesButtonSelectionListener);
        generateTestCasesButton.setSelection(KeYProjectProperties.isGenerateTestCases(getProject()));

        autoDeleteTestCasesButton = new Button(testCaseGenComposite, SWT.CHECK);
        autoDeleteTestCasesButton.setText("Delete unnecessary test cases automatically");
        autoDeleteTestCasesButton.setSelection(KeYProjectProperties.isAutoDeleteTestCases(getProject()));

        Group counterExampleSettings = new Group(builderSettingsComposite, SWT.NONE);
        counterExampleSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        counterExampleSettings.setLayout(new GridLayout(1, false));
        counterExampleSettings.setText("Counter Example Generation");
        Composite counterExampleComposite = new Composite(counterExampleSettings, SWT.NONE);
        counterExampleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        counterExampleComposite.setLayout(new GridLayout(1, false));

        generateCounterExamplesButton = new Button(counterExampleComposite, SWT.CHECK);
        generateCounterExamplesButton.setText(generateCounterExamplesButtonText);
        generateCounterExamplesButton.setSelection(KeYProjectProperties.isGenerateCounterExamples(getProject()));


        Group excludedJavaTypesSettings = new Group(builderSettingsComposite, SWT.NONE);
        excludedJavaTypesSettings.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        excludedJavaTypesSettings.setLayout(new GridLayout(1, false));
        excludedJavaTypesSettings.setText("Excluded Java Types");
        excludedJavaTypesSettings.setToolTipText("For Java types selected in the list below, no proof onligations will be generated");
        Composite excludedJavaTypesComposite = new Composite(excludedJavaTypesSettings, SWT.RESIZE);
        excludedJavaTypesComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        excludedJavaTypesComposite.setLayout(new GridLayout(1, false));

        excludedJavaFilesList = createJavaTypeSelectionList(excludedJavaTypesComposite);

        update();
        return root;
    }

    private List createJavaTypeSelectionList(Composite excludedJavaTypesComposite) {
        List excludedJavaFilesList = new List(excludedJavaTypesComposite, SWT.FULL_SELECTION | SWT.MULTI);

        TreeSet<String> allTypes = KeYResourcesUtil.getAllTypesIn(getProject());
        excludedJavaFilesList.setItems(allTypes.toArray(new String[allTypes.size()]));
        excludedJavaFilesList.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                try {
                    KeYProjectProperties.setExcludedJavaTypes(getProject(), excludedJavaFilesList.getSelection());
                } catch (CoreException e) {
                    e.printStackTrace();
                }
                update();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {}
        });
        excludedJavaFilesList.setSelection(KeYProjectProperties.getExcludedJavaTypes(getProject()));
        return excludedJavaFilesList;
    }

    private void update(){
        boolean buildsEnabled = enableKeYResourcesBuildsButton.getSelection();
        enableBuildOnStartupButton.setEnabled(buildsEnabled);
        enableBuildRequiredProofsOnlyButton.setEnabled(buildsEnabled);
        autoDeleteProofFilesButton.setEnabled(buildsEnabled);

        enableMultiThreadingButton.setEnabled(buildsEnabled);
        boolean multiThreadingEnabled = enableMultiThreadingButton.getSelection() && enableMultiThreadingButton.getEnabled();
        numberOfThreadsSpinner.setEnabled(multiThreadingEnabled);
        numberOfThreadsText.setEnabled(multiThreadingEnabled);

        if(!SolverType.Z3_CE_SOLVER.isInstalled(true)) {
            generateTestCasesButton.setText(generateTestCasesButtonText + " - SMT Solver Z3 not installed!");
            generateTestCasesButton.setEnabled(false);
            autoDeleteTestCasesButton.setEnabled(false);

            generateCounterExamplesButton.setText(generateCounterExamplesButtonText + " - SMT Solver Z3 not installed!");
            generateCounterExamplesButton.setEnabled(false);
        }
        else {
            generateTestCasesButton.setText(generateTestCasesButtonText);
            generateTestCasesButton.setEnabled(buildsEnabled && (!multiThreadingEnabled || KeYProjectProperties.TEST_CASE_GENERATION_SUPPORTS_MULTITHREADING));
            autoDeleteTestCasesButton.setEnabled(generateTestCasesButton.getSelection() && generateTestCasesButton.getEnabled());

            generateCounterExamplesButton.setText(generateCounterExamplesButtonText);
            generateCounterExamplesButton.setEnabled(buildsEnabled);
        }
        excludedJavaFilesList.setEnabled(buildsEnabled);
    }


    /**
     * Sets the selection for the setNumberOfThreads DropDown Menu.
     */
    private void setSelectionForSetNumberOfThreads(){
        int index = KeYProjectProperties.getNumberOfThreads(getProject());
        if(index <= 0 || index >= 100){
            numberOfThreadsSpinner.setSelection(1);
        }
        else{
            numberOfThreadsSpinner.setSelection(index);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
        try {
            IProject project = getProject();
            KeYProjectProperties.setEnableKeYResourcesBuilds(project, enableKeYResourcesBuildsButton.getSelection());
            KeYProjectProperties.setEnableBuildOnStartup(project, enableBuildOnStartupButton.getSelection() && enableBuildOnStartupButton.isEnabled());
            KeYProjectProperties.setEnableBuildProofsEfficient(project, enableBuildRequiredProofsOnlyButton.getSelection() && enableBuildRequiredProofsOnlyButton.isEnabled());
            KeYProjectProperties.setAutoDeleteProofFiles(project, autoDeleteProofFilesButton.getSelection() && autoDeleteProofFilesButton.isEnabled());
            KeYProjectProperties.setEnableMultiThreading(project, enableMultiThreadingButton.getSelection() && enableMultiThreadingButton.isEnabled());
            KeYProjectProperties.setNumberOfThreads(project, String.valueOf(numberOfThreadsSpinner.getSelection()));
            boolean triggerBuild = (!KeYProjectProperties.isGenerateTestCases(project) && (generateTestCasesButton.isEnabled() && generateTestCasesButton.getSelection())) 
                    || (!KeYProjectProperties.isGenerateCounterExamples(project) && (generateCounterExamplesButton.isEnabled() && generateCounterExamplesButton.getSelection()));
            KeYProjectProperties.setGenerateTestCases(project, generateTestCasesButton.getSelection() && generateTestCasesButton.isEnabled());
            KeYProjectProperties.setAutoDeleteTestCases(project, autoDeleteTestCasesButton.getSelection() && autoDeleteTestCasesButton.isEnabled());
            KeYProjectProperties.setGenerateCounterExamples(project, generateCounterExamplesButton.getSelection() && generateCounterExamplesButton.isEnabled());
            KeYProjectProperties.setExcludedJavaTypes(project, excludedJavaFilesList.getSelection());

            if(triggerBuild) {
                KeYResourcesUtil.synchronizeProject(project);
                KeYProjectBuildProperties properties = new KeYProjectBuildProperties(project);
                KeYProjectBuildJob buildJob = new KeYProjectBuildJob(project, KeYProjectBuildJob.FULL_BUILD, properties);
                buildJob.setRule(new KeYProjectBuildMutexRule(project));
                buildJob.schedule();
            }
            return super.performOk();
        }
        catch (CoreException e) {
            LogUtil.getLogger().logError(e);
            LogUtil.getLogger().openErrorDialog(getShell(), e);
            return false;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void performDefaults() {
        enableKeYResourcesBuildsButton.setSelection(true);
        enableBuildOnStartupButton.setSelection(true);
        enableBuildRequiredProofsOnlyButton.setSelection(true);
        autoDeleteProofFilesButton.setSelection(true);
        enableMultiThreadingButton.setSelection(true);
        numberOfThreadsSpinner.setSelection(2);
        generateTestCasesButton.setSelection(false);
        autoDeleteTestCasesButton.setSelection(false);
        generateCounterExamplesButton.setSelection(false);
        excludedJavaFilesList.deselectAll();
        update();
        super.performDefaults();
    }
}
