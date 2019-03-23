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
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Amazon Simple Email Service Client.
 *
 * @author Key Bridge
 */
public class AwsSES {

  private static final Logger LOG = Logger.getLogger(AwsSES.class.getName());

  /**
   * Construct a new Simple Email Service Client.
   */
  public AwsSES() {
    /**
     * @TODO: write Shared Credentials File if not present at ~/.aws/credentials
     *
     * <pre>
     *   [default]
     *   aws_access_key_id = YOUR_AWS_ACCESS_KEY_ID
     *   aws_secret_access_key = YOUR_AWS_SECRET_ACCESS_KEY
     * </pre>
     *
     * See
     * https://docs.aws.amazon.com/ses/latest/DeveloperGuide/create-shared-credentials-file.html
     */
  }

  /**
   * Send an Email through Amazon SES Programmatically using an AWS SDK.
   * <p>
   * To send an email using the Amazon SES API, you can use the Query interface
   * directly, or you can use an AWS SDK to handle low-level details such as
   * assembling and parsing HTTP requests and responses.
   * <p>
   * File Attachments: To attach a file to an email, you have to encode the
   * attachment using base64 encoding. Attachments are typically placed in
   * dedicated MIME message parts.
   *
   * @param recipientEmail     A "To" address.
   * @param senderEmail        The "From" address. This address must be verified
   *                           with Amazon SES.
   * @param subject            The subject line for the email.
   * @param htmlMessageContent The HTML formatted message body for the email.
   * @param textMessageContent The text message body for recipients with
   *                           non-HTML email clients.
   */
  public void sendMail(String recipientEmail, String senderEmail, String subject, String htmlMessageContent, String textMessageContent) {
    try {
      // Replace US_WEST_2 with the AWS Region you're using for Amazon SES.
      AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

      SendEmailRequest request = new SendEmailRequest()
        .withSource(senderEmail) // From
        .withDestination(new Destination().withToAddresses(recipientEmail)) // To
        .withMessage(new Message()
          .withBody(new Body()
            .withHtml(new Content().withCharset("UTF-8").withData(htmlMessageContent))
            .withText(new Content().withCharset("UTF-8").withData(textMessageContent)))
          .withSubject(new Content().withCharset("UTF-8").withData(subject)));
//        .withConfigurationSetName(CONFIGSET);

//      if (htmlMessageContent != null) {        request.getMessage().getBody().setHtml(new Content().withCharset("UTF-8").withData(htmlMessageContent));      }
//      if (textMessageContent != null) {        request.getMessage().getBody().setText(new Content().withCharset("UTF-8").withData(textMessageContent));      }
      client.sendEmail(request);
      LOG.log(Level.INFO, "Sending email To: {0} Re: {1}", new Object[]{recipientEmail, subject});
    } catch (Exception ex) {
      LOG.log(Level.WARNING, "Email Error. Failed to send. To: {0} Re: {1}  Error: {2}", new Object[]{recipientEmail, subject, ex.getMessage()});
    }
  }

