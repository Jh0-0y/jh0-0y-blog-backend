package com.blog.backend.infra.s3.service;

import com.blog.backend.infra.s3.dto.S3UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * S3 파일 업로드/조회/삭제 기능을 제공하는 서비스 인터페이스
 */
public interface S3Service {

    /**
     * 이미지를 S3에 업로드
     *
     * @param file 업로드할 이미지 파일
     * @return S3UploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    S3UploadResult uploadPublicImage(MultipartFile file) throws IOException;

    /**
     * 동영상을 S3에 업로드
     *
     * @param file 업로드할 동영상 파일
     * @return S3UploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    S3UploadResult uploadPublicVideo(MultipartFile file) throws IOException;

    /**
     * 문서를 S3에 업로드
     *
     * @param file 업로드할 문서 파일
     * @return S3UploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    S3UploadResult uploadPublicDocument(MultipartFile file) throws IOException;

    /**
     * 오디오를 S3에 업로드
     *
     * @param file 업로드할 오디오 파일
     * @return S3UploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    S3UploadResult uploadPublicAudio(MultipartFile file) throws IOException;

    /**
     * 압축 파일을 S3에 업로드
     *
     * @param file 업로드할 압축 파일
     * @return S3UploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    S3UploadResult uploadPublicArchive(MultipartFile file) throws IOException;

    /**
     * 시스템 정적 자원을 S3에 업로드
     *
     * @param file 업로드할 에셋 파일
     * @return S3UploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    S3UploadResult uploadPublicAssets(MultipartFile file) throws IOException;

    /**
     * 파일을 S3에 업로드 (자동 타입 분류)
     * 저장 타입은 내부적으로 분류됨
     *
     * @param file 업로드할 파일
     * @return S3UploadResult 업로드된 파일 메타데이터
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    S3UploadResult uploadFile(MultipartFile file) throws IOException;

    /**
     * S3에 업로드된 파일을 삭제하고 CloudFront 캐시도 무효화
     *
     * @param s3Key 삭제할 파일의 S3 Key
     */
    void deleteFile(String s3Key);

    /**
     * S3에 업로드된 여러 파일을 일괄 삭제하고 CloudFront 캐시도 무효화
     *
     * AWS S3의 DeleteObjectsRequest를 사용하여 한 번의 요청으로 최대 1,000개까지 삭제 가능
     * 1,000개를 초과하는 경우 자동으로 분할 처리됨
     *
     * 장점:
     * - 네트워크 왕복 시간(RTT) 최소화
     * - API 요청 비용 절감
     * - 삭제 속도 대폭 향상
     *
     * 트랜잭션 처리:
     * - 일부 파일 삭제 실패 시 성공한 파일 목록만 반환
     * - 호출자는 성공한 파일만 DB에서 삭제해야 함
     *
     * @param s3Keys 삭제할 파일들의 S3 Key 목록
     * @return 삭제 성공한 S3 Key 목록 (실패한 파일은 제외됨)
     */
    List<String> deleteFiles(List<String> s3Keys);

    /**
     * S3에 Private로 업로드된 파일의 접근 가능한 Presigned URL을 생성
     * URL 만료시간을 주어 생명주기를 설정
     *
     * @param s3Key 조회할 파일의 S3 Key
     * @param minutes URL 유효 시간(분)
     * @return Presigned URL
     */
    String getPresignedUrl(String s3Key, int minutes);
}