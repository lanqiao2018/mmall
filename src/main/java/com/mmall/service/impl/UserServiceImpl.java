package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    //登录
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultcount = userMapper.checkUsername(username);
        if(resultcount == 0 ){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //MD5加密
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selelctLogin(username,md5Password);
        if(user == null){
            return  ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return  ServerResponse.createBySuccess("登录成功",user);
    }
    //注册
    @Override
    public ServerResponse<String> register(User user){
//        //检查用户名
//       int userCount =  userMapper.checkUsername(user.getUsername());
//       if(userCount > 0 ){
//          return  ServerResponse.createByErrorMessage("用户名已存在");
//       }
//        // 检查Email
//        int emailCount = userMapper.checkEmail(user.getEmail());
//       if(emailCount > 0 ){
//           return  ServerResponse.createByErrorMessage("邮箱已被注册");
//       }
        ServerResponse validateResult = this.checkVlidate(user.getUsername(),Const.USERNAME);
        if(!validateResult.isSuccess()){
            return  validateResult;
        }
        validateResult = this.checkVlidate(user.getEmail(),Const.EMAIL);
        if(!validateResult.isSuccess()){
            return  validateResult;
        }

        //设置角色
       user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

       int resulutCount =  userMapper.insert(user);
       if(resulutCount == 0){
        ServerResponse.createByErrorMessage("注册失败");
       }
        return  ServerResponse.createBySuccessMessage("注册成功");
    }
    // 校验用户名和邮箱是否可用

    public  ServerResponse<String> checkVlidate(String str,String type){
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int userCount =  userMapper.checkUsername(str);
                if(userCount > 0 ){
                    return  ServerResponse.createByErrorMessage("用户名已存在");
                }

            }
            if(Const.EMAIL.equals(type)){
                int emailCount = userMapper.checkEmail(str);
                if(emailCount > 0 ){
                    return  ServerResponse.createByErrorMessage("邮箱已被注册");
                }
            }
        }else{
          return   ServerResponse.createByErrorMessage("参数错误");
        }
        return  ServerResponse.createBySuccessMessage("校验成功");

    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
       ServerResponse<String> vildateResponse =  checkVlidate(username,Const.USERNAME);
       if(vildateResponse.isSuccess()){
           //用户不存在
           return  ServerResponse.createByErrorMessage("用户不存在");
       }
      String question =  userMapper.selectQuestion(username);
       if(StringUtils.isBlank(question)){
         return  ServerResponse.createByErrorMessage("找回密码的问题是空的");
       }
        return   ServerResponse.createBySuccess(question);
    }
    //校验问题答案
    @Override
    public ServerResponse<String> checkAnswer(String username,String question ,String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount > 0 ){
            //问题和答案正确 并是当前用户的设置
            String  forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return  ServerResponse.createBySuccess(forgetToken);
        }

        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token参数需要传递");
        }
        ServerResponse<String> vildateResponse =  checkVlidate(username,Const.USERNAME);
        if(vildateResponse.isSuccess()){
            //用户不存在
            return  ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX +username);
        if(StringUtils.isBlank(token)){
            return  ServerResponse.createByErrorMessage("token 无效或者tonken过期");
        }
        if(StringUtils.equals(forgetToken,token)){
            String passwordMd5 = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,passwordMd5);
            if(rowCount > 0 ){
                return  ServerResponse.createBySuccessMessage("重置密码成功");
            }
        }else{
            return  ServerResponse.createByErrorMessage("token错误，请重新获取重置密码token");
        }

        return   ServerResponse.createByErrorMessage("重置密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //  //防止横向越权，要校验一下这个用户的旧密码，一定要指向这个用户。因为我们会查询一个count(1),如果不指定id,那么结果可能就是true  count>0
      int count =   userMapper.checkPassowrd(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
      if(count  == 0 ){
        return  ServerResponse.createByErrorMessage("原密码错误");
      }
       user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
      count = userMapper.updateByPrimaryKeySelective(user);
      if(count > 0 ){
          return  ServerResponse.createBySuccessMessage("密码更新成功");
      }
      return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0 ){
            return  ServerResponse.createByErrorMessage("邮箱已被使用，请更换邮箱在尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0 ){
            return  ServerResponse.createBySuccess("个人信息更新成功",updateUser);
        }

    return ServerResponse.createByErrorMessage("个人信息更新失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user= userMapper.selectByPrimaryKey(userId);
        if(user==null){
            ServerResponse.createByErrorMessage("找不到当前用户");
        }
        //将返回给Controlller层的密码设置为空，不传给前台
        user.setPassword(StringUtils.EMPTY);
        return  ServerResponse.createBySuccess(user);
    }


}
