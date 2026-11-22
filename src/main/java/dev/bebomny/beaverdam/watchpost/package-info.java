@ApplicationModule(
        allowedDependencies = {
                "downloader :: events",
                "common"
        }
)
package dev.bebomny.beaverdam.watchpost;

import org.springframework.modulith.ApplicationModule;
