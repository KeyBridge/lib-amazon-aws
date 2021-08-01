package ch.keybridge.aws.s3;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A DTO that holds parsed S3 key information.
 *
 * @author Andrius Druzinis-Vitkus
 * @since 0.0.1 created 2019-02-27
 */
public class S3Key {

  /**
   * The S3 key verbatim.
   */
  private final String keyRaw;
  /**
   * Parsed key prefix
   */
  private final List<String> intermediatePath;
  /**
   * File name
   */
  private final String fileName;
  /**
   * File extension
   */
  private final String extension;

  public S3Key(String keyRaw) {
    this.keyRaw = Objects.requireNonNull(keyRaw);

    String[] tokens = keyRaw.split("/");
    intermediatePath = Arrays.asList(tokens).subList(0, tokens.length - 1);
    fileName = tokens[tokens.length - 1];

    /**
     * Find the extension, if present.
     */
    final int extensionSeparator = fileName.lastIndexOf('.');
    extension = extensionSeparator == -1 ? null : fileName.substring(extensionSeparator + 1);
  }

  /**
   * Get the unmodified S3 Key.
   *
   * @return unmodified S3 key.
   */
  public String getKeyRaw() {
    return keyRaw;
  }

  /**
   * Path segments contained in the key. For S3 key a/b/c/d/file.png, returns
   * ["a", "b", "c", "d"].
   *
   * @return Path segments contained
   */
  public List<String> getIntermediatePath() {
    return intermediatePath;
  }

  /**
   * Get file name.
   *
   * @return file name.
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Get file extension.
   *
   * @return file extension.
   */
  public String getExtension() {
    return extension;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    S3Key key = (S3Key) o;

    if (keyRaw != null ? !keyRaw.equals(key.keyRaw) : key.keyRaw != null) {
      return false;
    }
    if (intermediatePath != null ? !intermediatePath.equals(key.intermediatePath) : key.intermediatePath != null) {
      return false;
    }
    if (fileName != null ? !fileName.equals(key.fileName) : key.fileName != null) {
      return false;
    }
    return extension != null ? extension.equals(key.extension) : key.extension == null;
  }

  @Override
  public int hashCode() {
    int result = keyRaw != null ? keyRaw.hashCode() : 0;
    result = 31 * result + (intermediatePath != null ? intermediatePath.hashCode() : 0);
    result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
    result = 31 * result + (extension != null ? extension.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "S3Key{"
      + "keyRaw='" + keyRaw + '\''
      + ", intermediatePath=" + intermediatePath
      + ", fileName='" + fileName + '\''
      + ", extension='" + extension + '\''
      + '}';
  }
}
