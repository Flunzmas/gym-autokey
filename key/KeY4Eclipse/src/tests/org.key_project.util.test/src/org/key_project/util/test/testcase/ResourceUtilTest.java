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

package org.key_project.util.test.testcase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.key_project.util.eclipse.ResourceUtil;
import org.key_project.util.eclipse.ResourceUtil.IFileOpener;
import org.key_project.util.java.ArrayUtil;
import org.key_project.util.java.IOUtil;
import org.key_project.util.java.StringUtil;
import org.key_project.util.test.util.TestUtilsUtil;

/**
 * Contains tests for {@link ResourceUtil}
 * @author Martin Hentschel
 */
public class ResourceUtilTest extends TestCase {
   /**
    * {@link ResourceUtil#ensureExists(org.eclipse.core.resources.IContainer)}.
    * @throws CoreException 
    */
   @Test
   public void testEnsureExists() throws CoreException {
      // Test null
      assertFalse(ResourceUtil.ensureExists(null));
      // Test not existing project
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("ResourceUtilTest_testEnsureExists");
      assertTrue(ResourceUtil.ensureExists(project));
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      // Test existing project
      assertTrue(ResourceUtil.ensureExists(project));
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      // Test not existing folder
      project.delete(true, null);
      IFolder folder = project.getFolder("folder");
      assertTrue(ResourceUtil.ensureExists(folder));
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      assertTrue(folder.exists());
      // Test existing folder
      assertTrue(ResourceUtil.ensureExists(folder));
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      assertTrue(folder.exists());
      // Test not existing sub folder
      IFolder subFolder = folder.getFolder("subFolder");
      assertTrue(ResourceUtil.ensureExists(subFolder));
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      assertTrue(folder.exists());
      assertTrue(subFolder.exists());
      // Test existing sub folder
      assertTrue(ResourceUtil.ensureExists(subFolder));
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      assertTrue(folder.exists());
      assertTrue(subFolder.exists());
      // Test not existing project
      project.delete(true, null);
      assertTrue(ResourceUtil.ensureExists(subFolder));
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      assertTrue(folder.exists());
      assertTrue(subFolder.exists());
   }
   
   /**
    * {@link ResourceUtil#encodeURIPath(String)}.
    */
   @Test
   public void testEncodeURIPath() {
      // Test null
      assertEquals(null, ResourceUtil.encodeURIPath(null));
      // Test normal URIs
      assertEquals("A", ResourceUtil.encodeURIPath("A"));
      assertEquals("A/B", ResourceUtil.encodeURIPath("A/B"));
      assertEquals("/Users/MyName/MyFile.html", ResourceUtil.encodeURIPath("/Users/MyName/MyFile.html"));
      assertEquals("C:/Users/MyName/MyFile.html", ResourceUtil.encodeURIPath("C:\\Users\\MyName\\MyFile.html"));
      assertEquals("./MyName/MyFile.html", ResourceUtil.encodeURIPath(".\\MyName\\MyFile.html"));
      assertEquals("./MyName/MyFile.html", ResourceUtil.encodeURIPath("./MyName/MyFile.html"));
      assertEquals("../../MyName/MyFile.html", ResourceUtil.encodeURIPath("..\\..\\MyName\\MyFile.html"));
      assertEquals("../../MyName/MyFile.html", ResourceUtil.encodeURIPath("../../MyName/MyFile.html"));
      // Test URIs with spaces
      assertEquals("/Users/My%20Name/My%20File.html", ResourceUtil.encodeURIPath("/Users/My Name/My File.html"));
      assertEquals("C:/Users/My%20Name/My%20File.html", ResourceUtil.encodeURIPath("C:\\Users\\My Name\\My File.html"));
   }
   
