package org.key_project.jmlediting.ui.outline;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.key_project.javaeditor.outline.IOutlineModifier;
import org.key_project.javaeditor.util.LogUtil;
import org.key_project.jmlediting.core.profile.JMLPreferencesHelper;

/**
 * Extends the Java Outline with JML Specifications.
 * 
 * @author Timm Lippert
 *
 */
@SuppressWarnings("restriction")
public class JMLinJavaOutlineExtension implements IOutlineModifier {
   private Map<Object, Object[]> cache = new HashMap<Object, Object[]>();

   private JMLASTCommentLocator comments = null;
   private IJavaElement root = null;

   public Object[] modify(Object parent, Object[] currentChildren, JavaOutlinePage javaOutlinePage) {
      if (JMLPreferencesHelper.isExtensionEnabled()) {
         Object[] cachedChildren = cache.get(parent);
         if (cachedChildren == null) {
            cachedChildren = computeChildren(parent, currentChildren, javaOutlinePage);
            cache.put(parent, cachedChildren);
         }
         return cachedChildren;
      }
      else {
         return currentChildren;
      }
   }
   
   protected Object[] computeChildren(Object parent, Object[] currentChildren, JavaOutlinePage javaOutlinePage) {
      if (!(parent instanceof IJavaElement)) {
         return currentChildren;
      }

      final IJavaElement javaParent = (IJavaElement) parent;

      // first call with i compilation unit initialize everything
      if (javaParent.getParent().getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
         CompilationUnit cu = null;
//         try {
//            JavaEditor javaEditor = ObjectUtil.get(javaOutlinePage, "fEditor");
//            cu = JDTUtil.getSharedCompilationUnit(javaEditor);
//         }
//         catch (Exception e) {
//            LogUtil.getLogger().logWarning("JDT implementation has changed: " + e.getMessage());
//         }
         comments = new JMLASTCommentLocator((ICompilationUnit) javaParent, cu);
         root = javaParent;
      }
      else if (comments == null)
         return currentChildren;

      // add invariants to class
      if (javaParent.getElementType() == IJavaElement.TYPE
               && javaParent.getParent().equals(root)) {

         Object[] childrenToShowInOutline = new Object[currentChildren.length
                  + comments.getClassInvariants().size()];

         // add old elements
         for (int i = comments.getClassInvariants().size(); i < currentChildren.length
                  + comments.getClassInvariants().size(); i++) {
            childrenToShowInOutline[i] = currentChildren[i
                     - comments.getClassInvariants().size()];
         }
         int i = 0;
         // add JML elements
         for (JMLComments node : comments.getClassInvariants()) {
            childrenToShowInOutline[i++] = new JMLOutlineElement((IJavaElement) parent, node);
         }
         return childrenToShowInOutline;
      }

      // add JML #Spezifications to methods
      if (javaParent.getElementType() == IJavaElement.METHOD) {
         IMethod method = (IMethod) javaParent;
         int offset = -1;
         int arroffset = 0;
         List<JMLComments> comlist = null;
         Object[] childrenToShowInOutline;

         try {
            offset = method.getNameRange().getOffset();
         }
         catch (JavaModelException e) {
            LogUtil.getLogger().logError(e);
            ;
         }

         comlist = comments.getMethodJMLComm(offset);

         // method has JML comments, add to array with content to show.
         arroffset = comlist.size();

         childrenToShowInOutline = new Object[currentChildren.length + arroffset];
         int i = 0;
         // add new elements
         for (JMLComments com : comlist) {
            childrenToShowInOutline[i++] = new JMLOutlineElement((IJavaElement) parent, com);
         }
         // add old elements
         for (i = arroffset; i < currentChildren.length + comlist.size(); i++) {
            childrenToShowInOutline[i] = currentChildren[i - comlist.size()];
         }

         return childrenToShowInOutline;

      }
      // add field declarations
      if (javaParent.getElementType() == IJavaElement.FIELD) {
         IField field = (IField) javaParent;
         JMLComments toAdd = null;
         Object[] childrenToShowInOutline;

         try {
            toAdd = comments.getFieldJMLComm(field.getSourceRange().getLength()
                     + field.getSourceRange().getOffset());
         }
         catch (JavaModelException e) {
            LogUtil.getLogger().logError(e);
         }
         if (toAdd != null) {
            childrenToShowInOutline = new Object[currentChildren.length + 1];
            childrenToShowInOutline[0] = new JMLOutlineElement(javaParent, toAdd);
            // add old elements
            for (int i = 1; i <= currentChildren.length; i++) {
               childrenToShowInOutline[i] = currentChildren[i - 1];
            }
         }
         else
            childrenToShowInOutline = currentChildren;

         return childrenToShowInOutline;
      }

      return currentChildren;
   }

   @Override
   public void changeDetected(ElementChangedEvent event) {
      cache = new HashMap<Object, Object[]>();
   }
}
