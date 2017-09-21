package io.thinkstack.maven.plugins;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "s3-multipart-upload")
public class S3MultipartUpload extends AbstractMojo {

  @Parameter(property = "s3-multipart-upload.bucket", required = true)
  private String bucket;

  @Parameter(property = "s3-multipart-upload.source", required = true)
  private String source;

  @Parameter(property = "s3-multipart-upload.key", required = true)
  private String key;

  @Parameter(property = "s3-multipart-upload.profile")
  private String profile;

  @Parameter(property = "s3-multipart-upload.chunkCount", required = false, defaultValue = "20")
  private int chunkCount;

  private static final String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
  private static final String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");

  private static final Logger logger = LoggerFactory.getLogger(S3MultipartUpload.class);
  private static final long MIN_FILE_SIZE = 5242880;

  private static AmazonS3 s3Client;
  private static File file;

  public void execute() throws MojoExecutionException {
    // Initialise variables that are used throughout the class
    AWSCredentialsProvider credentials = Utils.getCredentials(logger, this.profile, accessKey, secretKey);
    file = new File(this.source);
    s3Client = AmazonS3ClientBuilder.standard().withCredentials(credentials).build();

    checkArguments();
    putObjectAsMultiPart();
  }

  private void putObjectAsMultiPart() {
    List<PartETag> partETags = new ArrayList<>();
    List<MultiPartFileUploader> uploaders = new ArrayList<>();

    // Step 1: Initialize.
    InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(this.bucket, file.getName());
    InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
    long contentLength = file.length();

    // This may need to change in the future if the size of the file grows so large that the individual chunks become
    // too big. In that case, just determine a decent part size in bytes. Don't go too small though otherwise too many
    // http connections are established and some time out.
    long partSize = file.length() / this.chunkCount;

    try {
      // Step 2: Upload parts.
      long filePosition = 0;
      for (int i = 1; filePosition < contentLength; i++) {
        // Last part can be less than part size. Adjust part size.
        partSize = Math.min(partSize, (contentLength - filePosition));

        // Create request to upload a part.
        UploadPartRequest uploadRequest =
            new UploadPartRequest().
                withBucketName(this.bucket).
                withKey(file.getName()).
                withUploadId(initResponse.getUploadId()).
                withPartNumber(i).
                withFileOffset(filePosition).
                withFile(file).
                withPartSize(partSize).
                withSdkClientExecutionTimeout(-1);

        uploadRequest.setGeneralProgressListener(new UploadProgressListener(file, i, partSize));

        // Upload part and add response to our list.
        MultiPartFileUploader uploader = new MultiPartFileUploader(uploadRequest);
        uploaders.add(uploader);
        uploader.upload();

        filePosition += partSize;
      }

      for (MultiPartFileUploader uploader : uploaders) {
        uploader.join();
        partETags.add(uploader.getPartETag());
      }

      // Step 3: complete.
      CompleteMultipartUploadRequest compRequest =
          new CompleteMultipartUploadRequest(this.bucket,
              file.getName(),
              initResponse.getUploadId(),
              partETags);

      s3Client.completeMultipartUpload(compRequest);

    } catch (Throwable t) {
      logger.error("Unable to put object as multipart to Amazon S3 for file " + file.getName(), t);
      s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(this.bucket, file.getName(), initResponse.getUploadId()));

    } finally {
      s3Client.shutdown();
    }
  }

  private void checkArguments() throws MojoExecutionException {
    String error;

    logger.info(String.format("Using the following arguments: bucket = %s, source = %s, key = %s, profile = %s, accessKey = %s, secretKey = ***",
        this.bucket,
        this.source,
        this.key,
        this.profile,
        this.accessKey));

    if (!file.exists()) {
      error = String.format("File not found: %s", file.getAbsolutePath());
      logger.error(error);
      throw new MojoExecutionException(error);
    }

    if (file.length() < MIN_FILE_SIZE) {
      error = String.format("File size (%s bytes) is too small for multipart uploads. File needs to be greater than %s bytes", file.length(), MIN_FILE_SIZE);
      logger.error(error);
      throw new MojoExecutionException(error);
    }

    long partSize = file.length() / this.chunkCount;
    if (partSize < MIN_FILE_SIZE) {
      error = String.format("File part size (%s bytes) is too small for multipart uploads. " +
          "File parts need to be greater than %s bytes. Consider using fewer chunks (chunkCount = %s)",
          partSize, MIN_FILE_SIZE, this.chunkCount);
      logger.error(error);
      throw new MojoExecutionException(error);
    }
  }

  private class UploadProgressListener implements ProgressListener {

    File file;
    int partNo;
    long partLength;

    UploadProgressListener(File file, int partNo, long partLength) {
      this.file = file;
      this.partNo = partNo;
      this.partLength = partLength;
    }

    @Override
    public void progressChanged(ProgressEvent progressEvent) {
      switch (progressEvent.getEventType()) {
        case TRANSFER_STARTED_EVENT:
          logger.info(String.format("Upload started for file %s",
              file.getName()));
          break;
        case TRANSFER_COMPLETED_EVENT:
          logger.info(String.format("Upload completed for file '%s', %s bytes have been transferred.",
              file.getName(), file.length()));
          break;
        case TRANSFER_FAILED_EVENT:
          logger.info(String.format("Upload failed for file '%s', %s bytes have been transferred.",
              file.getName(), progressEvent.getBytesTransferred()));
          break;
        case TRANSFER_CANCELED_EVENT:
          logger.info(String.format("Upload cancelled for file '%s', %s bytes have been transferred.",
              file.getName(), progressEvent.getBytesTransferred()));
          break;
        case TRANSFER_PART_STARTED_EVENT:
          logger.info(String.format("Upload started for part %s of file '%s'",
              partNo, file.getName()));
          break;
        case TRANSFER_PART_COMPLETED_EVENT:
          logger.info(String.format("Upload completed for part %s of file '%s', %s bytes transferred.",
              partNo, file.getName(), (partLength > 0 ? partLength : progressEvent.getBytesTransferred())));
          break;
        case TRANSFER_PART_FAILED_EVENT:
          logger.info(String.format("Upload failed for part %s of file '%s', %s bytes have been transferred.",
              partNo, file.getName(), progressEvent.getBytesTransferred()));
          break;
      }
    }
  }

  private class MultiPartFileUploader extends Thread {
    private UploadPartRequest uploadRequest;
    private PartETag partETag;

    MultiPartFileUploader(UploadPartRequest uploadRequest) {
      this.uploadRequest = uploadRequest;
    }

    @Override
    public void run() {
      partETag = s3Client.uploadPart(uploadRequest).getPartETag();
    }

    private PartETag getPartETag() {
      return partETag;
    }

    private void upload() {
      start();
    }

  }
}
