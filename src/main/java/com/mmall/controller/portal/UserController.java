package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller//声明该类为一个Controller
@RequestMapping("/user/")//声明当前类的一级请求路径
public class UserController {
    //自动注入service
    @Autowired
    IUserService iUserService;
    //登录
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
      ServerResponse<User> response =   iUserService.login(username,password);
      if(response.isSuccess()){
          session.setAttribute(Const.CURRENT_USER,response.getData());
      }
        return response;
    }
    //退出登录
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> logout(HttpSession session){
        session.invalidate();
        return ServerResponse.createBySuccess();
    }
    //注册
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<String> register(User user){

    return iUserService.register(user);
    }

    /**
     * 用户对用户注册时的用户名和邮箱的唯一性进行校验
     * @param str 校验属性值
     * @param type 校验的属性名：
     * @return
     */
    @RequestMapping(value = "check_validate.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValidate(String str, String type){
         return    iUserService.checkVlidate(str,type);
    }
    //登陆后获取用户信息
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession  session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user != null){

            return  ServerResponse.createBySuccess(user);
        }
        return  ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
    }

    //获取忘记密码是的密码提示问题
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public  ServerResponse<String> fotgetGetQuestion(String username){
        return  iUserService.selectQuestion(username);
    }
    // 获取忘记密码的密码提示问题的答案
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> fogetCheckAnswer(String username,String question ,String answer){
        return  iUserService.checkAnswer(username, question, answer);
    }

    //忘记密码的重置密码
    @RequestMapping(value = "forget_rest_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> fogetRestPassword(String username,String passwordNew,String token){
        return  iUserService.forgetResetPassword(username,passwordNew,token);
    }

    // 登录状态的重置密码
    @RequestMapping(value = "rest_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
          return  ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }
    //更新用户信息
    @RequestMapping(value = "update_userInfomation.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_userInfomation(HttpSession session ,User user){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
           return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());

        ServerResponse<User> response =   iUserService.updateInformation(user);
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return  response;
    }

    //获取用户详细信息接口，当用户没有登录，我们强制让其登录
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){

        User currentUser= (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser==null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录，请先登录！status=10");

        }

        return iUserService.getInformation(currentUser.getId());

    }
}