   /**
    * {@link ResourceUtil#decodeURIPath(String)}.
    */
   @Test
   public void testDecodeURIPath() {
      // Test null
      assertEquals(null, ResourceUtil.decodeURIPath(null));
      // Test normal URIs
      assertEquals("A", ResourceUtil.decodeURIPath("A"));
      assertEquals("A/B", ResourceUtil.decodeURIPath("A/B"));
      assertEquals("/Users/MyName/MyFile.html", ResourceUtil.decodeURIPath("/Users/MyName/MyFile.html"));
      assertEquals("C:/Users/MyName/MyFile.html", ResourceUtil.decodeURIPath("C:/Users/MyName/MyFile.html"));
      assertEquals("./MyName/MyFile.html", ResourceUtil.decodeURIPath("./MyName/MyFile.html"));
      assertEquals("../../MyName/MyFile.html", ResourceUtil.decodeURIPath("../../MyName/MyFile.html"));
      // Test URIs with spaces
      assertEquals("/Users/My Name/My File.html", ResourceUtil.decodeURIPath("/Users/My%20Name/My%20File.html"));
      assertEquals("C:/Users/My Name/My File.html", ResourceUtil.decodeURIPath("C:/Users/My%20Name/My%20File.html"));
   }
   
   /**
    * Tests {@link ResourceUtil#createFolder(org.eclipse.core.resources.IContainer, String)}.
    */
   @Test
   public void testCreateFolder() throws CoreException {
      IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testCreateFolder");
      // Test invalid parameter
      IFolder folder = ResourceUtil.createFolder(null, null);
      assertNull(folder);
      folder = ResourceUtil.createFolder(project, "");
      assertNull(folder);
      folder = ResourceUtil.createFolder(null, "myFolder");
      assertNull(folder);
      // Test not existing folder
      folder = ResourceUtil.createFolder(project, "myFolder");
      assertNotNull(folder);
      assertTrue(folder.exists());
      // Test existing folder
      folder = ResourceUtil.createFolder(project, "myFolder");
      assertNotNull(folder);
      assertTrue(folder.exists());
   }
   
   /**
    * Tests {@link ResourceUtil#createProject(String)}.
    */
   @Test
   public void testCreateProject() throws CoreException {
      // Test null
      IProject project = ResourceUtil.createProject(null);
      assertNull(project);
      // Test not existing project
      project = ResourceUtil.createProject("ResourceUtilTest_testCreateProject");
      assertNotNull(project);
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      // Test existing but closed project
      project.close(null);
      project = ResourceUtil.createProject("ResourceUtilTest_testCreateProject");
      assertNotNull(project);
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      // Test existing and open project
      project = ResourceUtil.createProject("ResourceUtilTest_testCreateProject");
      assertNotNull(project);
      assertTrue(project.exists());
      assertTrue(project.isOpen());
      // Test project recreation
      project.delete(true, null);
      project = ResourceUtil.createProject("ResourceUtilTest_testCreateProject");
      assertNotNull(project);
      assertTrue(project.exists());
      assertTrue(project.isOpen());
   }
   
   /**
    * Tests {@link ResourceUtil#getWorkspaceLocation()}
    * @throws CoreException Occurred Exception
    */
   @Test
   public void testGetWorkspaceLocation() throws CoreException {
      File location = ResourceUtil.getWorkspaceLocation();
      assertNotNull(location);
      assertTrue(location.isDirectory());
      // Make sure that test project does not exist
      String projectName = "ResourceUtilTest_testGetWorkspaceLocation";
      File projectLocation = new File(location, projectName);
      assertFalse(projectLocation.isDirectory());
      // Create project
      IProject project = TestUtilsUtil.createProject(projectName);
      assertTrue(projectLocation.isDirectory());
      // Delete project
      project.delete(true, null);
      assertFalse(projectLocation.isDirectory());
   }
   
   /**
    * Tests {@link ResourceUtil#validateWorkspaceFileName(String)}
    */
   @Test
   public void testValidateWorkspaceFileName() {
      assertEquals(null, ResourceUtil.validateWorkspaceFileName(null));
      assertEquals("Hello_World", ResourceUtil.validateWorkspaceFileName("Hello World"));
      assertEquals("Hello_World_txt", ResourceUtil.validateWorkspaceFileName("Hello World.txt"));
      assertEquals("Hello__World_txt", ResourceUtil.validateWorkspaceFileName("Hello<>World.txt"));
      assertEquals("_Hello_World___txt_", ResourceUtil.validateWorkspaceFileName(".Hello.World...txt."));
   }
   
