@ApplicationModule(
        allowedDependencies = {
                "watchpost",
                "watchpost :: api",
                "watchpost :: events",
                "analytics",
                "analytics :: api"
        }
)
package dev.bebomny.beaverdam.web;

import org.springframework.modulith.ApplicationModule;
