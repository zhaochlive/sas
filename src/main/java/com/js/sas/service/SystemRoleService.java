package com.js.sas.service;


import com.js.sas.entity.*;
import com.js.sas.repository.SystemMenuRepository;
import com.js.sas.repository.SystemRoleMenuRepository;
import com.js.sas.repository.SystemRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * @author daniel
 * @description: 系统菜单
 * @create: 2019-10-16 16:47
 */
@Service
public class SystemRoleService {

    @Autowired
    private SystemRoleRepository systemRoleRepository;

    @Autowired
    private SystemMenuService systemMenuService;
    @Autowired
    private SystemRoleMenuRepository systemRoleMenuRepository;

    public List<SystemRole> findAll(){
        return systemRoleRepository.findAll();
    }

    public SystemRole getByName(String name) {
        SystemRole systemRole = new SystemRole();
        systemRole.setName(name);
        Optional<SystemRole> one = systemRoleRepository.findOne(Example.of(systemRole));
        return one.orElse(null);
    }

    public SystemRole save(SystemRole systemRole) {
        return systemRoleRepository.save(systemRole);
    }

    public SystemRole getByRoleId(Long aLong) {
        return systemRoleRepository.findById(aLong).orElse(null);
    }

    public List<SystemRoleMenu> getRoleMenuByRoleId(long roleId) {
        return systemRoleMenuRepository.getRoleMenuBySystemRoleId(roleId);
    }

    @Transactional
    public int saveRoleMenu(SystemRole systemRole, HashSet<Long> menuIds) {
        List<SystemRoleMenu> roleMenus = new ArrayList<>();
        for (Long aLong : menuIds) {
            SystemMenu systemMenu = systemMenuService.getById(aLong);
            if (systemRole == null) {
                continue;
            }
            SystemRoleMenu role = new SystemRoleMenu();
            role.setMenu(systemMenu);
            role.setRole(systemRole);
            roleMenus.add(role);
        }
        systemRoleMenuRepository.deleteRoleMenuBySystemRoleId(systemRole.getRoleId());
        List<SystemRoleMenu> roleMenuss = systemRoleMenuRepository.saveAll(roleMenus);
        return roleMenuss.size();
    }

}
