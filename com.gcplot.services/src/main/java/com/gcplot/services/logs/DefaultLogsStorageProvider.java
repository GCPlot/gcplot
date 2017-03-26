package com.gcplot.services.logs;

import com.gcplot.fs.LogsStorage;
import com.gcplot.fs.LogsStorageProvider;
import com.gcplot.model.gc.SourceType;
import com.gcplot.services.S3Connector;
import com.gcplot.services.logs.s3.S3LogsStorage;

import java.util.Properties;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/14/17
 */
public class DefaultLogsStorageProvider implements LogsStorageProvider {
    private static final String S3_BUCKET_NAME = "s3.bucket";
    private static final String S3_ACCESS_KEY = "s3.access_key";
    private static final String S3_SECRET_KEY = "s3.secret_key";
    private static final String S3_REGION_ID = "s3.region.id";
    private static final String S3_PREFIX = "s3.prefix";
    private S3Connector internalConnector;
    private String prefix;
    private LogsStorage internal;

    public void init() {
        S3LogsStorage s3LogsStorage = new S3LogsStorage();
        s3LogsStorage.setConnector(internalConnector);
        s3LogsStorage.setPrefix(prefix);
        this.internal = s3LogsStorage;
    }

    @Override
    public LogsStorage get(SourceType type, Properties config) {
        switch (type) {
            case INTERNAL:
                return internal;
            case S3:
                S3Connector s3Connector = new S3Connector();
                s3Connector.setBucket(config.getProperty(S3_BUCKET_NAME));
                s3Connector.setAccessKey(config.getProperty(S3_ACCESS_KEY));
                s3Connector.setSecretKey(config.getProperty(S3_SECRET_KEY));
                s3Connector.setRegion(config.getProperty(S3_REGION_ID));
                s3Connector.init(true);

                S3LogsStorage storage = new S3LogsStorage();
                storage.setConnector(s3Connector);
                storage.setPrefix(config.getProperty(S3_PREFIX, ""));
                return storage;
            case GCS:
                throw new RuntimeException("GCS is not supported yet.");
            default:
                return null;
        }
    }

    public void setInternalConnector(S3Connector internalConnector) {
        this.internalConnector = internalConnector;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
