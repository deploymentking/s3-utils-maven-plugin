package io.thinkstack.maven.plugins;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;

public class Utils {
  public static AWSCredentialsProvider getCredentials(Logger logger, String profile, String accessKey, String secretKey)
      throws MojoExecutionException {

    String error;

    if (profile == null || profile.isEmpty()) {
      if (accessKey == null || accessKey.isEmpty()) {
        error = String.format("Profile has not been specified and the AWS Access Key Id is empty or null");
        logger.error(error);
        throw new MojoExecutionException(error);
      }

      if (secretKey == null || secretKey.isEmpty()) {
        error = String.format("Profile has not been specified and the AWS Secret Access Key is empty or null");
        logger.error(error);
        throw new MojoExecutionException(error);
      }
    }

    // Grab the credentials from the profile supplied OR the environmental variables
    AWSCredentialsProvider credentials = "".equals(profile) ?
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)) :
        new ProfileCredentialsProvider(profile);

    return credentials;
  }
}
