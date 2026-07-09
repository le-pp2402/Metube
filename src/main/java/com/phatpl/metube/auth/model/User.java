package com.phatpl.metube.auth.model;

import org.hibernate.annotations.SQLRestriction;

import com.phatpl.metube.common.id.SnowflakeIdListener;
import com.phatpl.metube.common.model.Auditable;
import com.phatpl.metube.common.model.ISoftDelete;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(SnowflakeIdListener.class)
@SQLRestriction("deleted = false")
public class User extends Auditable implements ISoftDelete {
  @Id
  @Column(nullable = false, updatable = false)
  @Setter(AccessLevel.NONE)
  Long id;

  @Column(length = 80, nullable = false)
  String username;

  @Column(name = "pwd_hash", length = 200, nullable = false)
  String pwdHash;

  @Column(length = 100, nullable = false)
  String email;

  @Column(name = "stream_key", length = 200)
  String streamKey;

  @Column(name = "avatar_url", length = 200)
  String avatarUrl;

  @Column(nullable = false)
  boolean active = true;

  @Column(nullable = false)
  boolean verified = false;

  @Column(name = "token_ver", nullable = false)
  Long tokenVer = 0L;

  private User(String username, String pwdHash, String email) {
    this.username = username;
    this.pwdHash = pwdHash;
    this.email = email;
  }

  public static User register(String username, String password, String email) {
    return new User(username, password, email);
  }

  public void changePassword(String newHashedPwd) {
    this.pwdHash = newHashedPwd;
  }

  public void updateProfile(String newUsername, String newEmail,
      String newAvatarUrl) {
    if (newUsername != null && !newUsername.isBlank()) {
      this.username = newUsername;
    }
    if (newEmail != null && !newEmail.isBlank()) {
      this.email = newEmail;
    }
    if (newAvatarUrl != null && !newAvatarUrl.isBlank()) {
      this.avatarUrl = newAvatarUrl;
    }
  }

  public void changeStreamKey(String newStreamKey) {
    this.streamKey = newStreamKey;
  }

  public void revokeTokens() {
    this.tokenVer += 1;
  }

  @Column(nullable = false)
  boolean deleted = false;

  @Override
  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public void delete() {
    this.deleted = true;
  }

  @Override
  public void restore() {
    this.deleted = false;
  }
}
