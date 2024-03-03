package edu.java.service;

import edu.java.client.BotClient;
import edu.java.dto.response.LinkUpdateResponse;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    @InjectMocks
    private BotService botService;
    @Mock
    private BotClient botClient;
    @Captor
    private ArgumentCaptor<LinkUpdateResponse> responseCaptor;
    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );

    @Nested
    class SendLinkUpdateTest {

        @Test
        void successfulPostUpdate() {
            Link link = Link.builder()
                .id(1L)
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .checkedAt(CHECKED_AT)
                .chats(Set.of(Chat.builder().chatId(2L).build()))
                .build();

            botService.sendLinkUpdate(link, "new update");

            verify(botClient).postUpdate(responseCaptor.capture());
            LinkUpdateResponse response = responseCaptor.getValue();
            assertThat(response.url().toString()).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(response.description()).isEqualTo("new update");
            assertThat(response.tgChatIds().size()).isEqualTo(1);
            assertThat(response.tgChatIds().get(0)).isEqualTo(2L);
        }
    }
}
