package bot;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class Controller {
    @GetMapping("/check")
    public String check() {
        return "It's alive!";
    }



}
