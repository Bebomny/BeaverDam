@ApplicationModule(
        allowedDependencies = {
                "watchpost",
                "watchpost :: api",
                "watchpost :: events",
                "analytics",
                "analytics :: api",
                "downloader",
                "downloader :: events",
                "dockerintegration",
                "dockerintegration :: api",
                "dockerintegration :: events",
                "common"
        }
)
package dev.bebomny.beaverdam.discord;

import org.springframework.modulith.ApplicationModule;
