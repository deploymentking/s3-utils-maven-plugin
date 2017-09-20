package io.thinkstack.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.junit.Test;

import java.io.File;

public class S3MultipartUploadTest extends AbstractMojoTestCase {
  protected void setUp() throws Exception {
    // required for mojo lookups to work
    super.setUp();
  }

  @Test()
  public void testExecuteValidProperties() throws Exception {
    try {
      getSomeMojo("src/test/resources/valid-props.xml");
    } catch (MojoExecutionException exception) {
      fail("Should not have thrown MojoExecutionException");
    }
  }

  @Test()
  public void testExecuteFileSizeTooSmall() throws Exception {
    try {
      getSomeMojo("src/test/resources/file-too-small.xml");
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("File size (4194304 bytes) is too small for multipart uploads. File needs to be greater than 5242880 bytes"));
      return;
    }
    fail("Should have thrown ComponentConfigurationException");
  }

  @Test()
  public void testExecuteFileNotFound() throws Exception {
    try {
      getSomeMojo("src/test/resources/file-not-found.xml");
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("File not found: /Users/leemyring/source/DeploymentKing/s3-multipart-upload-maven-plugin/src/test/resources/5mb.dat"));
      return;
    }
    fail("Should have thrown ComponentConfigurationException");
  }


  @Test()
  public void testExecuteFilePartSizeTooSmall() throws Exception {
    try {
      getSomeMojo("src/test/resources/file-part-too-small.xml");
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("File part size (1048576 bytes) is too small for multipart uploads. File parts need to be greater than 5242880 bytes. Consider using fewer chunks (chunkCount = 20)"));
      return;
    }
    fail("Should have thrown ComponentConfigurationException");
  }

  @Test()
  public void testExecuteInvalidProperties() throws Exception {
    try {
      getSomeMojo("src/test/resources/invalid-props.xml");
    } catch (ComponentConfigurationException exception) {
      assertTrue(exception.getMessage().contains("Cannot find 'notbucket' in class io.thinkstack.maven.plugins.S3MultipartUpload"));
      return;
    }
    fail("Should have thrown ComponentConfigurationException");
  }

  private void getSomeMojo(String file) throws Exception {
    File pom = new File(getBasedir(), file);
    S3MultipartUpload mojo = (S3MultipartUpload) lookupMojo("s3-multipart-upload", pom);
    assertNotNull(mojo);
    mojo.execute();
  }
}
