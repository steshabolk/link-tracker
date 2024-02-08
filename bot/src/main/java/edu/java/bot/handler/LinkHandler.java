package edu.java.bot.handler;

import edu.java.bot.dto.LinkDto;

public interface LinkHandler {

    LinkDto parseLink(String link);
}
