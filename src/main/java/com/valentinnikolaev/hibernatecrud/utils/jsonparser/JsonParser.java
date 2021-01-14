package com.valentinnikolaev.hibernatecrud.utils.jsonparser;

import java.util.List;

public interface JsonParser<T> {

    List<T> parseList(String text);

    String serialise(List<T> entities);
}
