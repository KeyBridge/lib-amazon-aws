/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.keybridge.application.lib.amazon.aws;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author jesse
 */
public class S3ClientTest extends TestCase {

  private final S3Client client;

  public S3ClientTest() throws IOException {
    Properties p = new Properties();
    p.load(Files.newInputStream(Paths.get(System.getProperty("user.home"), "etc", "aws.properties")));
    client = new S3Client(p.getProperty("s3.aws.access.key"),
                          p.getProperty("s3.aws.secret.key"));
  }

  /**
   * Test of uploadFile method, of class S3Client.
   */
  public void testUploadFile() {
    URL file = S3ClientTest.class.getClassLoader().getResource("1267441.png");
    assertNotNull(file);
//    System.out.println("  testUploadFile " + file);
    assertNotNull(client);
    try {
      client.uploadFile("kbs3temp", "test", Paths.get(file.toURI()));
    } catch (Exception ex) {
      Logger.getLogger(S3ClientTest.class.getName()).log(Level.SEVERE, null, ex);
      fail(ex.getMessage());
    }
    System.out.println("  test Upload File OK");
  }

  /**
   * Test of getETag method, of class S3Client.
   */
  public void testGetETag() {
    String etag = S3Client.getETag("kbs3temp/test/1267441.png");
    assertNotNull(etag);
    System.out.println("  test Get ETag OK: " + etag);
  }

  /**
   * Test of getImageMetaData method, of class S3Client.
   */
  public void testGetImageMetaData() {
    Map<String, String> metadata = S3Client.getImageMetaData("kbs3temp/test/1267441.png");
    assertFalse(metadata.isEmpty());
    System.out.println("  test Get Image MetaData OK: " + metadata);
  }

  /**
   * Test of removeRemoteFiles method, of class S3Client.
   */
  public void _testRemoveRemoteFiles() {
    System.out.println("  test Remove Remote Files");
  }

  /**
   * Test of removeRemoteFile method, of class S3Client.
   */
  public void _testRemoveRemoteFile() {
    System.out.println("  test Remove Remote File");
  }

}
