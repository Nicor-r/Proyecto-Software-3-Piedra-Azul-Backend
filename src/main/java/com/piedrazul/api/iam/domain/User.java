package com.piedrazul.api.iam.domain;

import java.util.Objects;

/**
 * Entidad de dominio User.
 * IMPORTANTE: "password" almacena el HASH BCrypt de la contrasena,
 * nunca texto plano. La responsabilidad de generar ese hash es de
 * PasswordHasher (capa repository/infrastructure), no de esta clase.
 */
public class User {

    private String id;
    private String email;
    private String password;
    private RoleEnum role;

    public User() {
    }

    public User(String id, String email, String password, RoleEnum role) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
