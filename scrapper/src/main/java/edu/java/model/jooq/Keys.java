/*
 * This file is generated by jOOQ.
 */
package edu.java.model.jooq;


import edu.java.model.jooq.tables.Chats;
import edu.java.model.jooq.tables.ChatsLinks;
import edu.java.model.jooq.tables.Links;
import edu.java.model.jooq.tables.records.ChatsLinksRecord;
import edu.java.model.jooq.tables.records.ChatsRecord;
import edu.java.model.jooq.tables.records.LinksRecord;

import javax.annotation.processing.Generated;

import org.jooq.ForeignKey;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in the
 * default schema.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.18.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ChatsRecord> CONSTRAINT_3 = Internal.createUniqueKey(Chats.CHATS, DSL.name("CONSTRAINT_3"), new TableField[] { Chats.CHATS.ID }, true);
    public static final UniqueKey<ChatsRecord> CONSTRAINT_3D = Internal.createUniqueKey(Chats.CHATS, DSL.name("CONSTRAINT_3D"), new TableField[] { Chats.CHATS.CHAT_ID }, true);
    public static final UniqueKey<ChatsLinksRecord> CONSTRAINT_34D8 = Internal.createUniqueKey(ChatsLinks.CHATS_LINKS, DSL.name("CONSTRAINT_34D8"), new TableField[] { ChatsLinks.CHATS_LINKS.CHAT_ID, ChatsLinks.CHATS_LINKS.LINK_ID }, true);
    public static final UniqueKey<LinksRecord> CONSTRAINT_4 = Internal.createUniqueKey(Links.LINKS, DSL.name("CONSTRAINT_4"), new TableField[] { Links.LINKS.ID }, true);
    public static final UniqueKey<LinksRecord> CONSTRAINT_45 = Internal.createUniqueKey(Links.LINKS, DSL.name("CONSTRAINT_45"), new TableField[] { Links.LINKS.URL }, true);

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------

    public static final ForeignKey<ChatsLinksRecord, ChatsRecord> CONSTRAINT_34 = Internal.createForeignKey(ChatsLinks.CHATS_LINKS, DSL.name("CONSTRAINT_34"), new TableField[] { ChatsLinks.CHATS_LINKS.CHAT_ID }, Keys.CONSTRAINT_3, new TableField[] { Chats.CHATS.ID }, true);
    public static final ForeignKey<ChatsLinksRecord, LinksRecord> CONSTRAINT_34D = Internal.createForeignKey(ChatsLinks.CHATS_LINKS, DSL.name("CONSTRAINT_34D"), new TableField[] { ChatsLinks.CHATS_LINKS.LINK_ID }, Keys.CONSTRAINT_4, new TableField[] { Links.LINKS.ID }, true);
}
