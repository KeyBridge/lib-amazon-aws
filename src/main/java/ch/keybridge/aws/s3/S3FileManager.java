package ch.keybridge.aws.s3;

import ch.keybridge.aws.AwsS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * S3 File manager.
 *
 * @author Andrius Druzinis-Vitkus
 * @since 0.0.1 created 2019-02-27
 */
public class S3FileManager {

  private static final Logger LOG = Logger.getLogger(S3FileManager.class.getName());

  /**
   * The configured S3 client instance.
   */
  private final AwsS3 s3Client;

  /**
   * Construct a new S3FileManager instance.
   *
   * @param s3Client the S3 client to use
   */
  public S3FileManager(AwsS3 s3Client) {
    this.s3Client = s3Client;
  }

  /**
   * Delete and S3 object.
   *
   * @param s3Key valid S3 object key
   */
  public void deleteFile(String s3Key) {
    LOG.log(Level.FINE, "Deleting key {0}", s3Key);
    s3Client.delete(s3Key);
  }

  /**
   * Get the URL of an S3 object
   *
   * @param s3Key valid S3 object key
   * @return URL of the associated S3 object.
   */
  public String getS3ObjectUrl(String s3Key) {
    return s3Client.getUrl(s3Key);
  }

  /**
   * Upload file using the provided S3 key and metadata
   *
   * @param s3Key       valid S3 object key
   * @param inputStream input stream containing file data
   * @param metadata    file metadata
   * @return operation result metadata
   */
  public PutObjectResult uploadFile(String s3Key, InputStream inputStream, ObjectMetadata metadata) {
    return s3Client.upload(s3Key, inputStream, metadata);
  }

  /**
   * Get all files in all available buckets.
   * <p>
   * Security warning: This method lists ALL files under the root bucket. It
   * should only be available to called by application users with admin
   * privileges.
   *
   * @return a file hierarchy containing all available files.
   */
  public FileNode getFileTree() {
    return parseS3FilesAsHierarchy(s3Client.list(""));
  }

  /**
   * Get all files under a specified sub-directory within the root bucket. e.g.
   * For user storage this will is that belong to a specific user.
   *
   * @param path the sub-directory path
   * @return a file hierarchy containing files under the path.
   */
  public FileNode getUserFiles(String path) {
    final String prefix = path + '/';
    return parseS3FilesAsHierarchy(s3Client.list(prefix));
  }

  /**
   * Parse a list of S3ObjectSummary objects as a file hierarchy. The root
   * element is always a directory node with the name 'root'.
   * <p>
   * <h2>Example</h2>
   * Given this input:
   * <pre>
   * uc/87982fbbd3/206a2dcd-68db-d45f-ae67-e63854b045c3.xml
   * uc/87982fbbd3/world_sovereign_border.sql.gz
   * uc/90afd80709/01acd559-52da-aa36-a09f-9e042bed3e91.png
   * uc/90afd80709/07313419-f0b9-70ee-5a89-0b40070070c7.jpg
   * </pre> it will return the following tree:
   * <pre>
   * root: uc: 87982fbbd3: 206a2dcd-68db-d45f-ae67-e63854b045c3.xml
   * world_sovereign_border.sql.gz 90afd80709:
   * 01acd559-52da-aa36-a09f-9e042bed3e91.png
   * 07313419-f0b9-70ee-5a89-0b40070070c7.jpg
   * </pre>
   *
   * @param files
   * @return
   */
  private static FileNode parseS3FilesAsHierarchy(List<S3ObjectSummary> files) {
    final FileNode root = new FileNode("root");
    for (S3ObjectSummary s3ObjectSummary : files) {
      S3Key parsedKey = new S3Key(s3ObjectSummary.getKey());
      FileNode parentDir = root.getOrCreate(s3ObjectSummary.getBucketName());
      for (String intermediateDirectory : parsedKey.getIntermediatePath()) {
        parentDir = parentDir.getOrCreate(intermediateDirectory);
      }
      FileNode file = parentDir.getOrCreate(parsedKey.getFileName());
      file.setS3ObjectSummary(s3ObjectSummary);
    }
    return root;
  }

}
