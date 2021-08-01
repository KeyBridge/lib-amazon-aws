/*
 * Copyright (C) 2018 Key Bridge
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ch.keybridge.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Amazon S3 REST Client.
 * <p>
 * This is a convenience wrapper around the {@link AmazonS3} utility class,
 * providing automatic credential loading via a resource file plus simple access
 * to common methods.
 * <p>
 * Developer note: Amazon S3 Clients must allow for acceptance of self-signed
 * certificates. or, if the servers identity does not matter, can alternatively
 * disable checking the server authenticity. This is done by adding the run-time
 * flag '-Dcom.amazonaws.sdk.disableCertChecking=true' to the VM options, which
 * adds the tag a a command-line flag.
 *
 * @author Key Bridge
 * @since ca 2/17/15 or earlier
 * @since rewritten 12/1/18 to include complete file control
 */
public class AwsS3 {

  private static final Logger LOG = Logger.getLogger(AwsS3.class.getName());

  /**
   * 10. The optional parameter indicating the maximum number of keys to include
   * in the response.
   */
  private static final int MAX_KEYS = 1000;

  /**
   * The AWS region.
   */
  private Regions region;
  /**
   * The AWS S3 bucket name.
   */
  private String bucketName;
  /**
   * The optional parameter indicating the maximum number of keys to include in
   * the response. Default is 100.
   */
  private int maxKeys = MAX_KEYS;

  /**
   * The AWS S3 access key.
   */
  private String awsAccessKeyId;
  /**
   * The AWS S3 secret key.
   */
  private String awsSecretAccessKey;

  /**
   * Default no-arg constructor. Provides an S3 client with AWS credentials read
   * from the default location; either `.aws/credential` or from environment
   * variables.
   *
   * @see
   * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/AuthUsingAcctOrUserCredentials.html">Using
   * AWS credentials</a>
   */
  public AwsS3() {
  }

  /**
   * Construct a new AwsS3Client, specifying the complete access configuration.
   *
   * @param bucketConfig an S3 bucket configuration
   * @since v1.2.0 added -1/31/19
   */
  public AwsS3(S3BucketConfig bucketConfig) {
    this.awsAccessKeyId = bucketConfig.getAwsAccessKeyId();
    this.awsSecretAccessKey = bucketConfig.getAwsSecretAccessKey();
    this.bucketName = bucketConfig.getBucketName();
    this.region = bucketConfig.getRegion();
  }

  /**
   * Construct a new AwsS3Client, specifying the access credentials.
   * <p>
   * This method is NOT preferred. You should set the credentials in a
   * {@code .aws/credential} file or environment variable.
   *
   * @param awsAccessKeyId     The AWS S3 access key.
   * @param awsSecretAccessKey The AWS S3 secret access key.
   */
  public AwsS3(String awsAccessKeyId, String awsSecretAccessKey) {
    this.awsAccessKeyId = awsAccessKeyId;
    this.awsSecretAccessKey = awsSecretAccessKey;
  }

  //<editor-fold defaultstate="collapsed" desc="Getter and Setter">
  public Regions getRegion() {
    return region;
  }

  public void setRegion(Regions region) {
    this.region = region;
  }

  public AwsS3 withRegion(Regions region) {
    this.region = region;
    return this;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public AwsS3 withBucketName(String bucketName) {
    this.bucketName = bucketName;
    return this;
  }

  public int getMaxKeys() {
    return maxKeys;
  }

  public void setMaxKeys(int maxKeys) {
    this.maxKeys = maxKeys;
  }

  public AwsS3 withMaxKeys(int maxKeys) {
    this.maxKeys = maxKeys;
    return this;
  }//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Common">
  /**
   * Internal method to build an Amazon S3 client. Amazon S3 provides storage
   * for the Internet, and is designed to make web-scale computing easier for
   * developers.
   *
   * @return a new client.
   */
  private AmazonS3 buildS3Client() {
    /**
     * Use either the default profile credentials provider or a basic provider,
     * depending upon whether the credential is provided.
     */
    AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
    if (awsAccessKeyId != null && awsSecretAccessKey != null) {
      BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey);
      builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
    } else {
      builder.withCredentials(new ProfileCredentialsProvider());
    }
    /**
     * Conditionally set the region.
     */
    return region != null
           ? builder.withRegion(region).build()
           : builder.build();

  }//</editor-fold>

