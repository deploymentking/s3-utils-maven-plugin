package io.thinkstack.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.junit.Test;

import java.io.File;

public class S3DownloadTest extends AbstractMojoTestCase {
  protected void setUp() throws Exception {
    // required for mojo lookups to work
    super.setUp();
  }

  @Test()
  public void testExecuteValidProperties() throws Exception {
    try {
      getSomeMojo("src/test/resources/s3-download-valid-props.xml");
    } catch (MojoExecutionException exception) {
      fail("Should not have thrown MojoExecutionException");
    }
  }

  @Test()
  public void testExecuteNoProfile() throws Exception {
    try {
      getSomeMojo("src/test/resources/s3-download-no-profile.xml");
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("Profile has not been specified and the AWS Access Key Id is empty or null"));
      return;
    }
    fail("Should have thrown MojoExecutionException");
  }

  @Test()
  public void testExecuteInvalidDownloadLocation() throws Exception {
    try {
      getSomeMojo("src/test/resources/s3-download-invalid-download-location.xml");
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("Download location not found: 'dir-not-exist'"));
      return;
    }
    fail("Should have thrown MojoExecutionException");
  }

  @Test()
  public void testExecuteDownloadLocationNotADir() throws Exception {
    try {
      getSomeMojo("src/test/resources/s3-download-not-directory.xml");
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("Download location not a directory: 'src/test/resources/4mb.dat'"));
      return;
    }
    fail("Should have thrown MojoExecutionException");
  }

  @Test()
  public void testExecuteNonExistentBucket() throws Exception {
    try {
      getSomeMojo("src/test/resources/s3-download-non-existent-bucket.xml");
    } catch (MojoExecutionException exception) {
      assertTrue(exception.getMessage().contains("Bucket not found: io.thinkstack.not.exist"));
      return;
    }
    fail("Should have thrown MojoExecutionException");
  }

  private void getSomeMojo(String file) throws Exception {
    File pom = new File(getBasedir(), file);
    S3Download mojo = (S3Download) lookupMojo("s3-download", pom);
    assertNotNull(mojo);
    mojo.execute();
  }
}
