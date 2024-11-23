package org.system.citronix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CitronixApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CitronixApplication.class);
        app.setBanner((environment, sourceClass, out) -> {
            out.println(" ____  __.    ___.            _________            .___");
            out.println("|    |/ _|____\\_ |__   ____   \\_   ___ \\  ____   __| _/____");
            out.println("|      < /  _ \\| __ \\_/ __ \\  /    \\  \\/ /  _ \\ / __ |/ __ \\");
            out.println("|    |  (  <_> ) \\_\\ \\  ___/  \\     \\___(  <_> ) /_/ \\  ___/");
            out.println("|____|__ \\____/|___  /\\___  >  \\______  /\\____/\\____ |\\___  >");
            out.println("        \\/         \\/     \\/          \\/            \\/    \\/");
            out.println("                                                    ");

        });
        app.run(args);
    }

}
