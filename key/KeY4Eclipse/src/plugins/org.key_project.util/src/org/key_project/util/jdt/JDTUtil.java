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

package org.key_project.util.jdt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JavaConventionsUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementLabelComposer;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.key_project.util.Activator;
import org.key_project.util.eclipse.Logger;
import org.key_project.util.eclipse.ResourceUtil;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.java.CollectionUtil;
import org.key_project.util.java.IFilter;
import org.key_project.util.java.IOUtil;
import org.key_project.util.java.ObjectUtil;
import org.key_project.util.java.thread.AbstractRunnableWithException;
import org.key_project.util.java.thread.IRunnableWithException;

/**
 * Provides static methods to work with JDT.
 * @author Martin Hentschel
 */
@SuppressWarnings("restriction")
public class JDTUtil {
   /**
    * File extension of Java source files.
    */
   public static final String JAVA_FILE_EXTENSION = "java";
   
   /**
    * File extension of Java source files with leading dot.
    */
   public static final String JAVA_FILE_EXTENSION_WITH_DOT = "." + JAVA_FILE_EXTENSION;
   
   /**
    * Forbid instances by this private constructor.
    */
   private JDTUtil() {
   }

   /**
    * Creates a new {@link IJavaProject} that is an {@link IProject} with a JDT nature.
    * @param name The project name.
    * @param referencedProjects Additional {@link IProject}s to include in the build path.
    * @return The created {@link IJavaProject}.
    * @throws CoreException Occurred Exception.
    */
   public static IJavaProject createJavaProject(String name, 
                                                IProject... referencedProjects) throws CoreException {
      return createJavaProject(name, 
                               getOutputFolderName(), 
                               new String[] {getSourceFolderName()}, 
                               referencedProjects);
   }

   /**
    * Creates a new {@link IJavaProject} that is an {@link IProject} with a JDT nature.
    * @param name The project name.
    * @param outputFolderName The name of the output folder to create.
    * @param srcFolderNames The name of the source folder to create.
    * @param referencedProjects Additional {@link IProject}s to include in the build path.
    * @return The created {@link IJavaProject}.
    * @throws CoreException Occurred Exception.
    */
   public static IJavaProject createJavaProject(String name, 
                                                String outputFolderName,
                                                String[] srcFolderNames,
                                                IProject... referencedProjects) throws CoreException {
      IProject project = ResourceUtil.createProject(name);
      IFolder bin = project.getFolder(outputFolderName);
      CoreUtility.createDerivedFolder(bin, true, true, null);
      IFolder[] src = new IFolder[srcFolderNames.length];
      for (int i = 0; i < srcFolderNames.length; i++) {
         src[i] = project.getFolder(srcFolderNames[i]);
         CoreUtility.createFolder(src[i], true, true, null);
      }
      return convertToJavaProject(project, bin, src, referencedProjects);
   }
   
