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
//    SpringApplication.run(Main.class, args);
    Thread t = new Thread(()->{
      while(true) {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
    t.setDaemon(true);
    t.start();

    System.out.println("STARTED!");
//    System.exit(0);
  }
}