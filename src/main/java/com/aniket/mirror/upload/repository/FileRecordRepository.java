package com.aniket.mirror.upload.repository;

import com.aniket.mirror.upload.entity.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, String> {
}
