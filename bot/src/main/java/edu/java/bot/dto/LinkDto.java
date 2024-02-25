package edu.java.bot.dto;

import edu.java.bot.enums.LinkType;
import java.net.URI;

public record LinkDto(LinkType linkType, URI uri) {
}
