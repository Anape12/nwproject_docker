package jp.nw.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEntity {

    private int id;

    private String userId;

    private String password;

    private String birthDate;

    private String permission;

    private String passwordExpiration;

    private String deleteFlag;
}
