package com.openclassrooms.datashare.entities;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "fileLink")
public class FileLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "extension", nullable = false)
    private String extension;

    @NotNull
    @Column(name = "size", nullable = false)
    private long size;

    @NotBlank
    @Column(name = "fileLink", nullable = false, unique = true)
    private String fileLink;

    @Nullable
    @Column(name = "password")
    private String password;

    @NotNull
    @Column(name = "usePassword")
    private Boolean usePassword;

    @NotNull
    @Column(name = "expirationDate", nullable = false)
    private LocalDate expirationDate;

    @NotNull
    @Column(name = "isExpired")
    private Boolean isExpired;

    @ElementCollection
    @CollectionTable(name = "tags", joinColumns = @JoinColumn(name = "file_link_id"))
    @Column(name = "tag")
    private List<String> tags;

    @ManyToOne
    @JoinColumn(name = "idUser")
    private User user;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime created_at;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updated_at;

    public boolean isExpired(){
        return this.expirationDate != null && !this.expirationDate.isAfter(LocalDate.now());
    }
}
