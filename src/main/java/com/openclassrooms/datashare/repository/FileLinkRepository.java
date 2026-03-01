package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.FileLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileLinkRepository extends JpaRepository<FileLink, Long> {
}
