package ru.astondevs.learn.vorobev.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.astondevs.learn.vorobev.dto.CreateUserRequest;
import ru.astondevs.learn.vorobev.dto.UpdateUserRequest;
import ru.astondevs.learn.vorobev.dto.UserResponse;
import ru.astondevs.learn.vorobev.entity.User;
import ru.astondevs.learn.vorobev.repository.UserRepository;
import ru.astondevs.learn.vorobev.exception.ResourceNotFoundException;
import ru.astondevs.learn.vorobev.exception.DuplicateEmailException;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Пользователь с email " + request.getEmail() + " уже существует");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .age(request.getAge())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Создан новый пользователь с ID: {}", savedUser.getId());

        return UserResponse.fromEntity(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + id + " не найден"));

        boolean needsUpdate = false;

        if (request.getName() != null && !request.getName().trim().isEmpty()
                && !request.getName().trim().equals(user.getName())) {
            user.setName(request.getName().trim());
            needsUpdate = true;
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()
                && !request.getEmail().trim().equals(user.getEmail())) {
            String newEmail = request.getEmail().trim();

            userRepository.findByEmail(newEmail)
                    .ifPresent(existingUser -> {
                        if (!existingUser.getId().equals(id)) {
                            throw new DuplicateEmailException("Пользователь с email " + newEmail + " уже существует");
                        }
                    });

            user.setEmail(newEmail);
            needsUpdate = true;
        }

        if (request.getAge() != null && !request.getAge().equals(user.getAge())) {
            user.setAge(request.getAge());
            needsUpdate = true;
        }

        if (needsUpdate) {
            User updatedUser = userRepository.save(user);
            log.info("Пользователь с ID {} успешно обновлен", id);
            return UserResponse.fromEntity(updatedUser);
        }

        return UserResponse.fromEntity(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
        }

        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удален", id);
    }
}