  /**
   *
   * Gets the metadata for the specified Amazon S3 object without actually
   * fetching the object itself. This is useful in obtaining only the object
   * metadata, and avoids wasting bandwidth on fetching the object data.
   * <p>
   * The object metadata contains information such as content type, content
   * disposition, etc., as well as custom user metadata that can be associated
   * with an object in Amazon S3.
   * <p>
   * This uses the pre-configured bucket containing the object's whose metadata
   * is being retrieved.
   *
   * @param key The key of the object whose metadata is being retrieved.
   * @return All Amazon S3 object metadata for the specified object.
   * @throws SdkClientException     If any errors are encountered in the client
   *                                while making the request or handling the
   *                                response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while
   *                                processing the request.
   */
  public ObjectMetadata getObjectMetadata(String key) throws SdkClientException, AmazonServiceException {
    return buildS3Client().getObjectMetadata(bucketName, key);
  }

  /**
   * Listing Object Keys
   * <p>
   * Keys can be listed by prefix. By choosing a common prefix for the names of
   * related keys and marking these keys with a special character that delimits
   * hierarchy, you can use the list operation to select and browse keys
   * hierarchically. This is similar to how files are stored in directories
   * within a file system.
   * <p>
   * Amazon S3 exposes a list operation that lets you enumerate the keys
   * contained in a bucket. Keys are selected for listing by bucket and prefix.
   * For example, consider a bucket named "dictionary" that contains a key for
   * every English word. You might make a call to list all the keys in that
   * bucket that start with the letter "q". List results are always returned in
   * UTF-8 binary order.
   *
   * @param prefix An optional prefix parameter restricting the response to keys
   *               that begin with the specified prefix.
   * @return a non-null ArrayList
   * @throws AmazonServiceException The call was transmitted successfully, but
   *                                Amazon S3 couldn't process it, so it
   *                                returned an error response.
   * @throws SdkClientException     Amazon S3 couldn't be contacted for a
   *                                response, or the client couldn't parse the
   *                                response from Amazon S3.
   */
  public List<S3ObjectSummary> list(String prefix) throws AmazonServiceException, SdkClientException {
    List<S3ObjectSummary> objectSummaries = new ArrayList<>();
    /**
     * Contains options to return a list of summary information about the
     * objects in the specified bucket. Depending on the request parameters,
     * additional information is returned.
     */
    ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(maxKeys);
    /**
     * Set the optional prefix parameter, restricting the response to keys that
     * begin with the specified prefix.
     */
    if (prefix != null && !prefix.isEmpty()) {
      request.setPrefix(prefix);
    }
    /**
     * Results of a listing of objects from an S3 bucket.
     */
    ListObjectsV2Result result;
    /**
     * Instantiate a new S3 client.
     */
    AmazonS3 s3Client = buildS3Client();
    do {
      /**
       * Returns a list of summary information about the objects in the
       * specified bucket.
       */
      result = s3Client.listObjectsV2(request);
      /**
       * Collect the list of object summaries describing the objects stored in
       * the S3 bucket.
       */
      objectSummaries.addAll(result.getObjectSummaries());
      /**
       * If there are more than maxKeys keys in the bucket, get a continuation
       * token and list the next objects.
       */
      request.setContinuationToken(result.getNextContinuationToken());
    } while (result.isTruncated());
    /**
     * Done.
     */
    return objectSummaries;
  }

