package com.smartlearning.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 255)
    private String profileImagePath;

    @Column(length = 40)
    private String phone;

    @Column(length = 40)
    private String alternatePhone;

    @Column(length = 20)
    private String gender;

    @Column(length = 40)
    private String dateOfBirth;

    @Column(length = 120)
    private String experience;

    @Column
    private Integer careerGap;

    @Column(length = 120)
    private String currentState;

    @Column(length = 120)
    private String currentCity;

    @Column(length = 500)
    private String preferredLocation;

    @Column(length = 255)
    private String githubUrl;

    @Column(length = 255)
    private String linkedinUrl;

    @Column(length = 255)
    private String resumeUrl;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
