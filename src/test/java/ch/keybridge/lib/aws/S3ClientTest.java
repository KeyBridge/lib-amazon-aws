/*
 * Copyright 2018 Key Bridge. All rights reserved. Use is subject to license
 * terms.
 *
 * This software code is protected by Copyrights and remains the property of
 * Key Bridge and its suppliers, if any. Key Bridge reserves all rights in and to
 * Copyrights and no license is granted under Copyrights in this Software
 * License Agreement.
 *
 * Key Bridge generally licenses Copyrights for commercialization pursuant to
 * the terms of either a Standard Software Source Code License Agreement or a
 * Standard Product License Agreement. A copy of either Agreement can be
 * obtained upon request by sending an email to info@keybridgewireless.com.
 *
 * All information contained herein is the property of Key Bridge and its
 * suppliers, if any. The intellectual and technical concepts contained herein
 * are proprietary.
 */
package ch.keybridge.lib.aws;

import ch.keybridge.lib.aws.type.AwsRegion;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.transfer.*;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Key Bridge
 */
public class S3ClientTest {

  private S3Client client;

  public S3ClientTest() {
  }

  @Before
  public void setUp() {
    this.client = new S3Client();
    AwsRegion region = AwsRegion.US_EAST_1;
    String bucketName = "kbs3temp";
    client.setBucketName(bucketName);
    client.setRegion(region);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testList() {

//Bucket ARN: arn:aws:s3:::kbs3temp
    client.setMaxKeys(3);

    List<S3ObjectSummary> response = client.list(null);

    System.out.println(" ------------------------------------------------------- ");
    for (S3ObjectSummary s3ObjectSummary : response) {
      System.out.println("   " + s3ObjectSummary);
    }
    System.out.println(" ------------------------------------------------------- ");
  }

//  @Test
  public void testUpload() throws AmazonClientException, AmazonServiceException, InterruptedException {
//    client.upload();
    Upload upload = client.uploadAsync("tmp/nbbackup/foo.tmp", Paths.get("/tmp/nbbackup6785828523077417973.tmp"));
    UploadResult result = upload.waitForUploadResult();
    System.out.println("  uploaded ");
    System.out.println("  " + result.getETag());
  }

//  @Test
  public void testUploadDirectory() throws InterruptedException {
    MultipleFileUpload uploads = client.uploadDirectory("etl", Paths.get("/tmp/etl"), null);
    TransferProgress progress = uploads.getProgress();
    /**
     * Progress ranges from 0 to 100.
     */
    double tp = progress.getPercentTransferred();

    while (tp < 100) {
      Thread.sleep(100);
      tp = progress.getPercentTransferred();
      System.out.println("  " + tp + "% " + progress.getBytesTransferred() + " of " + progress.getTotalBytesToTransfer() + " bytes");
    }
    /**
     * Wait for the last file transfer to finish before exiting.
     */
    Thread.sleep(1000);

  }

//  @Test
  public void testDownload() throws IOException {
    String fileObjectKeyName = "etl/foo/nbbackup1451552495868070804.tmp";
    client.download(fileObjectKeyName, Paths.get("/tmp/bar/foo.tmp"));
  }

//  @Test
  public void downloadAsync() throws InterruptedException {
    String fileObjectKeyName = "etl/foo/nbbackup1451552495868070804.tmp";
    Path file = Paths.get("/tmp/bar/foo.tmp");
    Download dn = client.downloadAsync(fileObjectKeyName, file);

    TransferProgress progress = dn.getProgress();
    double tp = progress.getPercentTransferred();

    while (tp < 100) {
      Thread.sleep(10);
      tp = progress.getPercentTransferred();
      System.out.println("  " + tp + "% " + progress.getBytesTransferred() + " of " + progress.getTotalBytesToTransfer() + " bytes");
    }
    /**
     * Wait for the last file transfer to finish before exiting.
     */
    Thread.sleep(1000);
  }

//  @Test
  public void downloadDirectory() throws InterruptedException {
    String keyPrefix = "etl";
    Path destinationDirectory = Paths.get("/tmp/bar");

    MultipleFileDownload dn = client.downloadDirectory(keyPrefix, destinationDirectory);

    TransferProgress progress = dn.getProgress();
    double tp = progress.getPercentTransferred();

    while (tp < 100) {
      Thread.sleep(100);
      tp = progress.getPercentTransferred();
      System.out.println("  " + tp + "% " + progress.getBytesTransferred() + " of " + progress.getTotalBytesToTransfer() + " bytes");
    }
    /**
     * Wait for the last file transfer to finish before exiting.
     */
    Thread.sleep(1000);
  }

//  @Test
  public void testDelete() {
    String fileObjectKeyName = "etl/foo/nbbackup1451552495868070804.tmp";
    client.delete(fileObjectKeyName);
  }

//  @Test
  public void testdeleteManu() {
    List<String> files = Arrays.asList("etl/foo/nbbackup1451552495868070804.tmp", "etl/foo/nbbackup6739056349827850053.tmp", "etl/foo/nbbackup1973598169851388919.tmp", "etl/foo/nbbackup6784960907250871608.tmp", "etl/foo/nbbackup2217161633707386187.tmp", "etl/foo/nbbackup6785828523077417973.tmp", "etl/foo/nbbackup2236090102726107993.tmp", "etl/foo/nbbackup6788697954604782842.tmp", "etl/foo/nbbackup2469693039842925381.tmp", "etl/foo/nbbackup708099909082382504.tmp", "etl/foo/nbbackup2829970401009443746.tmp", "etl/foo/nbbackup7111438522893165904.tmp");
    DeleteObjectsResult result = client.delete(files);

    System.out.println("delete manu files OK");

  }

}