   /**
    * Tests {@link ResourceUtil#copyIntoWorkspace(org.eclipse.core.resources.IContainer, File...)}.
    */
   @Test
   public void testCopyIntoWorkspace() throws Exception {
      // Create project
      IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testCopyIntoWorkspace");
      IFile projectFile = project.getFile(".project");
      assertTrue(projectFile.exists());
      assertEquals(1, project.members().length); // .project file
      IFolder notExistingFolder = project.getFolder("NotExisting");
      assertFalse(notExistingFolder.exists());
      // Create files to copy in a temporary directory
      File tempDir = IOUtil.createTempDirectory("ResourceUtilTest", "testCopyIntoWorkspace");
      try {
         new File(tempDir, "emptyFolder").mkdirs();
         IOUtil.writeTo(new FileOutputStream(new File(tempDir, "Text.txt")), "Text.txt");
         File subDir = new File(tempDir, "subFolder");
         subDir.mkdirs();
         IOUtil.writeTo(new FileOutputStream(new File(subDir, "SubFile.txt")), "SubFile.txt");
         File subSubDir = new File(subDir, "subSubFolder");
         subSubDir.mkdirs();
         IOUtil.writeTo(new FileOutputStream(new File(subSubDir, "SubSubFileA.txt")), "SubSubFileA.txt");
         IOUtil.writeTo(new FileOutputStream(new File(subSubDir, "SubSubFileB.txt")), "SubSubFileB.txt");
         // Test null target
         try {
            ResourceUtil.copyIntoWorkspace(null, null, tempDir);
            fail("Exception expected");
         }
         catch (Exception e) {
            assertEquals("Target not defined.", e.getMessage());
         }
         // Test not existing target
         try {
            ResourceUtil.copyIntoWorkspace(notExistingFolder, null, tempDir);
            fail("Exception expected");
         }
         catch (Exception e) {
            assertEquals("Target \"" + notExistingFolder + "\" does not exist.", e.getMessage());
         }
         // Test null source
         ResourceUtil.copyIntoWorkspace(project, null, null, (File[])null);
         ResourceUtil.copyIntoWorkspace(project, null, null, (File)null);
         assertEquals(1, project.members().length); // .project file
         assertTrue(projectFile.exists());
         // Copy initial content
         ResourceUtil.copyIntoWorkspace(project, null, null, tempDir.listFiles());
         assertEquals(4, project.members().length);
         assertTrue(projectFile.exists());
         IFolder targetEmptyFolder = project.getFolder("emptyFolder");
         assertTrue(targetEmptyFolder.exists());
         assertEquals(0, targetEmptyFolder.members().length);
         IFile targetText = project.getFile("Text.txt");
         assertEquals("Text.txt", ResourceUtil.readFrom(targetText));
         IFolder targetSubDir = project.getFolder("subFolder");
         assertTrue(targetSubDir.exists());
         assertEquals(2, targetSubDir.members().length);
         IFile targetSubFile = targetSubDir.getFile("SubFile.txt");
         assertEquals("SubFile.txt", ResourceUtil.readFrom(targetSubFile));
         IFolder targetSubSubDir = targetSubDir.getFolder("subSubFolder"); 
         assertTrue(targetSubSubDir.exists());
         assertEquals(2, targetSubSubDir.members().length);
         IFile targetSubSubFileA = targetSubSubDir.getFile("SubSubFileA.txt");
         assertEquals("SubSubFileA.txt", ResourceUtil.readFrom(targetSubSubFileA));
         IFile targetSubSubFileB = targetSubSubDir.getFile("SubSubFileB.txt");
         assertEquals("SubSubFileB.txt", ResourceUtil.readFrom(targetSubSubFileB));
         // Test different source directories
         IProject projectSubSubDir = TestUtilsUtil.createProject("ResourceUtilTest_testCopyIntoWorkspace_sourceDirectory0");
         ResourceUtil.copyIntoWorkspace(projectSubSubDir, null, subSubDir, subSubDir.listFiles());
         assertEquals(2, targetSubSubDir.members().length);
         assertEquals("SubSubFileA.txt", ResourceUtil.readFrom(projectSubSubDir.getFile("SubSubFileA.txt")));
         assertEquals("SubSubFileB.txt", ResourceUtil.readFrom(projectSubSubDir.getFile("SubSubFileB.txt")));
         IProject projectSubDir = TestUtilsUtil.createProject("ResourceUtilTest_testCopyIntoWorkspace_sourceDirectory1");
         ResourceUtil.copyIntoWorkspace(projectSubDir, null, subDir, subSubDir.listFiles());
         assertEquals(2, projectSubDir.members().length);
         assertEquals("SubSubFileA.txt", ResourceUtil.readFrom(projectSubDir.getFile(new Path("subSubFolder/SubSubFileA.txt"))));
         assertEquals("SubSubFileB.txt", ResourceUtil.readFrom(projectSubDir.getFile(new Path("subSubFolder/SubSubFileB.txt"))));
         IProject projectTmpDir = TestUtilsUtil.createProject("ResourceUtilTest_testCopyIntoWorkspace_sourceDirectory2");
         ResourceUtil.copyIntoWorkspace(projectTmpDir, null, tempDir, subSubDir.listFiles());
         assertEquals(2, projectTmpDir.members().length);
         assertEquals("SubSubFileA.txt", ResourceUtil.readFrom(projectTmpDir.getFile(new Path("subFolder/subSubFolder/SubSubFileA.txt"))));
         assertEquals("SubSubFileB.txt", ResourceUtil.readFrom(projectTmpDir.getFile(new Path("subFolder/subSubFolder/SubSubFileB.txt"))));
         // Prepare temporary directory for adding new files and folders
         IOUtil.delete(tempDir);
         new File(tempDir, "newEmptyFolder").mkdirs();
         IOUtil.writeTo(new FileOutputStream(new File(tempDir, "NewText.txt")), "NewText.txt");
         File newSubDir = new File(tempDir, "newSubFolder");
         newSubDir.mkdirs();
         IOUtil.writeTo(new FileOutputStream(new File(newSubDir, "NewSubFile.txt")), "NewSubFile.txt");
         // Add new content
         ResourceUtil.copyIntoWorkspace(project, null, null, tempDir.listFiles());
         assertEquals(7, project.members().length);
         assertTrue(projectFile.exists());
         assertTrue(targetEmptyFolder.exists());
         assertEquals(0, targetEmptyFolder.members().length);
         assertEquals("Text.txt", ResourceUtil.readFrom(targetText));
         assertTrue(targetSubDir.exists());
         assertEquals(2, targetSubDir.members().length);
         assertEquals("SubFile.txt", ResourceUtil.readFrom(targetSubFile));
         assertTrue(targetSubSubDir.exists());
         assertEquals(2, targetSubSubDir.members().length);
         assertEquals("SubSubFileA.txt", ResourceUtil.readFrom(targetSubSubFileA));
         assertEquals("SubSubFileB.txt", ResourceUtil.readFrom(targetSubSubFileB));
         IFolder targetNewEmptyFolder = project.getFolder("newEmptyFolder");
         assertTrue(targetNewEmptyFolder.exists());
         assertEquals(0, targetNewEmptyFolder.members().length);
         IFile targetNewText = project.getFile("NewText.txt");
         assertEquals("NewText.txt", ResourceUtil.readFrom(targetNewText));
         IFolder targetNewSubDir = project.getFolder("newSubFolder");
         assertTrue(targetNewSubDir.exists());
         assertEquals(1, targetNewSubDir.members().length);
         IFile targetNewSubFile = targetNewSubDir.getFile("NewSubFile.txt");
         assertEquals("NewSubFile.txt", ResourceUtil.readFrom(targetNewSubFile));
         // Prepare temporary directory for replacing files
         newSubDir = new File(tempDir, "newSubFolder");
         newSubDir.mkdirs();
         IOUtil.writeTo(new FileOutputStream(new File(newSubDir, "NewSubFile.txt")), "NewSubFile-Changed.txt");
         IOUtil.writeTo(new FileOutputStream(new File(tempDir, "Text.txt")), "Text-Changed.txt");
         // Replace some files
         ResourceUtil.copyIntoWorkspace(project, null, null, tempDir.listFiles());
         assertEquals(7, project.members().length);
         assertTrue(projectFile.exists());
         assertTrue(targetEmptyFolder.exists());
         assertEquals(0, targetEmptyFolder.members().length);
         assertEquals("Text-Changed.txt", ResourceUtil.readFrom(targetText));
         assertTrue(targetSubDir.exists());
         assertEquals(2, targetSubDir.members().length);
         assertEquals("SubFile.txt", ResourceUtil.readFrom(targetSubFile));
         assertTrue(targetSubSubDir.exists());
         assertEquals(2, targetSubSubDir.members().length);
         assertEquals("SubSubFileA.txt", ResourceUtil.readFrom(targetSubSubFileA));
         assertEquals("SubSubFileB.txt", ResourceUtil.readFrom(targetSubSubFileB));
         assertTrue(targetNewEmptyFolder.exists());
         assertEquals(0, targetNewEmptyFolder.members().length);
         assertEquals("NewText.txt", ResourceUtil.readFrom(targetNewText));
         assertTrue(targetNewSubDir.exists());
         assertEquals(1, targetNewSubDir.members().length);
         assertEquals("NewSubFile-Changed.txt", ResourceUtil.readFrom(targetNewSubFile));
         // Replace some files with a custom opener
         IFileOpener opener = new IFileOpener() {
            @Override
            public InputStream open(File file) throws IOException {
               String content = IOUtil.readFrom(file);
               content += "-Modified";
               return new ByteArrayInputStream(content.getBytes());
            }
         };
         ResourceUtil.copyIntoWorkspace(project, opener, null, tempDir.listFiles());
         assertEquals(7, project.members().length);
         assertTrue(projectFile.exists());
         assertTrue(targetEmptyFolder.exists());
         assertEquals(0, targetEmptyFolder.members().length);
         assertEquals("Text-Changed.txt-Modified", ResourceUtil.readFrom(targetText));
         assertTrue(targetSubDir.exists());
         assertEquals(2, targetSubDir.members().length);
         assertEquals("SubFile.txt", ResourceUtil.readFrom(targetSubFile));
         assertTrue(targetSubSubDir.exists());
         assertEquals(2, targetSubSubDir.members().length);
         assertEquals("SubSubFileA.txt", ResourceUtil.readFrom(targetSubSubFileA));
         assertEquals("SubSubFileB.txt", ResourceUtil.readFrom(targetSubSubFileB));
         assertTrue(targetNewEmptyFolder.exists());
         assertEquals(0, targetNewEmptyFolder.members().length);
         assertEquals("NewText.txt-Modified", ResourceUtil.readFrom(targetNewText));
         assertTrue(targetNewSubDir.exists());
         assertEquals(1, targetNewSubDir.members().length);
         assertEquals("NewSubFile-Changed.txt-Modified", ResourceUtil.readFrom(targetNewSubFile));
      }
      finally {
         IOUtil.delete(tempDir);
      }
   }
   
