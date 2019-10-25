package com.js.sas.controller;

import com.js.sas.entity.SystemMenu;
import com.js.sas.entity.SystemRole;
import com.js.sas.entity.SystemUser;
import com.js.sas.entity.SystemUserRole;
import com.js.sas.service.SystemMenuService;
import com.js.sas.service.SystemRoleService;
import com.js.sas.service.SystemUserService;
import com.js.sas.utils.DateTimeUtils;
import com.js.sas.utils.MD5Util;
import com.js.sas.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author daniel
 * @description: 系统用户
 * @create: 2019-10-19 14:18
 */
@Controller
@RequestMapping()
public class SystemUserController {

    @Autowired
    private SystemUserService systemUserService;
    @Autowired
    private SystemMenuService systemMenuService;
    @Autowired
    private SystemRoleService systemRoleService;

    /**
     * 所有用户
     * @param username
     * @return
     */
    @RequestMapping("user/getUserList")
    @ResponseBody
    public Object getUserList(@Param("username")String username,HttpServletRequest request){
        SystemUser manage = (SystemUser)request.getSession().getAttribute(LoginController.SYSTEM_USER);

        Map<String, Object> resultMap = new HashMap<>();
        List<SystemUser> systemUsers = new ArrayList<>();
        if (StringUtils.isNotBlank(username)){
            SystemUser user = systemUserService.getUserByUserName(username);
            systemUsers.add(user);
        }else {
            List<SystemUser> allSystemUser = systemUserService.getAllSystemUser();
            if (manage.getId()==1000){
                systemUsers.addAll(allSystemUser);
            }else {
                Iterator<SystemUser> iterator = allSystemUser.iterator();
                while (iterator.hasNext()){
                    if (iterator.next().getId()==1000){
                        iterator.remove();
                        break;
                    }
                }
                systemUsers.addAll(allSystemUser);
            }
        }

        resultMap.put("total",systemUsers.size());
        resultMap.put("rows",systemUsers);
        return resultMap;

    }

    @RequestMapping("addUser")
    public String addUser(){
        return "/pages/systemManage/addUser.html";
    }
    @RequestMapping("modifyPwd")
    public String modifyPwd(){
        return "/pages/systemManage/modifyPwd.html";
    }

    @RequestMapping("/user/addUser")
    @ResponseBody
    public Object addUser(SystemUser systemUser )throws IOException {
        if (systemUser!=null&&StringUtils.isNotBlank(systemUser.getUserName())){
            Assert.hasText(systemUser.getUserName(),"账号信息不存在");
            Assert.hasText(systemUser.getPassword(),"密码设置为空");
            SystemUser user = systemUserService.getUserByUserName(systemUser.getUserName());
            if (user!=null){
                return new Result("400","账号已存在",null);
            }else {
                systemUser.setCreateTime(new Timestamp(new Date().getTime()));
                systemUser.setPassword(MD5Util.generate(systemUser.getPassword()));
                systemUserService.save(systemUser);
            }
            return new Result("200","创建成功",null);

        }else{
            return new Result("400","未获取到表单信息",null);
        }

    }

    /**
     * 修改密码接口
     * @param oldPassword
     * @param newPassword
     * @param request
     * @return
     */
    @RequestMapping("user/modifyPassword")
    @ResponseBody
    public Result modifyPassword(@Param("oldPassword") String oldPassword,@Param("password")  String newPassword, HttpServletRequest request){

        SystemUser systemUser = (SystemUser)request.getSession().getAttribute(LoginController.SYSTEM_USER);
        if(systemUser==null||systemUser.getId()==null){
            return new Result("400","未获取到登录用户信息，请登录后再修改密码",null);
        }
        String password = systemUser.getPassword();
        //系统记录的密码和旧密码比较
        if (!MD5Util.verify(oldPassword,password)){
            return new Result("400","输入的原密码错误，请重新输入",null);
        }
        //生成新的加密
        systemUser.setPassword(MD5Util.generate(newPassword));
        systemUserService.upDatePassword(systemUser);
        return new Result("200","success",null);
    }

    @RequestMapping("modifyRole")
    public ModelAndView modifyUserRole(@Param("userId")Long userId, ModelAndView model){
        SystemUser systemUser = systemUserService.getUserByUserId(userId);
        List<SystemRole> all = systemRoleService.findAll();
        List<SystemUserRole> userRoles = systemUserService.getUserRoleByUserId(systemUser.getId());
        ArrayList<Long> list = new ArrayList<>();

        userRoles.stream().forEach(systemUserRole -> list.add(systemUserRole.getRole().getRoleId()));
        model.addObject("user",systemUser);
        model.addObject("roles",all);
        model.addObject("userRoles",list);
        model.setViewName("pages/systemManage/userRole");
        return model;
    }

    @RequestMapping(value = "user/addUserRole",method= RequestMethod.POST)
    @ResponseBody
    public Object register(@RequestParam(value ="userId",required = true)Long userId
            ,@RequestParam(value ="roleIds",required =false) HashSet<Long> roles){
        SystemUser systemUser = systemUserService.getUserByUserId(userId);
        if (systemUser == null) {
            return new Result("400","用户不存在",null);
        }
        int saveUserRole = systemUserService.saveUserRole(systemUser, roles);
        return new Result("200","success",null);
    }

    @RequestMapping(value = "user/resetPassword",method= RequestMethod.POST)
    @ResponseBody
    public Object resetPassword(@RequestParam(value ="userId",required = true)Long userId
           ,HttpServletRequest request){
        SystemUser systemUser = (SystemUser)request.getSession().getAttribute(LoginController.SYSTEM_USER);
        if(systemUser==null||systemUser.getId()==null){
            return new Result("400","未获取到登录用户信息，请登录后再修改密码",null);
        }

        //生成新的加密
        systemUser.setPassword(MD5Util.generate("123456"));
        systemUserService.upDatePassword(systemUser);
        return new Result("200","success",null);
    }

}
