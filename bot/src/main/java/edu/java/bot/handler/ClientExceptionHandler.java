package edu.java.bot.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.dto.response.ScrapperErrorResponse;
import edu.java.bot.enums.BotReply;
import edu.java.bot.util.BotSendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Component
public class ClientExceptionHandler {

    private final ObjectMapper mapper;

    public byte[] handleClientResponse(RuntimeException ex) {
        log.info("client error: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc
            && ArrayUtils.isNotEmpty(clientExc.getResponseBodyAsByteArray())) {
            log.info("response: {}", clientExc.getResponseBodyAsString());
            return clientExc.getResponseBodyAsByteArray();
        }
        return new byte[0];
    }

    public SendMessage getReplyForScrapperErrorResponse(RuntimeException ex, Long chatId) {
        byte[] responseBody = handleClientResponse(ex);
        ScrapperErrorResponse response = parseScrapperResponseBody(responseBody);
        if (response == null) {
            return null;
        }
        return getReplyByExceptionCode(chatId, response);
    }

    private ScrapperErrorResponse parseScrapperResponseBody(byte[] responseBody) {
        try {
            return mapper.readValue(responseBody, ScrapperErrorResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private SendMessage getReplyByExceptionCode(Long chatId, ScrapperErrorResponse errorResponse) {
        try {
            BotReply botReply = BotReply.valueOf(errorResponse.code());
            return BotSendMessage.getSendMessage(chatId, botReply.getReply());
        } catch (RuntimeException ex) {
            log.info("cant parse scrapper exception code: {}", errorResponse.code());
            return null;
        }
    }
}
