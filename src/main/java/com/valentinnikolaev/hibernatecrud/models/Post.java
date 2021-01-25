package com.valentinnikolaev.hibernatecrud.models;

import com.valentinnikolaev.hibernatecrud.utils.hibernateconverters.LocalDateTimeAttributeConverter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table (name = "posts")
public class Post {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column
    private String content;

    @Column (name = "creating_date")
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime created;

    @Column (name = "updating_date")
   @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime updated;

    @Transient
    private Clock clock;

    public Post() {
    }

    public Post(Long id, User user, String content, Clock clock) {
        this.id      = id;
        this.user    = user;
        this.content = content;
        this.clock   = Clock.systemUTC();
    }

    public Post(User user, String content, Clock clock) {
        this.user  = user;
        this.content = content;
        this.clock   = clock;
        this.created = LocalDateTime.now(clock);
        this.updated = created;
    }

    public Post(String content, Clock clock) {
        this.content = content;
        this.clock = clock;
        this.created = LocalDateTime.now(clock);
        this.updated = created;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getDateOfCreation() {
        return created;
    }

    public LocalDateTime getDateOfLastUpdate() {
        return updated;
    }

    public void setUser(User user) {
        user.getPosts().add(this);
        this.user = user;
    }

    public Post setContent(String content) {
        this.content = content;
        this.updated = LocalDateTime.now(clock);
        return this;
    }

    public Post setClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = this.content.hashCode() + user.getId().hashCode() + created.hashCode() +
                   updated.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (this.hashCode() != obj.hashCode()) {
            return false;
        }

        Post comparingObj = (Post) obj;
        return this.content.equals(comparingObj.content) && this.user == comparingObj.getUser();
    }

    public boolean equalsContent(Post post) {
        return this.content.equals(post.getContent());
    }

    @Override
    public String toString() {
        return "Post{" + "id=" + id + ", userId=" + user.getId() + ", content='" + content + '\'' +
               ", created=" + created + ", updated=" + updated + '}';
    }

    public static class PostBuilder {
        private Long postId;
        private String postContent;
        private User user;
        private Clock clock;

        public PostBuilder() {
        }

        public PostBuilder withId(Long id) {
            this.postId = id;
            return this;
        }

        public PostBuilder withContent(String content) {
            this.postContent = content;
            return this;
        }

        public PostBuilder withUser(User user) {
            this.user = user;
            return this;
        }

        public PostBuilder withClock(Clock clock) {
            this.clock = clock;
            return this;
        }

        public Post build() {
            if (postId != null && postContent != null && user != null && clock != null) {
                return new Post(postId, user, postContent, clock);
            } else if (postId == null && postContent != null && user != null && clock != null) {
                return new Post(user, postContent, clock);
            } else if (postId == null && postContent != null && user == null && clock != null) {
                return new Post(postContent, clock);
            } else if (postId == null && postContent != null && user == null && clock == null) {
                return new Post(postContent, Clock.systemUTC());
            }
            return new Post();
        }
    }
}

