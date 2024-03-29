# lib-amazon-aws

**Amazon AWS client and automation utilities.**

Provides a convenient wrapper for various AWS Java **Version 1.x** SDK resources.

# S3 client use examples

The easiest strategy is to create an S3 client then use it directly for simple file functions. YOu can also inject the S3 client into an S3 file manager for more complex transactions.

```java
/**
 * Declare configuration and credentials. These may also be declared in a
 * config file or as environment variables.
 */
String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
String BUCKET_DEFAULT = "BUCKET_DEFAULT";
com.amazonaws.regions.Regions REGION_DEFAULT = Regions.US_EAST_1;
/**
 * Instantiate a bucket config and S3 client.
 */
S3BucketConfig bucket = new S3BucketConfig(AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, REGION_DEFAULT, BUCKET_DEFAULT);
AwsS3 s3Client = new AwsS3(bucket);
/**
 * You may use the S3 client directly for simple list, upload and download.
 * Use the file manager for more complicated functions.
 */
S3FileManager s3FileManager = new S3FileManager(s3Client);
/**
 * Execute file functions 
 */
```



## Authentication

**Set Up AWS Credentials and Region for Development**

To connect to any of the supported services with the AWS SDK for Java, you must provide AWS credentials. The AWS SDKs and CLIs use provider chains to look for AWS credentials in several different places, including system/user environment variables and local AWS configuration files. 

You can set your credentials for use by the AWS SDK for Java in several ways. 
Set credentials in the AWS credentials profile file on your local system, located at:

    ~/.aws/credentials on Linux, macOS, or Unix

This file should contain lines in the following format:

```sh
[default]
aws_access_key_id = your_access_key_id
aws_secret_access_key = your_secret_access_key
```

Set the AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables.
To set these variables on Linux, macOS, or Unix, use export:

```sh
export AWS_ACCESS_KEY_ID=your_access_key_id
export AWS_SECRET_ACCESS_KEY=your_secret_access_key
```

Once you set your AWS credentials using one of these methods, the AWS SDK for Java loads them automatically by using the default credential provider chain.


**Setting the AWS Region**

You should set a default AWS Region to use for accessing AWS services with the AWS SDK for Java.
You can use techniques similar to those for setting credentials to set your default AWS Region.
Set the AWS Region in the AWS config file on your local system, located at:

    ~/.aws/config on Linux, macOS, or Unix
    

```
[default]
region = your_aws_region
```

Set the AWS_REGION environment variable.

On Linux, macOS, or Unix, use export :

```sh
export AWS_REGION=your_aws_region
```


## Code Examples

See [Amazon S3 Examples Using the AWS SDK for Java](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-s3.html)

## Resources:

  *  [Set Up AWS Credentials](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/setup-credentials.html)
  *  [AWS Region Selection](https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/java-dg-region-selection.html)

