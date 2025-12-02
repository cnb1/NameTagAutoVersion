package com.nametag.classloader;

import org.eclipse.jgit.api.Git;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/update")
public class UpdateController {

    private final DynamicClassLoaderManager manager;

    public UpdateController(DynamicClassLoaderManager manager) {
        this.manager = manager;
    }

    @PostMapping("/apply")
    public String applyUpdate() throws Exception {
        manager.pauseAll();

        try {
            // 1. Pull latest code
            Git.open(new File("./dynamic/repo")).pull().call();

            // 2. Build the new module.jar
            Runtime.getRuntime().exec("./dynamic/repo/mvnw -q package").waitFor();

            // 3. Reload classes
            manager.loadModule();
        } finally {
            manager.resumeAll();
        }

        return "Update applied!";
    }

    @GetMapping("/run")
    public Object runUpdatedLogic() throws Exception {
        return manager.invoke("com.example.MyLogic", "execute");
    }

}
