package org.ject.recreation.storage.db.core;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "`user`")
@Getter
public class UserEntity {
    @Id
    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 20)
    private String platform;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 