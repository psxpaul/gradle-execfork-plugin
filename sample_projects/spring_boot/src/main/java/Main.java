import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAutoConfiguration
public class Main {

  @RequestMapping("/")
  public String home() {
    return "PING";
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }
}