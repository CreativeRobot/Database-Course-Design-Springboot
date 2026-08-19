package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.UpdateProfileDTO;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserProfileVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public Result<UserProfileVo> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public Result<UserProfileVo> updateCurrentUser(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        return Result.success(userService.updateProfile(userId, updateProfileDTO));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UserProfileVo> updateAvatar(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        return Result.success(userService.updateAvatar(userId, file));
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(userId, changePasswordDTO);
        return Result.success(null);
    }
}