   /**
    * Tests {@link ResourceUtil#readFrom(IFile)}
    * @throws Exception
    */
   @Test
   public void testReadFrom() throws Exception {
      // Create project and file
      IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testReadFrom");
      IFile file = TestUtilsUtil.createFile(project, "file.txt", "Hello World!");
      IFile notExisting = project.getFile("NotExistingFile.txt");
      assertFalse(notExisting.exists());
      // Test reading
      assertNull(ResourceUtil.readFrom(null));
      assertNull(ResourceUtil.readFrom(notExisting));
      assertEquals("Hello World!", ResourceUtil.readFrom(file));
   }
   
   /**
    * Tests {@link ResourceUtil#createFile(IFile, java.io.InputStream, org.eclipse.core.runtime.IProgressMonitor)}.
    */
   @Test
   public void testCreateFile() throws Exception {
      // Create project
      IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testCreateFile");
      IFile toCreate = project.getFile("NewFile.txt");
      assertFalse(toCreate.exists());
      // Test null
      ResourceUtil.createFile(null, null, null);
      assertFalse(toCreate.exists());
      // Create file
      ResourceUtil.createFile(toCreate, new ByteArrayInputStream("Hello".getBytes()), null);
      assertTrue(toCreate.exists());
      assertEquals("Hello", ResourceUtil.readFrom(toCreate));
      // Change content of file
      ResourceUtil.createFile(toCreate, new ByteArrayInputStream("Changed".getBytes()), null);
      assertTrue(toCreate.exists());
      assertEquals("Changed", ResourceUtil.readFrom(toCreate));
      // Change content to null
      ResourceUtil.createFile(toCreate, null, null);
      assertTrue(toCreate.exists());
      assertEquals("", ResourceUtil.readFrom(toCreate));
      // Change content of file again
      ResourceUtil.createFile(toCreate, new ByteArrayInputStream("Changed Again".getBytes()), null);
      assertTrue(toCreate.exists());
      assertEquals("Changed Again", ResourceUtil.readFrom(toCreate));
      // Delete file
      toCreate.delete(true, null);
      assertFalse(toCreate.exists());
      // Create file again with null
      ResourceUtil.createFile(toCreate, null, null);
      assertTrue(toCreate.exists());
      assertEquals("", ResourceUtil.readFrom(toCreate));
      // Change content of file
      ResourceUtil.createFile(toCreate, new ByteArrayInputStream("Content".getBytes()), null);
      assertTrue(toCreate.exists());
      assertEquals("Content", ResourceUtil.readFrom(toCreate));
   }
   
