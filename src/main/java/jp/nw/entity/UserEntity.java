package jp.nw.entity;

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

    private String passwordExpiration;

    private String deleteFlag;
}
