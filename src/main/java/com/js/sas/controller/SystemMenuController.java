package com.js.sas.controller;

import com.js.sas.entity.SystemMenu;
import com.js.sas.entity.SystemRole;
import com.js.sas.service.SystemMenuService;
import com.js.sas.service.SystemRoleService;
import com.js.sas.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping()
@Controller
public class SystemMenuController {

    @Autowired
    private SystemMenuService systemMenuService;

    @RequestMapping(value = "addMenu",method = RequestMethod.GET)
    public String addMenu(){
        return "pages/systemManage/addMenu";
    }

    @RequestMapping(value = "menu/getMenuList")
    @ResponseBody
    public Object getRoleList(@Param("name")String name){

        Map<String, Object> resultMap = new HashMap<>();
        List<SystemMenu> systemMenus = new ArrayList<>();
        if (StringUtils.isNotBlank(name)){
            SystemMenu systemMenu = systemMenuService.getByName(name);
            systemMenus.add(systemMenu);
        }else {
            systemMenus.addAll(systemMenuService.findAll());
        }

        resultMap.put("total",systemMenus.size());
        resultMap.put("rows",systemMenus);
        return resultMap;
    }

    @RequestMapping(value = "menu/addMenu",method = RequestMethod.POST)
    @ResponseBody
    public Object addUser(SystemMenu systemMenu )throws IOException {
        if (systemMenu!=null&&StringUtils.isNotBlank(systemMenu.getName())){
            SystemMenu byName = systemMenuService.getByName(systemMenu.getName());
            if (byName!=null){
                return new Result("400","菜单已存在",null);
            }else {
                systemMenuService.save(systemMenu);
            }
            return new Result("200","创建成功",null);

        }else{
            return new Result("400","未获取到表单信息",null);
        }

    }


}
