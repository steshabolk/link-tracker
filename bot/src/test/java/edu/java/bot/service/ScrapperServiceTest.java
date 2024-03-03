package edu.java.bot.service;

import edu.java.bot.client.ScrapperClient;
import edu.java.bot.dto.request.AddLinkRequest;
import edu.java.bot.dto.request.RemoveLinkRequest;
import edu.java.bot.dto.response.LinkResponse;
import edu.java.bot.dto.response.ListLinksResponse;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScrapperServiceTest {

    @InjectMocks
    private ScrapperService scrapperService;
    @Mock
    private ScrapperClient scrapperClient;

    @Nested
    class RegisterChatTest {

        @Test
        void successfulRegisterChat() {
            scrapperService.registerChat(1L);

            verify(scrapperClient).registerChat(1L);
        }
    }

    @Nested
    class DeleteChatTest {

        @Test
        void successfulDeleteChat() {
            scrapperService.deleteChat(1L);

            verify(scrapperClient).deleteChat(1L);
        }
    }

    @Nested
    class AddLinkTest {

        @Test
        void successfulAddLinkChat() {
            scrapperService.addLink(1L, URI.create("https://github.com/JetBrains/kotlin"));

            verify(scrapperClient).addLink(anyLong(), any(AddLinkRequest.class));
        }
    }

    @Nested
    class RemoveLinkTest {

        @Test
        void successfulRemoveLinkChat() {
            scrapperService.removeLink(1L, URI.create("https://github.com/JetBrains/kotlin"));

            verify(scrapperClient).removeLink(anyLong(), any(RemoveLinkRequest.class));
        }
    }

    @Nested
    class GetLinkTest {

        @Test
        void successfulGetLinksChat() {
            doReturn(new ListLinksResponse(
                List.of(new LinkResponse(2L, URI.create("https://github.com/JetBrains/kotlin"))),
                1
            )).when(scrapperClient).getLinks(1L);

            List<URI> links = scrapperService.getLinks(1L);

            verify(scrapperClient).getLinks(1L);
            assertThat(links.size()).isEqualTo(1);
            assertThat(links.get(0).toString()).isEqualTo("https://github.com/JetBrains/kotlin");
        }
    }
}
