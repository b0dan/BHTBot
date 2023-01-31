package com.github.b0dan.BHTBot;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.lang.ArrayIndexOutOfBoundsException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.channel.server.ServerChannelChangeNameEvent;
import org.javacord.api.event.channel.server.ServerChannelDeleteEvent;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.event.server.role.RoleChangeNameEvent;
import org.javacord.api.event.server.role.RoleDeleteEvent;
import org.javacord.api.event.server.role.UserRoleAddEvent;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.event.user.UserChangeNicknameEvent;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

public class Commands {
	//All the available commands and a get method for them.
	private final List<String> availableCommands = new ArrayList<>() {{
		add("updateMembers");
		add("commandsHelp");
		add("contractsHelp");
		add("rpsHelp");
		add("idHelp");
		add("setOnLeaveRole");
		add("setOnLeavePing");
		add("showContracts");
		add("addContract");
		add("updateContract");
		add("removeContract");
		add("showPriorityContracts");
		add("addPriorityContract");
		add("removePriorityContract");
		add("showChannels");
		add("addChannel");
		add("removeChannel");
		add("showRoles");
		add("addRole");
		add("removeRole");
		add("rpsPlay");
		add("rpsHighscores");
	}};
	
	public List<String> getAvailableCommands() {
		return availableCommands;
	}

	private static final Logger logger = LogManager.getLogger(Commands.class); //Creates an instance of the 'Logger' class for 'Commands.class'.

	private long onLeaveRoleID = 336588534351790080L; //The role value for the 'onLeaveRole' from the `~setOnLeaveRole` command (default: Luminous Path -> 336588534351790080L).
	private int pingable = 1; //The ping value for the 'onLeaveMessage' from the `~setOnLeavePing` command: 0 = disabled, 1 = enabled (default).

	private Role guestRole; //The 'Guest' role.

	@SuppressWarnings("FieldMayBeFinal")
	private List<Integer> firstPages = new ArrayList<>(); //A list with all the "first pages" from the `~showContracts` command.
	@SuppressWarnings("FieldMayBeFinal")
	private Multimap<String, String> allMembers = ArrayListMultimap.create(); //A Multimap containing all the members' display names and whether or not they are Guests with their discord tag as a key.

