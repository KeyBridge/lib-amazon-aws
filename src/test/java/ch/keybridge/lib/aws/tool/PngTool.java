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
package ch.keybridge.lib.aws.tool;

import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.chunks.ChunkHelper;
import ar.com.hjg.pngj.chunks.PngChunk;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientBuilder;

/**
 * Relies upon PNGJ : pure Java library for high performance PNG encoding
 * http://hjg.com.ar/pngj/
 *
 * @author Key Bridge
 */
public class PngTool {

  private static final Logger LOG = Logger.getLogger(PngTool.class.getName());

  /**
   * The S3 URL base. "https://s3.amazonaws.com/"
   */
  private static final String S3_URL_BASE = "https://s3.amazonaws.com/";

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
      throw new IllegalArgumentException("URL path should ONLY contain the PATH portion of a URL and not the domain.  " + urlPath);
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
      LOG.log(Level.SEVERE, null, e);
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
      throw new IllegalArgumentException("URL path should ONLY contain the PATH portion of a URL and not the domain.  " + urlPath);
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
      LOG.log(Level.SEVERE, null, exception);
    }
    return imageMetadata;
  }
}
