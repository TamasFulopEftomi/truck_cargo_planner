package com.eftomi.tcp.service;

import com.eftomi.tcp.entity.User;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface UserService {

    @Transactional
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    public boolean registration(String name, String email, String password);
    public boolean login(String email, String password);
    public Optional<User> getUser(String email);
}
