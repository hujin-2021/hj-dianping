package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.List;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author hj
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public Result sendCode(String phone, HttpSession session) {
         //1.校验手机号
         if(RegexUtils.isPhoneInvalid(phone)){
             //2.不符合则返回错误信息
             return Result.fail("手机号格式错误");
         }
         //3.符合，生成验证码
         String code = RandomUtil.randomNumbers(6);
         //4.保存code到session
         session.setAttribute("code",code);
         //5.发送验证码
         log.debug("验证码发送成功");
         log.debug(code);
         return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号
        String phone=loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.不符合则返回错误信息
            return Result.fail("手机号格式错误");
        }
        //2.校验验证码
        Object checkCode=session.getAttribute("code");
        if(checkCode==null||!checkCode.toString().equals(loginForm.getCode())){
            return Result.fail("验证码错误");
        }
        //4.根据手机号查询用户
        User user=query().eq("phone",phone).one();//.list可以得到多个
        //5.判断是否存在
        if(user==null){
            //6.不存在，创建新用户
            user=creatUserWithPhone(phone);
        }
        UserDTO userDTO=BeanUtil.copyProperties(user, UserDTO.class);
        session.setAttribute("user", userDTO);

        return Result.ok("登陆成功");
    }

    public User creatUserWithPhone(String phone){
        User user=new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        save(user);
        return user;
    }

}
