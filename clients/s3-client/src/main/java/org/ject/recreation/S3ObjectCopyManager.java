package org.ject.recreation;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.concurrent.atomic.AtomicInteger;


@Component
public class S3ObjectCopyManager {

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    public S3ObjectCopyManager(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public int copyObjectsByPrefix(String sourcePrefix, String destinationPrefix) {
        ListObjectsV2Request originalObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(sourcePrefix)
                .build();

        ListObjectsV2Response originalObjectsResponse = s3Client.listObjectsV2(originalObjectsRequest);

        AtomicInteger copiedCount = new AtomicInteger(0);

        originalObjectsResponse.contents().stream()
                .filter(s3Object -> !s3Object.key().equals(sourcePrefix)) // 폴더 객체 제외
                .forEach(s3Object -> {
                    String sourceKey = s3Object.key();
                    String destinationKey = s3Object.key().replaceFirst(sourcePrefix, destinationPrefix);

                    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                            .sourceBucket(bucketName)
                            .sourceKey(sourceKey)
                            .destinationBucket(bucketName)
                            .destinationKey(destinationKey)
                            .build();


                    s3Client.copyObject(copyRequest);
                    copiedCount.getAndIncrement();
                });

        return copiedCount.get();
    }
}
