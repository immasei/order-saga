package com.example.store.service;

import com.example.store.dto.account.SignUpDTO;
import com.example.store.dto.account.UpdateCustomerDTO;
import com.example.store.dto.account.UserDTO;
import jakarta.transaction.Transactional;
import com.example.store.model.Admin;
import com.example.store.model.Customer;
import com.example.store.model.User;
import com.example.store.enums.UserRole;
import com.example.store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    // --- UserDetailsService
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("##########################\nusername: " + username + "\n##########################");
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("User with email "+ username +" not found"));
    }

    // --- Queries
    public User getUserById(UUID id) {
        User user = userRepository.getOrThrow(id);
        return user;
    }

    public List<UserDTO> getUsersByRole(UserRole role) {
        return userRepository.findAllByRole(role)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserDTO getUserByIdAndRole(UUID id, UserRole role) {
        User user = userRepository.getOrThrow(id, role);
        return toResponse(user);
    }

    @Transactional
    public UserDTO createUser(SignUpDTO signUpDto) {
        UserRole role = parseRole(signUpDto);
        User newUser = buildUserByRole(signUpDto, role);

        newUser.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        newUser.setRole(role);

        User saved = userRepository.saveAndFlush(newUser); // let db enforce email uniqueness
        return toResponse(saved);
    }

    @Transactional
    public UserDTO updateUserById(UUID id, UpdateCustomerDTO userDto) {
        User user = userRepository.getOrThrow(id);
        modelMapper.map(userDto, user); // managed entity mutated
        return toResponse(user);        // save() optional; flush on commit
    }

    @Transactional
    public void deleteUserById(UUID userId) {
        User user = userRepository.getOrThrow(userId);
        userRepository.delete(user);
    }

    // --- Utils
    private UserRole parseRole(SignUpDTO dto) {
        // role validation is defined in the DTO and enforced by the controller (@Valid)
        // convert to uppercase to match the enum format, since input may use mixed case
        return UserRole.valueOf(dto.getRole().toUpperCase());
    }

    private User buildUserByRole(SignUpDTO dto, UserRole role) {
        // exhaustive for all current roles, will fail compile if new roles are added
        Class<? extends User> entityClass = switch (role) {
            case ADMIN -> Admin.class;
            case CUSTOMER -> Customer.class;
            case GUEST -> throw new IllegalArgumentException("Guest accounts cannot be created.");
        };
        return modelMapper.map(dto, entityClass);
    }

    // --- Mapper
    public UserDTO toResponse(User user) {
        UserDTO userDto = modelMapper.map(user, UserDTO.class);
        return userDto;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User with email "+ email +" not found"));

    }
}
