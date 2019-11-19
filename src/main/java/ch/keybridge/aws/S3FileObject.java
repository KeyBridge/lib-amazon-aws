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
package ch.keybridge.aws;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Contains the summary of an object stored in an Amazon S3 bucket. This object
 * doesn't contain contain the object's full metadata or any of its contents.
 *
 * @author Key Bridge
 * @since v1.2.0 added 02/01/19
 */
@XmlRootElement(name = "S3FileObject")
@XmlType(name = "S3FileObject")
@XmlAccessorType(XmlAccessType.FIELD)
public class S3FileObject {

  /**
   * The name of the bucket in which this object is stored
   */
  protected String bucketName;

  /**
   * The key under which this object is stored
   */
  protected String key;

  /**
   * Hex encoded MD5 hash of this object's contents, as computed by Amazon S3
   */
  protected String eTag;

  /**
   * The size of this object, in bytes
   */
  protected long size;

  /**
   * The date, according to Amazon S3, when this object was last modified
   */
  protected Date lastModified;

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String geteTag() {
    return eTag;
  }

  public void seteTag(String eTag) {
    this.eTag = eTag;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

}
