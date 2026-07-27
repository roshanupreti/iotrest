package com.project.iotrest.pojos.user;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

/**
 * POJO for `users_access_rights` table.
 *
 * @author roshan
 */
@Table(name = "USERS_ACCESS_RIGHTS")
public record UserAccessRights(@Column(name = "USER_ID") Integer userId,
                                @Column(name = "CREATE_ACCESS") Boolean create,
                                @Column(name = "READ_ACCESS") Boolean read,
                                @Column(name = "UPDATE_ACCESS") Boolean update,
                                @Column(name = "DELETE_ACCESS") Boolean delete) {
}
