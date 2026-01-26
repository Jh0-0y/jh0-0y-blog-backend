package com.blog.backend.feature.user.service;

import com.blog.backend.feature.user.repository.UserRepository;
import com.blog.backend.global.core.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserValidatorService {

    private final UserRepository userRepository;

    public void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw CustomException.conflict("이미 사용 중인 이메일입니다");
        }
    }

    public void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw CustomException.conflict("이미 사용 중인 닉네임입니다");
        }
    }
}
