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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
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

/**
 *
 * @author Key Bridge
 */
public class AwsS3Test {

  private AwsS3 client;

  public AwsS3Test() {
  }

  @Before
  public void setUp() {
    Regions region = Regions.US_EAST_1;
    this.client = new AwsS3(region);

    /**
     * This is a temp bucket (manually) created for this test set. It will not
     * work for you. Add you own.
     */
    String bucketName = "temp-i0i0";
    client.setBucketName(bucketName);
    client.setRegion(region);
  }

  @After
  public void tearDown() {
  }

//  @Test
  public void testList() {
    /**
     * List the content of a bucket. The AwsS3 client has an internal loop to
     * fetch information about ALL objects. For some buckets with very large
     * number of files (such as logging) this can take a while. Turn on FINE
     * logging to watch as the list client goes back for more information until
     * it has retrieved all it needs.
     */
    client.setMaxKeys(3); // fetch object information thee files as a time (very small)

    List<S3ObjectSummary> response = client.list(null);

    /**
     * Dump the info:
     */
    System.out.println(" ------------------------------------------------------- ");
    for (S3ObjectSummary s3ObjectSummary : response) {
      System.out.println("   " + s3ObjectSummary);
    }
    System.out.println(" ------------------------------------------------------- ");
  }

//  @Test
  public void testUpload() throws AmazonClientException, AmazonServiceException, InterruptedException {
    /**
     * Blocking upload function.
     */
//    client.upload();
    Upload upload = client.uploadAsync("tmp/nbbackup/foo.tmp", Paths.get("/tmp/nbbackup6785828523077417973.tmp"));
    UploadResult result = upload.waitForUploadResult();
    System.out.println("  uploaded ");
    System.out.println("  " + result.getETag());
  }

//  @Test
  public void testUploadDirectory() throws InterruptedException {
    /**
     * Non blocking function to Recursively upload a local directory.
     * <p>
     * Developer Note: this is non-blocking, but will abort as soon as the
     * program exits. The logic below monitors the transfer progress then waits
     * a second for the transaction to complete before exiting.
     */
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
     * Wait for the last file transfer to complete before exiting.
     */
    Thread.sleep(1000);

  }

//  @Test
  public void testDownload() throws IOException {
    /**
     * Blocking download function. Like HTTP get.
     */
    String fileObjectKeyName = "etl/foo/nbbackup1451552495868070804.tmp";
    client.download(fileObjectKeyName, Paths.get("/tmp/bar/foo.tmp"));
  }

//  @Test
  public void downloadAsync() throws InterruptedException {
    /**
     * Non-blocking download function. As with upload, you must wait until the
     * transfer is complete before exiting or the transaction will abort (and
     * the file content will be discarded.)
     */
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
     * Wait for the last file transfer to complete before exiting.
     */
    Thread.sleep(1000);
  }

//  @Test
  public void downloadDirectory() throws InterruptedException {
    /**
     * Non-blocking function to recursively download an entire directory tree.
     * As above, you must wait until completion before exiting.
     */
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
     * Wait for the last file transfer to complete before exiting.
     */
    Thread.sleep(1000);
  }

//  @Test
  public void testDelete() {
    /**
     * Blocking delete function.
     */
    String fileObjectKeyName = "etl/foo/nbbackup1451552495868070804.tmp";
    client.delete(fileObjectKeyName);
  }

//  @Test
  public void testdeleteManu() {
    /**
     * Blocking delete function.
     * <p>
     * Developer note: Supposedly this supports deleting up to 1000 files in one
     * call. We've not gotten that to work doe to an XML encoding error internal
     * to the Version 1.x API. There are bugs issued (ca 2017) and the fix is
     * supposedly implemented in the Version 2.x API.
     * <p>
     * This does seem to work for a small number of files (~10 to 20).
     */
    List<String> files = Arrays.asList("etl/foo/nbbackup1451552495868070804.tmp",
                                       "etl/foo/nbbackup6739056349827850053.tmp",
                                       "etl/foo/nbbackup1973598169851388919.tmp",
                                       "etl/foo/nbbackup6784960907250871608.tmp",
                                       "etl/foo/nbbackup2217161633707386187.tmp",
                                       "etl/foo/nbbackup6785828523077417973.tmp",
                                       "etl/foo/nbbackup2236090102726107993.tmp",
                                       "etl/foo/nbbackup6788697954604782842.tmp",
                                       "etl/foo/nbbackup2469693039842925381.tmp",
                                       "etl/foo/nbbackup708099909082382504.tmp",
                                       "etl/foo/nbbackup2829970401009443746.tmp",
                                       "etl/foo/nbbackup7111438522893165904.tmp");
    DeleteObjectsResult result = client.delete(files);

    System.out.println("delete " + result.getDeletedObjects().size() + " files OK");

  }

}
