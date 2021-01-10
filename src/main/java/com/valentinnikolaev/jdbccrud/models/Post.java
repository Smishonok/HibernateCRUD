package com.valentinnikolaev.jdbccrud.models;

import javax.persistence.*;
import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table (name = "posts")
public class Post {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.EAGER)
    private User user;

    @Column
    private String content;

    @Column (name = "creating_date")
    private LocalDateTime created;

    @Column (name = "updating_date")
    private LocalDateTime updated;

    @Transient
    private Clock clock;

    public Post() {
    }

    public Post(Long id, User user, String content, LocalDateTime created, LocalDateTime updated) {
        this.id      = id;
        this.user    = user;
        this.content = content;
        this.created = created;
        this.updated = updated;
        this.clock   = Clock.systemUTC();
    }

    public Post(Long id, User user, String content, Clock clock) {
        this.id      = id;
        this.user  = user;
        this.content = content;
        this.clock   = clock;
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

    public void setContent(String content) {
        this.content = content;
        this.updated = LocalDateTime.now(clock);
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
}
