package com.js.sas.controller;

import com.js.sas.entity.SystemMenu;
import com.js.sas.entity.SystemUser;
import com.js.sas.service.SystemMenuService;
import com.js.sas.service.SystemUserService;
import com.js.sas.utils.MD5Util;
import com.js.sas.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author daniel
 * @description: 登录相关
 * @create: 2019-10-16 11:21
 */
@Controller
@RequestMapping()
@Slf4j
public class LoginController {
    public static final String SYSTEM_USER = "SYSTEM_USER";

    @Autowired
    private SystemUserService systemUserService;
    @Autowired
    private SystemMenuService systemMenuService;
    @PostMapping("/login")
    @ResponseBody
    public Result loginIn(HttpServletRequest request, HttpServletResponse response, Model model)throws IOException {
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String remember = request.getParameter("remember");
            Assert.hasText(username,"登录账号为空");
            Assert.hasText(password,"登录密码为空");
            HttpSession session = request.getSession();
            log.info(request.getParameter("remember"));
            SystemUser user = systemUserService.getUserByUserName(username.trim());
            if (user==null){
                return new Result("400","无效的登录账号",null);
            }
            if (!MD5Util.verify(password,user.getPassword())){
                return new Result("400","密码验证错误",null);
            }
            Cookie jinshang_name =null;
            Cookie jinshang_pwd = null;
            if (remember.equals("true")) {
                jinshang_name = new Cookie("jinshang_name", username);
                jinshang_pwd = new Cookie("jingshang_pwd", password);
                jinshang_name.setMaxAge(7 * 24 * 60 * 60);
                jinshang_pwd.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(jinshang_name);
                response.addCookie(jinshang_pwd);
            }else{
                jinshang_name = new Cookie("jinshang_name", username);
                jinshang_pwd = new Cookie("jingshang_pwd", password);
                jinshang_name.setMaxAge(0);
                jinshang_pwd.setMaxAge(0);
                response.addCookie(jinshang_name);
                response.addCookie(jinshang_pwd);
            }
            session.setAttribute(SYSTEM_USER,user);
            session.setMaxInactiveInterval(12*60*60*60);

            request.getRequestDispatcher("index");
        } catch (Exception e) {
            model.addAttribute("message",e.getMessage());
            return new Result("400",e.getMessage(),null);
        }
        return new Result("200","success",null);
    }



    /**
     * 退出登录 注销
     * @param request
     * @return
     */
    @RequestMapping("login_out")
    public String loginOut(HttpServletRequest request,HttpServletResponse response){
        request.getSession().removeAttribute(this.SYSTEM_USER);
//        Cookie cookie = new Cookie("message", "未获取到登录信息--请重新登录");
//        cookie.setMaxAge(5*60);
//        response.addCookie( cookie);
        return "pages/login_in";
    }

    /**
     * 退出登录 注销
     * @param request
     * @return
     */
    @RequestMapping("login_err")
    public String loginErr(HttpServletRequest request,HttpServletResponse response){
        request.getSession().removeAttribute(this.SYSTEM_USER);
        Cookie cookie = new Cookie("message", "未获取到登录信息--请重新登录");
        cookie.setMaxAge(5);
        response.addCookie( cookie);
        return "pages/login_in";
    }

    @RequestMapping(value = "/getMenu",method = RequestMethod.GET)
    @ResponseBody
    public Result getMenu(HttpServletRequest request){
        SystemUser systemUser = (SystemUser) request.getSession().getAttribute(this.SYSTEM_USER);
        if (systemUser==null){
            return new Result("400","未获取到用户登录信息",null);
        }
        //分配菜单
        List<SystemMenu> systemMenus = systemMenuService.getAllByUerId(systemUser.getId());
        List<SystemMenu> all = systemMenuService.findAll();
        //父节点
        List<SystemMenu> parentMenus = new ArrayList<>();
        for (SystemMenu systemMenu : all) {
            if (systemMenu.getPid()==0l){
                parentMenus.add(systemMenu);
            }
        }

        all.removeAll(systemMenus);
        List<String> list = new ArrayList<>();
        for (int i = 0 ;i< all.size();i++){
            if (all.get(i).getPid()!=0l) {
                list.add(all.get(i).getIdColumn());
            }
        }
        Set<SystemMenu > set = new HashSet<>();//菜单中需要的父菜单

        for (SystemMenu psystemMenu : parentMenus){//父类菜单
            for (SystemMenu systemMenu : systemMenus) {//分配菜单
                //判断分配菜单的pid是否包含所有父节点id
                if (systemMenu.getPid().equals(psystemMenu.getMenuId()) ){
                    set.add(psystemMenu);
                    continue;
                }
            }
        }
        parentMenus.removeAll(set);
        parentMenus.forEach(s -> list.add(s.getIdColumn()));
        return new Result("200","success",list);
    }
}
