package com.aniket.mirror.upload.repository;

import com.aniket.mirror.upload.entity.FileMirror;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMirrorRepository extends JpaRepository<FileMirror, Long> {
  List<FileMirror> findByFile_FileId(String fileId);
  Optional<FileMirror> findByFile_FileIdAndProviderName(String fileId, String providerName);
}
