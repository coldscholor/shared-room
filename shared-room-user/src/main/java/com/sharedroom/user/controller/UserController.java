package com.sharedroom.user.controller;

import com.sharedroom.common.result.Result;
import com.sharedroom.user.dto.LoginDTO;
import com.sharedroom.user.dto.RegisterDTO;
import com.sharedroom.user.service.UserService;
import com.sharedroom.user.vo.LoginVO;
import com.sharedroom.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserVO userVO = userService.register(registerDTO);
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser() {
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/update")
    public Result<UserVO> updateUser(@RequestBody UserVO userVO) {
        UserVO result = userService.updateUser(userVO);
        return Result.success("更新成功", result);
    }

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(@PathVariable Long userId) {
        UserVO userVO = new UserVO();
        // 这里可以根据需要实现获取其他用户信息的逻辑
        // 注意要过滤敏感信息
        return Result.success(userVO);
    }
}