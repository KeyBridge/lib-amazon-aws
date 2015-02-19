/*
 *  Copyright (C) 2014 Caulfield IP Holdings (Caulfield) and/or its affiliates.
 *  All rights reserved. Use is subject to license terms.
 *
 *  Software Code is protected by Caulfield Copyrights. Caulfield hereby reserves
 *  all rights in and to Caulfield Copyrights and no license is granted under
 *  Caulfield Copyrights in this Software License Agreement. Caulfield generally
 *  licenses Caulfield Copyrights for commercialization pursuant to the terms of
 *  either Caulfield's Standard Software Source Code License Agreement or
 *  Caulfield's Standard Product License Agreement.
 *
 *  A copy of either License Agreement can be obtained on request by email from:
 *  info@caufield.org.
 */
package ch.keybridge.application.lib.amazon.aws;

import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.chunks.ChunkHelper;
import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientBuilder;

/**
 * Amazon S3 REST Client.
 * <p>
 * This is a convenience wrapper around the {@link AmazonS3} utility class,
 * providing automatic credential loading via a resource file plus simple access
 * to common methods.
 * <p>
 * @author Jesse Caulfield <jesse@caulfield.org>
 */
public class S3Client {

  private static final Logger logger = Logger.getLogger(S3Client.class.getName());

  /**
   * The properties file that this class reads to load the AWS credentials.
   */
  private static final String BUNDLE = "aws";

  /**
   * The S3 URL base. "https://s3.amazonaws.com/"
   */
  private static final String S3_URL_BASE = "https://s3.amazonaws.com/";

  /**
   * The actual Amazon S3 HTTP service provider interface instance. This is
   * automatically initialized in the constructor.
   */
  private final AmazonS3 amazonS3;

  /**
   * Construct a new Amazon S3 client using the provided keys.
   * <p>
   * @param accessKey the AWS S3 access key
   * @param secretKey the AWS S3 secret key
   */
  public S3Client(String accessKey, String secretKey) {
    this.amazonS3 = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
  }

  /**
   * Get the actual Amazon S3 client instance. This provides direct access to
   * the Amazon S3 service provider interface.
   * <p>
   * @return the Amazon S3 client.
   */
  public AmazonS3 getAmazonS3() {
    return amazonS3;
  }

  /**
   * Get a new Amazon S3 client instance.
   * <p>
   * The Amazon S3 access credentials are loaded from the ResourceBundle or
   * Properties.
   * <p>
   * @return an S3 instance ready for user
   * @throws Exception if the local file amazon.properties is not found in the
   *                   etc directory
   */
  public static S3Client getInstance() throws Exception {
    /**
     * Load the properties containing the Amazon S3 access credentials. FAIL if
     * the properties file is not present or invalid.
     */
    ResourceBundle bundle;
    try {
      bundle = ResourceBundle.getBundle(BUNDLE);
    } catch (Exception e) {
      throw new Exception(BUNDLE + " resource bundle not found. Please add this properties file to the classpath.");
    }
    /**
     * Instantiate a new Amazon S3 HTTP Client.
     */
    try {
      return new S3Client(bundle.getString("s3.aws.access.key"),
                          bundle.getString("s3.aws.secret.key"));
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Amazon S3 Client failed to initialize. {0}", e);
      throw new Exception(BUNDLE + " resource bundle does contain s3.aws.access.key or secret.key entries.");
    }

  }

