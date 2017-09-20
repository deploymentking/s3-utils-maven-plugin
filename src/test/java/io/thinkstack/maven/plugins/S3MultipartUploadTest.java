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
      getSomeMojo("src/test/resources/basic-test-plugin-config-valid-props.xml");
    } catch (MojoExecutionException exception) {
      fail("Should not have thrown MojoExecutionException");
    }
  }

  @Test()
  public void testExecuteInvalidProperties() throws Exception {
    try {
      getSomeMojo("src/test/resources/basic-test-plugin-config-invalid-props.xml");
    } catch (ComponentConfigurationException exception) {
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
