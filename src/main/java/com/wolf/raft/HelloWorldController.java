package com.wolf.raft;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description:
 * <br/> Created on 1/5/2019
 *
 * @author 李超
 * @since 1.0.0
 */
@RestController
public class HelloWorldController {

    @RequestMapping("/hello")
    public String index() {
        return "Hello World1";
    }
}
