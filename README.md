# s3-utils-maven-plugin

## Table of contents

<!-- toc -->

- [Goal: s3-multipart-upload](#goal-s3-multipart-upload)
  * [Configuration properties](#configuration-properties)
    + [AWS Environment Variables](#aws-environment-variables)
  * [Example plugin definition](#example-plugin-definition)
  * [Running Unit Tests](#running-unit-tests)
    + [AWS Profile](#aws-profile)
    + [IntelliJ Setup](#intellij-setup)
      - [Run Maven Goal](#run-maven-goal)
      - [Run External Tool](#run-external-tool)
- [Goal: s3-download](#goal-s3-download)
  * [Configuration properties](#configuration-properties-1)
    + [AWS Environment Variables](#aws-environment-variables-1)
  * [Example plugin definition](#example-plugin-definition-1)
- [Deployment](#deployment)
- [References](#references)

<!-- tocstop -->

Maven plugin to allow:
* multipart uploads of large files via the low level Java SDK.
* downloading a single object into a folder.

## Goal: s3-multipart-upload

```bash
mvn io.thinkstack.maven.plugins:s3-utils-maven-plugin:1.0.6:s3-multipart-upload
```

### Configuration properties

| Property | Description | Required | Default |
|-----------|-------------|----------|---------|
|s3utils.bucket|The name of the bucket. |*yes*| |
|s3utils.source|The source file. |*yes*| |
|s3utils.key|The destination key in s3. | *yes*| |
|s3utils.profile|AWS Profile to use | *no* | if unspecified, the plugin falls back to environment variables. |
|s3utils.chunkSize|The number of chunks into which to split the source | *no* | 20 |

#### AWS Environment Variables
If the profile is not specified via the property e.g. `-Ds3utils.profile=example-profile-name` then the plugin will look
for the profile in `$AWS_PROFILE` environment variable. If this is null or empty, the plugin will attempt to load the AWS
keys `$AWS_ACCESS_KEY_ID` and `$AWS_SECRET_KEY_ID`

### Example plugin definition
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>io.thinkstack.maven.plugins</groupId>
        <artifactId>s3-utils-maven-plugin</artifactId>
        <version>1.0.6</version>
        <executions>
          <execution>
            <goals>
              <goal>s3-multipart-upload</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <bucket>s3-bucket</bucket>
          <source>/tmp/test.zip</source>
          <key>test.zip</key>
          <profile>your-aws-profile</profile>
          <chunkSize>20</chunkSize>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

### Running Unit Tests

#### AWS Profile
The test resources use the profile `io.thinkstack` so either change this to a value already configured or add a new AWS 
profile.

#### IntelliJ Setup
When running the unit tests in IntelliJ I have had to add the following commands _before_ the tests run.

* Run | Edit Configurations...
* Select JUnit Profile
* Add the following to the `Before launch:` panel

##### Run Maven Goal
This command ensures the javadoc descriptors are present before the mojo tests are run
```bash
mvn clean install -DskipTests -DskipITs
```

##### Run External Tool
This setup ensures that the 120mb size file is present. It can't be added to the project as it breaks GitHub's 
file size limit of 100mb
* Program = `/bin/dd`
* Parameters = `dd if=/dev/zero of=src/test/resources/120mb.dat  bs=125829120  count=1`
* Working directory = `~/path/to/source/DeploymentKing/s3-utils-maven-plugin`

## Goal: s3-download

```bash
mvn io.thinkstack.maven.plugins:s3-utils-maven-plugin:1.0.6:s3-download
```

### Configuration properties

| Property | Description | Required | Default |
|-----------|-------------|----------|---------|
|s3utils.bucket|The name of the bucket. |*yes*| |
|s3utils.key|The source key in s3. | *yes*| |
|s3utils.profile|AWS Profile | *no* | if unspecified, the plugin falls back to environment variables. |
|s3utils.folder|The path to the folder into which the object will be downloaded. |*yes*| |

#### AWS Environment Variables
If the profile is not specified via the property e.g. `-Ds3utils.profile=example-profile-name` then the plugin will look
for the profile in `$AWS_PROFILE` environment variable. If this is null or empty, the plugin will attempt to load the AWS
keys `$AWS_ACCESS_KEY_ID` and `$AWS_SECRET_KEY_ID`

### Example plugin definition
```xml
  <build>
    <plugins>
      <plugin>
        <groupId>io.thinkstack.maven.plugins</groupId>
        <artifactId>s3-utils-maven-plugin</artifactId>
        <version>1.0.6</version>
        <executions>
          <execution>
            <goals>
              <goal>s3-download</goal>
            </goals>
          </execution>
        </executions>
        <configuration>
          <bucket>io.thinkstack</bucket>
          <key>120mb.dat</key>
          <profile>io.thinkstack</profile>
          <folder>src/test/resources/</folder>
        </configuration>
      </plugin>
    </plugins>
  </build>
```

## Deployment
This project has not been deployed to Maven Central. The project has no distribution management section so it is the 
responsibility of anyone using this repo, to deploy it appropriately to their own repo using the following:

```bash
mvn deploy -DaltDeploymentRepository=repo-id::default::https://repo-url
```

## References
* [docs.aws.amazon.com: Upload a File (S3)](http://docs.aws.amazon.com/AmazonS3/latest/dev/llJavaUploadFile.html)
* [stackoverflow.com: Upload static resource to Amazon s3 server using maven plugin](https://stackoverflow.com/questions/20650514/upload-static-resource-to-amazon-s3-server-using-maven-plugin)
* [dzone.com: Amazon S3 Parallel MultiPart File Upload](https://dzone.com/articles/amazon-s3-parallel-multipart)
