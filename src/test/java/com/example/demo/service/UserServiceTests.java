package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.UploadFileVo;
import com.example.demo.vo.UserProfileVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserService userService;

    @Test
    void updateAvatarStoresUrlOnActiveUserAndReturnsItInProfile() {
        User user = User.builder()
                .id(42L)
                .username("reader")
                .status(1)
                .build();
        MockMultipartFile image = new MockMultipartFile(
                "file", "portrait.png", "image/png", new byte[]{1, 2, 3});
        UploadFileVo upload = new UploadFileVo(
                "/uploads/avatars/42/avatar.png", "avatar.png", "image/png", 3);
        when(userRepository.findByIdAndStatus(42L, 1)).thenReturn(Optional.of(user));
        when(fileStorageService.storeAvatar(eq(42L), any())).thenReturn(upload);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileVo result = userService.updateAvatar(42L, image);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("/uploads/avatars/42/avatar.png", savedUser.getValue().getAvatarUrl());
        assertEquals("/uploads/avatars/42/avatar.png", result.getAvatarUrl());
    }
}