  /**
   * Uploads new object to the specified Amazon S3 bucket. Supports objects up
   * to 5 GB in size.
   * <p>
   * https://docs.aws.amazon.com/AmazonS3/latest/dev/UploadObjSingleOpJava.html
   *
   * @param fileObjectKeyName The key under which to store the new object.
   * @param file              The path of the file to upload to Amazon S3.
   * @param metadata          OPTIONAL. The object metadata. This includes
   *                          custom user-supplied metadata, as well as the
   *                          standard HTTP headers that Amazon S3 sends and
   *                          receives (Content-Type, Content-Length, ETag,
   *                          Content-MD5, etc.).
   * @throws AmazonServiceException The call was transmitted successfully, but
   *                                Amazon S3 couldn't process it, so it
   *                                returned an error response.
   * @throws SdkClientException     Amazon S3 couldn't be contacted for a
   *                                response, or the client couldn't parse the
   *                                response from Amazon S3.
   */
  public void upload(String fileObjectKeyName, Path file, ObjectMetadata metadata) throws AmazonServiceException, SdkClientException {
    /**
     * Creates the first object by specifying the bucket name, object key, and
     * text data directly in a call to AmazonS3Client.putObject().
     * <p>
     * The second object by using a PutObjectRequest that specifies the bucket
     * name, object key, and file path. The PutObjectRequest also specifies the
     * ContentType header and title metadata.
     * <p>
     * Inspect and optionally correct the fileObjectKeyName.
     */
    String key = fileObjectKeyName.startsWith("/")
                 ? fileObjectKeyName.substring(1)
                 : fileObjectKeyName;
    PutObjectRequest request = new PutObjectRequest(bucketName, key, file.toFile());
    /**
     * Conditionally set the metadata.
     */
    if (metadata != null) {
      request.setMetadata(metadata);
    }
    buildS3Client().putObject(request);
  }

  /**
   * Upload the specified input stream and object metadata to Amazon S3 under
   * the specified bucket and key name.
   *
   * @param fileObjectKeyName The key under which to store the specified file.
   * @param inputStream       The input stream containing the data to be
   *                          uploaded to Amazon S3.
   * @param metadata          Additional metadata instructing Amazon S3 how to
   *                          handle the uploaded data (e.g. custom user
   *                          metadata, hooks for specifying content type,
   *                          etc.).
   *
   * @return A PutObjectResult object containing the information returned by
   *         Amazon S3 for the newly created object.
   * @throws SdkClientException     If any errors are encountered in the client
   *                                while making the request or handling the
   *                                response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while
   *                                processing the request.
   */
  public PutObjectResult upload(String fileObjectKeyName, InputStream inputStream, ObjectMetadata metadata) throws AmazonServiceException, SdkClientException {
    String key = fileObjectKeyName.startsWith("/")
                 ? fileObjectKeyName.substring(1)
                 : fileObjectKeyName;
    /**
     * When using an BufferedInputStream as data source, please remember to use
     * a buffer of size no less than
     * RequestClientOptions.DEFAULT_STREAM_BUFFER_SIZE while initializing the
     * BufferedInputStream. This is to ensure that the SDK can correctly mark
     * and reset the stream with enough memory buffer during signing and
     * retries.
     */
    return buildS3Client().putObject(bucketName, key, inputStream, metadata);
  }

  /**
   * Use the TransferManager to upload a file in parts using several different
   * threads.
   * <p>
   * This triggers an asynchronous process. Check the progress and wait for the
   * download to complete before exiting.
   *
   * @param fileObjectKeyName The key under which to store the new object.
   * @param file              The path of the file to upload to Amazon S3.
   * @return an asynchronous upload job. Provides thread control, call back and
   *         polling
   * @throws AmazonServiceException The call was transmitted successfully, but
   *                                Amazon S3 couldn't process it, so it
   *                                returned an error response.
   * @throws SdkClientException     Amazon S3 couldn't be contacted for a
   *                                response, or the client couldn't parse the
   *                                response from Amazon S3.
   */
  public Upload uploadAsync(String fileObjectKeyName, Path file) throws AmazonServiceException, SdkClientException {
    /**
     * Build a standard transfer manager using the underlying default S3 client.
     */
    TransferManager tx = TransferManagerBuilder.standard().withS3Client(buildS3Client()).build();
    /**
     * Inspect and optionally correct the fileObjectKeyName.
     */
    String key = fileObjectKeyName.startsWith("/")
                 ? fileObjectKeyName.substring(1)
                 : fileObjectKeyName;
    /**
     * This is the callback interface which is used by
     * TransferManager.uploadDirectory and TransferManager.uploadFileList. The
     * callback is invoked for each file that is uploaded by TransferManager and
     * given an opportunity to specify the metadata for each file.
     */
    return tx.upload(bucketName, key, file.toFile());
  }

