/*
 * Copyright 2019 Key Bridge. All rights reserved. Use is subject to license
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

import com.amazonaws.regions.Regions;

/**
 * An Amazon S3 bucket configuration containing access credentials and other
 * necessary configurations.
 *
 * @author Key Bridge
 * @since v1.2.0 created 01/31/19 for easier S3 client construction
 */
public class S3BucketConfig {

  /**
   * US_EAST_1. The default region.
   */
  private static final Regions REGION_DEFAULT = Regions.US_EAST_1;

  /**
   * The AWS access key.
   */
  private String awsAccessKeyId;
  /**
   * The AWS secret access key.
   */
  private String awsSecretAccessKey;
  /**
   * The AWS region.
   */
  private Regions region;
  /**
   * The AWS bucket name.
   */
  private String bucketName;

  public S3BucketConfig(Regions region, String bucketName) {
    this.region = region;
    this.bucketName = bucketName;
  }

  public String getAwsAccessKeyId() {
    return awsAccessKeyId;
  }

  public void setAwsAccessKeyId(String awsAccessKeyId) {
    this.awsAccessKeyId = awsAccessKeyId;
  }

  public String getAwsSecretAccessKey() {
    return awsSecretAccessKey;
  }

  public void setAwsSecretAccessKey(String awsSecretAccessKey) {
    this.awsSecretAccessKey = awsSecretAccessKey;
  }

  public Regions getRegion() {
    return region;
  }

  public void setRegion(Regions region) {
    this.region = region;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  @Override
  public String toString() {
    return "S3BucketConfig {" + "region=" + region + ", bucketName=" + bucketName + '}';
  }

}
