package com.portfolio.backend.domain.user;

import com.portfolio.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String title;  // 예: "풀스택 개발자"

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "github_url", length = 255)
    private String githubUrl;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "resume_url", length = 255)
    private String resumeUrl;

    @Builder
    public User(String email, String password, String name, String title,
                String bio, String githubUrl, String linkedinUrl, String resumeUrl) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.title = title;
        this.bio = bio;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.resumeUrl = resumeUrl;
    }

    // 프로필 업데이트
    public void updateProfile(String name, String title, String bio,
                              String githubUrl, String linkedinUrl, String resumeUrl) {
        this.name = name;
        this.title = title;
        this.bio = bio;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.resumeUrl = resumeUrl;
    }

    // 비밀번호 변경
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
}
