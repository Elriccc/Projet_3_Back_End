package com.openclassrooms.datashare.repository;

import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileLinkRepository extends JpaRepository<FileLink, Long> {
    List<FileLink> getFileLinksByUser(User user);
    Optional<FileLink> findByFileLink(String fileLink);
    List<FileLink> findByExpirationDateBefore(Date date);
}
