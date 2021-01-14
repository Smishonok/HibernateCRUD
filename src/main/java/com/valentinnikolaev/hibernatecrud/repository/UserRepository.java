package com.valentinnikolaev.hibernatecrud.repository;

import com.valentinnikolaev.hibernatecrud.models.User;

import java.util.Optional;

public interface UserRepository extends GenericRepository<User, Long> {

    Optional<User> get(long id, boolean loadPosts);
}
