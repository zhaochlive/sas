package com.js.sas.repository;

import com.js.sas.entity.SystemRole;
import com.js.sas.entity.SystemUser;
import com.js.sas.entity.SystemUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemUserRoleRepository extends JpaRepository<SystemUserRole,Long> {

    /**
     * 根据用户id获取该用户所有角色
     * @param user_id
     * @return
     */
    @Query(value = "select * from system_user_role where user_id=?1", nativeQuery = true)
    List<SystemUserRole> getSystemUserRoleBySystemUserId(Long user_id);

    /**
     * 根据角色id获取该用户所有角色
     * @param role_id
     * @return
     */
    @Query(value = "select * from system_user_role where role_id=?1", nativeQuery = true)
    List<SystemUserRole> getSystemUserRoleBySystemRoleId(Long role_id);


    /**
     * 根据用户id删除表信息
     * @param user_id
     */
    @Modifying
    @Query(value="delete from system_user_role where user_id=?1",nativeQuery=true)
    void deleteSystemUserRoleBySystemUserId(Long user_id);

    /**
     *
     * @param role_id
     */
    @Modifying
    @Query(value="delete from system_user_role where role_id=?1",nativeQuery=true)
    void deleteSystemUserRoleBySystemRoleId(Long role_id);
}
