package edu.java.service.jpa;

import edu.java.configuration.DatabaseAccessConfig;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import edu.java.exception.ApiException;
import edu.java.integration.IntegrationTest;
import edu.java.repository.jpa.JpaChatRepository;
import edu.java.repository.jpa.JpaLinkRepository;
import edu.java.util.LinkParser;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@Import({JpaLinkService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {"app.database-access-type=jpa"})
@DataJpaTest(includeFilters = @ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE, classes = DatabaseAccessConfig.JpaAccessConfig.class))
class JpaLinkServiceTest extends IntegrationTest {

    @Autowired
    private JpaLinkService linkService;
    @Autowired
    private JpaLinkRepository linkRepository;
    @Autowired
    private JpaChatRepository chatRepository;
    @MockBean
    private JpaChatService chatService;

    static MockedStatic<LinkParser> linkParserMock;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Chat CHAT = Chat.builder().id(1L).chatId(123L).build();

    @BeforeAll
    public static void init() {
        linkParserMock = mockStatic(LinkParser.class);
        linkParserMock.when(() -> LinkParser.parseLink(any(URI.class)))
            .thenReturn(Link.builder()
                .linkType(LinkType.GITHUB)
                .url("https://github.com/JetBrains/kotlin")
                .build());
    }

    @AfterAll
    public static void close() {
        linkParserMock.close();
    }

    @Nested
    class GetLinksToUpdateTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql",
            "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnLinksWhenOldLinksExist() {
            List<Link> links = linkService.getLinksToUpdate(60, 50);

            assertThat(links.size()).isEqualTo(1);

            Link first = links.get(0);
            assertThat(first.getId()).isEqualTo(1);
            assertThat(first.getLinkType()).isEqualTo(LinkType.GITHUB);
            assertThat(first.getCheckedAt()).isEqualTo(CHECKED_AT);
            assertThat(first.getChats())
                .usingElementComparator(Comparator
                    .comparing(Chat::getId)
                    .thenComparing(Chat::getChatId))
                .containsExactlyInAnyOrderElementsOf(Set.of(CHAT));
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql",
            "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnEmptyListWhenOldLinksDoNotExist() {
            Link link = linkRepository.findById(1L).get();
            link.setCheckedAt(OffsetDateTime.now());

            List<Link> links = linkService.getLinksToUpdate(60, 50);

