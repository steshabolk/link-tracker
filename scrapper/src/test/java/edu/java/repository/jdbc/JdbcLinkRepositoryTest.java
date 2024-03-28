package edu.java.repository.jdbc;

import edu.java.configuration.DatabaseAccessConfig;
import edu.java.entity.Chat;
import edu.java.entity.Link;
import edu.java.enums.LinkStatus;
import edu.java.enums.LinkType;
import edu.java.integration.IntegrationTest;
import edu.java.integration.config.JdbcTestConfig;
import edu.java.repository.jdbc.mapper.LinkMapper;
import edu.java.repository.jdbc.mapper.LinkWithChatsResultSetExtractor;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {JdbcLinkRepository.class, LinkMapper.class, LinkWithChatsResultSetExtractor.class})
@ContextConfiguration(classes = {JdbcTestConfig.class, DatabaseAccessConfig.JdbcAccessConfig.class})
@TestPropertySource(properties = {"app.database-access-type=jdbc"})
class JdbcLinkRepositoryTest extends IntegrationTest {

    @Autowired
    private JdbcLinkRepository linkRepository;

    private static final OffsetDateTime CHECKED_AT = OffsetDateTime.of(
        LocalDate.of(2024, 1, 1),
        LocalTime.of(0, 0, 0),
        ZoneOffset.UTC
    );
    private static final Link LINK =
        Link.builder()
            .linkType(LinkType.GITHUB)
            .url("https://github.com/JetBrains/kotlin")
            .checkedAt(CHECKED_AT)
            .build();
    private static final Chat CHAT = Chat.builder().id(1L).chatId(123L).build();

    @Nested
    class SaveLinkTest {

        @Test
        @Transactional
        @Rollback
        void shouldReturnSavedLink() {
            Link saved = linkRepository.save(LINK);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(LinkStatus.ACTIVE);
        }
    }

    @Nested
    class FindAllByChatTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql",
            "/sql/chats-links/add-links-list-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnLinks() {
            List<Link> links = linkRepository.findAllByChat(CHAT);

            assertThat(links.size()).isEqualTo(2);

            Link first = links.get(0);
            assertThat(first.getId()).isEqualTo(1);
            assertThat(first.getLinkType()).isEqualTo(LinkType.GITHUB);
            assertThat(first.getUrl()).isEqualTo("https://github.com/JetBrains/kotlin");
            assertThat(first.getCheckedAt()).isEqualTo(CHECKED_AT);
            assertThat(first.getStatus()).isEqualTo(LinkStatus.ACTIVE);

            Link second = links.get(1);
            assertThat(second.getId()).isEqualTo(2);
            assertThat(second.getLinkType()).isEqualTo(LinkType.STACKOVERFLOW);
            assertThat(second.getUrl()).isEqualTo("https://stackoverflow.com/questions/24840667");
            assertThat(second.getCheckedAt()).isEqualTo(CHECKED_AT);
            assertThat(second.getStatus()).isEqualTo(LinkStatus.ACTIVE);
        }
    }

    @Nested
    class FindByUrlTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnLink() {
            Optional<Link> link = linkRepository.findByUrl("https://github.com/JetBrains/kotlin");

            assertThat(link).isPresent();
            assertThat(link.get().getId()).isEqualTo(1);
            assertThat(link.get().getLinkType()).isEqualTo(LinkType.GITHUB);
        }

        @Test
        @Transactional
        void shouldReturnNullWhenLinkDoesNotExists() {
            Optional<Link> link = linkRepository.findByUrl("https://github.com/JetBrains/kotlin");

            assertThat(link).isEmpty();
        }
    }

    @Nested
    class UpdateStatusTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenStatusWasUpdated() {
            boolean isUpdated = linkRepository.updateStatus(Link.builder().id(1L).build(), LinkStatus.BROKEN);

            assertTrue(isUpdated);

            Optional<Link> link = linkRepository.findByUrl(LINK.getUrl());

            assertThat(link).isPresent();
            assertThat(link.get().getId()).isEqualTo(1);
            assertThat(link.get().getStatus()).isEqualTo(LinkStatus.BROKEN);
        }
    }

    @Nested
    class UpdateCheckedAtTest {

        @Test
        @Sql(scripts = {"/sql/links/add-link.sql"})
        @Transactional
        @Rollback
        void shouldReturnTrueWhenCheckedAtWasUpdated() {
            boolean isUpdated = linkRepository.updateCheckedAt(Link.builder().id(1L).build(), CHECKED_AT.plusHours(1));

            assertTrue(isUpdated);

            Optional<Link> link = linkRepository.findByUrl(LINK.getUrl());

            assertThat(link).isPresent();
            assertThat(link.get().getId()).isEqualTo(1);
            assertThat(link.get().getCheckedAt()).isEqualTo(CHECKED_AT.plusHours(1));
        }
    }

    @Nested
    class FindAllWithStatusAndOlderThanTest {

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql", "/sql/links/add-old-link.sql",
            "/sql/chats-links/add-links-list-to-chat.sql", "/sql/chats-links/add-old-link-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnLinksWhenOldLinksExist() {
            List<Link> links =
                linkRepository.findAllWithStatusAndOlderThan(LinkStatus.ACTIVE, CHECKED_AT, 50);

            assertThat(links.size()).isEqualTo(1);

            Link link = links.get(0);
            assertThat(link.getId()).isEqualTo(3);
            assertThat(link.getLinkType()).isEqualTo(LinkType.GITHUB);
            assertThat(link.getCheckedAt()).isEqualTo(CHECKED_AT.minusHours(6));
            assertThat(link.getStatus()).isEqualTo(LinkStatus.ACTIVE);
            assertThat(link.getChats().size()).isEqualTo(1);
            assertThat(link.getChats())
                .usingElementComparator(Comparator
                    .comparing(Chat::getId)
                    .thenComparing(Chat::getChatId))
                .containsExactlyInAnyOrderElementsOf(Set.of(CHAT));
        }

        @Test
        @Sql(scripts = {"/sql/chats/add-chat.sql", "/sql/links/add-links-list.sql",
            "/sql/chats-links/add-links-list-to-chat.sql"})
        @Transactional
        @Rollback
        void shouldReturnEmptyListWhenOldLinksDoNotExist() {
            List<Link> links =
                linkRepository.findAllWithStatusAndOlderThan(LinkStatus.ACTIVE, CHECKED_AT, 50);

            assertThat(links.size()).isEqualTo(0);
        }
    }
}
