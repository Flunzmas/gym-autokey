package org.key_project.sed.ui.composite;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.key_project.sed.core.annotation.ISEAnnotation;
import org.key_project.sed.core.annotation.ISEAnnotationAppearance;
import org.key_project.sed.core.annotation.ISEAnnotationType;
import org.key_project.sed.core.annotation.impl.AnnotationApperanceDefinition;

/**
 * This {@link Composite} is used to edit the appearance of an {@link ISEAnnotation}
 * @author Martin Hentschel
 */
public class AnnotationAppearanceComposite extends Composite {
   /**
    * The optional {@link ISEAnnotationType} defining the default values.
    */
   private final ISEAnnotationType annotationType;
   
   /**
    * Highlight background?
    */
   private Button highlightBackgroundButton;
   
   /**
    * Label in front of {@link #backgroundSelector}.
    */
   private Label backgroundLabel;

   /**
    * Selects the background color.
    */
   private ColorSelector backgroundSelector;

   /**
    * Highlight foreground?
    */
   private Button highlightForegroundButton;
   
   /**
    * Label in front of {@link #foregroundSelector}.
    */
   private Label foregroundLabel;
   
   /**
    * Selects the foreground color.
    */
   private ColorSelector foregroundSelector;
   
   /**
    * Restores the default values.
    */
   private Button restoreDefaultsButton;

   /**
    * Constructor.
    * @param parent The parent {@link Composite}.
    * @param style The style to use.
    * @param annotation The optional {@link ISEAnnotationType} defining the default values.
    */
   public AnnotationAppearanceComposite(Composite parent, int style, ISEAnnotationType annotationType) {
      super(parent, style);
      this.annotationType = annotationType;
      setLayout(new FillLayout());
      // Appearance Group
      Group appearanceGroup = new Group(this, SWT.NONE);
      appearanceGroup.setText("Appearance");
      appearanceGroup.setLayout(new GridLayout(2, false));
      // Background
      GridData highlightBackgroundButtonData = new GridData();
      highlightBackgroundButtonData.horizontalSpan = 2;
      highlightBackgroundButton = new Button(appearanceGroup, SWT.CHECK);
      highlightBackgroundButton.setLayoutData(highlightBackgroundButtonData);
      highlightBackgroundButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            updateBackgroundEnabled();
         }
      });
      backgroundLabel = new Label(appearanceGroup, SWT.NONE);
      backgroundLabel.setText("Background Color");
      highlightBackgroundButton.setText("Highlight Background Color");
      backgroundSelector = new ColorSelector(appearanceGroup);
      // Foreground
      GridData highlightForegroundButtonData = new GridData();
      highlightForegroundButtonData.horizontalSpan = 2;
      highlightForegroundButton = new Button(appearanceGroup, SWT.CHECK);
      highlightForegroundButton.setLayoutData(highlightForegroundButtonData);
      highlightForegroundButton.setText("Highlight Foreground Color");
      highlightForegroundButton.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            updateForegroundEnabled();
         }
      });
      foregroundLabel = new Label(appearanceGroup, SWT.NONE);
      foregroundLabel.setText("Foreground Color");
      foregroundSelector = new ColorSelector(appearanceGroup);
      // Defaults
      if (annotationType != null) {
         GridData restoreDefaultsButtonData = new GridData();
         restoreDefaultsButtonData.horizontalSpan = 2;
         restoreDefaultsButton = new Button(appearanceGroup, SWT.PUSH);
         restoreDefaultsButton.setLayoutData(restoreDefaultsButtonData);
         restoreDefaultsButton.setText("Restore Annotation &Defaults");
         restoreDefaultsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               restoreDefaults();
               updateBackgroundEnabled();
               updateForegroundEnabled();
            }
         });
      }
      // Update shown content
      showContent(annotationType);
      // Update enabled states.
      updateBackgroundEnabled();
      updateForegroundEnabled();
   }

   /**
    * Updates the shown content.
    */
   public void showContent(ISEAnnotationType annotationType) {
      if (annotationType != null) {
         highlightBackgroundButton.setSelection(annotationType.isHighlightBackground());
         backgroundSelector.setColorValue(annotationType.getBackgroundColor());
         highlightForegroundButton.setSelection(annotationType.isHighlightForeground());
         foregroundSelector.setColorValue(annotationType.getForegroundColor());
         updateBackgroundEnabled();
         updateForegroundEnabled();
      }
   }

   /**
    * Updates the shown content.
    */
   public void showContent(ISEAnnotationAppearance appearance) {
      if (appearance != null) {
         highlightBackgroundButton.setSelection(appearance.isHighlightBackground());
         backgroundSelector.setColorValue(appearance.getBackgroundColor());
         highlightForegroundButton.setSelection(appearance.isHighlightForeground());
         foregroundSelector.setColorValue(appearance.getForegroundColor());
         updateBackgroundEnabled();
         updateForegroundEnabled();
      }
   }

   /**
    * Updates the enabled state of {@link #backgroundLabel} and {@link #backgroundSelector}.
    */
   protected void updateBackgroundEnabled() {
      boolean enabled = highlightBackgroundButton.getSelection();
      backgroundLabel.setEnabled(enabled);
      backgroundSelector.setEnabled(enabled);
   }
   
   /**
    * Updates the enabled state of {@link #foregroundLabel} and {@link #foregroundSelector}.
    */
   protected void updateForegroundEnabled() {
      boolean enabled = highlightForegroundButton.getSelection();
      foregroundLabel.setEnabled(enabled);
      foregroundSelector.setEnabled(enabled);
   }
   
   /**
    * Restores the default values.
    */
   protected void restoreDefaults() {
      highlightBackgroundButton.setSelection(annotationType.isHighlightBackground());
      backgroundSelector.setColorValue(annotationType.getBackgroundColor());
      highlightForegroundButton.setSelection(annotationType.isHighlightForeground());
      foregroundSelector.setColorValue(annotationType.getForegroundColor());
   }

   /**
    * Applies the changes to the given {@link ISEAnnotationAppearance}.
    * @param appearance The {@link ISEAnnotationAppearance} to modify.
    */
   public void applyChanges(ISEAnnotationAppearance appearance) {
      if (appearance != null) {
         ISEAnnotationType type = appearance.getType();
         if (highlightBackgroundButton.getSelection() == type.isHighlightBackground()) {
            appearance.setCustomHighlightBackground(null);
         }
         else {
            appearance.setCustomHighlightBackground(highlightBackgroundButton.getSelection());
         }
         if (backgroundSelector.getColorValue().equals(type.getBackgroundColor())) {
            appearance.setCustomBackgroundColor(null);
         }
         else {
            appearance.setCustomBackgroundColor(backgroundSelector.getColorValue());
         }
         if (highlightForegroundButton.getSelection() == type.isHighlightForeground()) {
            appearance.setCustomHighlightForeground(null);
         }
         else {
            appearance.setCustomHighlightForeground(highlightForegroundButton.getSelection());
         }
         if (foregroundSelector.getColorValue().equals(type.getForegroundColor())) {
            appearance.setCustomForegroundColor(null);
         }
         else {
            appearance.setCustomForegroundColor(foregroundSelector.getColorValue());
         }
      }
   }
   
   /**
    * Returns the specified {@link ISEAnnotationAppearance}.
    * @return The specified {@link ISEAnnotationAppearance}.
    */
   public ISEAnnotationAppearance getAnnotationAppearance() {
      ISEAnnotationAppearance appearance = new AnnotationApperanceDefinition(annotationType);
      applyChanges(appearance);
      return appearance;
   }
}
