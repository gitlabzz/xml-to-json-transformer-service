package com.example.transformer;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.lang.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Nullable
    private final BuildProperties buildProperties;
    @Nullable
    private final GitProperties gitProperties;

    public HomeController(@Nullable BuildProperties buildProperties, @Nullable GitProperties gitProperties) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
        logger.info("Home page requested");
        if (buildProperties != null) {
            model.addAttribute("version", buildProperties.getVersion());
            model.addAttribute("buildTime", buildProperties.getTime());
        }
        if (gitProperties != null) {
            model.addAttribute("commitId", gitProperties.getShortCommitId());
        }
        return "index";
    }
}
