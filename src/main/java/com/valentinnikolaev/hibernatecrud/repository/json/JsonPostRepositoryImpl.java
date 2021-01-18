package com.valentinnikolaev.hibernatecrud.repository.json;

import com.valentinnikolaev.hibernatecrud.models.Post;
import com.valentinnikolaev.hibernatecrud.repository.PostRepository;
import com.valentinnikolaev.hibernatecrud.utils.jsonparser.JsonParser;
import com.valentinnikolaev.hibernatecrud.utils.jsonparser.JsonParserFactory;
import com.valentinnikolaev.hibernatecrud.utils.Constants;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonPostRepositoryImpl implements PostRepository {

    private JsonParser<Post> parser         = JsonParserFactory.getFactory(Post.class).getParser();
    private Path             repositoryPath = Constants.REPOSITORY_PATH.resolve(
            Constants.POST_REPOSITORY_FILE_NAME);

    {
        FileService.createRepository(repositoryPath);
    }

    @Override
    public Optional<Post> add(Post entity) {
        String repositoryData = FileService.getDataFromRepository(repositoryPath);
        List<Post> posts = parser.parseList(repositoryData) == null ? new ArrayList<>() :
                parser.parseList(repositoryData);
        posts.add(entity);

        String dataForWritingInRepo = parser.serialise(posts);
        FileService.writeDataIntoRepository(dataForWritingInRepo, repositoryPath);

        return parser.parseList(FileService.getDataFromRepository(repositoryPath)).stream().filter(
                post->post.equals(entity)).findFirst();
    }

    @Override
    public Optional<Post> get(Long aLong) {
        return parser.parseList(FileService.getDataFromRepository(repositoryPath)).stream().filter(
                post->post.getId() == aLong).findFirst();
    }

    @Override
    public List<Post> getPostsByUserId(Long userId) {
        return parser.parseList(FileService.getDataFromRepository(repositoryPath)).stream().filter(
                post->post.getUser().getId() == userId).collect(Collectors.toList());
    }

    @Override
    public Optional<Post> change(Post entity) {
        String     repositoryData = FileService.getDataFromRepository(repositoryPath);
        List<Post> posts          = parser.parseList(repositoryData);

        posts
                .stream()
                .filter(post->post.getId() == entity.getId())
                .collect(Collectors.toList())
                .forEach(posts::remove);
        posts.add(entity);

        String dataForWritingInRepo = parser.serialise(posts);
        FileService.writeDataIntoRepository(dataForWritingInRepo, repositoryPath);

        return parser.parseList(FileService.getDataFromRepository(repositoryPath)).stream().filter(
                post->post.getId() == entity.getId()).findFirst();
    }

    @Override
    public boolean remove(Long aLong) {
        String     repositoryData = FileService.getDataFromRepository(repositoryPath);
        List<Post> posts          = parser.parseList(repositoryData);

        posts.stream().filter(post->post.getId() == aLong).collect(Collectors.toList()).forEach(
                posts::remove);
        FileService.writeDataIntoRepository(parser.serialise(posts), repositoryPath);

        return parser
                .parseList(FileService.getDataFromRepository(repositoryPath))
                .stream()
                .noneMatch((post->post.getId() == aLong));
    }

    @Override
    public boolean removePostsByUserId(Long userId) {
        String     repositoryData = FileService.getDataFromRepository(repositoryPath);
        List<Post> posts          = parser.parseList(repositoryData);

        posts
                .stream()
                .filter(post->post.getUser().getId() == userId)
                .collect(Collectors.toList())
                .forEach(posts::remove);
        FileService.writeDataIntoRepository(parser.serialise(posts), repositoryPath);

        return parser
                .parseList(FileService.getDataFromRepository(repositoryPath))
                .stream()
                .noneMatch((post->post.getUser().getId() == userId));
    }

    @Override
    public List<Post> getAll() {
        return parser.parseList(FileService.getDataFromRepository(repositoryPath)) == null ?
                new ArrayList<>() : parser.parseList(
                FileService.getDataFromRepository(repositoryPath));
    }

    @Override
    public boolean isContains(Long aLong) {
        return parser
                .parseList(FileService.getDataFromRepository(repositoryPath))
                .stream()
                .anyMatch(post->post.getId() == aLong);
    }
}
