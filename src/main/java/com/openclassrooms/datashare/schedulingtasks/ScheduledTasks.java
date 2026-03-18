package com.openclassrooms.datashare.schedulingtasks;

import com.openclassrooms.datashare.entities.FileLink;
import com.openclassrooms.datashare.repository.FileLinkRepository;
import com.openclassrooms.datashare.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class ScheduledTasks {
    @Autowired
    private FileService service;
    @Autowired
    private FileLinkRepository repository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredFiles() {
        log.info("File expiration is running");
        List<FileLink> fileLinks = this.repository.findByExpirationDateBefore(new Date());
        this.service.deleteFileFromJob(fileLinks);
        this.repository.deleteAll(fileLinks);
        log.info("File expiration is over");
    }
}