package com.valentinnikolaev.hibernatecrud.repository.json;

import com.valentinnikolaev.hibernatecrud.models.User;
import com.valentinnikolaev.hibernatecrud.repository.UserRepository;
import com.valentinnikolaev.hibernatecrud.utils.jsonparser.JsonParser;
import com.valentinnikolaev.hibernatecrud.utils.jsonparser.JsonParserFactory;
import com.valentinnikolaev.hibernatecrud.utils.Constants;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonUserRepositoryImpl implements UserRepository {

    private JsonParser<User> parser         = JsonParserFactory.getFactory(User.class).getParser();
    private Path             repositoryPath = Constants.REPOSITORY_PATH.resolve(
            Constants.USER_REPOSITORY_FILE_NAME);

    {
        FileService.createRepository(repositoryPath);
    }

    @Override
    public Optional<User> add(User entity) {
        String repositoryData = FileService.getDataFromRepository(repositoryPath);
        List<User> users = parser.parseList(repositoryData) == null ? new ArrayList<>() :
                parser.parseList(repositoryData);
        users.add(entity);

        String dataForWritingInRepo = parser.serialise(users);
        FileService.writeDataIntoRepository(dataForWritingInRepo, repositoryPath);

        return parser.parseList(FileService.getDataFromRepository(repositoryPath)).stream().filter(
                user->user.equals(entity)).findFirst();
    }

    @Override
    public Optional<User> get(Long aLong) {
        return parser.parseList(FileService.getDataFromRepository(repositoryPath)).stream().filter(
                user->user.getId() == aLong).findFirst();
    }

    @Override
    public Optional<User> change(User entity) {
        String     repositoryData = FileService.getDataFromRepository(repositoryPath);
        List<User> users          = parser.parseList(repositoryData);

        users
                .stream()
                .filter(user->user.getId() == entity.getId())
                .collect(Collectors.toList())
                .forEach(users::remove);
        users.add(entity);

        String dataForWritingInRepo = parser.serialise(users);
        FileService.writeDataIntoRepository(dataForWritingInRepo, repositoryPath);

        return parser.parseList(FileService.getDataFromRepository(repositoryPath)).stream().filter(
                user->user.getId() == entity.getId()).findFirst();
    }

    @Override
    public boolean remove(Long aLong) {
        String     repositoryData = FileService.getDataFromRepository(repositoryPath);
        List<User> users          = parser.parseList(repositoryData);

        users.stream().filter(user->user.getId() == aLong).forEach(users::remove);
        return parser
                .parseList(FileService.getDataFromRepository(repositoryPath))
                .stream()
                .noneMatch((user->user.getId() == aLong));
    }

    @Override
    public List<User> getAll() {
        return parser.parseList(FileService.getDataFromRepository(repositoryPath)) == null ?
                new ArrayList<>() : parser.parseList(
                FileService.getDataFromRepository(repositoryPath));
    }

    @Override
    public boolean isContains(Long aLong) {
        return parser
                .parseList(FileService.getDataFromRepository(repositoryPath))
                .stream()
                .anyMatch(user->user.getId() == aLong);
    }

    //TODO add correct implementation
    @Override
    public Optional<User> get(long id, boolean loadPosts) {
        return Optional.empty();
    }
}