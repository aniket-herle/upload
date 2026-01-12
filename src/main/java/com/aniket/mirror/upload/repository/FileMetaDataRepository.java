package com.aniket.mirror.upload.repository;

import com.aniket.mirror.upload.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetaDataRepository extends JpaRepository<FileMetadata, String> {

}
