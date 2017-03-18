package com.gcplot.services;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.common.base.Strings;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/1/17
 */
public class S3Connector {
    private String bucket;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private AmazonS3 client;

    public void init() {
        AWSCredentials credentials;
        if (!Strings.isNullOrEmpty(accessKey) || !Strings.isNullOrEmpty(secretKey)) {
            credentials = new BasicAWSCredentials(accessKey, secretKey);
        } else {
            credentials = new AnonymousAWSCredentials();
        }
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(new ClientConfiguration()
                        .withMaxConnections(Runtime.getRuntime().availableProcessors() * 10)
                        .withMaxErrorRetry(50));
        if (!Strings.isNullOrEmpty(endpoint)) {
            builder = builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, null));
        }
        this.client = builder.build();
    }

    public AmazonS3 getClient() {
        return client;
    }

    public String getBucket() {
        return bucket;
    }
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getAccessKey() {
        return accessKey;
    }
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getEndpoint() {
        return endpoint;
    }
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
