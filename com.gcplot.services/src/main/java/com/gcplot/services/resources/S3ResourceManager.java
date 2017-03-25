package com.gcplot.services.resources;

import com.amazonaws.services.s3.model.*;
import com.gcplot.resource.ResourceManager;
import com.gcplot.services.S3Connector;
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
    private S3Connector connector;

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
            initRequest = new InitiateMultipartUploadRequest(connector.getBucket(), newPath, om);
            initRequest.setCannedACL(CannedAccessControlList.PublicRead);
            initResponse = connector.getClient().initiateMultipartUpload(initRequest);

            long contentLength = file.length();
            long partSize = 5242880; // Set part size to 5 MB.
            // Step 2: Upload parts.
            long filePosition = 0;
            for (int i = 1; filePosition < contentLength; i++) {
                // Last part can be less than 5 MB. Adjust part size.
                partSize = Math.min(partSize, (contentLength - filePosition));

                // Create request to upload a part.
                UploadPartRequest uploadRequest = new UploadPartRequest()
                        .withBucketName(connector.getBucket()).withKey(newPath)
                        .withUploadId(initResponse.getUploadId()).withPartNumber(i)
                        .withFileOffset(filePosition)
                        .withFile(file)
                        .withPartSize(partSize);

                // Upload part and add response to our list.
                partETags.add(connector.getClient().uploadPart(uploadRequest).getPartETag());

                filePosition += partSize;
            }

            // Step 3: Complete.
            CompleteMultipartUploadRequest compRequest = new
                    CompleteMultipartUploadRequest(
                    connector.getBucket(),
                    newPath,
                    initResponse.getUploadId(),
                    partETags);

            connector.getClient().completeMultipartUpload(compRequest);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            if (initResponse != null) {
                connector.getClient().abortMultipartUpload(new AbortMultipartUploadRequest(
                        connector.getBucket(), newPath, initResponse.getUploadId()));
            }
        }
    }

    @Override
    public boolean isDisabled() {
        return Strings.isNullOrEmpty(connector.getBucket()) || Strings.isNullOrEmpty(connector.getAccessKey())
                || Strings.isNullOrEmpty(connector.getSecretKey());
    }

    public S3Connector getConnector() {
        return connector;
    }
    public void setConnector(S3Connector connector) {
        this.connector = connector;
    }
}
