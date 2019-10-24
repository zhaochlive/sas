package com.js.sas.repository;

import com.js.sas.entity.SystemMenu;
import com.js.sas.entity.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SystemMenuRepository extends JpaRepository<SystemMenu,Long> {

    List<SystemMenu> getAllByPid(long l);

    /**
     * 根据用户id查询对应菜单
     * @param userId
     * @return
     */
    @Query( nativeQuery = true,value ="select me.* from system_menu me " +
            "left join system_role_menu srm on me.menu_id = srm.menu_id " +
            "left join system_role role on role.role_id = srm.role_id " +
            "left join system_user_role sur on sur.role_id = role.role_id " +
            "left join system_user user on user.id = sur.user_id " +
            "where user.id = ?1 group by menu_id")
    List<SystemMenu> getAllByUerId(long userId);
}
