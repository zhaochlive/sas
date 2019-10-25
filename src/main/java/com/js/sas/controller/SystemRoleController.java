package com.js.sas.controller;

import com.js.sas.entity.*;

import com.js.sas.service.SystemMenuService;
import com.js.sas.service.SystemRoleService;
import com.js.sas.utils.MD5Util;
import com.js.sas.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.internal.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@RequestMapping()
@Controller
public class SystemRoleController {

    @Autowired
    private SystemRoleService systemRoleService;

    @Autowired
    private SystemMenuService systemMenuService;

    @RequestMapping(value = "addRole",method = RequestMethod.GET)
    public String addRole(){
        return "pages/systemManage/addRole";
    }

    @RequestMapping(value = "role/getRoleList")
    @ResponseBody
    public Object getRoleList(@Param("name")String name, HttpServletRequest request){
        SystemUser manage = (SystemUser)request.getSession().getAttribute(LoginController.SYSTEM_USER);

        Map<String, Object> resultMap = new HashMap<>();
        List<SystemRole> systemRoles = new ArrayList<>();
        if (StringUtils.isNotBlank(name)){
            systemRoles.add(systemRoleService.getByName(name));
        }else {
            List<SystemRole> systemRoleAll = systemRoleService.findAll();
            if (manage.getId()==1000){
                systemRoles.addAll(systemRoleAll);
            }else {
                Iterator<SystemRole> iterator = systemRoleAll.iterator();
                while (iterator.hasNext()){
                    if (iterator.next().getRoleId()==1000){
                        iterator.remove();
                        break;
                    }
                }
                systemRoles.addAll(systemRoleAll);
            }
        }

        resultMap.put("total",systemRoles.size());
        resultMap.put("rows",systemRoles);
        return resultMap;
    }

    @RequestMapping(value = "role/addRole",method = RequestMethod.POST)
    @ResponseBody
    public Object addUser(SystemRole systemRole )throws IOException {
        if (systemRole!=null&&StringUtils.isNotBlank(systemRole.getName())){
            Assert.hasText(systemRole.getName(),"角色名称不存在");
            SystemRole byName = systemRoleService.getByName(systemRole.getName());
            if (byName!=null){
                return new Result("400","角色已存在",null);
            }else {
                systemRoleService.save(systemRole);
            }
            return new Result("200","创建成功",null);

        }else{
            return new Result("400","未获取到表单信息",null);
        }

    }

    @RequestMapping("modifyMenu")
    public ModelAndView modifyUserRole(@Param("roleId")long roleId, ModelAndView model){
        SystemRole byRoleId = systemRoleService.getByRoleId(roleId);
        List<SystemMenu> systemMenus = systemMenuService.findAll();
        List<SystemMenu> parentMenu = new ArrayList<>();
        List<SystemRoleMenu> roleMenus = systemRoleService.getRoleMenuByRoleId(roleId);
        List<Long> list = new ArrayList<>();
        roleMenus.forEach(roleMenu -> list.add(roleMenu.getMenu().getMenuId()));
        for (SystemMenu systemMenu : systemMenus) {
            if (systemMenu.getPid().equals(0L)){
                parentMenu.add(systemMenu);
            }
        }
        model.addObject("role",byRoleId);
        model.addObject("menus",systemMenus);
        model.addObject("pMenus",parentMenu);
        model.addObject("roleMenus",list);
        model.setViewName("pages/systemManage/roleMenu");
        return model;
    }

    @RequestMapping(value = "/role/saveRoleMenus",method= RequestMethod.POST)
    @ResponseBody
    public Object register(@RequestParam(value ="roleId",required = true)Long roleId
            ,@RequestParam(value ="menuIds",required =false) HashSet<Long> menuIds){
        SystemRole systemRole = systemRoleService.getByRoleId(roleId);
        if (systemRole == null) {
            return new Result("400","角色不存在",null);
        }
        int saveUserRole = systemRoleService.saveRoleMenu(systemRole, menuIds);
        return new Result("200","success",null);
    }

}
