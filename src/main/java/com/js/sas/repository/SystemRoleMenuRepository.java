package com.js.sas.repository;

import com.js.sas.entity.SystemRoleMenu;
import com.js.sas.entity.SystemRoleMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemRoleMenuRepository extends JpaRepository<SystemRoleMenu,Long> {

    /**
     * 根据菜单id获取该用户所有角色
     * @param menu_id
     * @return
     */
    @Query(value = "select * from system_role_menu where menu_id=?1", nativeQuery = true)
    List<SystemRoleMenu> getRoleMenuBySystemUserId(Long menu_id);

    /**
     * 根据角色id获取该用户所有角色
     * @param role_id
     * @return
     */
    @Query(value = "select * from system_role_menu where role_id=?1", nativeQuery = true)
    List<SystemRoleMenu> getRoleMenuBySystemRoleId(Long role_id);


    /**
     * 根据菜单id删除表信息
     * @param menu_id
     */
    @Modifying
    @Query(value="delete from system_role_menu where menu_id=?1",nativeQuery=true)
    void deleteRoleMenuBySystemUserId(Long menu_id);

    /**
     *
     * @param role_id
     */
    @Modifying
    @Query(value="delete from system_role_menu where role_id=?1",nativeQuery=true)
    void deleteRoleMenuBySystemRoleId(Long role_id);
}
