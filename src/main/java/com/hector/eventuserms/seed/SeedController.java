package com.hector.eventuserms.seed;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController()
@Tag(name = "Seed", description = "")
@RequestMapping("/seed")
public class SeedController {

    private final SeedService seedService;

    SeedController(SeedService seedService) {
        this.seedService = seedService;
    }

    @Operation(summary = "Execute seed", description = "Execute a seed to populate all database, deleting previous information.")
    @PostMapping("/execute")
    public String executeSeed() {
        return this.seedService.executeSeed();
    }
}
