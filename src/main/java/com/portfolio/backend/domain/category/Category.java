package com.portfolio.backend.domain.category;

import com.portfolio.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;  // URL용 식별자 (예: "spring-boot")

    @Builder
    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public void update(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}
