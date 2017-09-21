package io.thinkstack.maven.plugins;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Mojo(name = "s3-download")
public class S3Download extends AbstractMojo {

  @Parameter(property = "s3-download.bucket", required = true)
  private String bucket;

  @Parameter(property = "s3-download.key", required = true)
  private String key;

  @Parameter(property = "s3-download.profile")
  private String profile;

  @Parameter(property = "s3-download.folder", required = true)
  private String folder;

  private static final String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
  private static final String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

  private static final Logger logger = LoggerFactory.getLogger(S3Download.class);

  private static AmazonS3 s3Client;
  private static File downloadFolder;

  public void execute() throws MojoExecutionException {
    // Initialise variables that are used throughout the class
    AWSCredentialsProvider credentials = Utils.getCredentials(logger, this.profile, accessKey, secretKey);
    downloadFolder = new File(this.folder);
    s3Client = AmazonS3ClientBuilder.standard().withCredentials(credentials).build();

    checkArguments();
    getObject();
  }

  private void getObject() {

    try {
      S3Object s3object = s3Client.getObject(new GetObjectRequest(this.bucket, this.key));
      logger.info(String.format("Content-Type: %s", s3object.getObjectMetadata().getContentType()));

      File downloadedFile = downloadFolder.toPath().resolve(this.key).toFile();

      S3Object object = s3Client.getObject(new GetObjectRequest(this.bucket, this.key));
      S3ObjectInputStream objectContent = object.getObjectContent();

      Files.copy(objectContent, downloadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      objectContent.close();

    } catch (Throwable t) {
      logger.error(String.format("Unable to get object '%s' to '%s'", this.key, downloadFolder), t);

    } finally {
      s3Client.shutdown();
    }
  }

  private void checkArguments() throws MojoExecutionException {
    String error;

    logger.info(String.format("Using the following arguments: bucket = %s, key = %s, folder = %s, profile = %s, accessKey = %s, secretKey = ***",
        this.bucket,
        this.key,
        this.folder,
        this.profile,
        accessKey));

    if (!s3Client.doesBucketExist(this.bucket)) {
      error = String.format("Bucket not found: %s", this.bucket);
      logger.error(error);
      throw new MojoExecutionException(error);
    }

    if (!downloadFolder.exists()) {
      error = String.format("Download location not found: '%s'", downloadFolder);
      logger.error(error);
      throw new MojoExecutionException(error);
    }

    if (!downloadFolder.isDirectory()) {
      error = String.format("Download location not a directory: '%s'", downloadFolder);
      logger.error(error);
      throw new MojoExecutionException(error);
    }
  }
}
