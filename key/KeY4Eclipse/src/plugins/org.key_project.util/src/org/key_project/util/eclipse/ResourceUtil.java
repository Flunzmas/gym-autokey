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

package org.key_project.util.eclipse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.key_project.util.Activator;
import org.key_project.util.java.IOUtil;
import org.key_project.util.java.StringUtil;

/**
 * Provides static methods to work with workspace resources.
 * @author Martin Hentschel
 */
public class ResourceUtil {
   /**
    * Forbid instances by this private constructor.
    */
   private ResourceUtil() {
   }
   
   /**
    * Encodes the given path as URI path by escaping special characters
    * like spaces.
    * @param path The path to encode.
    * @return The encoded URI path.
    */
   public static String encodeURIPath(String path) {
      if (path != null) {
         org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createFileURI(path);
         return uri.devicePath();
      }
      else {
         return null;
      }
   }

   /**
    * Decodes the given URI path by interpreting three-digit escape sequences as 
    * the bytes of a UTF-8 encoded character and replacing them with the 
    * characters they represent. Incomplete escape sequences are ignored and 
    * invalid UTF-8 encoded bytes are treated as extended ASCII characters.
    * @param uriPath The URI path to decode.
    * @return The decoded URI path.
    */
   public static String decodeURIPath(String uriPath) {
      if (uriPath != null) {
         return org.eclipse.emf.common.util.URI.decode(uriPath);
      }
      else {
         return null;
      }
   }
   
   /**
    * Ensures that the segment is a valid workspace path segment meaning
    * that it is a valid file/folder name. Each invalid sign will be replaced
    * by {@code '_'}.
    * @param segment The segment to validate.
    * @return The validated workspace path segment in which each invalid sign is replaced.
    */
   public static String validateWorkspaceFileName(String name) {
      if (name != null) {
         for (int i = 1; i <= name.length(); i++) {
            String tmp = name.substring(0, i);
            IStatus status = ResourcesPlugin.getWorkspace().validateName(tmp, IResource.FILE);
            if (!status.isOK()) {
               StringBuilder strbuilder = new StringBuilder(name);
               strbuilder.setCharAt(i - 1, '_');
               name = strbuilder.toString();
            }
         }
         return name;
      }
      else {
         return name;
      }
   }

   /**
    * Returns the file name without file extension for the given {@link IFile}.
    * @param file The {@link IFile} for that the file name without extension is needed.
    * @return The file name without extension or {@code null} if it was not possible to compute it.
    */
   public static String getFileNameWithoutExtension(IFile file) {
      if (file != null) {
         return IOUtil.getFileNameWithoutExtension(file.getName());
      }
      else {
         return null;
      }
   }
   
   /**
    * Returns all workspace {@link IResource}s that represents the given
    * {@link File} in the local file system.
    * @param location The file or folder in the local file system.
    * @return The found workspace {@link IResource}s.
    */
   public static IResource[] findResourceForLocation(File location) {
      if (location != null) {
         URI uri = location.toURI();
         if (location.isFile()) {
            return ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
         }
         else {
            return ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(uri);
         }
      }
      else {
         return new IContainer[0];
      }
   }
   
   /**
    * Returns the local location on the hard discs for the {@link IResource}.
    * @param resource The {@link IResource}.
    * @return The local location for the {@link IResource} or {@code null} if the {@link IResource} is {@code null} or if the {@link IResource} is not local.
    */
   public static File getLocation(IResource resource) {
      File location = null;
      if (resource != null && resource.getLocationURI() != null) {
         location = new File(resource.getLocationURI());
      }
      return location;
   }

   /**
    * <p>
    * Returns the {@link Image} for the given {@link IResource}.
    * </p>
    * <p>
    * <b>Attention: </b> The caller is responsible to dispose the created {@link Image}!
    * </p>
    * @param resource The {@link IResource} for that an {@link Image} is needed.
    * @return The found {@link Image} or {@code null} if no one can be found.
    */
   public static Image getImage(IResource resource) {
      if (resource != null) {
          IWorkbenchAdapter adapter = (IWorkbenchAdapter)resource.getAdapter(IWorkbenchAdapter.class);
          ImageDescriptor id = adapter.getImageDescriptor(resource);
          return id.createImage();
      }
      else {
         return null;
      }
   }

