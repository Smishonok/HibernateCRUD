package com.valentinnikolaev.hibernatecrud.repository;

import com.valentinnikolaev.hibernatecrud.models.Post;

import java.util.List;

public interface PostRepository extends GenericRepository<Post,Long> {

    List<Post> getPostsByUserId(Long userId);

    boolean removePostsByUserId(Long userId);


}
