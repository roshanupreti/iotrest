package com.project.iotrest.pojos;


import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Objects;


/**
 * POJO for `users` table.
 *
 * @author roshan
 */
@Table(name = "USERS")
public class User {

    @Column(name = "ID")
    private Integer id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "USERNAME")
    private String userName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PASSWORD")
    //@JsonIgnore
    private String password;

    @Column(name = "IS_ACTIVE")
    private Boolean active;

    private UserAccessRights userAccessRights;

    public User() {
        // empty constructor needed for Jackson serialization/deserialization.
    }

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public UserAccessRights getUserAccessRights() {
        return userAccessRights;
    }

    public void setUserAccessRights(UserAccessRights userAccessRights) {
        this.userAccessRights = userAccessRights;
    }

    @Override
    public int hashCode() {
        return userName != null ? userName.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userName, user.userName);
    }
}
