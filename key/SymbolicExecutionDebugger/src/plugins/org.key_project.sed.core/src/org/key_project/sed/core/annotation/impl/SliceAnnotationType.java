package org.key_project.sed.core.annotation.impl;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.RGB;
import org.key_project.sed.core.annotation.ISEAnnotation;
import org.key_project.sed.core.annotation.ISEAnnotationType;
import org.key_project.sed.core.model.ISENode;

/**
 * The {@link ISEAnnotationType} used for slicing.
 * @author Martin Hentschel
 * @see SliceAnnotation
 */
public class SliceAnnotationType extends AbstractSEAnnotationType {
   /**
    * The ID of this annotation type.
    */
   public static final String TYPE_ID = "org.key_project.sed.core.annotationType.slice";
   
   /**
    * {@inheritDoc}
    */
   @Override
   public String getTypeId() {
      return TYPE_ID;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName() {
      return "Slice";
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isDefaultHighlightBackground() {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public RGB getDefaultBackgroundColor() {
      return new RGB(197, 112, 154); // Nice purple
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isDefaultHighlightForeground() {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public RGB getDefaultForegroundColor() {
      return new RGB(0, 0, 0); // black
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SliceAnnotation createAnnotation() {
      return new SliceAnnotation();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public SliceAnnotationLink createLink(ISEAnnotation source, ISENode target) {
      return new SliceAnnotationLink(source, target);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String saveAnnotation(ISEAnnotation annotation) {
      Assert.isTrue(annotation instanceof SliceAnnotation);
      return ((SliceAnnotation)annotation).getSeed();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void restoreAnnotation(ISEAnnotation annotation, String savedContent) {
      Assert.isTrue(annotation instanceof SliceAnnotation);
      ((SliceAnnotation)annotation).setSeed(savedContent);
   }
}
