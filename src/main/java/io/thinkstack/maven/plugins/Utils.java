package io.thinkstack.maven.plugins;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;

public class Utils {
  public static AWSCredentialsProvider getCredentials(Logger logger, String profile) throws MojoExecutionException {

    String error;
    boolean profileSupplied;

    final String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
    final String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
    final String profileEnv = System.getenv("AWS_PROFILE");


    if (profile == null || profile.isEmpty()) {
      logger.warn("Profile has not been specified via property, checking environment variable $AWS_PROFILE");

      if (profileEnv == null || profileEnv.isEmpty()) {
        // We have no profile information, check for the AWS keys
        profileSupplied = false;
        logger.warn("Profile has not been specified via property OR environment variable $AWS_PROFILE");

        if (accessKey == null || accessKey.isEmpty()) {
          error = String.format("Profile has not been specified and $AWS_ACCESS_KEY_ID is empty or null");
          logger.error(error);
          throw new MojoExecutionException(error);
        }

        if (secretKey == null || secretKey.isEmpty()) {
          error = String.format("Profile has not been specified and $AWS_SECRET_ACCESS_KEY is empty or null");
          logger.error(error);
          throw new MojoExecutionException(error);
        }

        // We have no profile information but we have found AWS keys
        logger.info(String.format("Profile not been specified so falling back to using $AWS_ACCESS_KEY_ID (%s) and " +
            "its associated $AWS_SECRET_ACCESS_KEY", accessKey));
      } else {
        // Profile was not specified via the property s3utils.profile BUT the environment variable has been set
        profileSupplied = true;
        profile = profileEnv;
        logger.info(String.format("Using profile supplied by environment variable $AWS_PROFILE (%s)", profileEnv));
      }
    } else {
      // Profile was specified via the property s3utils.profile
      profileSupplied = true;
      logger.info(String.format("Using profile supplied by property s3utils.profile (%s)", profile));
    }

    // Grab the credentials from the profile supplied OR the environmental variables
    AWSCredentialsProvider credentials = profileSupplied ?
        new ProfileCredentialsProvider(profile) :
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));

    return credentials;
  }
}
