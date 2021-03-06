package jdd.so.bot.actions.cmd;

import org.apache.log4j.Logger;

import java.sql.SQLException;

import fr.tunaki.stackoverflow.chat.Message;
import fr.tunaki.stackoverflow.chat.event.PingMessageEvent;
import jdd.so.bot.ChatRoom;
import jdd.so.bot.actions.BotCommand;

public class CommentOffensiveTpCommand extends CommentResponseAbstract {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(CommentOffensiveTpCommand.class);

	@Override
	public String getMatchCommandRegex() {
		return "(?i)(@que[a-zA-Z]* tp(\\s|$))";
	}

	@Override
	public int getRequiredAccessLevel() {
		return BotCommand.ACCESS_LEVEL_REVIEWER;
	}

	@Override
	public String getCommandName() {
		return "Confirm rude comment";
	}

	@Override
	public String getCommandDescription() {
		return "Report that comment is offensive";
	}

	@Override
	public String getCommandUsage() {
		return "tp";
	}

	@Override
	public void runCommand(ChatRoom room, PingMessageEvent event) {
		long parentMessage = event.getParentMessageId();
		Message pdm = room.getRoom().getMessage(parentMessage);
		if (pdm == null) {
			room.replyTo(event.getMessageId(), "Could not find message your are replying to");
			return;
		}
		String c = pdm.getPlainContent();
		if (!c.contains("#comment")) {
			room.replyTo(event.getMessageId(), "Your reply was not direct to an offensive comment");
			return;
		}

		confirm(room, event, c);
	}

	public void confirm(ChatRoom room, PingMessageEvent event, String content) {
		
		long commentId;
		try {
			commentId = getCommentId(content);
		} catch (RuntimeException e) {
			logger.error("runCommand(ChatRoom, PingMessageEvent)", e);
			room.replyTo(event.getMessageId(), "Sorry could not retrive comment id");
			return;
		}
		
		try {
			saveToDatabase(commentId, true);
		} catch (SQLException e) {
			logger.error("confirm(ChatRoom, PingMessageEvent, String)", e);
		}
		

		String edit = getEdit(event, content, true);

		room.edit(event.getParentMessageId(), content + edit).handleAsync((mId, thr) -> {
//			if (thr != null)
//				return room.replyTo(event.getMessageId(), "Thank you for confirming the duplicate").join();
			return mId;
		});

	}

	

}
