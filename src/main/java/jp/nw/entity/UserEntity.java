package jp.nw.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserEntity {

    private int id;

    private String userId;

    private String password;

    private String firstName;

    private String lastName;

    private String birthDate;

    private String permission;

    private String accountType;

    private String passwordExpiration;

    private String deleteFlag;

    private boolean accountDisabled;
    private int failedLoginCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private boolean forcePasswordChange;
}
