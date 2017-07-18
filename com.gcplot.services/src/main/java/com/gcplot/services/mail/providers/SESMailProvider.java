package com.gcplot.services.mail.providers;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import com.gcplot.configuration.ConfigProperty;
import com.google.common.base.Strings;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         7/18/17
 */
public class SESMailProvider extends BaseMailProvider {

    @Override
    void makeSend(String to, String subject, String msg) {
        String accessKey = config.readString(ConfigProperty.SES_ACCESS_KEY);
        String secretKey = config.readString(ConfigProperty.SES_SECRET_KEY);
        if (!Strings.isNullOrEmpty(accessKey)) {
            Destination destination = new Destination().withToAddresses(to);
            Content subj = new Content().withData(subject);
            Content textBody = new Content().withData(msg);
            Body body = new Body().withText(textBody);

            Message message = new Message().withSubject(subj).withBody(body);
            SendEmailRequest req = new SendEmailRequest().withSource(config.readString(ConfigProperty.EMAIL_DEFAULT_FROM))
                    .withDestination(destination).withMessage(message);

            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withRegion(config.readString(ConfigProperty.SES_REGION))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                    .build();
            try {
                client.sendEmail(req);
            } finally {
                client.shutdown();
            }
        }
    }

}
