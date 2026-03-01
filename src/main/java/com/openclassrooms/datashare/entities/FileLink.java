package com.openclassrooms.datashare.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column(name = "link", nullable = false)
    private String link;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "path")
    private String path;

    @Column(name = "password")
    private String password;

    @Column(name = "usePassword")
    private Boolean usePassword;

    @NotBlank
    @Column(name = "expirationDate", nullable = false)
    private LocalDate expirationDate;

    @NotBlank
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
        return this.expirationDate == null || !(this.expirationDate.isBefore(LocalDate.now()));
    }
}