	//A command that displays all available commands by typing `~commandsHelp` (not case-sensitive).
	public void displayCommands(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with all the commands.
			EmbedBuilder commands = new EmbedBuilder()
					.setTitle("General Commands")
					.setThumbnail(dApi.getYourself().getAvatar())
					.setColor(Color.RED)
					.addField("~commandsHelp", "Displays the general commands.")
					.addField("~contractsHelp", "Displays all contract related commands.")
					.addField("~rpsHelp", "Displays all the info needed to use the `~rpsPlay` (Rock-Paper-Scissors) command.")
					.addField("‎", "‎")
					.addField("~updateMembers", "Updates the list of current members in the server.")
					.addField("~setOnLeaveRole *[Role ID]*", "Sets which role to be pinged when someone leaves the server.")
					.addInlineField("~setOnLeavePing *0*", "Disables the ping when someone leaves the server.")
					.addInlineField("~setOnLeavePing *1*", "Enables the ping when someone leaves the server.")
					.setFooter("The commands are not case-sensitive!");
			mEvent.getChannel().sendMessage(commands);

			logger.info("Command (~commandsHelp) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command to manually update the members' Multimap by typing `~updateMembers` (not case-sensitive).
	public void manuallyUpdateMembers(Server server, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//Clear the members' Multimap if the server is not empty.
			if(!allMembers.isEmpty()) {
				allMembers.clear();
			}

			//Finds the 'Guest' role.
			for(User user: server.getMembers()) {
				for(Role role: user.getRoles(server)) {
					if(role.getId() == 437508442635239424L) { //Guest(437508442635239424L).
						guestRole = role;
						break;
					}
				}
			}

			//Fills out the members' Multimap.
			for(User user: server.getMembers()) {
				allMembers.put(user.getDiscriminatedName(), user.getDisplayName(server));
				if(user.getRoles(server).contains(guestRole)) {
					allMembers.put(user.getDiscriminatedName(), "GUEST");
				}
			}

			logger.info("Members updated by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error. //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command to print out the member's Multimap by typing `~getAllMembers` (not case-sensitive).
	public void getAllMembers(MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//If the server is empty, prints a corresponding message. If not, it prints out all the members in the server.
			if (allMembers.isEmpty()) {
				logger.warn("The server has no members."); //Sends a warning log about the server being empty.
			} else {
				System.out.println();
				for(Map.Entry<String, String> entry: allMembers.entries()) {
					System.out.println(entry);
				}
				System.out.println("Members: " + allMembers.size() + "\n");

				logger.info("Command (~getAllMembers) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
			}
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command to set what the `onLeave` role will be by typing `~setOnLeaveRole` (not case-sensitive).
	public void setOnLeaveRole(Server server, MessageCreateEvent mEvent, String onLeaveRole) {
		try {
			//Gets the number after the `~setOnLeaveRole` command and changes the role value if that role exists, also reacts on the message.
			if(server.getRoleById(Long.parseLong(onLeaveRole)).isPresent()) {
				onLeaveRoleID = Long.parseLong(onLeaveRole);
			}
			mEvent.getMessage().addReaction("👍");

			logger.info("Command (~setOnLeaveRole) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + onLeaveRoleID + "."); //Sends an info log about who issued the command.
		} catch(NumberFormatException | StringIndexOutOfBoundsException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The ID must be a digit of type `Long`. Use `~idHelp` for more information on how to get the ID.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(NoSuchElementException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The Role ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command to enable or disable the `onLeave` ping by typing `~setOnLeavePing` (not case-sensitive).
	public void setOnLeavePing(MessageCreateEvent mEvent, String ping) {
		try {
			//Gets the number after the `~setOnLeavePing` command, gives an error if it isn't 0 or 1 or changes the ping value if it is, also reacts on the message.
			if(Integer.parseInt(ping) != 0 && Integer.parseInt(ping) != 1) {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The number should be either 0 or 1.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} else {
				pingable = Integer.parseInt(ping);
				mEvent.getMessage().addReaction("👍");

				logger.info("Command (~setOnLeavePing) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + pingable + "."); //Sends an info log about who issued the command.
			}
		} catch(NumberFormatException | StringIndexOutOfBoundsException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The number should be either 0 or 1.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch (Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A listener that automatically updates the 'BHT/Channels' SQL database when someone changes the name of a channel that's inside said database.
	public void updateDBChannelsOnChangeName(ServerChannelChangeNameEvent ccnEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Channels).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//If the channel that has been renamed is in the 'BHT' SQL database (Channels), updates it in there.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(channelId) FROM Channels WHERE channelId = ?");
			preparedStatement0.setLong(1, ccnEvent.getChannel().getId());
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 1) {
	        		PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Channels SET channelName = ? WHERE channelId = ?");
	        		preparedStatement.setString(1, ccnEvent.getNewName());
				preparedStatement.setLong(2, ccnEvent.getChannel().getId());
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					logger.info("The 'BHT/Channels' database has been successfully updated due to a channel being renamed.");
				}
			}
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the 'BHT/Channels' SQL database when someone deletes a channel that's inside said database.
	public void updateDBChannelsOnDelete(ServerChannelDeleteEvent cdEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Channels).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//If the channel that has been deleted is in the 'BHT' SQL database (Channels), removes it from there.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(channelId) FROM Channels WHERE channelId = ?");
			preparedStatement0.setLong(1, cdEvent.getChannel().getId());
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 1) {
	        		PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Channels WHERE channelId = ?");
				preparedStatement.setLong(1, cdEvent.getChannel().getId());
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					logger.info("The 'BHT/Channels' database has been successfully updated due to a channel being deleted.");
				}
			}
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the 'BHT/Roles' SQL database when someone changes the name of a role that's inside said database.
	public void updateDBRolesOnChangeName(RoleChangeNameEvent rcnEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Roles).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//If the role that has been renamed is in the 'BHT' SQL database (Roles), updates it in there.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(roleId) FROM Roles WHERE roleId = ?");
			preparedStatement0.setLong(1, rcnEvent.getRole().getId());
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 1) {
	        		PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Roles SET roleName = ? WHERE roleId = ?");
	        		preparedStatement.setString(1, rcnEvent.getNewName());
				preparedStatement.setLong(2, rcnEvent.getRole().getId());
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					logger.info("The 'BHT/Roles' database has been successfully updated due to a role being renamed.");
				}
			}
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the 'BHT/Roles' SQL database when someone deletes a role that's inside said database.
	public void updateDBRolesOnDelete(RoleDeleteEvent rdEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Roles).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//If the role that has been deleted is in the 'BHT' SQL database (Roles), removes it from there.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(roleId) FROM Roles WHERE roleId = ?");
			preparedStatement0.setLong(1, rdEvent.getRole().getId());
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 1) {
	        		PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Roles WHERE roleId = ?");
				preparedStatement.setLong(1, rdEvent.getRole().getId());
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					logger.info("The 'BHT/Roles' database has been successfully updated due to a role being deleted.");
				}
			}
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the members' Multimap when someone joins the server and welcomes the new member.
	public void updateMembersOnJoinAndWelcome(ServerMemberJoinEvent jEvent) {
		try {
			Server server = jEvent.getServer(); //Gets the server.

			//Puts the user who joined the server in the newUsers List.
			String newUser = jEvent.getUser().getDiscriminatedName();
			List<String> newUsers = new ArrayList<>();
				newUsers.add(newUser);

			allMembers.put(newUser, jEvent.getUser().getDisplayName(server)); //Puts the user who joined the server in the members' Multimap.

			//Sends a welcoming message to the user that just joined.
			MessageBuilder welcomeMessage = new MessageBuilder();
			welcomeMessage
				.append("Welcome to ")
				.append("The Black Hand Triads", MessageDecoration.BOLD)
				.append(", " + jEvent.getUser().getMentionTag() + "! Please provide us the following info so we can validate you:\n\n")
				.append("In-Game Name:\nCurrent Rank:\nName of the person that tested you:", MessageDecoration.CODE_LONG)
				.append("\nAfter that, just wait until you get validated by one of our high-ranking members.");
			if(server.getSystemChannel().isPresent()) {
				welcomeMessage.send(server.getSystemChannel().get());
			}

			logger.info("Members updated due to " + jEvent.getUser().getDiscriminatedName() + " joining the server."); //Sends an info log about what issued the listener.

			//Gets triggered when someone joins the server and has a 'White Lotus' role given. When done, welcomes them to the server.
			jEvent.getUser().addUserRoleAddListener(event -> {
				if(newUsers.contains(event.getUser().getDiscriminatedName())) {
					MessageBuilder validationMessage = new MessageBuilder();
					if(server.getMemberByDiscriminatedName(event.getUser().getDiscriminatedName()).isPresent()) {
						if(event.getRole().getId() == 262782166847586305L) { //White Lotus (262782166847586305L).
							if(server.getChannelById(1031668061293518879L).isPresent() && server.getChannelById(1031668061293518879L).get().asServerTextChannel().isPresent()) {
								validationMessage
									.append("Welcome, " + server.getMemberByDiscriminatedName(event.getUser().getDiscriminatedName()).get().getMentionTag() + "! You have been validated.\n")
									.append("Be sure to read " + server.getChannelById(1031668061293518879L).get().asServerTextChannel().get().getMentionTag() + " before anything else!"); //#rules_and_triad_behavior (1031668061293518879L).
							}
						} else {
							validationMessage
								.append("Welcome, " + server.getMemberByDiscriminatedName(event.getUser().getDiscriminatedName()).get().getMentionTag() + "! You have been validated.");
						}
						//Add the "GUEST" value in the members' Multimap if the role given is a 'Guest'.
						if(event.getRole().equals(guestRole) && !allMembers.containsEntry(newUser, "GUEST")) {
							allMembers.put(newUser, "GUEST");
						}
					}
					validationMessage.send(server.getSystemChannel().get());

					newUsers.remove(event.getUser().getDiscriminatedName());
					logger.info(event.getUser().getDiscriminatedName() + " has been validated."); //Sends an info log about what issued the listener.
				}
			}).removeAfter(3, TimeUnit.DAYS).addRemoveHandler(() -> newUsers.remove(jEvent.getUser().getDiscriminatedName()));
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the members' Multimap when someone has their nickname changed.
	public void updateMembersOnNicknameChanged(UserChangeNicknameEvent nEvent) {
		try {
			Server server = nEvent.getServer(); //Gets the server.

			//Changes the name in the members' Multimap of the person that just had their nickname changed/updated.
			allMembers.removeAll(nEvent.getUser().getDiscriminatedName());
			allMembers.put(nEvent.getUser().getDiscriminatedName(), nEvent.getUser().getDisplayName(server));
			if(nEvent.getUser().getRoles(server).contains(guestRole)) {
				allMembers.put(nEvent.getUser().getDiscriminatedName(), "GUEST");
			}

			logger.info("Members updated due to " + nEvent.getUser().getDiscriminatedName() + " having their nickname changed - value: " + allMembers.get(nEvent.getUser().getDiscriminatedName()) + "."); //Sends an info log about what issued the listener.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the members' Multimap when someone is given the 'Guest' role.
	public void updateMembersOnGuestRoleAdded(UserRoleAddEvent arEvent) {
		try {
			//Adds a new "GUEST" value to the members' Multimap if the 'Guest' role is added.
			if(arEvent.getRole().equals(guestRole) && !allMembers.containsEntry(arEvent.getUser().getDiscriminatedName(), "GUEST")) {
				allMembers.put(arEvent.getUser().getDiscriminatedName(), "GUEST");
				logger.info("Members updated due to " + Iterables.get(allMembers.get(arEvent.getUser().getDiscriminatedName()), 0) + " having a 'Guest' role added."); //Sends an info log about what issued the listener.
			}
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the members' Multimap when someone has the 'Guest' role removed.
	public void updateMembersOnGuestRoleRemoved(UserRoleRemoveEvent rrEvent) {
		try {
			//Removes the "GUEST" value of the user from the members' Multimap if the 'Guest' role is removed.
			if(rrEvent.getRole().equals(guestRole) && allMembers.containsEntry(rrEvent.getUser().getDiscriminatedName(), "GUEST")) {
				allMembers.remove(rrEvent.getUser().getDiscriminatedName(), "GUEST");
				logger.info("Members updated due to " + Iterables.get(allMembers.get(rrEvent.getUser().getDiscriminatedName()), 0) + " having a 'Guest' role removed."); //Sends an info log about what issued the listener.
			}
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
		logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e.printStackTrace();
		}
	}

	//A listener that automatically updates the members' Multimap when someone leaves the server, sends a message about it and adds the person to the 'BHT/Contracts' SQL database.
	public void updateMembersOnLeave(ServerMemberLeaveEvent lEvent) {
		try {
			Server server = lEvent.getServer(); //Gets the server.
			AllowedMentions allowedMentions = new AllowedMentionsBuilder().addRole(onLeaveRoleID).setMentionRoles(true).build(); //Allows the `onLeave` role to be mentioned.

			//Sends a message and notifies the current members when someone leaves the server.
			MessageBuilder onLeaveMessage = new MessageBuilder();
			onLeaveMessage
				.append(Iterables.get(allMembers.get(lEvent.getUser().getDiscriminatedName()), 0) + " (" + lEvent.getUser().getDiscriminatedName() + ") ", MessageDecoration.BOLD)
				.append("just left ")
				.append("The Black Hand Triads", MessageDecoration.BOLD)
				.append(". Looks like loyalty wasn't one of their virtues. Hunt that sssnake down! ");

			//Checks if the ping value is 1 and notifies the `onLeave` role if yes, then sends the message.
			if(pingable == 1 && server.getRoleById(onLeaveRoleID).isPresent()) {
				onLeaveMessage
					.append(server.getRoleById(onLeaveRoleID).get().getMentionTag())
					.setAllowedMentions(allowedMentions);
			}
			long embedID = 0L;
			if(server.getSystemChannel().isPresent()) {
				embedID = onLeaveMessage.send(server.getSystemChannel().get()).get().getId();
			}

			//Checks if the user who had just left the server has a 'Guest' role and if not, checks if his looks like a real name and adds it to the 'BHT/Contracts' SQL database if yes.
			if(!allMembers.containsEntry(lEvent.getUser().getDiscriminatedName(), "GUEST")) {
				if(Iterables.get(allMembers.get(lEvent.getUser().getDiscriminatedName()), 0).matches("^[A-Z](?=.{2,19}$)[A-Za-z.]+(?:\\h+[A-Z][A-Za-z.]+)+$")) {
					//Opens up a connection to the 'BHT' SQL database (Contracts).
		        		Class.forName("com.mysql.cj.jdbc.Driver");
		        		Connection connection = DriverManager.getConnection("...");

					//Adds the person who had left the server to the 'BHT' SQL database (Contracts) if he's not there already.
					PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(contractName) FROM Contracts WHERE contractName = ?");
					preparedStatement0.setString(1, Iterables.get(allMembers.get(lEvent.getUser().getDiscriminatedName()), 0));
					ResultSet resultSet = preparedStatement0.executeQuery();
					resultSet.next();
		        		if(resultSet.getInt(1) == 0) {
		        			resultSet.close();
		        			preparedStatement0.close();

		        			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Contracts(contractName, isPriorityContract) VALUES(?, 0)");
						preparedStatement.setString(1, Iterables.get(allMembers.get(lEvent.getUser().getDiscriminatedName()), 0));
						int affectedRows = preparedStatement.executeUpdate();
						if(affectedRows > 0) {
							logger.info(Iterables.get(allMembers.get(lEvent.getUser().getDiscriminatedName()), 0) + " has been successfully added to the database."); //Sends an info log about a successful insertion into the database.
							server.getSystemChannel().get().getMessageById(embedID).get().addReaction("🐍"); //Reacts with a snake emoji to the leaving message if the person was successfully added to the 'BHT/Contracts' SQL database.

							//Closes the connections.
							preparedStatement.close();
							connection.close();
						} else {
							logger.warn(allMembers.get(lEvent.getUser().getDiscriminatedName()) + " can not be added to the database."); //Sends a warning log about an unsuccessful insertion into the database.
						}
					} else {
						resultSet.close();
						preparedStatement0.close();
						connection.close();
					}
		        	}
			}
			//Removes the person who had left the server from the members' Multimap.
			allMembers.removeAll(lEvent.getUser().getDiscriminatedName());

			logger.info("Members updated due to " + lEvent.getUser().getDiscriminatedName() + " leaving the server."); //Sends an info log about what issued the listener.
		} catch(NullPointerException | IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command that displays all the contract related commands by typing `~contractsHelp`(not case-sensitive).
	public void contractsHelp(MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with all the contract related commands.
			EmbedBuilder contractsCommands = new EmbedBuilder()
				.setTitle("Contracts Commands")
				.setThumbnail("https://i.imgur.com/HoOkwBs.png")
				.setColor(Color.RED)
				.addField("~showContracts", "Shows a list of all active contracts.")
				.addField("~addContract *[Full Name]*", "Adds a new contract.")
				.addField("~updateContract *[Contract ID]* *[Full Name]*", "Updates the contract with the specified ID.")
				.addField("~removeContract *[Contract ID]*", "Removes the contract with the specified ID.")
				.addField("‎", "‎")
				.addField("~showPriorityContracts", "Shows a list of all active priority contracts.")
				.addField("~addPriorityContract *[Contract ID]*", "Makes the contract with the specified ID to be a priority.")
				.addField("~removePriorityContract *[Contract ID]*", "Makes the contract with the specified ID to no longer be a priority.")
				.addField("‎", "‎")
				.addField("~showChannels", "Shows a list of all the channels where contract related commands can be used.")
				.addInlineField("~addChannel *[Channel ID]*", "Allows the use of contract related commands in the channel with the specified ID.")
				.addInlineField("~removeChannel *[Channel ID]*", "Disallows the use of contract related commands in the channel with the specified ID.")
				.addField("~showRoles", "Shows a list of all the roles who can use contract related commands.")
				.addInlineField("~addRole *[Role ID]*", "Allows the users who have the role with the specified ID to use contract related commands.")
				.addInlineField("~removeRole *[Role ID]*", "Disallows the users who have the role with the specified ID to use contract related commands.")
				.addField("~idHelp", "Information on how to get the Channel/Role ID.")
				.setFooter("The commands are not case-sensitive!");
			mEvent.getChannel().sendMessage(contractsCommands);

			logger.info("Command (~contractsHelp) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command that displays all active contracts by typing `~showContracts` (not case-sensitive).
	public void showContracts(MessageCreateEvent mEvent, int currentPage, int currentContractNumber, int firstContractOnPage, int lastContractOnPage, boolean pageBack) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//Opens up a connection to the 'BHT' SQL database (Contracts).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Creates a 'SELECT' SQL statement.
        		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        		ResultSet resultSet = statement.executeQuery("SELECT contractId, contractName FROM Contracts");

        		//An embed with all active contracts.
			EmbedBuilder contracts = new EmbedBuilder()
				.setThumbnail("https://i.imgur.com/HoOkwBs.png")
				.setColor(Color.RED)
				.setFooter("Make sure to remove the contract you've completed by typing `~removeContract [Contract ID]`!");

			//If there are any contracts, finds the total amount of pages needed to group the contracts. If not, notifies the user.
			resultSet.last();
			if(resultSet.getRow() > 0) {
				final int totalContracts = resultSet.getRow();
				int tempTotalPages = (totalContracts / 25) + 1;
				if(totalContracts % 25 == 0) {
					tempTotalPages = tempTotalPages - 1;
				}
				final int totalPages = tempTotalPages;

				//Adds the contracts to the above-mentioned embed.
				if(totalPages == 1 || currentPage == 1) {
					currentContractNumber = 1;
					resultSet.beforeFirst();
				} else if(pageBack) {
					resultSet.absolute(firstPages.get(currentPage - 1) - 1);
					currentContractNumber = resultSet.getRow() + 1;
				} else if(totalPages > 1 && currentPage > 1) {
					resultSet.absolute(currentContractNumber - 1);
				}
				boolean freshPage = true;
				while(resultSet.next()) {
					if(freshPage) {
						firstContractOnPage = resultSet.getRow();
						freshPage = false;
						if(!firstPages.contains(firstContractOnPage)) {
							firstPages.add(firstContractOnPage);
						}
					}
					contracts.addField(currentContractNumber + ". " + resultSet.getString("contractName"), "**Contract ID:** " + resultSet.getInt("contractId"));

	       				currentContractNumber++;
	       				if(resultSet.getRow() % 25 == 0 || resultSet.getRow() == totalContracts) {
	       					lastContractOnPage = resultSet.getRow();
	       					break;
	       				}
	        		}

				//Displays the title of the embed. If it has more than 25 contracts, displays the page as well.
				if(totalContracts > 25) {
					contracts.setTitle("Active Contracts | Page " + currentPage);
				} else {
					contracts.setTitle("Active Contracts");
				}
				long embedMessageId = mEvent.getChannel().sendMessage(contracts).get().getId();

				//Adds the reactions needed to "flip a page".
	        		if(currentPage == 1 && totalPages > 1) {
					mEvent.getChannel().getMessageById(embedMessageId).get().addReaction("➡");
					//mEvent.getMessage().addReaction("➡");
	        		} else if(currentPage == totalPages && totalPages > 1) {
					mEvent.getChannel().getMessageById(embedMessageId).get().addReaction("⬅");
					//mEvent.getMessage().addReaction("⬅");
	        		} else if(currentPage > 1 && currentPage < totalPages) {
					mEvent.getChannel().getMessageById(embedMessageId).get().addReaction("⬅");
					mEvent.getChannel().getMessageById(embedMessageId).get().addReaction("➡");
					//mEvent.getMessage().addReaction("⬅");
					//mEvent.getMessage().addReaction("➡");
	        		}

	        		//Adds a listener that "flips a page" when the one who called the command reacts on the `~showContracts` message. Also, removes all reactions after 35 seconds.
	        		if(totalPages > 1) {
					int ccn = currentContractNumber;
					int fcop = firstContractOnPage;
					int lcop = lastContractOnPage;
					mEvent.getChannel().getMessageById(embedMessageId).get().addReactionAddListener(event -> {
	        				if(event.getUserId() == mEvent.getMessageAuthor().getId()) {
	        					int cp = currentPage;
	        					boolean pressedBack = false;
	            					if((currentPage < totalPages) && event.getEmoji().equalsEmoji("➡")) {
	            						cp = cp + 1;
	            					} else if((currentPage > 1) && event.getEmoji().equalsEmoji("⬅")) {
	            						pressedBack = true;
	            						cp = cp - 1;
	            					}
							event.deleteMessage();
	            					showContracts(mEvent, cp, ccn, fcop, lcop, pressedBack);
	        				}
	            			}).removeAfter(35, TimeUnit.SECONDS).addRemoveHandler(() -> {
	            				try {
							mEvent.getChannel().getMessageById(embedMessageId).get().removeAllReactions();
	            				} catch(ExecutionException e0) {
	            					logger.error("Expected/Handled: " + e0 + " -> (" + e0.getCause() + ")"); //Sends an error log about an expected/handled error.
						} catch(Exception e0) {
							mEvent.getMessage().addReaction("⚠");
							logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
							logger.fatal("" + e0 + " -> (" + e0.getCause() + ")"); //Sends a fatal log about an unhandled error.
							e0.printStackTrace();
						}
	            			});
	        		}
			} else {
				contracts.addField("Empty", "There are no active contracts.");
				mEvent.getChannel().sendMessage(contracts);
			}

			//Closes the connections.
			resultSet.close();
			statement.close();
			connection.close();

			logger.info("Command/Page (~showContracts) called/flipped by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command/flipped a page.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command to manually add a contract to the 'BHT/Contracts' SQL database by typing `~addContract` (not case-sensitive).
	public void manuallyAddContractToDatabase(MessageCreateEvent mEvent, String snakeName) {
		try {
			//Checks if the name after the `~addContract` command resembles an actual name.
			if(snakeName.matches("^[A-Z](?=.{2,19}$)[A-Za-z.]+(?:\\h+[A-Z][A-Za-z.]+)+$")) {
				//Opens up a connection to the 'BHT' SQL database (Contracts).
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection("...");

				//Adds the person after the `~addContract` command to the 'BHT' SQL database (Contracts) if he's not there already.
				PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(contractName) FROM Contracts WHERE contractName = ?");
				preparedStatement0.setString(1, snakeName);
				ResultSet resultSet = preparedStatement0.executeQuery();
				resultSet.next();
				if(resultSet.getInt(1) == 0) {
					PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Contracts(contractName, isPriorityContract) VALUES(?, 0)");
					preparedStatement.setString(1, snakeName);
					int affectedRows = preparedStatement.executeUpdate();
					if(affectedRows > 0) {
						mEvent.getMessage().addReaction("👍");
						new MessageBuilder().append("A contract has been successfully placed on `" + snakeName + "`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

						logger.info("Command (~addContract) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + snakeName + "."); //Sends an info log about who issued the command.
					} else {
						mEvent.getMessage().addReaction("👎");
					}
					//Closes the connections.
					preparedStatement.close();
					connection.close();
				} else {
					mEvent.getMessage().addReaction("👎");
					new MessageBuilder().append("Error! There is already an active contract with that name.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					//Closes the connections.
					resultSet.close();
					preparedStatement0.close();
					connection.close();
				}
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! That is not a valid name:\n**1.** Each word must begin with a capital letter, have at least two letters in it and only one blank space in-between;\n**2.** Special characters and numbers are not allowed;\n**3.** The length of the full name must be between 3 and 20 letters;\n**4.** The name must contain at least one blank space.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
		} catch(StringIndexOutOfBoundsException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! That is not a valid name:\n**1.** Each word must begin with a capital letter, have at least two letters in it and only one blank space in-between;\n**2.** Special characters and numbers are not allowed;\n**3.** The length of the full name must be between 3 and 20 letters;\n**4.** The name must contain at least one blank space.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(NullPointerException | IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command to manually update a contract from the 'BHT/Contracts' SQL database by typing `~updateContract` (not case-sensitive).
	public void updateContractInDatabase(MessageCreateEvent mEvent, String snakeID, String snakeNewName) {
		try {
			if(snakeNewName.matches("^[A-Z](?=.{2,19}$)[A-Za-z.]+(?:\\h+[A-Z][A-Za-z.]+)+$")) {
				//Opens up a connection to the 'BHT' SQL database (Contracts).
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection("...");

				//Updates the contract with the ID after the `~updateContract` command from the 'BHT/Contracts' SQL database if he's not there already.
				PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(contractName) FROM Contracts WHERE contractName = ?");
				preparedStatement0.setString(1, snakeNewName);
				ResultSet resultSet = preparedStatement0.executeQuery();
				resultSet.next();
				if(resultSet.getInt(1) == 0) {
					PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Contracts SET contractName = ? WHERE contractId = ?");
					preparedStatement.setString(1, snakeNewName);
					preparedStatement.setInt(2, Integer.parseInt(snakeID));
					int affectedRows = preparedStatement.executeUpdate();
					if(affectedRows > 0) {
						mEvent.getMessage().addReaction("👍");
						new MessageBuilder().append("A contract with an ID of `" + snakeID + "` has been successfully updated to `" + snakeNewName + "`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

						logger.info("Command (~updateContract) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + snakeID + " (" + snakeNewName + ")."); //Sends an info log about who issued the command.
					} else {
						mEvent.getMessage().addReaction("👎");
						new MessageBuilder().append("Error! The Contract ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
					}
					//Closes the connections.
					preparedStatement.close();
					connection.close();
				} else {
					mEvent.getMessage().addReaction("👎");
					new MessageBuilder().append("Error! There is already an active contract with that name.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					//Closes the connections.
					resultSet.close();
					preparedStatement0.close();
					connection.close();
				}
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! That is not a valid name:\n**1.** Each word must begin with a capital letter, have at least two letters in it and only one blank space in-between;\n**2.** Special characters and numbers are not allowed;\n**3.** The length of the full name must be between 3 and 20 letters;\n**4.** The name must contain at least one blank space.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
		} catch(ArrayIndexOutOfBoundsException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! Please, follow the format: `~updateContract [Contract ID] [Full Name]`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~updateContract` command must be a digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(NullPointerException | IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command to manually remove a contract from the 'BHT/Contracts' SQL database by typing `~removeContract` (not case-sensitive).
	public void removeContractFromDatabase(MessageCreateEvent mEvent, String snakeID) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Contracts).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

			//Removes the contract with the ID after the `~removeContract` command from the 'BHT/Contracts' SQL database.
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Contracts WHERE contractId = ?");
			preparedStatement.setInt(1, Integer.parseInt(snakeID));
			int affectedRows = preparedStatement.executeUpdate();
			if(affectedRows > 0) {
				mEvent.getMessage().addReaction("👍");
				new MessageBuilder().append("A contract with an ID of `" + snakeID + "` has been successfully removed.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				logger.info("Command (~removeContract) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + snakeID + "."); //Sends an info log about who issued the command.
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The Contract ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
			//Closes the connections.
			preparedStatement.close();
			connection.close();
		} catch(StringIndexOutOfBoundsException | NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeContract` command must be a digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command that displays all active priority contracts by typing `~showPriorityContracts` (not case-sensitive).
	public void showPriorityContracts(MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with all the commands.
			EmbedBuilder priorityContracts = new EmbedBuilder()
				.setTitle("Priority Contracts")
				.setThumbnail("https://i.imgur.com/HoOkwBs.png")
				.setColor(Color.RED)
				.setFooter("Make sure to remove the contract that is no longer a priority by typing `~removePriorityContract [Contract ID]`!");

			//Opens up a connection to the 'BHT' SQL database (Contracts).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

			//Creates a 'SELECT' SQL statement.
			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ResultSet resultSet = statement.executeQuery("SELECT contractId, contractName FROM Contracts WHERE isPriorityContract = 1");

			resultSet.last();
			if(resultSet.getRow() > 0) {
				int currentPriorityContractNumber = 0;
				resultSet.beforeFirst();
				while(resultSet.next()) {
					currentPriorityContractNumber++;
					priorityContracts.addField(currentPriorityContractNumber + ". " + resultSet.getString("contractName"), "**Contract ID:** " + resultSet.getInt("contractId"));
				}
			} else {
				priorityContracts.addField("Empty", "There are no active contracts.");
			}
			mEvent.getChannel().sendMessage(priorityContracts);

			//Closes the connections.
			resultSet.close();
			statement.close();
			connection.close();

			logger.info("Command (~showPriorityContracts) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command to make a priority contract from the 'BHT/Contracts' SQL database by typing `~addPriorityContract` (not case-sensitive).
	public void addPriorityContract(MessageCreateEvent mEvent, String snakeID) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Contracts).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Sets the 'isPriorityContract' value to 1 to the contract with the ID after the `~addPriorityContract` command from the 'BHT/Contracts' SQL database, if it's not 1 already.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT isPriorityContract FROM Contracts WHERE contractId = ?");
			preparedStatement0.setInt(1, Integer.parseInt(snakeID));
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 0) {
				PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Contracts SET isPriorityContract = 1 WHERE contractId = ?");
				preparedStatement.setLong(1, Integer.parseInt(snakeID));
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					mEvent.getMessage().addReaction("👍");
					new MessageBuilder().append("The contract with an ID of `" + snakeID + "` is now a priority contract.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					logger.info("Command (~addPriorityContract) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + snakeID + "."); //Sends an info log about who issued the command.
				} else {
					mEvent.getMessage().addReaction("👎");
				}
				preparedStatement.close();
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The contract with the specified ID is already a priority contract.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}

			//Closes the connections.
			resultSet.close();
			preparedStatement0.close();
			connection.close();
		} catch(StringIndexOutOfBoundsException | NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~addPriorityContract` command must be a digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(SQLException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The Contract ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command to remove a priority contract from the 'BHT/Contracts' SQL database by typing `~removePriorityContract` (not case-sensitive).
	public void removePriorityContract(MessageCreateEvent mEvent, String snakeID) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Contracts).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Sets the 'isPriorityContract' value to 1 to the contract with the ID after the `~removePriorityContract` command from the 'BHT/Contracts' SQL database, if it's not 1 already.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT isPriorityContract FROM Contracts WHERE contractId = ?");
			preparedStatement0.setInt(1, Integer.parseInt(snakeID));
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 1) {
				PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Contracts SET isPriorityContract = 0 WHERE contractId = ?");
				preparedStatement.setLong(1, Integer.parseInt(snakeID));
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					mEvent.getMessage().addReaction("👍");
					new MessageBuilder().append("The contract with an ID of `" + snakeID + "` is no longer a priority contract.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					logger.info("Command (~removePriorityContract) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + snakeID + "."); //Sends an info log about who issued the command.
				} else {
					mEvent.getMessage().addReaction("👎");
				}
				preparedStatement.close();
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The contract with the specified ID is not a priority contract.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}

			//Closes the connections.
			resultSet.close();
			preparedStatement0.close();
			connection.close();
		} catch(StringIndexOutOfBoundsException | NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removePriorityContract` command must be a digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(SQLException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The Contract ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command that displays all the channels where contract related commands can be used by typing `~showChannels` (not case-sensitive).
	public void showChannels(MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

        		//An embed with the channels where contract related commands can be used.
			EmbedBuilder channels = new EmbedBuilder()
				.setTitle("Channels")
				.setThumbnail("https://i.imgur.com/HoOkwBs.png")
				.setColor(Color.RED)
				.setFooter("These are the channels where contract related commands can be used.");

			//Opens up a connection to the 'BHT' SQL database (Channels).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

			//Creates a 'SELECT' SQL statement.
        		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        		ResultSet resultSet = statement.executeQuery("SELECT * FROM Channels");

			//If there are any channels, adds them to the above-mentioned embed. If not, notifies the user.
        		resultSet.last();
        		if(resultSet.getRow() > 0) {
        			int k = 1;
        			resultSet.beforeFirst();
    				while(resultSet.next()) {
            				//Adds the channels to the above-mentioned embed.
    					channels.addField(k + ". " + resultSet.getString("channelName"), "**ID: **" + resultSet.getLong("channelId"));
    					k++;
    				}
        		} else {
        			channels.addField("Empty", "There are no white-listed channels.");
        		}
			mEvent.getChannel().sendMessage(channels);

			//Closes the above connections.
			resultSet.close();
			statement.close();
			connection.close();

			logger.info("Command (~showChannels) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command that adds the channel with the specified ID to a database of channels in which the use of certain commands is allowed by typing `~addChannel` (not case-sensitive).
	public void addChannel(Server server, MessageCreateEvent mEvent, String channelID) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Channels).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Adds the channel with the ID after the `~addChannel` command to the 'BHT/Channels' SQL database if it's not there already.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(channelId) FROM Channels WHERE channelId = ?");
			preparedStatement0.setLong(1, Long.parseLong(channelID));
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 0) {
				ServerTextChannel channel = null;
				if(server.getChannelById(channelID).isPresent() && server.getChannelById(channelID).get().asServerTextChannel().isPresent()) {
					channel = server.getChannelById(channelID).get().asServerTextChannel().get();
				}
				if(channel != null) {
					PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Channels(channelId, channelName) VALUES(?, ?)");
					preparedStatement.setLong(1, Long.parseLong(channelID));
					preparedStatement.setString(2, channel.getName());
					int affectedRows = preparedStatement.executeUpdate();
					if(affectedRows > 0) {
						mEvent.getMessage().addReaction("👍");
						new MessageBuilder().append("Contract related commands are now available in the following channel: " + channel.getMentionTag() + ".").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

						logger.info("Command (~addChannel) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + channelID + "=" + channel.getName() + "."); //Sends an info log about who issued the command.
						preparedStatement.close();
					} else {
						mEvent.getMessage().addReaction("👎");
						preparedStatement.close();
					}
				} else {
					mEvent.getMessage().addReaction("👎");
				}
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The channel is already white-listed.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				//Closes the connections.
				preparedStatement0.close();
			}
			//Closes the connections.
			preparedStatement0.close();
			resultSet.close();
			connection.close();
		} catch(StringIndexOutOfBoundsException | NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~addChannel` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(NoSuchElementException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! There is no channel with the specified ID in the server.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command that removes the channel with the specified ID from a database of channels in which the use of certain commands is allowed by typing `~removeChannel` (not case-sensitive).
	public void removeChannel(Server server, MessageCreateEvent mEvent, String channelID) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Channels).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Removes the channel with the ID after the `~removeChannel` command from the 'BHT/Channels' SQL database.
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Channels WHERE channelId = ?");
			preparedStatement.setLong(1, Long.parseLong(channelID));
			int affectedRows = preparedStatement.executeUpdate();
			if(affectedRows > 0) {
				ServerTextChannel channel = null;
				if(server.getChannelById(channelID).isPresent() && server.getChannelById(channelID).get().asServerTextChannel().isPresent()) {
					channel = server.getChannelById(channelID).get().asServerTextChannel().get();
				}
				if(channel != null) {
					mEvent.getMessage().addReaction("👍");
					new MessageBuilder().append("Contract related commands are no longer available in the following channel: " + channel.getMentionTag() + ".").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					logger.info("Command (~removeChannel) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + channelID + "=" + channel.getName() + "."); //Sends an info log about who issued the command.
				}
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The Channel ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
			//Closes the connections.
			preparedStatement.close();
			connection.close();
		} catch(StringIndexOutOfBoundsException | NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeChannel` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command that displays all the roles who can use contract related commands by typing `~showRoles` (not case-sensitive).
	public void showRoles(MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

        		//An embed with the roles who can use contract related commands.
			EmbedBuilder roles = new EmbedBuilder()
				.setTitle("Roles")
				.setThumbnail("https://i.imgur.com/HoOkwBs.png")
				.setColor(Color.RED)
				.setFooter("These are the roles who can use contract related commands.");

			//Opens up a connection to the 'BHT' SQL database (Roles).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

			//Creates a 'SELECT' SQL statement.
        		Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        		ResultSet resultSet = statement.executeQuery("SELECT * FROM Roles");

			//If there are any white-listed roles, adds them to the above-mentioned embed. If not, notifies the user.
        		resultSet.last();
        		if(resultSet.getRow() > 0) {
        			int k = 1;
        			resultSet.beforeFirst();
    				while(resultSet.next()) {
            				//Adds the roles to the above-mentioned embed.
    					roles.addField(k + ". " + resultSet.getString("roleName"), "**ID: **" + resultSet.getLong("roleId"));
    					k++;
    				}
        		} else {
        			roles.addField("Empty", "There are no white-listed roles.");
        		}
			mEvent.getChannel().sendMessage(roles);

			//Closes the above connections.
			resultSet.close();
			statement.close();
			connection.close();

			logger.info("Command (~showRoles) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A commands that adds the role with the specified ID to a database of roles which allow the use of certain commands by typing `~addRole` (not case-sensitive).
	public void addRole(Server server, MessageCreateEvent mEvent, String roleID) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Roles).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Adds the role with the ID after the `~addRole` command to the 'BHT/Roles' SQL database if it's not there already.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(roleId) FROM Roles WHERE roleId = ?");
			preparedStatement0.setLong(1, Long.parseLong(roleID));
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 0) {
				String roleName = null;
				if(server.getRoleById(Long.parseLong(roleID)).isPresent()) {
					roleName = server.getRoleById(Long.parseLong(roleID)).get().getName();
				}
				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Roles(roleId, roleName) VALUES(?, ?)");
				preparedStatement.setLong(1, Long.parseLong(roleID));
				preparedStatement.setString(2, roleName);
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					mEvent.getMessage().addReaction("👍");
					new MessageBuilder().append("Contract related commands are now available to the following role: `" + roleName + "`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					logger.info("Command (~addRole) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + roleID + "=" + roleName + "."); //Sends an info log about who issued the command.
					preparedStatement.close();
				} else {
					mEvent.getMessage().addReaction("👎");
					preparedStatement.close();
				}
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The role is already white-listed.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
			//Closes the connections.
			preparedStatement0.close();
			resultSet.close();
			connection.close();
		} catch(StringIndexOutOfBoundsException | NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~addRole` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(NoSuchElementException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! There is no role with the specified ID in the server.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A commands that removes the role with the specified ID from a database of roles which allow the use of certain commands by typing `~removeRole` (not case-sensitive).
	public void removeRole(Server server, MessageCreateEvent mEvent, String roleID) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Roles).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Removes the channel with the ID after the `~removeRole` command from the 'BHT/Roles' SQL database.
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Roles WHERE roleId = ?");
			preparedStatement.setLong(1, Long.parseLong(roleID));
			int affectedRows = preparedStatement.executeUpdate();
			if(affectedRows > 0) {
				String roleName = null;
				if(server.getRoleById(Long.parseLong(roleID)).isPresent()) {
					roleName = server.getRoleById(Long.parseLong(roleID)).get().getName();
				}
				mEvent.getMessage().addReaction("👍");
				new MessageBuilder().append("Contract related commands are no longer available to the following role: `" + roleName + "`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				logger.info("Command (~removeRole) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + roleID + "=" + roleName + "."); //Sends an info log about who issued the command.
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Error! The Role ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
			//Closes the connections.
			preparedStatement.close();
			connection.close();
		} catch(StringIndexOutOfBoundsException | NumberFormatException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeRole` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command that teaches you how to use the `~rpsPlay` command by typing `~rpsHelp` (not case-sensitive).
	public void rockPaperScissorsHelp(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with all the RPS related commands.
			EmbedBuilder rpsCommands = new EmbedBuilder()
				.setTitle("Rock-Paper-Scissors")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.addField("Rules", "Here's what beats what if for whatever reason you don't know how the game works:\n**1.** Paper beats rock;\n**2.** Rock beats scissors;\n**3.** Scissors beats paper.")
				.addInlineField("~rpsPlay *[option]*", "Plays a game of Rock-Paper-Scissors against the bot.")
				.addInlineField("~rpsHighscores", "Shows the Top 10 users with the highest score.")
				.setFooter("The commands are not case-sensitive!");
			mEvent.getChannel().sendMessage(rpsCommands);

			logger.info("Command (~rpsHelp) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command to play "Rock-Paper-Scissors" with the bot by typing `~rpsPlay` (not case-sensitive).
	public void rockPaperScissorsGame(MessageCreateEvent mEvent, String optionPlayer) {
		try {
			Random rand = new Random(); //Creates an instance from the 'Random' class.

			//The bot randomly chooses an option.
			String[] botOptions = {"🪨", "📜", "✂"};
			int optionB = rand.nextInt(3);
			String optionBot = null;
			switch(optionB) {
				case 0 -> optionBot = "Rock";
				case 1 -> optionBot = "Paper";
				case 2 -> optionBot = "Scissors";
				default -> logger.fatal("RPS Error!");
			}

			//Checks the user's input and starts the game if it matches one of the three options.
			if(optionPlayer.equalsIgnoreCase("Rock") || optionPlayer.equalsIgnoreCase("Paper") || optionPlayer.equalsIgnoreCase("Scissors")) {
				mEvent.getMessage().addReaction(botOptions[optionB]);

				//Outputs the winner of the game and if it's the user, inserts his name to the database. Also, mentions the one who called the command.
				if(optionPlayer.equalsIgnoreCase(optionBot)) {
					new MessageBuilder().append("\nYou chose: **`" + optionPlayer + "`**\nThe bot chose: **`" + optionBot + "`**\nIt's a **TIE**!").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
				} else if((optionPlayer.equalsIgnoreCase("Rock") && optionB == 1) || (optionPlayer.equalsIgnoreCase("Paper") && optionB == 2) || (optionPlayer.equalsIgnoreCase("Scissors") && optionB == 0)) {
					new MessageBuilder().append("\nYou chose: **`" + optionPlayer + "`**\nThe bot chose: **`" + optionBot + "`**\nYou **LOSE**!").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
				} else {
	        			new MessageBuilder().append("\nYou chose: **`" + optionPlayer + "`**\nThe bot chose: **`" + optionBot + "`**\nYou **WIN**!").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

	        			//Opens up a connection to the 'BHT' SQL database (Highscores).
					Class.forName("com.mysql.cj.jdbc.Driver");
					Connection connection = DriverManager.getConnection("...");

		        		//Adds the user to the 'BHT' SQL database (Highscores) if he's not there already and if he is, then his score gets updated.
		        		PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(highscoreUser) FROM Highscores WHERE highscoreUser = ?");
					preparedStatement0.setString(1, mEvent.getMessageAuthor().getDiscriminatedName());
					ResultSet resultSet = preparedStatement0.executeQuery();
					resultSet.next();
		        		if(resultSet.getInt(1) == 0) {
						PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Highscores(highscoreUser, score) VALUES(?, 1)");
						preparedStatement.setString(1, mEvent.getMessageAuthor().getDiscriminatedName());
						int affectedRows = preparedStatement.executeUpdate();
						if(affectedRows > 0) {
	        					mEvent.getMessage().addReaction("🎉");

							//Closes the connections.
							resultSet.close();
							preparedStatement.close();
							connection.close();
	        				}
		        		} else {
		        		//Updates the user's score.
		        		PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Highscores SET score=(score+1) WHERE highscoreUser = ?");
	        			preparedStatement.setString(1, mEvent.getMessageAuthor().getDiscriminatedName());
	        			int affectedRows = preparedStatement.executeUpdate();
	        			if(affectedRows > 0) {
	        				mEvent.getMessage().addReaction("🎉");

	        				//Closes the connections.
	        				resultSet.close();
	        				preparedStatement.close();
	        				connection.close();
	        			}
		        	}
	        	}
	        	logger.info("Command (~rpsPlay) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + optionPlayer + "."); //Sends an info log about who issued the command.
			} else {
				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! Please choose either `Rock`, `Paper` or `Scissors`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
		} catch(StringIndexOutOfBoundsException e1) {
			try {
				logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.

				mEvent.getMessage().addReaction("👎");
				new MessageBuilder().append("Wrong input! Please choose either `Rock`, `Paper` or `Scissors`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2) {
				mEvent.getMessage().addReaction("⚠");
				logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
				logger.fatal("" + e2 + " -> (" + e2.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e2.printStackTrace();
			}
		} catch(NullPointerException | IndexOutOfBoundsException e1) {
			logger.error("Expected/Handled: " + e1 + " -> (" + e1.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e1) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e1 + " -> (" + e1.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e1.printStackTrace();
		}
	}

	//A command that displays the top 10 users with the highest score in "Rock-Paper-Scissors" by typing `~rpsHighscores` (not case-sensitive).
	public void rockPaperScissorsHighscores(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//Opens up a connection to the 'BHT' SQL database (Highscores).
        		Class.forName("com.mysql.cj.jdbc.Driver");
        		Connection connection = DriverManager.getConnection("...");

        		//Creates a 'COUNT' SQL statement.
			Statement statement0 = connection.createStatement();
        		ResultSet resultSet0 = statement0.executeQuery("SELECT COUNT(highscoreId) FROM Highscores");
        		resultSet0.next();

        		//An embed with the top 10 users with the highest scores.
			EmbedBuilder highscores = new EmbedBuilder();
			highscores
				.setTitle("RPS Highscores")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.setFooter("A total of " + resultSet0.getInt(1) + " users have won against the bot.");

			//Closes the above connections.
			resultSet0.close();
        		statement0.close();

			//Creates a 'SELECT' SQL statement.
        		Statement statement = connection.createStatement();
        		ResultSet resultSet = statement.executeQuery("SELECT * FROM Highscores ORDER BY score DESC");

			//Adds the actual users to the above-mentioned embed.
			int k = 1;
			while(resultSet.next()) {
				highscores.addField(k + ". " + resultSet.getString("highscoreUser"), "Score: **" + resultSet.getInt("score") + "**");
				k++;
			}
			mEvent.getChannel().sendMessage(highscores);

			//Closes the above connections.
			resultSet.close();
			statement.close();
			connection.close();

			logger.info("Command (~rpsHighscores) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}

	//A command that displays all the information on how to get the Channel/Role ID by typing `~idHelp`(not case-sensitive).
	public void idHelp(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with information on how to get the Channel/Role ID.
			EmbedBuilder contractsCommands = new EmbedBuilder()
				.setTitle("How to get the Channel/Role ID")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.addField("Steps", "**1.** Go to `User Settings`;\n**2.** Scroll down to `Advanced` (under `APP SETTINGS`) and click on it;\n**3.** Enable `Developer Mode`.")
				.addField("Commands", "You can also use one of the commands (`~showChannels`, `~showRoles`) to check the ID of an already white-listed channel/role.")
				.setFooter("Use `~contractsHelp` for the commands where you can use the Channel/Role ID.");
			mEvent.getChannel().sendMessage(contractsCommands);

			logger.info("Command (~idHelp) called by " + Iterables.get(allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()), 0) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends an info log about who issued the command.
		} catch(IndexOutOfBoundsException e) {
			logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
		} catch(Exception e) {
			mEvent.getMessage().addReaction("⚠");
			logger.warn("Fatal error occurred!"); //Sends a warning log about a possible fatal error.
			logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
			e.printStackTrace();
		}
	}
}
