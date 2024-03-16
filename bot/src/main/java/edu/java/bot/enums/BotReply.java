package edu.java.bot.enums;

import edu.java.bot.util.LinkTypeUtil;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BotReply {

    START_COMMAND(String.format(
        "*hi!* %s\nthis is a link tracking bot %s\n%s",
        Emoji.WAVE.getMarkdown(),
        Emoji.ROBOT.getMarkdown(),
        CommandType.HELP.getCommandBulletPoint()
    )),

    HELP_COMMAND(String.format("%s select one of the available commands:\n%s", Emoji.INFO.getMarkdown(),
        Stream.of(CommandType.TRACK, CommandType.UNTRACK, CommandType.LIST)
            .map(CommandType::getCommandBulletPoint)
            .collect(Collectors.joining("\n"))
    )),

    TRACK_COMMAND(String.format("%s send a link to start tracking\n%s", Emoji.LINK.getMarkdown(),
        LinkTypeUtil.getAvailableSourcesDescription()
    )),

    UNTRACK_COMMAND(String.format("%s send a link to stop tracking", Emoji.LINK.getMarkdown())),

    UNKNOWN_COMMAND(String.format(
        "%s sorry, unable to process an unknown command\n%s",
        Emoji.ERROR.getMarkdown(),
        CommandType.HELP.getCommandBulletPoint()
    )),

    EMPTY_LIST(String.format(
        "%s your list of tracked links is empty\n%s",
        Emoji.BOOKMARK.getMarkdown(),
        CommandType.TRACK.getCommandBulletPoint()
    )),

    CHAT_NOT_FOUND(CommandType.START.getCommandBulletPoint()),

    LINK_ADDED(String.format("%s link was added", Emoji.CHECK.getMarkdown())),

    LINK_REMOVED(String.format("%s link was removed", Emoji.CHECK.getMarkdown())),

    LINK_NOT_FOUND(String.format(
        "%s unknown link\n%s",
        Emoji.ERROR.getMarkdown(),
        CommandType.LIST.getCommandBulletPoint()
    )),

    LINK_ALREADY_EXISTS(String.format(
        "%s link has already been added\n%s",
        Emoji.CHECK.getMarkdown(),
        CommandType.LIST.getCommandBulletPoint()
    )),

    INVALID_LINK(String.format("%s your link is invalid. please try again", Emoji.ERROR.getMarkdown())),

    NOT_SUPPORTED_SOURCE(String.format(
        "%s sorry, tracking is not supported on this resource",
        Emoji.ERROR.getMarkdown()
    ));

    private final String reply;
}
