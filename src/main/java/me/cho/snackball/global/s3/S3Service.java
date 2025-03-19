package me.cho.snackball.global.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.cho.snackball.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class S3Service {

    private final AmazonS3 amazonS3;
    private static final String IMAGE_URL = "https://snackball-static-files.s3.ap-northeast-2.amazonaws.com/54eec2d7-6382-44bb-a8f3-5cc28ae1ecb6_AnonymousProfileImg.png";

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 이미지 업로드 하기
     */
    public String uploadProfileImage(MultipartFile image, User user) {

        // 기존 이미지 삭제
        if (user.getProfileImage() != null && !user.getProfileImage().equals(IMAGE_URL)) {
            amazonS3.deleteObject(bucket, extractObjectKey(user.getProfileImage()));
        }

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename(); // 고유한 파일 이름 생성

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        // S3에 파일 업로드 요청 생성
        PutObjectRequest putObjectRequest;
        try{
            putObjectRequest = new PutObjectRequest(bucket, fileName, image.getInputStream(), metadata);
        } catch (IOException e) {
            log.error("IOException 발생: {}", e.getMessage(), e);
            //TODO 예외 만들기
            throw new RuntimeException("이미지 업로드 중 서버 오류가 발생했습니다.", e);
        }

        // S3에 파일 업로드
        amazonS3.putObject(putObjectRequest);

        return getPublicUrl(fileName);
    }

    // Summernote에서 업로드된 Base64 이미지 처리 후 S3에 저장
    public String uploadBase64Image(String base64Image) {
        try {
            byte[] imageBytes = decodeBase64Image(base64Image);
            String fileName = "study-images/" + UUID.randomUUID() + ".png";  // 파일명 랜덤 생성

            InputStream inputStream = new ByteArrayInputStream(imageBytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/png");

            amazonS3.putObject(bucket, fileName, inputStream, metadata);
            return getPublicUrl(fileName);  // S3 URL 반환

        } catch (Exception e) {
            log.error("Base64 이미지 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Base64 이미지 업로드 실패", e);
        }
    }

    // 기존 로컬 파일 S3로 이동 후 URL 반환
    public String uploadLocalFile(String localFilePath) {
        String fileName = "study-images/" + UUID.randomUUID() + ".png";

        amazonS3.putObject(bucket, fileName, new java.io.File(localFilePath));
        return getPublicUrl(fileName);
    }

    private String extractObjectKey(String imageUrl) {
        // URL 형식: https://{bucket-name}.s3.{region}.amazonaws.com/{object-key}
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

    private String getPublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, amazonS3.getRegionName(), fileName);
    }

    // Base64 인코딩된 이미지 데이터를 디코딩
    private byte[] decodeBase64Image(String base64Image) {
        String base64Data = base64Image.split(",")[1];  // "data:image/png;base64,..." 형식에서 데이터만 추출
        return Base64.getDecoder().decode(base64Data);
    }
}
