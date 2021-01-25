package com.valentinnikolaev.hibernatecrud.repository;

import com.valentinnikolaev.hibernatecrud.models.Post;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends GenericRepository<Post,Long> {

    Optional<Post> add(Long id, String content, Clock clock);

    List<Post> getPostsByUserId(Long userId);

    boolean removePostsByUserId(Long userId);


}
