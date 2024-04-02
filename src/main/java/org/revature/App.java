package org.revature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class App {
    /**
     * Logger setup
     */
    public static Logger log = LogManager.getLogger();
    public static void main(String[] args) {
        ApplicationContext appContext = SpringApplication.run(App.class);
    }
}
