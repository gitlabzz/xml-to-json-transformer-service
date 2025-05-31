package com.example.transformer;

import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.info.GitProperties;
import org.springframework.lang.Nullable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final BuildProperties buildProperties;
    @Nullable
    private final GitProperties gitProperties;

    public HomeController(BuildProperties buildProperties, @Nullable GitProperties gitProperties) {
        this.buildProperties = buildProperties;
        this.gitProperties = gitProperties;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index(Model model) {
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
