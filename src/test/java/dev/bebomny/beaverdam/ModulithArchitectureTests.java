package dev.bebomny.beaverdam;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithArchitectureTests {

    @Test
    void verifyModuleBoundaries() {
        ApplicationModules.of(BeaverDamApplication.class).verify();
    }
}
