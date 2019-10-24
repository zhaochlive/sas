package com.js.sas.service;


import com.js.sas.entity.SystemRole;
import com.js.sas.entity.SystemUser;
import com.js.sas.entity.SystemUserRole;
import com.js.sas.repository.SystemUserRepository;
import com.js.sas.repository.SystemUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.persistence.Transient;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author daniel
 * @description: 系统用户
 * @create: 2019-10-16 16:47
 */
@Service
public class SystemUserService {

    @Autowired
    private SystemUserRepository systemUserRepository;
    @Autowired
    private SystemRoleService systemRoleService;

    @Autowired
    private SystemUserRoleRepository systemUserRoleRepository;

    public int upDatePassword(SystemUser user){
        return systemUserRepository.upDatePassword(user.getId(),user.getPassword());
    }

    public SystemUser getUserByUserName(String username){

        SystemUser systemUser = new SystemUser();
        systemUser.setUserName(username);
        Optional<SystemUser> user = systemUserRepository.findOne(Example.of(systemUser));
        return user.orElse(null);
    }

    public List<SystemUser> getAllSystemUser(){
        return systemUserRepository.findAll();
    }

    public SystemUser save(SystemUser systemUser) {
        return systemUserRepository.saveAndFlush(systemUser);

    }

    /**
     *
     * @param userId
     * @return
     */
    public List<SystemUserRole> getUserRoleByUserId(Long userId){
       return systemUserRoleRepository.getSystemUserRoleBySystemUserId(userId);
    }

    public SystemUser getUserByUserId(Long userId) {
        return systemUserRepository.findById(userId).orElse(null);
    }

    @Transactional
    public int saveUserRole(SystemUser systemUser, HashSet<Long> roles) {
        List<SystemUserRole> userRoles = new ArrayList<>();
        for (Long aLong : roles) {
            SystemRole systemRole = systemRoleService.getByRoleId(aLong);
            if (systemRole == null) {
                continue;
            }
            SystemUserRole role = new SystemUserRole();
            role.setUser(systemUser);
            role.setRole(systemRole);
            userRoles.add(role);
        }
        systemUserRoleRepository.deleteSystemUserRoleBySystemUserId(systemUser.getId());
        List<SystemUserRole> userRoleList = systemUserRoleRepository.saveAll(userRoles);
        return userRoleList.size();
    }
}