   /**
    * Returns the default output folder name.
    * @return The default output folder name.
    */
   public static String getOutputFolderName() {
      return PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.SRCBIN_BINNAME);
   }
   
   /**
    * Returns the default source folder name.
    * @return The default source folder name.
    */
   public static String getSourceFolderName() {
      return PreferenceConstants.getPreferenceStore().getString(PreferenceConstants.SRCBIN_SRCNAME);
   }
   
   /**
    * Converts the given {@link IProject} into an {@link IJavaProject}.
    * @param project The {@link IProject} to convert.
    * @param bin The {@link IContainer} to use as output location.
    * @param src The {@link IContainer} which provides the source files.
    * @param referencedProjects Additional {@link IProject}s to include in the build path.
    * @return The created {@link IJavaProject}.
    * @throws CoreException Occurred Exception.
    */
   public static IJavaProject convertToJavaProject(IProject project, 
                                                   final IContainer bin, 
                                                   final IContainer[] src, 
                                                   final IProject... referencedProjects) throws CoreException {
      final IJavaProject javaProject = JavaCore.create(project); 
      IRunnableWithException run = new AbstractRunnableWithException() {
         @Override
         public void run() {
            try {
               JavaCapabilityConfigurationPage page = new JavaCapabilityConfigurationPage();
               IClasspathEntry[] entries = new IClasspathEntry[src.length + referencedProjects.length];
               for (int i = 0; i < src.length; i++) {
                  entries[i] = JavaCore.newSourceEntry(src[i].getFullPath());
                  
               }
               for (int i = 0; i < referencedProjects.length; i++) {
                  entries[i + src.length] = JavaCore.newProjectEntry(referencedProjects[i].getFullPath());
               }
               entries = ArrayUtil.addAll(entries, getDefaultJRELibrary());
               page.init(javaProject, bin.getFullPath(), entries, false);
               page.configureJavaProject(null);
            }
            catch (Exception e) {
               setException(e);
            }
         }
      };
      Display.getDefault().syncExec(run);
      if (run.getException() instanceof CoreException) {
         throw (CoreException)run.getException();
      }
      else if (run.getException() != null) {
         throw new CoreException(new Logger(Activator.getDefault(), Activator.PLUGIN_ID).createErrorStatus(run.getException()));
      }
      return javaProject;
   }
   
   /**
    * Returns the default JRE library entries.
    * @return The default JRE library entries.
    */
   public static IClasspathEntry[] getDefaultJRELibrary() {
       return PreferenceConstants.getDefaultJRELibrary();
   }
   
   /**
    * Searches the {@link IMethod} as JDT representation which ends
    * at the given index.
    * @param cu The {@link ICompilationUnit} to search in.
    * @param endIndex The index in the file at that the required method ends.
    * @return The found {@link IMethod} or {@code null} if the JDT representation is not available.
    * @throws JavaModelException Occurred Exception.
    * @throws IOException Occurred Exception.
    */
   public static IMethod findJDTMethod(ICompilationUnit cu, int endIndex) throws JavaModelException, IOException {
      IMethod result = null;
      if (cu != null) {
         IType[] types = cu.getAllTypes();
         int i = 0;
         while (result == null && i < types.length) {
            IMethod[] methods = types[i].getMethods();
            int j = 0;
            while (result == null && j < methods.length) {
               ISourceRange methodRange = methods[j].getSourceRange();
               if (endIndex == methodRange.getOffset() + methodRange.getLength()) {
                  result = methods[j];
               }
               j++;
            }
            i++;
         }
      }
      return result;
   }
   
   /**
    * Searches the {@link IType} as JDT representation which ends
    * at the given index.
    * @param cu The {@link ICompilationUnit} to search in.
    * @param endIndex The index in the file at that the required method ends.
    * @return The found {@link IType} or {@code null} if the JDT representation is not available.
    * @throws JavaModelException Occurred Exception.
    * @throws IOException Occurred Exception.
    */
   public static IType findJDTType(ICompilationUnit cu, int endIndex) throws JavaModelException, IOException {
      IType result = null;
      if (cu != null) {
         IType[] types = cu.getAllTypes();
         int i = 0;
         while (result == null && i < types.length) {
            ISourceRange typeRange = types[i].getSourceRange();
            if (endIndex == typeRange.getOffset() + typeRange.getLength()) {
               result = types[i];
            }
            i++;
         }
      }
      return result;
   }
   
   /**
    * Returns the tab width used in the given {@link IJavaElement}.
    * @param element The {@link IJavaElement} to get its tab width.
    * @return The tab width.
    */
   public static int getTabWidth(IJavaElement element) {
      return element != null ? CodeFormatterUtil.getTabWidth(element.getJavaProject()) : 0;
   }
   
   /**
    * Returns the first {@link IJavaElement} from the given once that
    * has the given text label.
    * @param elements The {@link IJavaElement}s to search in.
    * @param textLabel The text label for that the {@link IJavaElement} is needed.
    * @return The first found {@link IJavaElement} or {@code null} if no one was found.
    * @throws JavaModelException Occurred Exception 
    */
   public static IMethod getElementForQualifiedMethodLabel(IMethod[] elements, String textLabel) throws JavaModelException {
       IMethod result = null;
       if (elements != null) {
           int i = 0;
           while (result == null && i < elements.length) {
               if (ObjectUtil.equals(textLabel, getQualifiedMethodLabel(elements[i]))) {
                   result = elements[i];
               }
               i++;
           }
       }
       return result;
   }
   
   /**
    * <p>
    * Returns the unique method signature for the given {@link IMethod}.
    * Parameter types are replaced with the full qualified type names.
    * </p>
    * <p>
    * Example method declaration: {@code public int foo(Date ud, java.sql.Date sd)}<br>
    * Created signature: {@code foo(java.util.Date, java.sql.Date)}
    * </p>
    * @param method The {@link IMethod} for that the signature label is needed.
    * @return The created label.
    * @throws JavaModelException Occurred Exception.
    */
   public static String getQualifiedMethodLabel(IMethod method) throws JavaModelException {
      try {
         if (method != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(method.getElementName());
            sb.append("(");
            ILocalVariable[] parameters = method.getParameters();
            boolean afterFirst = false;
            JavaElementLabelComposerHelper c = new JavaElementLabelComposerHelper(sb, method.getDeclaringType());
            for (ILocalVariable parameter : parameters) {
               if (afterFirst) {
                  sb.append(", ");
               }
               else {
                  afterFirst = true;
               }
               c.appendTypeSignatureLabel(parameter, parameter.getTypeSignature(), JavaElementLabels.F_PRE_TYPE_SIGNATURE);
            }
            sb.append(")");
            return sb.toString();
         }
         else {
            return null;
         }
      }
      catch (JavaModelRuntimeException e) {
         throw e.getCause();
      }
   }
   
   /**
    * Utility class to compute the full qualified type of a method parameter.
    * @author Martin Hentschel
    */
   private static class JavaElementLabelComposerHelper extends JavaElementLabelComposer {
      /**
       * The Type that contains the method.
       */
      private IType declaringType;

      /**
       * Constructor.
       * @param buffer The {@link StringBuffer} to fill.
       * @param declaringType The Type that contains the method.
       */
      private JavaElementLabelComposerHelper(StringBuffer buffer, IType declaringType) {
         super(buffer);
         this.declaringType = declaringType;
      }

      /**
       * <p>
       * {@inheritDoc}
       * </p>
       * <p>
       * Changed visibility to public.
       * </p>
       */
      @Override
      public void appendTypeSignatureLabel(IJavaElement enclosingElement, String typeSig, long flags) {
         super.appendTypeSignatureLabel(enclosingElement, typeSig, flags);
      }

      /**
       * Overwritten to return the fulil qualified type name instead of the simple type name.
       */
      @Override
      protected String getSimpleTypeName(IJavaElement enclosingElement, String typeSig) {
         try {
            String simpleName = Signature.toString(Signature.getTypeErasure(typeSig));
            String[][] resolvedTypes = declaringType.resolveType(simpleName);
            if (resolvedTypes != null && resolvedTypes.length > 0) {
               return (resolvedTypes[0][0].equals("") ? "" : resolvedTypes[0][0] + ".") + resolvedTypes[0][1];
            } 
            else {
               return simpleName;
            }
         }
         catch (JavaModelException e) {
            throw new JavaModelRuntimeException(e);
         }
      }
   }
   
   /**
    * A utility {@link RuntimeException} that is used to transfer
    * a {@link JavaModelException} back in the call hierarchy.
    * @author Martin Hentschel
    */
   private static class JavaModelRuntimeException extends RuntimeException {
      /**
       * Generated UID.
       */
      private static final long serialVersionUID = 9027197807876279139L;

      /**
       * Constructor.
       * @param cause The {@link JavaModelException} to wrap.
       */
      private JavaModelRuntimeException(JavaModelException cause) {
         super(cause);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public JavaModelException getCause() {
         return (JavaModelException)super.getCause();
      }
   }
   
   /**
    * Returns a human readable text label for the given {@link IJavaElement}.
    * @param element The {@link IJavaElement} to convert,
    * @return The human readable text label. An empty {@link String} is returned if the given {@link IJavaElement} is {@code null}.
    */
   public static String getTextLabel(IJavaElement element) {
       return JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_DEFAULT);
   }
   
   /**
    * Returns the first {@link IJavaElement} from the given once that
    * has the given text label.
    * @param elements The {@link IJavaElement}s to search in.
    * @param textLabel The text label for that the {@link IJavaElement} is needed.
    * @return The first found {@link IJavaElement} or {@code null} if no one was found.
    */
   public static IJavaElement getElementForTextLabel(IJavaElement[] elements, String textLabel) {
       IJavaElement result = null;
       if (elements != null) {
           int i = 0;
           while (result == null && i < elements.length) {
               if (ObjectUtil.equals(textLabel, getTextLabel(elements[i]))) {
                   result = elements[i];
               }
               i++;
           }
       }
       return result;
   }
   
   /**
    * Adds the given {@link IClasspathEntry} to the {@link IJavaProject}.
    * @param javaProject The {@link IJavaProject} to add to.
    * @param entryToAdd The {@link IClasspathEntry} to add.
    * @throws JavaModelException Occurred Exception.
    */
   public static void addClasspathEntry(IJavaProject javaProject,
                                        IClasspathEntry entryToAdd) throws JavaModelException {
       if (javaProject != null && entryToAdd != null) {
           IClasspathEntry[] entries = javaProject.getRawClasspath();
           for(IClasspathEntry e : entries) {
              if(e != null && e.getPath().equals(entryToAdd.getPath())) {
                 return;
              }
           }
           IClasspathEntry[] newEntries = ArrayUtil.add(entries, entryToAdd);
           javaProject.setRawClasspath(newEntries, null);
       }
   }
   
   /**
    * Returns all {@link IJavaProject}s.
    * @return All {@link IJavaProject}s.
    * @throws JavaModelException
    */
   public static IJavaProject[] getAllJavaProjects() throws JavaModelException {
      return JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
   }

   /**
    * Returns all available packages.
    * @return All packages.
    * @throws JavaModelException Occurred Exception.
    */
   public static IJavaElement[] getAllPackageFragmentRoot() throws JavaModelException {
      IJavaProject[] jProjects = getAllJavaProjects();
      Set<IJavaElement> packages = new HashSet<IJavaElement>();
      for (IJavaProject jProject : jProjects) {
         IPackageFragmentRoot[] froots = jProject.getAllPackageFragmentRoots();
         for (IPackageFragmentRoot froot : froots) {
            if (froot != null && froot.exists() && !froot.isReadOnly()) {
               IJavaElement[] children = froot.getChildren();
               for (IJavaElement element : children) {
                  packages.add(element);
               }
            }
         }
      }
      return packages.toArray(new IJavaElement[packages.size()]);
   }
   
   /**
    * Returns the package that contains the {@link IJavaElement}.
    * @param element The {@link IJavaElement}.
    * @return The package that contains the given {@link IJavaElement}.
    */
   public static IJavaElement getPackage(IJavaElement element) {
      if (element != null) {
         if (element instanceof IPackageDeclaration) {
            return element;
         }
         else if (element instanceof IPackageFragment) {
            return element;
         }
         else if (element instanceof IPackageFragmentRoot) {
            return element;
         }
         else {
            return getPackage(element.getParent());
         }
      }
      else {
         return null;
      }
   }
   
   /**
    * <p>
    * Returns the {@link IJavaProject} for the given {@link IProject}.
    * </p>
    * <p>
    * <b>Attention:</b> It is also an {@link IJavaProject} returned even
    * if the {@link IProject} is no Java project (has no JDT nature).
    * To verify if an {@link IProject} is a real Java project use
    * {@link JDTUtil#isJavaProject(IProject)}.
    * </p>
    * @param project The {@link IProject} for that an {@link IJavaProject} is needed.
    * @return The {@link IJavaProject} representation of the {@link IProject} or {@code null} if the given {@link IProject} is {@code null}.
    */
   public static IJavaProject getJavaProject(IProject project) {
       return JavaCore.create(project);
   }

   /**
    * <p>
    * Returns the {@link IJavaProject} for the given {@link IProject}.
    * </p>
    * <p>
    * <b>Attention:</b> It is also an {@link IJavaProject} returned even
    * if the {@link IProject} is no Java project (has no JDT nature).
    * To verify if an {@link IProject} is a real Java project use
    * {@link JDTUtil#isJavaProject(IProject)}.
    * </p>
    * @param projectName The name of the {@link IProject} for that an {@link IJavaProject} is needed.
    * @return The {@link IJavaProject} representation of the {@link IProject} with the given name or {@code null} if the given project name is {@code null}/empty.
    */
   public static IJavaProject getJavaProject(String projectName) {
       IProject project = ResourceUtil.getProject(projectName);
       return getJavaProject(project);
   }
   
   /**
    * Checks if the given {@link IProject} is a Java project.
    * @param project The {@link IProject} to check.
    * @return {@code true} is Java project, {@code false} is no Java project.
    */
   public static boolean isJavaProject(IProject project) {
       if (project != null) {
           IJavaProject javaProject = getJavaProject(project);
           return isJavaProject(javaProject);
       }
       else {
           return false;
       }
   }

   /**
    * Checks if the given {@link IJavaProject} is a Java project.
    * @param project The {@link IJavaProject} to check.
    * @return {@code true} is Java project, {@code false} is no Java project.
    */
   public static boolean isJavaProject(IJavaProject javaProject) {
      return javaProject != null && javaProject.exists();
   }
   
   /**
    * Checks if the given {@link IResource} is a "Java" file.
    * @param file The {@link IResource} to check.
    * @return {@code true} is Java file, {@code false} is something else or {@code null}.
    */
   public static boolean isJavaFile(IResource file) {
      if (file != null) {
         String extension = file.getFileExtension();
         return extension != null && extension.equalsIgnoreCase(JAVA_FILE_EXTENSION);
      }
      else {
         return false;
      }
   }
   
   /**
    * Checks if the given {@link IResource} is or is contained in a source folder of its project.
    * @param resource The {@link IResource} to check.
    * @return {@code true} is source folder of its project or contained in a source folder of its project, {@code false} is somewhere else.
    * @throws JavaModelException Occurred Exception.
    */
   public static boolean isInSourceFolder(IResource resource) throws JavaModelException {
      boolean inSourceFolder = false;
      if (resource != null) {
         IJavaProject javaProject = getJavaProject(resource.getProject());
         if (javaProject != null && javaProject.exists()) {
             IClasspathEntry[] entries = javaProject.getRawClasspath();
             int i = 0;
             while (!inSourceFolder && i < entries.length) {
                if (entries[i].getContentKind() == IPackageFragmentRoot.K_SOURCE) {
                   IPackageFragmentRoot[] roots = javaProject.findPackageFragmentRoots(entries[i]);
                   int j = 0;
                   while (!inSourceFolder && j < roots.length) {
                      IResource rootResource = roots[j].getResource();
                      if (rootResource != null && rootResource.contains(resource)) {
                         inSourceFolder = true;
                      }
                      j++;
                   }
                }
                i++;
             }
         }
      }
      return inSourceFolder;
   }

   /**
    * Returns all source {@link IPackageFragmentRoot}s.
    * @param javaProject The {@link IJavaProject} to read source {@link IPackageFragmentRoot}s from.
    * @return The found {@link IPackageFragmentRoot}s.
    * @throws JavaModelException Occurred Exception.
    */
   public static List<IPackageFragmentRoot> getSourcePackageFragmentRoots(IJavaProject javaProject) throws JavaModelException {
      List<IPackageFragmentRoot> result = new LinkedList<IPackageFragmentRoot>();
      if (javaProject != null && javaProject.exists()) {
         IClasspathEntry[] entries = javaProject.getRawClasspath();
         for (IClasspathEntry entry : entries) {
             if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE &&
                 entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IPackageFragmentRoot[] roots = javaProject.findPackageFragmentRoots(entry);
                CollectionUtil.addAll(result, roots);
             }
         }
      }
      return result;
   }
   
   /**
    * Returns the locations in the local file system of all used
    * source entries in the java build path of the given project.
    * @param project The given Project.
    * @return The found source locations in the file system.
    * @throws JavaModelException Occurred Exception.
    */
   public static List<File> getSourceLocations(IProject project) throws JavaModelException {
       return getSourceLocations(project, new HashSet<IProject>());
   }
   
   /**
    * Internal helper method that is used in {@link #getSourceLocations(IProject)}
    * to compute the source path. It is required to solve cycles in project dependencies.
    * @param project The given Project.
    * @param alreadyHandledProjects The already handled {@link IProject} that don't need to be analysed again.
    * @return The found source locations in the file system.
    * @throws JavaModelException Occurred Exception.
    */    
   private static List<File> getSourceLocations(IProject project, Set<IProject> alreadyHandledProjects) throws JavaModelException {
       List<File> result = new LinkedList<File>();
       if (project != null) {
           Assert.isNotNull(alreadyHandledProjects);
           alreadyHandledProjects.add(project);
           IJavaProject javaProject = getJavaProject(project);
           if (javaProject != null && javaProject.exists()) {
               IClasspathEntry[] entries = javaProject.getRawClasspath();
               for (IClasspathEntry entry : entries) {
                   if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE) {
                       List<File> location = getLocationFor(javaProject, entry, IPackageFragmentRoot.K_SOURCE, alreadyHandledProjects);
                       if (location != null) {
                           result.addAll(location);
                       }
                   }
               }
           }
       }
       return result;
   }
   
   /**
    * Returns the {@link IResource}s in the workspace of all used
    * source entries in the java build path of the given project.
    * @param project The given Project.
    * @return The found source {@link IResource}s in the workspace.
    * @throws JavaModelException Occurred Exception.
    */
   public static List<IResource> getSourceResources(IProject project) throws JavaModelException {
       return getSourceResources(project, new HashSet<IProject>());
   }
   
   /**
    * Internal helper method that is used in {@link #getSourceResources(IProject)}
    * to compute the source path. It is required to solve cycles in project dependencies.
    * @param project The given Project.
    * @param alreadyHandledProjects The already handled {@link IProject} that don't need to be analysed again.
    * @return The found source {@link IResource}s in the workspace.
    * @throws JavaModelException Occurred Exception.
    */    
   private static List<IResource> getSourceResources(IProject project, Set<IProject> alreadyHandledProjects) throws JavaModelException {
       List<IResource> result = new LinkedList<IResource>();
       if (project != null) {
           Assert.isNotNull(alreadyHandledProjects);
           alreadyHandledProjects.add(project);
           IJavaProject javaProject = getJavaProject(project);
           if (javaProject != null && javaProject.exists()) {
               IClasspathEntry[] entries = javaProject.getRawClasspath();
               for (IClasspathEntry entry : entries) {
                   if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE) {
                       List<IResource> location = getResourceFor(javaProject, entry, IPackageFragmentRoot.K_SOURCE, alreadyHandledProjects);
                       if (location != null) {
                           result.addAll(location);
                       }
                   }
               }
           }
       }
       return result;
   }
   
   /**
    * Returns the {@link IResource}s of the given {@link IClasspathEntry}.
    * @param javaProject The actual {@link IJavaProject} that provides the {@link IClasspathEntry}.
    * @param entry The given {@link IClasspathEntry}.
    * @param alreadyHandledProjects The already handled {@link IProject} that don't need to be analysed again.
    * @return The found {@link IResource}s.
    * @throws JavaModelException 
    */
   private static List<IResource> getResourceFor(IJavaProject javaProject, 
                                                 IClasspathEntry entry,
                                                 int expectedKind,
                                                 Set<IProject> alreadyHandledProjects) throws JavaModelException {
       if (entry != null) {
           if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER ||
               entry.getEntryKind() == IClasspathEntry.CPE_SOURCE ||
               entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY ||
               entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
               List<IResource> result = new LinkedList<IResource>();
               IPackageFragmentRoot[] roots = javaProject.findPackageFragmentRoots(entry);
               for (IPackageFragmentRoot root : roots) {
                   if (root.getKind() == expectedKind) {
                       if (root.getResource() != null) {
                           if (root.getResource().getLocationURI() != null) {
                               result.add(root.getResource());
                           }
                       }
                       else if (root.getPath() != null) {
                           IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(root.getPath());
                           if (resource != null && resource.exists()) {
                               result.add(resource);
                           }
                       }
                   }
               }
               return result; // Ignore containers
           }
           else if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
               Assert.isNotNull(entry.getPath());
               IResource project = ResourcesPlugin.getWorkspace().getRoot().findMember(entry.getPath());
               Assert.isTrue(project instanceof IProject);
               if (!alreadyHandledProjects.contains(project)) {
                   return getSourceResources((IProject)project, alreadyHandledProjects);
               }
               else {
                   return null; // Project was already analyzed, no need to do it again.
               }
           }
           else {
               Assert.isTrue(false, "Unknown content kind \"" + entry.getContentKind() + "\" of class path entry \"" + entry + "\".");
               return null;
           }
       }
       else {
           return null;
       }
   }
   
   /**
    * Returns the locations of the given {@link IClasspathEntry}.
    * @param javaProject The actual {@link IJavaProject} that provides the {@link IClasspathEntry}.
    * @param entry The given {@link IClasspathEntry}.
    * @param alreadyHandledProjects The already handled {@link IProject} that don't need to be analysed again.
    * @return The found locations.
    * @throws JavaModelException 
    */
   public static List<File> getLocationFor(IJavaProject javaProject, 
                                            IClasspathEntry entry,
                                            int expectedKind,
                                            Set<IProject> alreadyHandledProjects) throws JavaModelException {
       if (entry != null) {
           if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER ||
               entry.getEntryKind() == IClasspathEntry.CPE_SOURCE ||
               entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY ||
               entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
               List<File> result = new LinkedList<File>();
               IPackageFragmentRoot[] roots = javaProject.findPackageFragmentRoots(entry);
               for (IPackageFragmentRoot root : roots) {
                   if (root.getKind() == expectedKind) {
                       if (root.getResource() != null) {
                           if (root.getResource().getLocationURI() != null) {
                               result.add(ResourceUtil.getLocation(root.getResource()));
                           }
                       }
                       else if (root.getPath() != null) {
                           File location = new File(root.getPath().toString());
                           if (location.exists()) {
                               result.add(location);
                           }
                       }
                   }
               }
               return result; // Ignore containers
           }
           else if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
               Assert.isNotNull(entry.getPath());
               IResource project = ResourcesPlugin.getWorkspace().getRoot().findMember(entry.getPath());
               Assert.isTrue(project instanceof IProject);
               if (!alreadyHandledProjects.contains(project)) {
                   return getSourceLocations((IProject)project, alreadyHandledProjects);
               }
               else {
                   return null; // Project was already analyzed, no need to do it again.
               }
           }
           else {
               Assert.isTrue(false, "Unknown content kind \"" + entry.getContentKind() + "\" of class path entry \"" + entry + "\".");
               return null;
           }
       }
       else {
           return null;
       }
   }
   
   /**
    * Parses the given {@link ICompilationUnit} in the specified range into an AST. 
    * @param compilationUnit The {@link ICompilationUnit} to parse.
    * @param offset The start index in the text to parse.
    * @param length The length of the text to parse.
    * @return The {@link ASTNode} which is the root of the AST.
    */
   public static ASTNode parse(ICompilationUnit compilationUnit, int offset, int length) {
      ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL); // Hopefully always the newest AST level (e.g. AST.JLS4)
      parser.setSource(compilationUnit);
      parser.setSourceRange(offset, length);
      return parser.createAST(null);
   }
   
   /**
    * Returns the {@link Block} which represents the method body
    * of the given {@link IMethod} in an {@link ASTParser}.
    * @param method The {@link IMethod} to lookup its body.
    * @return The found {@link Block} or {@code null} if not available.
    * @throws JavaModelException Occurred Exception.
    */
   public static Block getMethodBody(IMethod method) throws JavaModelException {
      if (method != null) {
         int methodStart = method.getSourceRange().getOffset();
         int methodLength = method.getSourceRange().getLength();
         ASTNode node = JDTUtil.parse(method.getCompilationUnit(), methodStart, methodLength);
         MethodBodySearcher searcher = new MethodBodySearcher(methodStart, methodLength);
         node.accept(searcher);
         return searcher.getResult();
      }
      else {
         return null;
      }
   }
   
   /**
    * Utility class used by {@link JDTUtil#getMethodBody(IMethod)}
    * to compute the result.
    * @author Martin Hentschel
    */
   private static class MethodBodySearcher extends ASTVisitor {
      /**
       * The result.
       */
      private Block result;
      
      /**
       * The start index of the method.
       */
      private int methodStart;
      
      /**
       * The end index of the method.
       */
      private int methodEnd;
      
      /**
       * Constructor.
       * @param methodStart The start index of the method.
       * @param methodLength The end index of the method.
       */
      public MethodBodySearcher(int methodStart, int methodLength) {
         this.methodStart = methodStart;
         this.methodEnd = methodStart + methodLength;
      }
      
      /**
       * {@inheritDoc}
       */
      @Override
      public boolean visit(Block node) {
         if (node.getStartPosition() >= methodStart &&
             node.getStartPosition() + node.getLength() <= methodEnd) {
            result = node;
            return false;
         }
         else {
            return true;
         }
      }

      /**
       * Returns the result.
       * @return The result.
       */
      public Block getResult() {
         return result;
      }
   }

   /**
    * Searches the {@link IMethod} with the given name and full qualified parameter types.
    * @param jdtType The {@link IType} which provides the available methods.
    * @param name The name of the methods.
    * @param parameterTypes The full qualified parameter types.
    * @return The found {@link IMethod} if available or {@code null} otherwise.
    * @throws JavaModelException Occurred Exception.
    */
   public static IMethod findJDTMethod(final IType jdtType, final String name, final String[] parameterTypes) throws JavaModelException {
      try {
         if (jdtType != null) {
            IMethod[] methods = jdtType.getMethods();
            return ArrayUtil.search(methods, new IFilter<IMethod>() {
               @Override
               public boolean select(IMethod element) {
                  try {
                     if (ObjectUtil.equals(name, element.getElementName())) {
                        String[] parameters = element.getParameterTypes();
                        if (parameters.length == parameterTypes.length) {
                           boolean parametersMatches = true;
                           int i = 0;
                           while (parametersMatches && i < parameters.length) {
                              String resolvedType = resolveTypeSignature(parameters[i], jdtType);
                              if (!ObjectUtil.equals(resolvedType, parameterTypes[i])) {
                                 parametersMatches = false;
                              }
                              i++;
                           }
                           return parametersMatches;
                        }
                        else {
                           return false;
                        }
                     }
                     else {
                        return false;
                     }
                  }
                  catch (JavaModelException e) {
                     throw new RuntimeException(e);
                  }
               }
            });
         }
         else {
            return null;
         }
      }
      catch (RuntimeException e) {
         throw (JavaModelException)e.getCause();
      }
   }
   
   /**
    * Resolves the given type signature.
    * @param refTypeSig The type signature to resolve.
    * @param declaringType The declaring {@link IType}.
    * @return The full qualified name of the type signature.
    * @throws JavaModelException Occurred Exception.
    */
   public static String resolveTypeSignature(String refTypeSig, IType declaringType) throws JavaModelException {
      String type = JavaModelUtil.getResolvedTypeName(refTypeSig, declaringType);
      int arrayCount = Signature.getArrayCount(refTypeSig);
      StringBuffer sb = new StringBuffer();
      sb.append(type);
      for (int i = 0; i < arrayCount; i++) {
         sb.append("[]");
      }
      return sb.toString();
   }

   /**
    * Returns the full qualified type name of the given {@link IType}.
    * @param type The {@link IType}.
    * @return The full qualified {@link IType} or {@code null} if no {@link IType} is defined.
    */
   public static String getQualifiedTypeName(IType type) {
      if (type != null) {
         StringBuffer sb = new StringBuffer();
         JavaElementLabelComposerHelper c = new JavaElementLabelComposerHelper(sb, type.getDeclaringType());
         c.appendTypeLabel(type, JavaElementLabels.T_FULLY_QUALIFIED);
         return sb.toString();
      }
      else {
         return null;
      }
   }

   /**
    * Builds the given {@link IJavaProject} in the background {@link Job}.
    * @param project The {@link IJavaProject} to build in background.
    */
   public static void buildInBackground(IJavaProject javaProject) {
      buildInBackground(javaProject.getProject());
   }

   /**
    * Builds the given {@link IProject} in the background {@link Job}.
    * @param project The {@link IProject} to build in background.
    */
   public static void buildInBackground(IProject project) {
      CoreUtility.startBuildInBackground(project);
   }

   /**
    * Ensures that the given name is a valid Java type name by replacing
    * all invalid characters with {@code '_'}.
    * @param name The name to validate.
    * @param project The {@link IJavaProject} in which the name will be used.
    * @return The validated name.
    */
   public static String ensureValidJavaTypeName(String name, IJavaProject project) {
      if (name != null) {
         StringBuffer sb = new StringBuffer();
         char[] characters = name.toCharArray();
         for (int i = 0; i < characters.length; i++) {
            String nameToValidate = sb.toString() + characters[i];
            IStatus status = project != null ?
                             JavaConventionsUtil.validateJavaTypeName(nameToValidate, project) :
                             JavaConventions.validateJavaTypeName(nameToValidate, JavaModelUtil.VERSION_LATEST, JavaModelUtil.VERSION_LATEST);
            if (status.isOK() || (status.getSeverity() == IStatus.WARNING && characters[i] != '.')) {
               sb.append(characters[i]);
            }
            else {
               sb.append("_");
            }
         }
         return sb.toString();
      }
      else {
         return null;
      }
   }
   
   
   /**
    * Returns all {@link IPackageFragmentRoot}s of the given {@link IJavaProject}.
    * @param project The {@link IJavaProject} to list its {@link IPackageFragmentRoot}s.
    * @return The found {@link IPackageFragmentRoot}s.
    * @throws JavaModelException Occurred Exception.
    */
   public static List<IPackageFragmentRoot> getSourceFolders(IJavaProject project) throws JavaModelException {
      List<IPackageFragmentRoot> result = new LinkedList<IPackageFragmentRoot>();
      if (project != null) {
         IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
         for (IPackageFragmentRoot root : roots) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
               result.add(root);
            }
         }
      }
      return result;
   }
   
   /**
    * Lists all {@link ICompilationUnit}s recursively within given {@link IJavaElement}s
    * @param javaElements The {@link IJavaElement}s to list its contained {@link ICompilationUnit}s.
    * @return The recursively found {@link ICompilationUnit}s.
    * @throws JavaModelException Occurred Exception.
    */
   public static List<ICompilationUnit> listCompilationUnit(List<? extends IJavaElement> javaElements) throws JavaModelException{
      List<ICompilationUnit> result = new LinkedList<ICompilationUnit>();
      if (javaElements != null) {
         for (IJavaElement root : javaElements) {
            findCompilationUnitsRecursively(root, result);
         }
      }
      return result;
   }
   
   /**
    * Utility method used by {@link #listCompilationUnit(List)}.
    * @param javaElement The current {@link IJavaElement} to search in.
    * @param toFill The result {@link List} to fill.
    * @throws JavaModelException Occurred Exception.
    */
   private static void findCompilationUnitsRecursively(IJavaElement javaElement, List<ICompilationUnit> toFill) throws JavaModelException {
      if (javaElement instanceof ICompilationUnit) {
         toFill.add((ICompilationUnit) javaElement);
      }
      else if (javaElement instanceof IParent) {
         IJavaElement[] children = ((IParent) javaElement).getChildren();
         for (IJavaElement child : children) {
            findCompilationUnitsRecursively(child, toFill);
         }
      }
   }
   
   /**
    * Parses the given {@link ICompilationUnit}.
    * @param compilationUnit The {@link ICompilationUnit} to parse.
    * @return The parsed {@link ASTNode} or {@code null} if no {@link ICompilationUnit} is defined.
    */
   public static ASTNode parse(ICompilationUnit compilationUnit) {
      if (compilationUnit != null) {
         ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
         parser.setResolveBindings(true);
         parser.setSource(compilationUnit);
         Map<String, String> options = JavaCore.getOptions();
         JavaCore.setComplianceOptions(JavaModelUtil.VERSION_LATEST, options);
         parser.setCompilerOptions(options);
         ASTNode result = parser.createAST(null);
         return result;
      }
      else {
         return null;
      }
   }
   
   /**
    * Parses the given {@link IClassFile}.
    * @param classFile The {@link IClassFile} to parse.
    * @return The parsed {@link ASTNode} or {@code null} if no {@link ICompilationUnit} is defined.
    */
   public static ASTNode parse(IClassFile classFile) {
      if (classFile != null) {
         ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
         parser.setResolveBindings(true);
         parser.setSource(classFile);
         Map<String, String> options = JavaCore.getOptions();
         JavaCore.setComplianceOptions(JavaModelUtil.VERSION_LATEST, options);
         parser.setCompilerOptions(options);
         ASTNode result = parser.createAST(null);
         return result;
      }
      else {
         return null;
      }
   }
   
   /**
    * Parses the given {@link ICompilationUnit}.
    * @param in The {@link InputStream} to parse.
    * @return The parsed {@link ASTNode} or {@code null} if no {@link ICompilationUnit} is defined.
    * @throws IOException Occurred Exception
    */
   public static ASTNode parse(InputStream in) throws IOException {
      String content = IOUtil.readFrom(in);
      return parse(content);
   }
   
   /**
    * Parses the given {@link ICompilationUnit}.
    * @param content The {@link String} to parse.
    * @return The parsed {@link ASTNode} or {@code null} if no {@link ICompilationUnit} is defined.
    */
   public static ASTNode parse(String content) {
      return parse(content, ASTParser.K_COMPILATION_UNIT);
   }
   
   /**
    * Parses the given {@link ICompilationUnit}.
    * @param content The {@link String} to parse.
    * @return The parsed {@link ASTNode} or {@code null} if no {@link ICompilationUnit} is defined.
    */
   public static ASTNode parse(String content, int kind) {
      if (content != null) {
         ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
         parser.setKind(kind);
         parser.setResolveBindings(true);
         parser.setSource(content.toCharArray());
         Map<String, String> options = JavaCore.getOptions();
         JavaCore.setComplianceOptions(JavaModelUtil.VERSION_LATEST, options);
         parser.setCompilerOptions(options);
         ASTNode result = parser.createAST(null);
         return result;
      }
      else {
         return null;
      }
   }

   /**
    * Lists all Java container (JRE) of the given {@link IJavaProject}s.
    * @param javaProject The {@link IJavaProject}.
    * @return The descriptions of the avilable Java container.
    * @throws JavaModelException Occurred Exception.
    */
   public static List<String> getJavaContainerDescriptions(IJavaProject javaProject) throws JavaModelException {
      List<String> result = new LinkedList<String>();
      if (javaProject != null && javaProject.isOpen()) {
         IClasspathEntry[] cps = javaProject.getRawClasspath();
         for (IClasspathEntry entry : cps) {
            if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
               IClasspathContainer container= JavaCore.getClasspathContainer(entry.getPath(), javaProject);
               result.add(container.getDescription());
            }
         }
      }
      return result;
   }

   /**
    * Returns the shared {@link CompilationUnit} of the given {@link IEditorPart}.
    * <p>
    * Synchronization is not performed!
    * @param editorPart The {@link IEditorPart}.
    * @return The shared {@link CompilationUnit} if available.
    */
   public static CompilationUnit getSharedCompilationUnit(IEditorPart editorPart) {
      if (editorPart != null) {
         ITypeRoot typeRoot = EditorUtility.getEditorInputJavaElement(editorPart, false);
         if (typeRoot != null) {
            return SharedASTProvider.getAST(typeRoot, SharedASTProvider.WAIT_NO, null);
         }
         else {
            return null;
         }
      }
      else {
         return null;
      }
   }
}