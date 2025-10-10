package org.example.userservice.dao;

import org.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    User update(User user);

    void delete(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}