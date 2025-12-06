package com.nametag.classloader;

import org.eclipse.jgit.api.Git;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Scheduled(cron = "*/10 * * * * *")
    public String applyUpdate() throws Exception {

        manager.pauseAll();

        try {
            // 1. Pull latest code
            Git.open(new File("/Users/connorblack/dev/NameTagAutoVersion/")).pull().call();

            manager.loadModule();
        } finally {
            manager.resumeAll();
        }

        runUpdatedLogic();
        return "Update applied!";
    }

    private void runUpdatedLogic() throws Exception {
        MyLogic myLogic = new MyLogic();
        System.out.println(myLogic.execute());
    }

}