   /**
    * Tests {@link ResourceUtil#getFileNameWithoutExtension(IFile)}.
    */
   @Test
   public void testGetFileNameWithoutExtension() {
      IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testGetFileNameWithoutExtension");
      assertNull(ResourceUtil.getFileNameWithoutExtension(null));
      assertEquals("test", ResourceUtil.getFileNameWithoutExtension(project.getFile("test.txt")));
      assertEquals("hello.world", ResourceUtil.getFileNameWithoutExtension(project.getFile("hello.world.diagram")));
      assertEquals("", ResourceUtil.getFileNameWithoutExtension(project.getFile(".project")));
      assertEquals("file", ResourceUtil.getFileNameWithoutExtension(project.getFile("file")));
   }
   
   /**
    * Tests {@link ResourceUtil#getProject(String)}.
    */
   @Test
   public void testGetProject() {
      // Create example projects
      IProject project1 = TestUtilsUtil.createProject("ResourceUtilTest_testGetProject1");
      IProject project2 = TestUtilsUtil.createProject("ResourceUtilTest_testGetProject2");
      // Test null
      assertNull(ResourceUtil.getProject(null));
      // Test empty
      assertNull(ResourceUtil.getProject(StringUtil.EMPTY_STRING));
      // Test invalid
      IProject invalid = ResourceUtil.getProject("INVALID"); 
      assertNotNull(invalid);
      assertFalse(invalid.exists());
      assertFalse(invalid.isOpen());
      // Test valid
      assertEquals(project1, ResourceUtil.getProject(project1.getName()));
      assertEquals(project2, ResourceUtil.getProject(project2.getName()));
   }
    
