package com.sharedroom.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sharedroom.common.entity.User;
import com.sharedroom.common.exception.BusinessException;
import com.sharedroom.common.result.ResultCode;
import com.sharedroom.common.utils.JwtUtils;
import com.sharedroom.common.utils.UserContext;
import com.sharedroom.user.dto.LoginDTO;
import com.sharedroom.user.dto.RegisterDTO;
import com.sharedroom.user.mapper.UserMapper;
import com.sharedroom.user.service.UserService;
import com.sharedroom.user.vo.LoginVO;
import com.sharedroom.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserVO register(RegisterDTO registerDTO) {
        // 校验用户名是否已存在
        if (getByUsername(registerDTO.getUsername()) != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXIST);
        }
        
        // 校验手机号是否已存在
        if (StringUtils.hasText(registerDTO.getPhone()) && getByPhone(registerDTO.getPhone()) != null) {
            throw new BusinessException(ResultCode.PHONE_ALREADY_EXIST);
        }
        
        // 校验邮箱是否已存在
        if (StringUtils.hasText(registerDTO.getEmail()) && getByEmail(registerDTO.getEmail()) != null) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXIST);
        }
        
        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1); // 正常状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        // 保存用户
        if (!save(user)) {
            throw new BusinessException("用户注册失败");
        }
        
        // 返回用户信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setPassword(null); // 不返回密码
        
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        return userVO;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 根据用户名查询用户
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        
        // 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        
        // 校验用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 生成JWT令牌
        String token = JwtUtils.generateToken(user.getId(), user.getUsername());
        
        // 构建登录结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        return loginVO;
    }

    @Override
    public User getByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
    }

    @Override
    public User getByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .eq(User::getDeleted, 0));
    }

    @Override
    public User getByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .eq(User::getDeleted, 0));
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.LOGIN_REQUIRED);
        }
        
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setPassword(null); // 不返回密码
        
        return userVO;
    }

    @Override
    public UserVO updateUser(UserVO userVO) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.LOGIN_REQUIRED);
        }
        
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }
        
        // 更新用户信息
        if (StringUtils.hasText(userVO.getNickname())) {
            user.setNickname(userVO.getNickname());
        }
        if (StringUtils.hasText(userVO.getAvatar())) {
            user.setAvatar(userVO.getAvatar());
        }
        if (userVO.getGender() != null) {
            user.setGender(userVO.getGender());
        }
        user.setUpdateTime(LocalDateTime.now());
        
        if (!updateById(user)) {
            throw new BusinessException("用户信息更新失败");
        }
        
        UserVO result = new UserVO();
        BeanUtils.copyProperties(user, result);
        result.setPassword(null); // 不返回密码
        
        log.info("用户信息更新成功: userId={}", userId);
        return result;
    }
}