  /**
   * Send Raw Email Using the Amazon SES API
   * <p>
   * Simple Mail Transfer Protocol (SMTP) specifies how email messages are to be
   * sent by defining the mail envelope and some of its parameters, but it does
   * not concern itself with the content of the message. Instead, the Internet
   * Message Format (RFC 5322) defines how the message is to be constructed.
   * <p>
   * With the Internet Message Format specification, every email message
   * consists of a header and a body. The header consists of message metadata,
   * and the body contains the message itself. For more information about email
   * headers and bodies, see Email Format and Amazon SES.
   * <p>
   * The SMTP protocol was originally designed to send email messages that only
   * contained 7-bit ASCII characters. This limitation makes SMTP insufficient
   * for non-ASCII text encodings (such as Unicode), binary content, or
   * attachments. The Multipurpose Internet Mail Extensions standard (MIME) was
   * developed to overcome these limitations, making it possible to send many
   * other kinds of content using SMTP.
   * <p>
   * The MIME standard works by breaking the message body into multiple parts
   * and then specifying what is to be done with each part. For example, one
   * part of an email message body might be plain text, while another might be
   * HTML. In addition, MIME allows email messages to contain one or more
   * attachments. Message recipients can view the attachments from within their
   * email clients, or they can save the attachments.
   * <p>
   * The message header and content are separated by a blank line. Each part of
   * the email is separated by a boundary, a string of characters that denotes
   * the beginning and ending of each part.
   * <p>
   * The multipart message in the following example contains a text and an HTML
   * part. It also contains an attachment.
   *
   * @param recipientEmail
   * @param senderEmail
   * @param subject
   * @param htmlMessageContent
   * @param textMessageContent
   * @param attachmentType
   * @param attachmentName
   * @param attachment
   * @throws AddressException
   * @throws MessagingException
   */
//  public void sendMailWithAttachment(String recipientEmail, String senderEmail, String subject, String htmlMessageContent, String textMessageContent, String attachmentType, String attachmentName, byte[] attachment) throws AddressException, MessagingException {
//
//    Session session = Session.getDefaultInstance(new Properties());
//
//    // Create a new MimeMessage object.
//    MimeMessage message = new MimeMessage(session);
//
//    // Add subject, from and to lines.
//    message.setSubject(subject, "UTF-8");
//    message.setFrom(new InternetAddress(senderEmail));
//    message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
//
//    // Create a multipart/alternative child container.
//    MimeMultipart msg_body = new MimeMultipart("alternative");
//
//    // Create a wrapper for the HTML and text parts.
//    MimeBodyPart wrap = new MimeBodyPart();
//
//    // Define the text part.
//    MimeBodyPart textPart = new MimeBodyPart();
//    textPart.setContent(textMessageContent, "text/plain; charset=UTF-8");
//
//    // Define the HTML part.
//    MimeBodyPart htmlPart = new MimeBodyPart();
//    htmlPart.setContent(htmlMessageContent, "text/html; charset=UTF-8");
//
//    // Add the text and HTML parts to the child container.
//    msg_body.addBodyPart(textPart);
//    msg_body.addBodyPart(htmlPart);
//
//    // Add the child container to the wrapper object.
//    wrap.setContent(msg_body);
//
//    // Create a multipart/mixed parent container.
////     Multipart
//    MimeMultipart msg = new MimeMultipart("mixed");
//
//    // Add the parent container to the message.
//    message.setContent(msg);
//
//    // Add the multipart/alternative part to the message.
//    msg.addBodyPart(wrap);
//
//    // Define the attachment
//    MimeBodyPart att = new MimeBodyPart();
//    DataSource fds = new FileDataSource(ATTACHMENT);
//    att.setDataHandler(new DataHandler(fds));
//    att.setFileName(fds.getName());
//
//    // Add the attachment to the message.
//    msg.addBodyPart(att);
//
//    // Try to send the email.
//    try {
//      System.out.println("Attempting to send an email through Amazon SES "
//        + "using the AWS SDK for Java...");
//
//      // Instantiate an Amazon SES client, which will make the service call with the supplied AWS credentials.
//      // Replace US_WEST_2 with the AWS Region you're using for Amazon SES.
//      AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
//        .withRegion(Regions.US_WEST_2).build();
//
//      // Print the raw email content on the console
//      PrintStream out = System.out;
//      message.writeTo(out);
//
//      // Send the email.
//      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//      message.writeTo(outputStream);
//      RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
//
//      SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage).withConfigurationSetName(CONFIGURATION_SET);
//
//      client.sendRawEmail(rawEmailRequest);
//      System.out.println("Email sent!");
//      // Display an error if something goes wrong.
//    } catch (Exception ex) {
//      System.out.println("Email Failed");
//      System.err.println("Error message: " + ex.getMessage());
//      ex.printStackTrace();
//    }
//  }
}
