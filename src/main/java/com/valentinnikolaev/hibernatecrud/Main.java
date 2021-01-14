package com.valentinnikolaev.hibernatecrud;

import com.valentinnikolaev.hibernatecrud.utils.beansconfigurations.ViewBeansConfig;
import com.valentinnikolaev.hibernatecrud.view.MainView;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ViewBeansConfig.class);
        MainView mainView = context.getBean(MainView.class);
        mainView.initiateMainView();
    }
}
