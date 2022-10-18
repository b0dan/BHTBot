package com.github.b0dan.BHTBot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.event.user.UserChangeNicknameEvent;

@SuppressWarnings("unused")
public interface BotInterface {
	public void displayCommands(DiscordApi dApi, MessageCreateEvent mEvent);
	public void manuallyUpdateMembers(DiscordApi dApi, MessageCreateEvent mEvent);
	public void getAllMembers(DiscordApi dApi, MessageCreateEvent mEvent);
	public void setOnLeaveRole(DiscordApi dApi, MessageCreateEvent mEvent);
	public void setOnLeavePing(MessageCreateEvent mEvent);

	public void updateMembersOnJoinAndWelcome(DiscordApi dApi, ServerMemberJoinEvent jEvent);
	public void updateMembersOnNicknameChanged(DiscordApi dApi, UserChangeNicknameEvent nEvent);
	public void updateMembersOnLeave(DiscordApi dApi, ServerMemberLeaveEvent lEvent);

	public void contractsHelp(DiscordApi dApi, MessageCreateEvent mEvent);
	public void showContracts(DiscordApi dApi, MessageCreateEvent mEvent, int currentPage, int currentContractNumber, int firstContractOnPage, int lastContractOnPage, boolean pageBack);
	public void manuallyAddContractToDatabase(MessageCreateEvent mEvent);
	public void updateContractInDatabase(MessageCreateEvent mEvent);
	public void removeContractFromDatabase(MessageCreateEvent mEvent);

	public void showChannels(DiscordApi dApi, MessageCreateEvent mEvent);
	public void addChannel(DiscordApi dApi, MessageCreateEvent mEvent);
	public void removeChannel(DiscordApi dApi, MessageCreateEvent mEvent);

	public void showRoles(DiscordApi dApi, MessageCreateEvent mEvent);
	public void addRole(DiscordApi dApi, MessageCreateEvent mEvent);
	public void removeRole(DiscordApi dApi, MessageCreateEvent mEvent);

	public void rockPaperScissorsHelp(DiscordApi dApi, MessageCreateEvent mEvent);
	public void rockPaperScissorsGame(DiscordApi dApi, MessageCreateEvent mEvent);
	public void rockPaperScissorsHighscores(DiscordApi dApi, MessageCreateEvent mEvent);

	public void idHelp(DiscordApi dApi, MessageCreateEvent mEvent);
	public void sourceCode(MessageCreateEvent mEvent);
}
