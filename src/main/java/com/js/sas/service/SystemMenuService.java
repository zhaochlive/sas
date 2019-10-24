package com.js.sas.service;


import com.js.sas.entity.SystemMenu;
import com.js.sas.entity.SystemRole;
import com.js.sas.entity.SystemUser;
import com.js.sas.repository.SystemMenuRepository;
import com.js.sas.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author daniel
 * @description: 系统菜单
 * @create: 2019-10-16 16:47
 */
@Service
public class SystemMenuService {

    @Autowired
    private SystemMenuRepository systemMenuRepository;

    /**
     * 根据人员生成菜单
     * @return
     */
    public String getMenu(SystemUser user){
        List<SystemMenu> systemMenus = systemMenuRepository.findAll();

        //一级菜单
        Map<Long, SystemMenu> firstLevel = new LinkedHashMap<>();
        //二级菜单
        Map<Long, SystemMenu> secondLevel = new LinkedHashMap<>();

        for (SystemMenu systemMenu : systemMenus) {
            if (systemMenu.getPid()==0){
                firstLevel.put(systemMenu.getMenuId(),systemMenu);
            }else{
                secondLevel.put(systemMenu.getMenuId(),systemMenu);
            }
        }
        StringBuilder menuCode = new StringBuilder();
        for(Long lon :firstLevel.keySet()){
            SystemMenu systemMenu = firstLevel.get(lon);
            menuCode.append("<li id=\""+systemMenu.getIdColumn()+"\" class=\"active\">");
            menuCode.append("<a href=\"/\">");
            menuCode.append("<i class=\""+systemMenu.getIcon()+"\"></i> <span class=\"nav-label\">"+systemMenu.getName()+"</span><span class=\"fa arrow\"></span></a>");
            menuCode.append("<ul class=\"nav nav-second-level collapse\">");
            for(Long lng :secondLevel.keySet()){
                SystemMenu menu = secondLevel.get(lng);
                if (menu.getPid().equals(systemMenu.getMenuId())){
                    menuCode.append("<li id=\""+menu.getIdColumn()+"\"><a href=\""+menu.getUrl()+"\">"+menu.getName()+"</a></li>");
                }
            }
            menuCode.append("</ul></li>");
        }

        return menuCode.toString();
    }

    public SystemMenu save(SystemMenu systemMenu){
        return systemMenuRepository.save(systemMenu);
    }

    public SystemMenu getByName(String name){
        SystemMenu systemMenu = new SystemMenu();
        systemMenu.setName(name);
        Optional<SystemMenu> user = systemMenuRepository.findOne(Example.of(systemMenu));
        return user.orElse(null);
    }
    public List<SystemMenu> findAll(){
        return systemMenuRepository.findAll();
    }

    public List<SystemMenu> getAllByPid(long l) {
        return systemMenuRepository.getAllByPid(l);
    }

    public SystemMenu getById(Long aLong) {
        return systemMenuRepository.getOne(aLong);
    }

    public List<SystemMenu> getAllByUerId(Long id) {
        return systemMenuRepository.getAllByUerId(id);
    }
}


