package com.team_5_back_repository.project.global.cloudstorage.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.team_5_back_repository.project.global.cloudstorage.entity.FileEntity;
import com.team_5_back_repository.project.global.cloudstorage.repository.FileEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Component
@Service
public class StorageService {

    private final AmazonS3Client amazonS3Client;
    private final FileEntityRepository fileEntityRepository;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucket;

    // MultipartFile을 전달받아 File로 전환한 후 S3에 업로드
    public FileEntity upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
        return upload(uploadFile, dirName);
    }

    // 여러 MultipartFile을 전달받아 S3에 업로드
    public List<FileEntity> multiUpload(List<MultipartFile> multipartFiles, String dirName) {
        List<FileEntity> fileEntityList = new ArrayList<>();
        multipartFiles.forEach(multipartFile -> {
            try {
                File uploadFile = convert(multipartFile)
                        .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
                fileEntityList.add(upload(uploadFile, dirName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return fileEntityList;
    }

    private FileEntity upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + changedImageName(uploadFile.getName());
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile); // 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)

        return fileEntityRepository.save(FileEntity.builder()
                .fileName(uploadFile.getName())
                .imgUrl(uploadImageUrl)
                .build());
    }

    // 실질적인 s3 업로드 부분
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, uploadFile)
        );
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("파일이 삭제되었습니다.");
        } else {
            log.info("파일이 삭제되지 못했습니다.");
        }
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        String fileName = URLEncoder.encode(file.getOriginalFilename(), StandardCharsets.UTF_8);
        File convertFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
        if (convertFile.createNewFile() || convertFile.exists()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    // 랜덤 파일 이름 메서드 (파일 이름 중복 방지)
    private String changedImageName(String originName) {
        String random = UUID.randomUUID().toString();
        return random + originName;
    }

}