  /**
   * Recursively uploads all files in a directory. S3 will overwrite any
   * existing objects that happen to have the same key, just as when uploading
   * individual files, so use with caution.
   * <p>
   * This triggers an asynchronous process. Check the progress and wait for the
   * download to complete before exiting.
   *
   * @param virtualDirectoryKeyPrefix The key prefix of the virtual directory to
   *                                  upload to. Use the null or empty string to
   *                                  upload files to the root of the bucket.
   * @param directory                 The directory to upload.
   * @param metadataProvider          OPTIONAL. the callback interface invoked
   *                                  for each file to specify the metadata for
   *                                  the file.
   * @return Multiple file upload of an entire virtual directory. Contains a
   *         collection of sub transfers associated with the multi file upload.
   * @throws AmazonServiceException The call was transmitted successfully, but
   *                                Amazon S3 couldn't process it, so it
   *                                returned an error response.
   * @throws SdkClientException     Amazon S3 couldn't be contacted for a
   *                                response, or the client couldn't parse the
   *                                response from Amazon S3.
   */
  public MultipleFileUpload uploadDirectory(String virtualDirectoryKeyPrefix, Path directory, ObjectMetadataProvider metadataProvider) throws AmazonServiceException, SdkClientException {
    /**
     * Build a standard transfer manager using the underlying default S3 client.
     */
    TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(buildS3Client()).build();
    /**
     * bucketName - The name of the bucket to upload objects to.
     * <p>
     * virtualDirectoryKeyPrefix - The key prefix of the virtual directory to
     * upload to. Use the null or empty string to upload files to the root of
     * the bucket.
     * <p>
     * directory - The directory to upload.
     * <p>
     * includeSubdirectories - Whether to include subdirectories in the upload.
     * If true, files found in subdirectories will be included with an
     * appropriate concatenation to the key prefix.
     */
    return metadataProvider == null
           ? transferManager.uploadDirectory(bucketName, virtualDirectoryKeyPrefix, directory.toFile(), true)
           : transferManager.uploadDirectory(bucketName, virtualDirectoryKeyPrefix, directory.toFile(), true, metadataProvider);
  }

