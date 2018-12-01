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
package ch.keybridge.lib.aws.type;

/**
 * AWS Regions and Endpoints
 * <p>
 * To reduce data latency in your applications, most Amazon Web Services offer a
 * regional endpoint to make your requests. An endpoint is a URL that is the
 * entry point for a web service. For example,
 * https://dynamodb.us-west-2.amazonaws.com is an entry point for the Amazon
 * DynamoDB service.
 * <p>
 * Some services, such as IAM, do not support regions; therefore, their
 * endpoints do not include a region. Some services, such as Amazon EC2, let you
 * specify an endpoint that does not include a specific region, for example,
 * https://ec2.amazonaws.com. In that case, AWS routes the endpoint to
 * us-east-1.
 * <p>
 * If a service supports regions, the resources in each region are independent.
 * For example, if you create an Amazon EC2 instance or an Amazon SQS queue in
 * one region, the instance or queue is independent from instances or queues in
 * another region.
 *
 * @author Key Bridge
 * @see <a href="https://docs.aws.amazon.com/general/latest/gr/rande.html">AWS
 * Regions</a>
 */
public enum AwsRegion {

  /**
   * US East (Ohio)
   */
  US_EAST_2("us-east-2"),
  /**
   * US East (N. Virginia)
   */
  US_EAST_1("us-east-1"),
  /**
   * US West (N. California)
   */
  US_WEST_1("us-west-1"),
  /**
   * US West (Oregon)
   */
  US_WEST_2("us-west-2"),
  /**
   * Asia Pacific (Mumbai)
   */
  AP_SOUTH_1("ap-south-1"),
  /**
   * Asia Pacific (Osaka-Local) ***
   */
  AP_NORTHEAST_3("ap-northeast-3"),
  /**
   * Asia Pacific (Seoul)
   */
  AP_NORTHEAST_2("ap-northeast-2"),
  /**
   * Asia Pacific (Singapore)
   */
  AP_SOUTHEAST_1("ap-southeast-1"),
  /**
   * Asia Pacific (Sydney)
   */
  AP_SOUTHEAST_2("ap-southeast-2"),
  /**
   * Asia Pacific (Tokyo)
   */
  AP_NORTHEAST_1("ap-northeast-1"),
  /**
   * Canada (Central)
   */
  CA_CENTRAL_1("ca-central-1"),
  /**
   * China (Beijing)
   */
  CN_NORTH_1("cn-north-1"),
  /**
   * China (Ningxia)
   */
  CN_NORTHWEST_1("cn-northwest-1"),
  /**
   * EU (Frankfurt)
   */
  EU_CENTRAL_1("eu-central-1"),
  /**
   * EU (Ireland)
   */
  EU_WEST_1("eu-west-1"),
  /**
   * EU (London)
   */
  EU_WEST_2("eu-west-2"),
  /**
   * EU (Paris)
   */
  EU_WEST_3("eu-west-3"),
  /**
   * South America (São Paulo)
   */
  SA_EAST_1("sa-east-1");

  /**
   * The Region code.
   */
  private final String region;

  private AwsRegion(String name) {
    this.region = name;
  }

  /**
   * Get the region value.
   *
   * @return the region
   */
  public String getRegion() {
    return region;
  }

  public static AwsRegion fromRegion(String region) {
    return AwsRegion.valueOf(region.toUpperCase().replaceAll("-", "_").trim());
  }

  @Override
  public String toString() {
    return region;
  }

}
