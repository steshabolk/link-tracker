package edu.java.bot.controller;

import edu.java.bot.dto.request.LinkUpdate;
import edu.java.bot.exception.ApiErrorResponse;
import edu.java.bot.service.BotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "update")
@RequiredArgsConstructor
@RestController
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "false")
public class BotController {

    private final BotService botService;

    @Operation(summary = "send an update")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "update is processed",
                     content = @Content),
        @ApiResponse(responseCode = "400", description = "invalid request parameters",
                     content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/updates")
    public void sendUpdate(@RequestBody @Valid LinkUpdate linkUpdate) {
        botService.sendLinkUpdate(linkUpdate);
    }
}