  /**
   * Upload the indicated file to Amazon AWS.
   * <p/>
   * Developer note: Amazon S3 Clients must allow for acceptance of self-signed
   * certificates. or, if the servers identity does not matter, can
   * alternatively disable checking the server authenticity. This is done by
   * adding the run-time flag '-Dcom.amazonaws.sdk.disableCertChecking=true' to
   * the VM options, which adds the tag a a command-line flag.
   * <p/>
   * @param bucketName the S3 top level bucket name
   * @param prefix     (optional) A parameter restricting the response to keys
   *                   beginning with the specified prefix. Use prefixes to
   *                   separate a bucket into different sets of keys, similar to
   *                   how a file system organizes files into directories. Set
   *                   to NULL to place the file at the root of the bucket.
   * @param file       the WSDBA zip file to be uploaded
   * @return the image object ETag value produced by the S3 HTTP(S) server
   * @throws IOException if the local file cannot be read
   * @throws Exception   if the Amazon S3 client encounters and error uploading
   *                     the file object
   */
  public String uploadFile(String bucketName, String prefix, Path file) throws IOException, Exception {
    /**
     * Generate the key under which to store the new object. The File Key is the
     * fully qualified file name (including path prefix) but not including the
     * ROOT bucket.
     */
    String putObjectFileKey = (prefix == null || prefix.isEmpty())
                              ? ""
                              : (prefix + "/")
                                + file.getFileName();
    /**
     * Upload an object to the bucket. You can also specify your own metadata
     * when uploading to S3, which allows you set a variety of options like
     * content-type and content-encoding, plus additional metadata specific to
     * your applications.
     * <p>
     * Construct a new PutObjectRequest object to upload a file to the specified
     * bucket and key. After constructing the request specify object canned ACL:
     * the pre-configured access control policy to use for the new object.
     */
    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, putObjectFileKey, file.toFile());
    putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
    /**
     * Uploads a new object to the specified Amazon S3 bucket.
     */
    try {
      amazonS3.putObject(putObjectRequest);
    } catch (AmazonClientException amazonClientException) {
      logger.log(Level.SEVERE, "S3Client upload error: {0}", amazonClientException.getMessage());
      throw new Exception("AmazonClientException means the client encountered "
                          + "a serious internal problem while trying to communicate with S3, "
                          + "such as not being able to access the network: " + amazonClientException.getMessage());
    }
    /**
     * Query the S3 server and return the new image ETag value.
     */
    return getETag(bucketName + "/" + prefix + file.getFileName());
  }

  /**
   * Helper method to get the HTTP(S) server generated ETag value for the
   * indicated path and filename.
   * <p>
   * @param uri a complete URI pointing to the remote file:
   * @return the server generated ETag value - NULL on failure.
   */
  public static String getETag(URI uri) {
    try {
      return ClientBuilder.newClient().target(uri).request().head().getHeaderString("ETag");
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Helper method to get the AWS-S3 HTTP(S) server generated ETag value for the
   * indicated path and filename.
   * <p>
   * @param urlPath the URL object path and filename: e.g. the S3 object key
   *                (URL Path) and filename. The S3 server is prepended to this
   *                string to create a fully qualified URL. This string should
   *                not begin with a slash "/"
   * @return the S3 server generated ETag value - NULL on failure.
   */
  public static String getETag(String urlPath) throws IllegalArgumentException {
    if (urlPath.toLowerCase(Locale.getDefault()).startsWith("http")) {
      throw new IllegalArgumentException("URL path should ONLY contain the PATH portion of a URL: " + urlPath);
    }
    /**
     * Build a new web resource target for the indicated filename and key index
     * (URL path).
     * <p>
     * Invoke HTTP HEAD method for the current request synchronously and get the
     * "ETag" string header. If the message header is not present then null is
     * returned. If the message header is present but has no value then the
     * empty string is returned. If the message header is present more than once
     * then the values of joined together and separated by a ',' character.
     */
    try {
      return ClientBuilder.newClient().target(S3_URL_BASE + urlPath).request().head().getHeaderString("ETag");
    } catch (Exception e) {
      logger.log(Level.SEVERE, null, e);
      return null;
    }
  }

  /**
   * Get the image metadata describing the geographic extent of a map overlay
   * image for an image stored on the Amazon S3 system.
   * <p>
   * @param urlPath the bucket, path (and filename) pointing to the PNG image of
   *                interest
   * @return a non-null Map of key/value pairs describing the geographic extent
   *         of an overlay.
   */
  public static Map<String, String> getImageMetaData(String urlPath) {
    if (urlPath.toLowerCase(Locale.getDefault()).startsWith("http://")) {
      throw new IllegalArgumentException("URL path should ONLY contain the PATH portion of a URL: " + urlPath);
    }
    /**
     * Create a key/value map to store our keywords of interest.
     */
    Map<String, String> imageMetadata = new HashMap<>();
    /**
     * Download the image and reads its metadata. Extract the values into a
     * metadata map.
     */
    try {
      PngReader pngReader = new PngReader(new URL(S3_URL_BASE + urlPath).openStream());
      pngReader.readSkippingAllRows();
      for (PngChunk pngChunk : pngReader.getChunksList().getChunks()) {
        if (!ChunkHelper.isText(pngChunk)) {
          continue;
        }
        PngChunkTextVar pngChunkTextVar = (PngChunkTextVar) pngChunk;
        imageMetadata.put(pngChunkTextVar.getKey(), pngChunkTextVar.getVal());
      }
      pngReader.end();
      return imageMetadata;
    } catch (IOException exception) {
      logger.log(Level.SEVERE, null, exception);
    }
    return imageMetadata;
  }

  //<editor-fold defaultstate="collapsed" desc="Remote File Utilities">
  /**
   * Remove all files on S3 within the indicated bucket and prefix.
   * <p>
   * For example, if given "bucket" and "a/b/c/" this method will delete all
   * files on the Amazon S3 system residing under "/bucket/a/b/c".
   * <p>
   * @param bucketName the S3 top level bucket name
   * @param prefix     a file prefix name. This is the S3 equivalent of a
   *                   directory path. The prefix must NOT contain a preceeding
   *                   slash but should contain a trailing slash. e.g. NOT
   *                   "/a/b/c" but rather "a/b/c/".
   */
  public void removeRemoteFiles(String bucketName, String prefix) {
    /**
     * Delete all current objects.
     */
    ObjectListing objectList = amazonS3.listObjects(bucketName, prefix);
    for (S3ObjectSummary s3ObjectSummary : objectList.getObjectSummaries()) {
      amazonS3.deleteObject(bucketName, s3ObjectSummary.getKey());
    }
  }

  /**
   * Remove a single file on S3 within the indicated bucket and prefix.
   * <p>
   * For example, if given "bucket" and "a/b/c/" and the filename is "foo.bar"
   * this method will delete the file on the Amazon S3 system residing at
   * "/bucket/a/b/c/foo.bar".
   * <p>
   * @param bucketName the S3 top level bucket name
   * @param prefix     a file prefix name. This is the S3 equivalent of a
   *                   directory path. The prefix must NOT contain a preceeding
   *                   slash but should contain a trailing slash. e.g. NOT
   *                   "/a/b/c" but rather "a/b/c/".
   * @param fileName   the filename, including extension.
   */
  public void removeRemoteFile(String bucketName, String prefix, String fileName) {
    amazonS3.deleteObject(bucketName, prefix + fileName);
  }//</editor-fold>

}