   /**
    * Tests {@link ResourceUtil#findResourceForLocation(File)}
    */
   @Test
   public void testFindResourceForLocation() throws CoreException, IOException {
      // Create temp file
      File tempFile = null;
      try {
         tempFile = File.createTempFile("Test", ".txt");
         // Create project
         IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testfindResource");
         IFolder folder = TestUtilsUtil.createFolder(project, "emptyFolder");
         File location = ResourceUtil.getLocation(folder);
         IFile file = TestUtilsUtil.createFile(project, "Test.txt", "Hello World!");
         File fileLocation = ResourceUtil.getLocation(file);
         // Test null
         IResource[] result = ResourceUtil.findResourceForLocation(null);
         assertNotNull(result);
         assertEquals(0, result.length);
         // Text existing location
         result = ResourceUtil.findResourceForLocation(location);
         assertNotNull(result);
         assertEquals(1, result.length);
         assertEquals(folder, result[0]);
         // Text existing file location
         result = ResourceUtil.findResourceForLocation(fileLocation);
         assertNotNull(result);
         assertEquals(1, result.length);
         assertEquals(file, result[0]);
         // Test not existing location
         result = ResourceUtil.findResourceForLocation(new File("INVALID"));
         assertNotNull(result);
         assertEquals(0, result.length);
         // Test existing location which is not part of the workspace
         result = ResourceUtil.findResourceForLocation(tempFile.getParentFile());
         assertNotNull(result);
         assertEquals(0, result.length);
         // Test existing file location which is not part of the workspace
         result = ResourceUtil.findResourceForLocation(tempFile);
         assertNotNull(result);
         assertEquals(0, result.length);
      }
      finally {
         if (tempFile != null) {
            tempFile.delete();
         }
      }
   }

