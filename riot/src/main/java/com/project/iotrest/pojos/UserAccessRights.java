package com.project.iotrest.pojos;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * POJO for `users_access_rights` table.
 *
 * @author roshan
 */
@Table(name = "USERS_ACCESS_RIGHTS")
public class UserAccessRights {

    @Column(name = "USER_ID")
    private Integer userId;

    @Column(name = "CREATE_ACCESS")
    private Boolean create;

    @Column(name = "READ_ACCESS")
    private Boolean read;

    @Column(name = "UPDATE_ACCESS")
    private Boolean update;

    @Column(name = "DELETE_ACCESS")
    private Boolean delete;

    public UserAccessRights() {
        // empty constructor needed for Jackson serialization/deserialization.
    }

    public UserAccessRights(Integer userId, Boolean create, Boolean read, Boolean update, Boolean delete) {
        this.userId = userId;
        this.create = create;
        this.read = read;
        this.update = update;
        this.delete = delete;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Boolean getCreate() {
        return create;
    }

    public void setCreate(Boolean create) {
        this.create = create;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public Boolean getDelete() {
        return delete;
    }

    public void setDelete(Boolean delete) {
        this.delete = delete;
    }
}