   /**
    * Returns the {@link IProject} for the given name. If no project with
    * the name an {@link IProject} reference is returned, 
    * but {@link IProject#exists()} is still {@code false}.
    * @param projectName The name of the project.
    * @return The found {@link IProject} or {@code null} if the project name is {@code null}/empty.
    */
   public static IProject getProject(String projectName) {
      if (!StringUtil.isTrimmedEmpty(projectName)) {
         return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      }
      else {
         return null;
      }
   }

   /**
    * <p>
    * Copies the given {@link File}s into the specified {@link IContainer}.
    * </p>
    * <p>
    * <b>Attention: </b> Existing files will be overwritten!
    * </p>
    * @param target The {@link IContainer} to copy to.
    * @param opener An optional {@link IFileOpener} which can be used to modify the content of files during the copy process.
    * @param source The {@link File} to copy.
    * @throws CoreException Occurred Exception.
    */
   public static void copyIntoWorkspace(IContainer target, IFileOpener opener, File startDirectory, Collection<File> source) throws CoreException {
      if (source != null) {
         copyIntoWorkspace(target, opener, startDirectory, source.toArray(new File[source.size()]));
      }
   }

   /**
    * <p>
    * Copies the given {@link File}s into the specified {@link IContainer}.
    * </p>
    * <p>
    * <b>Attention: </b> Existing files will be overwritten!
    * </p>
    * @param target The {@link IContainer} to copy to.
    * @param opener An optional {@link IFileOpener} which can be used to modify the content of files during the copy process.
    * @param source The {@link File} to copy.
    * @throws CoreException Occurred Exception.
    */
   public static void copyIntoWorkspace(IContainer target, IFileOpener opener, File startDirectory, File... source) throws CoreException {
      try {
         if (source != null) {
            if (target == null) {
               throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Target not defined.", null));
            }
            if (!target.exists()) {
               throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Target \"" + target + "\" does not exist.", null));
            }
            if (opener == null) {
               opener = new DefaultFileOpener();
            }
            for (File file : source) {
               if (file != null) {
                  IContainer fileTarget = target;
                  if (IOUtil.contains(startDirectory, file)) {
                     Deque<File> parentsBelowStartDirectory = new LinkedList<File>();
                     File parentFile = file.getParentFile();
                     while (!startDirectory.equals(parentFile)) {
                        parentsBelowStartDirectory.addFirst(parentFile);
                        parentFile = parentFile.getParentFile();
                     }
                     for (File parentDirectory : parentsBelowStartDirectory) {
                        IFolder subFolder = fileTarget.getFolder(new Path(parentDirectory.getName()));
                        if (!subFolder.exists()) {
                           subFolder.create(true, true, null);
                        }
                        fileTarget = subFolder;
                     }
                  }
                  if (file.isFile()) {
                     IFile targetFile = fileTarget.getFile(new Path(file.getName()));
                     createFile(targetFile, opener.open(file), null);
                  }
                  else {
                     IFolder targetFolder = fileTarget.getFolder(new Path(file.getName()));
                     if (!targetFolder.exists()) {
                        targetFolder.create(true, true, null);
                     }
                     copyIntoWorkspace(targetFolder, opener, file, file.listFiles());
                  }
               }
            }
         }
      }
      catch (IOException e) {
         throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
      }
   }
   
   /**
    * Instances of this class are used to open an {@link InputStream} of a {@link File}.
    * @author Martin Hentschel
    */
   public static interface IFileOpener {
      /**
       * Opens the {@link InputStream} for the given {@link File}.
       * @param file The {@link File} to open.
       * @return The {@link InputStream} for the given {@link File}.
       * @throws IOException Occurred Exception.
       */
      public InputStream open(File file) throws IOException;
   }
   
   /**
    * Default implementation of {@link IFileOpener}.
    * @author Martin Hentschel
    */
   public static class DefaultFileOpener implements IFileOpener {
      /**
       * {@inheritDoc}
       */
      @Override
      public InputStream open(File file) throws IOException {
         return new FileInputStream(file);
      }
   }

   /**
    * <p>
    * Creates the given {@link IFile} if not already present or changes
    * it contents otherwise.
    * </p>
    * <p>
    * <b>Attention: </b> Existing files will be overwritten!
    * </p>
    * @param file The {@link IFile} to create or to change its content.
    * @param in The {@link InputStream} which provides the new content.
    * @param monitor An optional {@link IProgressMonitor} to use.
    * @throws CoreException Occurred Exception.
    */
   public static void createFile(IFile file, InputStream in, IProgressMonitor monitor) throws CoreException {
      try {
         if (file != null) {
            if (in == null) {
               in = new ByteArrayInputStream(new byte[0]);
            }
            if (file.exists()) {
               file.setContents(in, true, true, monitor);
            }
            else {
               file.create(in, true, monitor);
            }
         }
      }
      finally {
         try {
            if (in != null) {
               in.close();
            }
         }
         catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
         }
      }
   }
   
   /**
    * Reads the complete content from the {@link IFile}.
    * @param file The {@link IFile} to read from.
    * @return The read content or {@code null} if the {@link IFile} is {@code null} or don't exist.
    * @throws CoreException Occurred Exception.
    */
   public static String readFrom(IFile file) throws CoreException {
      try {
         if (file != null && file.exists()) {
            InputStream content = file.getContents();
            return IOUtil.readFrom(content);
         }
         else {
            return null;
         }
      }
      catch (IOException e) {
         throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
      }
   }
   
   /**
    * Computes the MD5 Sum for the given {@link IFile} content.
    * @param iFile - the {@link IFile} to use
    * @return the MD5 Sum as {@link String}
    * @throws CoreException 
    * @throws IOException 
    */
   public static String computeContentMD5(IFile iFile) throws IOException, CoreException{
      return IOUtil.computeMD5(iFile.getContents());
   }

   /**
    * Copies the {@link IResource} to the given target {@link File}.
    * @param source The {@link IFile} to copy.
    * @param target The target {@link File} to copy to.
    * @return {@code true} if copy was performed and {@code false} if not performed.
    * @throws CoreException Occurred Exception
    */
   public static boolean copyIntoFileSystem(IResource resource, File target) throws CoreException {
      try {
         if (target != null) {
            if (resource instanceof IFile) {
               return IOUtil.copy(((IFile) resource).getContents(), new FileOutputStream(target));
            }
            else if (resource instanceof IContainer) {
               boolean done = target.exists() ? 
                              true :
                              target.mkdirs();
               IResource[] members = ((IContainer) resource).members();
               if (members != null) {
                  for (IResource member : members) {
                     if (!copyIntoFileSystem(member, new File(target, member.getName()))) {
                        done = false;
                     }
                  }
               }
               return done;
            }
            else {
               return false;
            }
         }
         else {
            return false;
         }
      }
      catch (IOException e) {
         throw new CoreException(new Logger(Activator.getDefault(), Activator.PLUGIN_ID).createErrorStatus(e));
      }
   }
   
   /**
    * Returns the location of the workspace.
    * @return The location of the workspace.
    */
   public static File getWorkspaceLocation() {
      return getLocation(ResourcesPlugin.getWorkspace().getRoot());
   }

   /**
    * Creates an {@link IProject} with the given name and ensures that it is open.
    * @param projectName The Name of the {@link IProject} to create.
    * @return The open {@link IProject}.
    * @throws CoreException Occurred Exception.
    */
   public static IProject createProject(String projectName) throws CoreException {
      IProject project = getProject(projectName);
      if (project != null) {
         if (!project.exists()) {
            project.create(null);
         }
         if (!project.isOpen()) {
            project.open(null);
         }
      }
      return project;
   }
   
   /**
    * Creates an {@link IFolder} in the {@link IContainer} with the given folder name.
    * @param parent The {@link IContainer} to create folder in.
    * @param folderName The name of the folder to create.
    * @return The created or existing folder.
    * @throws CoreException Occurred Exception.
    */
   public static IFolder createFolder(IContainer parent, String folderName) throws CoreException {
      if (parent != null && !StringUtil.isTrimmedEmpty(folderName)) {
         IFolder folder = parent.getFolder(new Path(folderName));
         if (!folder.exists()) {
            folder.create(true, true, null);
         }
         return folder;
      }
      else {
         return null;
      }
   }

   /**
    * Ensures that the given {@link IContainer} exists.
    * @param container The {@link IContainer} to check.
    * @return {@code true} if {@link IContainer} exists and {@code false} otherwise.
    * @throws CoreException Occurred Exception.
    */
   public static boolean ensureExists(IContainer container) throws CoreException {
      if (container instanceof IFolder) {
         if (!container.exists()) { 
            if (ensureExists(container.getParent())) {
               ((IFolder) container).create(true, true, null);
               return true;
            }
            else {
               return false;
            }
         }
         else {
            return true;
         }
      }
      else if (container instanceof IProject) {
         IProject project = (IProject) container;
         if (!project.exists()) {
            project.create(null);
         }
         if (!project.isOpen()) {
            project.open(null);
         }
         return true;
      }
      else {
         return false;
      }
   }
}