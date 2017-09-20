# s3-multipart-upload-maven-plugin
Maven plugin to allow multipart uploads of large files via the low level Java SDK.

## Configuration parameters

| Parameter | Description | Required | Comment |
|-----------|-------------|----------|---------|
|bucket|The name of the bucket. |*yes*| |
|source|The source file. |*yes*| |
|key|The destination key in s3. | *yes*| |
|profile|AWS Profile | *no* | if unspecified, the plugin falls back to environment variables. |

## Example plugin definition
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>io.thinkstack.maven.plugins</groupId>
        <artifactId>s3-multipart-upload-maven-plugin</artifactId>
        <version>1.0.0</version>
        <configuration>
          <bucket>s3-bucket</bucket>
          <source>/tmp/large-file.zip</source>
          <key>large-file.zip</key>
          <profile>your-aws-profile</profile>
        </configuration>
      </plugin>
    </plugins>
  </build>
```
## Example Maven command
```bash
mvn io.thinkstack.maven.plugins:s3-upload-maven-plugin:1.0:s3-multipart-upload
```

### References
* [docs.aws.amazon.com: Upload a File (S3)](http://docs.aws.amazon.com/AmazonS3/latest/dev/llJavaUploadFile.html)
* [stackoverflow.com: Upload static resource to Amazon s3 server using maven plugin](https://stackoverflow.com/questions/20650514/upload-static-resource-to-amazon-s3-server-using-maven-plugin)
* [dzone.com: Amazon S3 Parallel MultiPart File Upload](https://dzone.com/articles/amazon-s3-parallel-multipart)
