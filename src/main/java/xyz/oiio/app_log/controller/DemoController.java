package xyz.oiio.app_log.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.oiio.app_log.exception.DuplicateUserException;

@RestController
@RequestMapping("/demo")
public class DemoController {

    @GetMapping("/business-error")
    public void triggerBusinessError() {
        throw new DuplicateUserException("test_user");
    }

    @GetMapping("/system-error")
    public void triggerSystemError() {
        throw new NullPointerException("Simulated System NullPointerException");
    }
}
