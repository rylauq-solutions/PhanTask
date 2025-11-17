package com.phantask.authentication.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    private Long userId;   //same as User.uid

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId          // <-- IMPORTANT: Shares PK with User
    @JoinColumn(name = "uid")
    private User user;

    private String fullName;
    private String department;
    private String phone;
    private String photoUrl;
    private String yearOfStudy;
}
