package om.dxline.dxtalent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TalentAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalentAdminApplication.class, args);
    }
}
