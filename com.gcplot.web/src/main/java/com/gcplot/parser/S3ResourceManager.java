package com.gcplot.parser;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.retry.v2.RetryPolicy;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.gcplot.resource.ResourceManager;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         9/10/16
 */
public class S3ResourceManager implements ResourceManager {
    private static final Logger LOG = LoggerFactory.getLogger(S3ResourceManager.class);
    private String bucket;
    private String accessKey;
    private String secretKey;
    private AmazonS3 client;

    public void init() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(new ClientConfiguration()
                        .withMaxConnections(Runtime.getRuntime().availableProcessors() * 10)
                        .withMaxErrorRetry(50))
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    @Override
    public void upload(File file, String newPath, String contentType) {
        List<PartETag> partETags = new ArrayList<>();

        newPath = newPath + "/" + file.getName();
        // Step 1: Initialize.
        ObjectMetadata om = new ObjectMetadata();
        if (contentType == null) {
            try {
                contentType = Files.probeContentType(file.toPath());
            } catch (IOException ignored) {
            }
        }
        if (contentType != null) {
            om.addUserMetadata("Content-Type", contentType);
        }
        InitiateMultipartUploadRequest initRequest;
        InitiateMultipartUploadResult initResponse = null;

        try {
            initRequest = new InitiateMultipartUploadRequest(bucket, newPath, om);
            initRequest.setCannedACL(CannedAccessControlList.PublicRead);
            initResponse = client.initiateMultipartUpload(initRequest);

            long contentLength = file.length();
            long partSize = 5242880; // Set part size to 5 MB.
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(bucket).withKey(newPath)
                        .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(client.uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new
                    CompleteMultipartUploadRequest(
                    bucket,
                    newPath,
                    initResponse.getUploadId(),
                    partETags);

            client.completeMultipartUpload(compRequest);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            if (initResponse != null) {
                client.abortMultipartUpload(new AbortMultipartUploadRequest(
                        bucket, newPath, initResponse.getUploadId()));
            }
        }
    }

    @Override
    public boolean isDisabled() {
        return Strings.isNullOrEmpty(bucket) || Strings.isNullOrEmpty(accessKey)
                || Strings.isNullOrEmpty(secretKey);
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
}
