package com.phatpl.metube.auth.model;

import com.phatpl.metube.common.id.SnowflakeIdListener;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity 
@Getter 
@Table(name = "users") 
@FieldDefaults(level = AccessLevel.PRIVATE) 
@EntityListeners(SnowflakeIdListener.class)
public class User {
  @Id @Column(nullable = false, updatable = false) @Setter(AccessLevel.NONE)
  Long id;

  @Column(length = 80, nullable = false)
  String username;

  @Column(length = 200, nullable = false)
  String password;

  @Column(length = 100, nullable = false)
  String email;

  @Column(length = 200)
  String streamKey;

  @Column(length = 200)
  String avatarUrl;

  public void register(String username, String password, String email) {
    this.username = username;
    this.password = password;
    this.email = email;
  }

  public void changePassword(String newPassword) {
    this.password = newPassword;
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

  public void updateStreamKey(String newStreamKey) {
    this.streamKey = newStreamKey;
  }
}
