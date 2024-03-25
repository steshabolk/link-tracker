package edu.java.service.jdbc;

import edu.java.entity.Chat;
import edu.java.exception.ApiException;
import edu.java.repository.jdbc.JdbcChatRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JdbcChatServiceTest {

    @InjectMocks
    private JdbcChatService chatService;
    @Mock
    private JdbcChatRepository chatRepository;

    private static final Chat CHAT = Chat.builder().id(1L).chatId(123L).build();

    @Nested
    class FindByChatIdTest {

        @Test
        void shouldReturnChatWhenChatExists() {
            doReturn(Optional.of(CHAT)).when(chatRepository).findByChatId(anyLong());

            Chat chat = chatService.findByChatId(123L);

            assertThat(chat).isNotNull();
            assertThat(chat.getId()).isEqualTo(1);
        }

        @Test
        void shouldThrowExceptionWhenChatNotFound() {
            doReturn(Optional.empty()).when(chatRepository).findByChatId(anyLong());

            ApiException ex = catchThrowableOfType(
                () -> chatService.findByChatId(123L),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("CHAT_NOT_FOUND");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.chatNotFound");
            assertThat(ex.getMessageArgs().length).isEqualTo(1);
            assertThat(ex.getMessageArgs()[0]).isEqualTo("123");
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    class RegisterChatTest {

        @Test
        void shouldSaveChatWhenChatDoesNotExists() {
            doReturn(false).when(chatRepository).existsByChatId(anyLong());

            chatService.registerChat(123L);

            verify(chatRepository).save(any(Chat.class));
        }

        @Test
        void shouldThrowExceptionWhenChatExists() {
            doReturn(true).when(chatRepository).existsByChatId(anyLong());

            ApiException ex = catchThrowableOfType(
                () -> chatService.registerChat(123L),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("CHAT_ALREADY_EXISTS");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.chatAlreadyExists");
            assertThat(ex.getMessageArgs().length).isEqualTo(1);
            assertThat(ex.getMessageArgs()[0]).isEqualTo("123");
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Nested
    class DeleteChatTest {

        @Test
        void shouldDeleteChatWhenChatExists() {
            doReturn(true).when(chatRepository).existsByChatId(anyLong());

            chatService.deleteChat(123L);

            verify(chatRepository).delete(anyLong());
        }

        @Test
        void shouldThrowExceptionWhenChatDoesNotExists() {
            doReturn(false).when(chatRepository).existsByChatId(anyLong());

            ApiException ex = catchThrowableOfType(
                () -> chatService.deleteChat(123L),
                ApiException.class
            );
            assertThat(ex.getCode()).isEqualTo("CHAT_NOT_FOUND");
            assertThat(ex.getMessageProp()).isEqualTo("ex.api.chatNotFound");
            assertThat(ex.getMessageArgs().length).isEqualTo(1);
            assertThat(ex.getMessageArgs()[0]).isEqualTo("123");
            assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
