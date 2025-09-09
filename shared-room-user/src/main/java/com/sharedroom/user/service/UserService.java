package com.sharedroom.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sharedroom.common.entity.User;
import com.sharedroom.user.dto.LoginDTO;
import com.sharedroom.user.dto.RegisterDTO;
import com.sharedroom.user.vo.LoginVO;
import com.sharedroom.user.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getByPhone(String phone);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User getByEmail(String email);

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    UserVO getCurrentUser();

    /**
     * 更新用户信息
     *
     * @param userVO 用户信息
     * @return 更新后的用户信息
     */
    UserVO updateUser(UserVO userVO);
}