   /**
    * Tests {@link ResourceUtil#getLocation(org.eclipse.core.resources.IResource)}
    */
   @Test
   public void testGetLocation() {
      // Create project
      IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testGetLocation");
      IFolder folder = TestUtilsUtil.createFolder(project, "emptyFolder");
      IFile file = TestUtilsUtil.createFile(project, "file.txt", "Hello World!");
      IFolder subfolder = TestUtilsUtil.createFolder(project, "subFolder");
      IFolder subFolderFolder = TestUtilsUtil.createFolder(subfolder, "emptyFolderInSubfolder");
      IFile subFolderFile = TestUtilsUtil.createFile(subfolder, "fileInSubfolder.txt", "Hello World!");
      // test null
      assertNull(ResourceUtil.getLocation(null));
      // test project
      File projectLocation = ResourceUtil.getLocation(project);
      assertNotNull(projectLocation);
      assertTrue(projectLocation.exists() && projectLocation.isDirectory());
      // test folder
      File folderLocation = ResourceUtil.getLocation(folder);
      assertNotNull(folderLocation);
      assertTrue(folderLocation.exists() && folderLocation.isDirectory());
      assertTrue(ArrayUtil.contains(projectLocation.listFiles(), folderLocation));
      // test file
      File fileLocation = ResourceUtil.getLocation(file);
      assertNotNull(fileLocation);
      assertTrue(fileLocation.exists() && fileLocation.isFile());
      assertTrue(ArrayUtil.contains(projectLocation.listFiles(), fileLocation));
      // test subfolder
      File subfolderLocation = ResourceUtil.getLocation(subfolder);
      assertNotNull(subfolderLocation);
      assertTrue(subfolderLocation.exists() && subfolderLocation.isDirectory());
      assertTrue(ArrayUtil.contains(projectLocation.listFiles(), subfolderLocation));
      // test subFolderFolder
      File subFolderFolderLocation = ResourceUtil.getLocation(subFolderFolder);
      assertNotNull(subFolderFolderLocation);
      assertTrue(subFolderFolderLocation.exists() && subFolderFolderLocation.isDirectory());
      assertTrue(ArrayUtil.contains(subfolderLocation.listFiles(), subFolderFolderLocation));
      // test subFolderFile
      File subFolderFileLocation = ResourceUtil.getLocation(subFolderFile);
      assertNotNull(subFolderFileLocation);
      assertTrue(subFolderFileLocation.exists() && subFolderFileLocation.isFile());
      assertTrue(ArrayUtil.contains(subfolderLocation.listFiles(), subFolderFileLocation));
   }
   
   /**
    * Tests {@link ResourceUtil#copyIntoFileSystem(IFile, File)}
    */
   @Test
   public void testCopyIntoFileSystem() throws Exception {
      // Create project
      IProject project = TestUtilsUtil.createProject("ResourceUtilTest_testCopyIntoFileSystem");
      IFile file = TestUtilsUtil.createFile(project, "Test.txt", "Hello World!");
      IFolder folder = TestUtilsUtil.createFolder(project, "folder");
      IFile subFile = TestUtilsUtil.createFile(folder, "TestSub.txt", "Hello Sub Folder!");
      // Create tmp file
      File tmpFile = File.createTempFile("Test", ".txt");
      tmpFile.delete();
      // Create temp dir
      File tempDir = IOUtil.createTempDirectory("Test", "folder");
      try {
         // Test null
         assertFalse(ResourceUtil.copyIntoFileSystem(null, tmpFile));
         assertFalse(tmpFile.exists());
         assertFalse(ResourceUtil.copyIntoFileSystem(file, null));
         assertFalse(tmpFile.exists());
         assertFalse(ResourceUtil.copyIntoFileSystem(null, null));
         assertFalse(tmpFile.exists());
         // Test copy not existing file
         assertTrue(ResourceUtil.copyIntoFileSystem(file, tmpFile));
         assertTrue(tmpFile.exists());
         assertEquals("Hello World!", IOUtil.readFrom(tmpFile));
         // Test copy existing file
         assertTrue(ResourceUtil.copyIntoFileSystem(file, tmpFile));
         assertTrue(tmpFile.exists());
         assertEquals("Hello World!", IOUtil.readFrom(tmpFile));
         // Test copy project
         assertTrue(ResourceUtil.copyIntoFileSystem(project, tempDir));
         assertTrue(tempDir.exists());
         assertTrue(new File(tempDir, file.getName()).exists());
         assertEquals("Hello World!", IOUtil.readFrom(new File(tempDir, file.getName())));
         assertTrue(new File(tempDir, folder.getName()).exists());
         assertEquals("Hello Sub Folder!", IOUtil.readFrom(new File(tempDir, folder.getName() + File.separator + subFile.getName())));
      }
      finally {
         tmpFile.delete();
         tempDir.delete();
      }
   }
}