            assertThat(links.size()).isEqualTo(0);
        }

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnEmptyListWhenLinkHasNoChats() {
            List<Link> links = linkService.getLinksToUpdate(60, 50);

            assertThat(links.size()).isEqualTo(0);
        }
    }

    @Nested
    class UpdateStatusTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void updateStatusTest() {
            linkService.updateLinkStatus(linkRepository.findById(1L).get(), LinkStatus.BROKEN);

            assertThat(linkRepository.findAll().get(0).getStatus()).isEqualTo(LinkStatus.BROKEN);
        }
    }

    @Nested
    class UpdateCheckedAtTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void updateCheckedAtTest() {
            linkService.updateCheckedAt(linkRepository.findById(1L).get(), CHECKED_AT.plusHours(1));

            assertThat(linkRepository.findAll().get(0).getCheckedAt()).isEqualTo(CHECKED_AT.plusHours(1));
        }
    }

    @Nested
    class AddLinkToChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void addAnExistingLinkInDbTest() {
            doReturn(chatRepository.findById(1L).get()).when(chatService).findByChatId(anyLong());

            LinkResponse response =
                linkService.addLinkToChat(123L, URI.create("https://github.com/JetBrains/kotlin"));

            List<Chat> chats = chatRepository.findAll();
            assertThat(chats.size()).isEqualTo(1);
            assertThat(chats.get(0).getLinks().size()).isEqualTo(1);

            List<Link> links = linkRepository.findAll();
            assertThat(links.size()).isEqualTo(1);
            assertThat(links.get(0).getChats().size()).isEqualTo(1);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.url()).isEqualTo(URI.create("https://github.com/JetBrains/kotlin"));
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void addNewLinkTest() {
            doReturn(chatRepository.findById(1L).get()).when(chatService).findByChatId(anyLong());

            LinkResponse response =
                linkService.addLinkToChat(123L, URI.create("https://github.com/JetBrains/kotlin"));

            List<Chat> chats = chatRepository.findAll();
            assertThat(chats.size()).isEqualTo(1);
            assertThat(chats.get(0).getLinks().size()).isEqualTo(1);

            List<Link> links = linkRepository.findAll();
            assertThat(links.size()).isEqualTo(1);
            assertThat(links.get(0).getChats().size()).isEqualTo(1);

            assertThat(response.id()).isEqualTo(links.get(0).getId());
            assertThat(response.url()).isEqualTo(URI.create("https://github.com/JetBrains/kotlin"));
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql",
            "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldThrowExceptionWhenLinkExists() {
            doReturn(chatRepository.findById(1L).get()).when(chatService).findByChatId(anyLong());

            ApiException ex = catchThrowableOfType(
                () -> linkService.addLinkToChat(123L, URI.create("https://github.com/JetBrains/kotlin")),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("LINK_ALREADY_EXISTS");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.linkAlreadyExists");
            assertThat(ex.getMessageArgs().length).isEqualTo(1);
            assertThat(ex.getMessageArgs()[0]).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    class RemoveLinkFromChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql",
            "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void removeLinkTest() {
            doReturn(chatRepository.findById(1L).get()).when(chatService).findByChatId(anyLong());

            LinkResponse response =
                linkService.removeLinkFromChat(123L, URI.create("https://github.com/JetBrains/kotlin"));

            List<Chat> chats = chatRepository.findAll();
            assertThat(chats.size()).isEqualTo(1);
            assertThat(chats.get(0).getLinks().size()).isEqualTo(0);

            List<Link> links = linkRepository.findAll();
            assertThat(links.size()).isEqualTo(1);
            assertThat(links.get(0).getChats().size()).isEqualTo(0);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.url()).isEqualTo(URI.create("https://github.com/JetBrains/kotlin"));
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql"})
        @Transactional
        @Rollback
        void shouldThrowExceptionWhenLinkDoesNotExists() {
            doReturn(chatRepository.findById(1L).get()).when(chatService).findByChatId(anyLong());

            ApiException ex = catchThrowableOfType(
                () -> linkService.removeLinkFromChat(123L, URI.create("https://github.com/JetBrains/kotlin")),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("LINK_NOT_FOUND");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.linkNotFound");
            assertThat(ex.getMessageArgs().length).isEqualTo(1);
            assertThat(ex.getMessageArgs()[0]).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldThrowExceptionWhenLinkNotAdded() {
            doReturn(chatRepository.findById(1L).get()).when(chatService).findByChatId(anyLong());

            ApiException ex = catchThrowableOfType(
                () -> linkService.removeLinkFromChat(123L, URI.create("https://github.com/JetBrains/kotlin")),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("LINK_NOT_FOUND");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.linkNotFound");
            assertThat(ex.getMessageArgs().length).isEqualTo(1);
            assertThat(ex.getMessageArgs()[0]).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class GetChatLinksTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-link.sql",
            "/sql/chats-links/add-link-to-chat.sql"})
        @Transactional
        @Rollback
        void getChatLinksTest() {
            doReturn(chatRepository.findById(1L).get()).when(chatService).findByChatId(anyLong());

            ListLinksResponse response = linkService.getChatLinks(123L);

            assertThat(response.links().size()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(1);

            LinkResponse link = response.links().get(0);
            assertThat(link.id()).isEqualTo(1L);
            assertThat(link.url()).isEqualTo(URI.create("https://github.com/JetBrains/kotlin"));
        }
    }
}
