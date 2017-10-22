package com.gcplot.messages;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         3/5/17
 */
public class ConnectorSettingsMessage {
    @JsonProperty("s3_bucket")
    public String s3Bucket;
    @JsonProperty("s3_base_path")
    public String s3BasePath;
    @JsonProperty("s3_access_key")
    public String s3AccessKey;
    @JsonProperty("s3_secret_key")
    public String s3SecretKey;
    @JsonProperty("s3_region")
    public String s3Region;

    public ConnectorSettingsMessage(@JsonProperty("s3_bucket") String s3Bucket,
                                    @JsonProperty("s3_base_path") String s3BasePath,
                                    @JsonProperty("s3_access_key") String s3AccessKey,
                                    @JsonProperty("s3_secret_key") String s3SecretKey,
                                    @JsonProperty("s3_region") String s3Region) {
        this.s3Bucket = s3Bucket;
        this.s3BasePath = s3BasePath;
        this.s3AccessKey = s3AccessKey;
        this.s3SecretKey = s3SecretKey;
        this.s3Region = s3Region;
    }

}
