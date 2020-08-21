package org.key_project.sed.ui.wizard.page;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.key_project.sed.core.annotation.ISEAnnotation;
import org.key_project.sed.ui.composite.AnnotationAppearanceComposite;
import org.key_project.sed.ui.edit.ISEAnnotationEditor;
import org.key_project.sed.ui.util.SEDImages;

/**
 * This {@link WizardPage} is used to edit an {@link ISEAnnotation}.
 * @author Martin Hentschel
 */
public class EditWizardPage extends WizardPage {
   /**
    * The {@link ISEAnnotation} to edit.
    */
   private final ISEAnnotation annotation;
   
   /**
    * The optionally used {@link ISEAnnotationEditor}.
    */
   private final ISEAnnotationEditor editor;
   
   /**
    * The used {@link AnnotationAppearanceComposite}.
    */
   private AnnotationAppearanceComposite annotationAppearanceComposite;
   
   /**
    * Constructor.
    * @param pageName The name of this wizard page.
    * @param editor The optionally used {@link ISEAnnotationEditor}.
    * @param annotation The {@link ISEAnnotation} to edit.
    */
   public EditWizardPage(String pageName, ISEAnnotationEditor editor, ISEAnnotation annotation) {
      super(pageName);
      setTitle("Edit Annotation");
      setDescription("Change the details and the appearance of the annotation.");
      setImageDescriptor(SEDImages.getImageDescriptor(SEDImages.ANNOTATION_EDIT_WIZARD));
      this.annotation = annotation;
      this.editor = editor;
      if (this.editor != null) {
         this.editor.addPropertyChangeListener(ISEAnnotationEditor.PROP_ERROR_MESSAGE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
               updatePageCompleted();
            }
         });
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void createControl(Composite parent) {
      Composite root = new Composite(parent, SWT.NONE);
      setControl(root);
      root.setLayout(new GridLayout(1, false));
      // Editor
      if (editor != null) {
         Control control = editor.createContent(root);
         if (control != null) {
            control.setLayoutData(new GridData(GridData.FILL_BOTH));
         }
      }
      annotationAppearanceComposite = new AnnotationAppearanceComposite(root, SWT.NONE, annotation.getType());
      annotationAppearanceComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      annotationAppearanceComposite.showContent(annotation);
   }
   
   /**
    * Updates the page completed state.
    */
   protected void updatePageCompleted() {
      if (editor != null) {
         String errorMessage = editor.getErrorMessage();
         setPageComplete(errorMessage == null);
         setErrorMessage(errorMessage);
      }
      else {
         setPageComplete(true);
         setErrorMessage(null);
      }
   }

   /**
    * Applies the changes to the {@link ISEAnnotation}.
    */
   public void applyChanges() {
      annotationAppearanceComposite.applyChanges(annotation);
   }
}