  /**
   * Get an Object Using the AWS SDK for Java
   * <p>
   * When you download an object through the AWS SDK for Java, Amazon S3 returns
   * all of the object's metadata and an input stream from which to read the
   * object's contents.
   * <p>
   * https://docs.aws.amazon.com/AmazonS3/latest/dev/RetrievingObjectUsingJava.html
   *
   * @param fileObjectKeyName The key where the online object is stored.
   * @param file              the local file to save the object
   * @throws AmazonServiceException The call was transmitted successfully, but
   *                                Amazon S3 couldn't process it, so it
   *                                returned an error response.
   * @throws SdkClientException     Amazon S3 couldn't be contacted for a
   *                                response, or the client couldn't parse the
   *                                response from Amazon S3.
   * @throws IOException            if the local file cannot be created or
   *                                written
   */
  public void download(String fileObjectKeyName, Path file) throws AmazonServiceException, SdkClientException, IOException {
    /**
     * Represents an object stored in Amazon S3. This object contains the data
     * content and the object metadata stored by Amazon S3, such as content
     * type, content length, etc. Use try with resources to ensure that the
     * network connection doesn't remain open.
     */
    try (S3Object s3Object = buildS3Client().getObject(new GetObjectRequest(bucketName, fileObjectKeyName));
         S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
      Files.copy(inputStream, file, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Get an Object Using the AWS SDK for Java.
   * <p>
   * This triggers an asynchronous process. Check the progress and wait for the
   * download to complete before exiting.
   *
   * @param fileObjectKeyName The key under which the object to download is
   *                          stored.
   * @param file              The file to download the object's data to.
   * @return A new Download object to use to check the state of the download,
   *         listen for progress notifications, and otherwise manage the
   *         download.
   * @throws AmazonClientException  If any errors are encountered in the client
   *                                while making the request or handling the
   *                                response.
   * @throws AmazonServiceException The call was transmitted successfully, but
   *                                Amazon S3 couldn't process it, so it
   *                                returned an error response.
   */
  public Download downloadAsync(String fileObjectKeyName, Path file) throws AmazonClientException, AmazonServiceException {
    /**
     * Build a standard transfer manager using the underlying default S3 client.
     */
    TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(buildS3Client()).build();
    /**
     * Schedules a new transfer to download data from Amazon S3 and save it to
     * the specified file. This method is non-blocking and returns immediately
     * (i.e. before the data has been fully downloaded). Use the returned
     * Download object to query the progress of the transfer, add listeners for
     * progress events, and wait for the download to complete.
     */
    return transferManager.download(bucketName, fileObjectKeyName, file.toFile());
  }

  /**
   * Downloads all objects in the virtual directory designated by the keyPrefix
   * given to the destination directory given. All virtual subdirectories will
   * be downloaded recursively.
   * <p>
   * Important: This triggers an asynchronous process. Check the progress and
   * wait for the download to complete before exiting.
   *
   * @param keyPrefix            The key prefix for the virtual directory, or
   *                             null for the entire bucket. All subdirectories
   *                             will be downloaded recursively.
   * @param destinationDirectory The directory to place downloaded files.
   *                             Subdirectories will be created as necessary.
   * @return Multiple file download of an entire virtual directory.
   */
  public MultipleFileDownload downloadDirectory(String keyPrefix, Path destinationDirectory) {
    /**
     * Build a standard transfer manager using the underlying default S3 client.
     */
    TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(buildS3Client()).build();
    /**
     * bucketName - The bucket containing the virtual directory
     * <p>
     * keyPrefix - The key prefix for the virtual directory, or null for the
     * entire bucket. All subdirectories will be downloaded recursively.
     * <p>
     * destinationDirectory - The directory to place downloaded files.
     * Subdirectories will be created as necessary.
     */
    return transferManager.downloadDirectory(bucketName, keyPrefix, destinationDirectory.toFile());
  }

  /**
   * Deleting Objects
   * <p>
   * You can delete one or more objects directly from Amazon S3. You have the
   * following options when deleting an object:
   * <p>
   * Delete a single object—Amazon S3 provides the DELETE API that you can use
   * to delete one object in a single HTTP request.
   * <p>
   * Delete multiple objects—Amazon S3 also provides the Multi-Object Delete API
   * that you can use to delete up to 1000 objects in a single HTTP request.
   * <p>
   * When deleting objects from a bucket that is not version-enabled, you
   * provide only the object key name, however, when deleting objects from a
   * version-enabled bucket, you can optionally provide version ID of the object
   * to delete a specific version of the object.
   *
   * @param fileObjectKeyName The key where the online object is stored.
   * @throws AmazonServiceException The call was transmitted successfully, but
   *                                Amazon S3 couldn't process it, so it
   *                                returned an error response.
   * @throws SdkClientException     Amazon S3 couldn't be contacted for a
   *                                response, or the client couldn't parse the
   *                                response from Amazon S3.
   */
  public void delete(String fileObjectKeyName) throws AmazonServiceException, SdkClientException {
    buildS3Client().deleteObject(new DeleteObjectRequest(bucketName, fileObjectKeyName));
  }

  /**
   * Provides options for deleting multiple objects in a specified bucket. You
   * may specify up to 1000 keys at a time.
   *
   * @param fileObjectKeyNames a collection of keys where the online objects are
   *                           stored.
   * @return Successful response to
   *         AmazonS3.deleteObjects(DeleteObjectsRequest). If one or more
   *         objects couldn't be deleted as instructed, a
   *         MultiObjectDeleteException is thrown instead.
   * @throws AmazonServiceException     The call was transmitted successfully,
   *                                    but Amazon S3 couldn't process it, so it
   *                                    returned an error response.
   * @throws SdkClientException         Amazon S3 couldn't be contacted for a
   *                                    response, or the client couldn't parse
   *                                    the response from Amazon S3.
   * @throws MultiObjectDeleteException - if one or more of the objects couldn't
   *                                    be deleted.
   */
  public DeleteObjectsResult delete(Collection<String> fileObjectKeyNames) throws AmazonServiceException, SdkClientException, MultiObjectDeleteException {
    /**
     * Convert the file key names to a list of delete names. KeyVersion is a key
     * to delete, with an optional version attribute.
     */
    List<KeyVersion> keys = fileObjectKeyNames.stream()
      .map(s -> new KeyVersion(s))
      .collect(Collectors.toList());
    /**
     * Construct a new DeleteObjectsRequest, specifying the bucket name. Set the
     * quiet element for this request. When true, only errors will be returned
     * in the service response.
     */
    DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName)
      .withQuiet(true)
      .withKeys(keys);
    return buildS3Client().deleteObjects(request);
  }

  /**
   * Schedules a new transfer to copy data from one Amazon S3 location to
   * another Amazon S3 location. This method is non-blocking and returns
   * immediately (i.e. before the copy has finished). TransferManager doesn't
   * support copying of encrypted objects whose encryption materials are stored
   * in an instruction file.
   * <p>
   * Use the returned Copy object to check if the copy is complete.
   * <p>
   * If resources are available, the copy request will begin immediately.
   * Otherwise, the copy is scheduled and started as soon as resources become
   * available.
   *
   * @param sourceKey      The name of the Amazon S3 object.
   * @param destinationKey The name of the object in the destination bucket.
   *
   * @return A new <code>Copy</code> object to use to check the state of the
   *         copy request being processed.
   *
   * @throws AmazonClientException  If any errors are encountered in the client
   *                                while making the request or handling the
   *                                response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while
   *                                processing the request.
   */
  public Copy copy(String sourceKey, String destinationKey) {
    return copy(bucketName, sourceKey, bucketName, destinationKey);
  }

  /**
   * Developer note: 12/01/18 This is not tested for buckets in different
   * availability zones.
   * <p>
   * Schedules a new transfer to copy data from one Amazon S3 location to
   * another Amazon S3 location. This method is non-blocking and returns
   * immediately (i.e. before the copy has finished). TransferManager doesn't
   * support copying of encrypted objects whose encryption materials are stored
   * in an instruction file.
   * <p>
   * Use the returned Copy object to check if the copy is complete.
   * <p>
   * If resources are available, the copy request will begin immediately.
   * Otherwise, the copy is scheduled and started as soon as resources become
   * available.
   * <p>
   * <b>Note:</b> If the {@code TransferManager} is created with a regional S3
   * client and the source and destination buckets are in different regions, use
   * the {@code #copy(CopyObjectRequest, AmazonS3, TransferStateChangeListener)}
   * method.
   *
   * @param sourceBucketName      The name of the bucket from where the object
   *                              is to be copied.
   * @param sourceKey             The name of the Amazon S3 object.
   * @param destinationBucketName The name of the bucket to where the Amazon S3
   *                              object has to be copied.
   * @param destinationKey        The name of the object in the destination
   *                              bucket.
   *
   * @return A new <code>Copy</code> object to use to check the state of the
   *         copy request being processed.
   *
   * @throws AmazonClientException  If any errors are encountered in the client
   *                                while making the request or handling the
   *                                response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while
   *                                processing the request.
   */
  public Copy copy(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
    /**
     * CopyObjectRequest extends AmazonWebServiceRequest implements
     * SSEAwsKeyManagementParamsProvider, Serializable, S3AccelerateUnsupported
     * <p>
     * Provides options for copying an Amazon S3 object from a source location
     * to a new destination. All CopyObjectRequests must specify a source bucket
     * and key, along with a destination bucket and key. Beyond that, requests
     * also specify: Object metadata for new object, a CannedAccessControlList
     * for the new object, and Constraints controlling if the copy will be
     * performed or not.
     */
//    CopyObjectRequest request = new CopyObjectRequest(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
    /**
     * Build a standard transfer manager using the underlying default S3 client.
     */
    TransferManager transferManager = TransferManagerBuilder.standard().withS3Client(buildS3Client()).build();
    return transferManager.copy(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
//    transferManager.cop
  }

  /**
   * Get S3 object URL.
   *
   * @param objectKey The name of the Amazon S3 object.
   * @return S3 object URL
   */
  public String getUrl(String objectKey) {
    return buildS3Client().getUrl(bucketName, objectKey).toString();
  }
}
