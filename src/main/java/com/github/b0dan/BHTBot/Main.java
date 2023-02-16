package com.github.b0dan.BHTBot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class Main {
	public static void main(String[] args) {
		Logger logger = LogManager.getLogger(Main.class); //Creates an instance of the 'Logger' class for 'Main.class'.

		DiscordApi api = new DiscordApiBuilder().setToken("...").setAllIntents().login().join(); //Creates an object from the Discord API.
		System.out.println(api.getServers() + "\nBHT Bot is online!\n");
		api.updateActivity(ActivityType.LISTENING, "~commandsHelp"); //Updates the activity to "Listening to ~commandsHelp".

       		Commands cmd = new Commands(); //Creates an object from the 'Commands' class.

      	 	//Different commands and permissions on who can use them.
       		api.addMessageCreateListener(event -> { //Head Shan Chu(262802994574131200L), Shan Chu(262784255816499201L), Tech Manager(343680704502300672L), Crew Leader(419232575261769730L), Luminous Path(336588534351790080L).
			try {
				if(event.getServer().isPresent()) {
					Server server = event.getServer().get();
					if(server.getId() == 262781891705307137L) {
						if(event.getMessageContent().charAt(0) == '~') {
							String[] commandArguments;
							if(event.getMessageContent().length() <= 11 && !event.getMessageContent().equalsIgnoreCase("~addContract")) {
								commandArguments = event.getMessageContent().split(" ", 3);
							} else if(event.getMessageContent().substring(0, 12).equalsIgnoreCase("~addContract")) {
								commandArguments = event.getMessageContent().split(" ", 2);
							} else {
								commandArguments = event.getMessageContent().split(" ", 3);
							}
							String command = commandArguments[0];
							String argument1 = null;
							if(commandArguments.length >= 2) {
								argument1 = commandArguments[1];
							}
							String argument2 = null;
							if(commandArguments.length == 3) {
								argument2 = commandArguments[2];
							}

							switch(command.substring(1).toLowerCase()) {
								case "updatemembers" -> {
									if(server.getSystemChannel().isPresent() && (event.getMessageAuthor().isBotOwner() || event.getMessageAuthor().isServerAdmin())) {
										if(event.getChannel().equals(server.getSystemChannel().get())) {
											cmd.manuallyUpdateMembers(server, event);
										} else {
											event.getMessage().addReaction("👎");
											new MessageBuilder().append("You can only use the `~updateMembers` command in ")
												.append(server.getSystemChannel().get().getMentionTag() + ".")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										}
									} else {
										event.getMessage().addReaction("👎");

										Role role1 = null;
										if(server.getRoleById(262784255816499201L).isPresent()) {
											role1 = server.getRoleById(262784255816499201L).get();
										}
										Role role2 = null;
										if(server.getRoleById(262802994574131200L).isPresent()) {
											role2 = server.getRoleById(262802994574131200L).get();
										}

										if(role1 != null && role2 != null) {
											new MessageBuilder().append("You can only use the `~updateMembers` command if you have at least one of the following roles: ")
												.append("`" + role1.getName() + "`").append(", ")
												.append("`" + role2.getName() + "`").append(".")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										}
									}
								}
								case "getallmembers" -> {
									if(server.getSystemChannel().isPresent() && event.getMessageAuthor().isBotOwner()) {
										if(event.getChannel().equals(server.getSystemChannel().get())) {
											cmd.getAllMembers(event);
										}
									}
								}
								case "commandshelp" -> cmd.displayCommands(api, event);
								case "contractshelp" -> cmd.contractsHelp(event);
								case "rpshelp" -> cmd.rockPaperScissorsHelp(api, event);
								case "setonleaverole" -> {
									if(event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner()) {
										if(event.getChannel().equals(server.getSystemChannel().get())) {
											cmd.setOnLeaveRole(server, event, argument1);
										} else {
											event.getMessage().addReaction("👎");
											new MessageBuilder().append("You can only use the `~setOnLeaveRole` command in ")
												.append(server.getSystemChannel().get().getMentionTag() + ".")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										}
									} else {
										event.getMessage().addReaction("👎");

										Role role1 = null;
										if(server.getRoleById(262784255816499201L).isPresent()) {
											role1 = server.getRoleById(262784255816499201L).get();
										}
										Role role2 = null;
										if(server.getRoleById(262802994574131200L).isPresent()) {
											role2 = server.getRoleById(262802994574131200L).get();
										}

										if(role1 != null && role2 != null) {
											new MessageBuilder()
												.append("You can only use the `~setOnLeavePing` command if you have at least one of the following roles: ")
												.append("`" + role1.getName() + "`").append(", ")
												.append("`" + role2.getName() + "`").append(".")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										}
									}
								}
								case "setonleaveping" -> {
									if(event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner()) {
										if(event.getChannel().equals(server.getSystemChannel().get())) {
											cmd.setOnLeavePing(event, argument1);
										} else {
											event.getMessage().addReaction("👎");
											new MessageBuilder().append("You can only use the `~setOnLeavePing` command in ")
												.append(server.getSystemChannel().get().getMentionTag() + ".")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										}
									} else {
										event.getMessage().addReaction("👎");

										Role role1 = null;
										if(server.getRoleById(262784255816499201L).isPresent()) {
											role1 = server.getRoleById(262784255816499201L).get();
										}
										Role role2 = null;
										if(server.getRoleById(262802994574131200L).isPresent()) {
											role2 = server.getRoleById(262802994574131200L).get();
										}

										if(role1 != null && role2 != null) {
											new MessageBuilder()
												.append("You can only use the `~setOnLeavePing` command if you have at least one of the following roles: ")
												.append("`" + role1.getName() + "`").append(", ")
												.append("`" + role2.getName() + "`").append(".")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										}
									}
								}
								case "showcontracts" -> cmd.showContracts(event, 1, 1, 0, 0, false);
								case "addcontract" -> {
									//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
									Class.forName("com.mysql.cj.jdbc.Driver");
									Connection connection = DriverManager.getConnection("...");

									//Creates a 'SELECT' SQL statement.
									Statement statement = connection.createStatement();

									//Checks if the user issuing the command is in the correct channel.
									ResultSet resultSet = statement.executeQuery("SELECT channelId FROM Channels");
									boolean channel = false;
									if(event.getChannel().asServerTextChannel().isPresent()) {
										while(resultSet.next() && !channel) {
											if(resultSet.getLong(1) == event.getChannel().asServerTextChannel().get().getId()) {
												channel = true;
											}
										}
									}

									//Checks if the user issuing the command has at least one of the required roles.
									resultSet = statement.executeQuery("SELECT roleId FROM Roles");
									boolean role = false;
										while(resultSet.next() && !role) {
											if(server.getRoleById(resultSet.getLong(1)).isPresent()) {
												if(server.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
													role = true;
												}
											}
										}
									//}

									//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
									if((channel && role) || (channel && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner()))) {
										//Closes the connections.
										resultSet.close();
										statement.close();
										connection.close();

										cmd.manuallyAddContractToDatabase(event, argument1);
									} else if(!channel) {
										event.getMessage().addReaction("👎");

										//Prints out the channels where the command can be used.
										ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
										MessageBuilder channels = new MessageBuilder().append("The command `~addContract` can only be used in the following channels: ");

										if(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										while(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels
													.append(", ")
													.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									} else {
										event.getMessage().addReaction("👎");

										//Prints out the roles who can use the command.
										ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
										MessageBuilder roles = new MessageBuilder().append("You can only use the `~addContract` command if you have at least one of the following roles: ");

										if(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										while(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles
													.append(", ")
													.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									}
								}
								case "updatecontract" -> {
									//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
									Class.forName("com.mysql.cj.jdbc.Driver");
									Connection connection = DriverManager.getConnection("...");

									//Creates a 'SELECT' SQL statement.
									Statement statement = connection.createStatement();

									//Checks if the user issuing the command is in the correct channel.
									ResultSet resultSet = statement.executeQuery("SELECT channelId FROM Channels");
									boolean channel = false;
									while(resultSet.next() && !channel) {
										if(resultSet.getLong(1) == event.getChannel().asServerTextChannel().get().getId()) {
											channel = true;
										}
									}

									//Checks if the user issuing the command has at least one of the required roles.
									resultSet = statement.executeQuery("SELECT roleId FROM Roles");
									boolean role = false;
									while(resultSet.next() && !role) {
										if(server.getRoleById(resultSet.getLong(1)).isPresent()) {
											if(server.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
												role = true;
											}
										}
									}

									//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
									if((channel && role) || (channel && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner()))) {
										//Closes the connections.
										resultSet.close();
										statement.close();
										connection.close();

										cmd.updateContractInDatabase(event, argument1, argument2);
									} else if(!channel) {
										event.getMessage().addReaction("👎");

										//Prints out the channels where the command can be used.
										ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
										MessageBuilder channels = new MessageBuilder().append("The command `~updateContract` can only be used in the following channels: ");

										if(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										while(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels
													.append(", ")
													.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									} else {
										event.getMessage().addReaction("👎");

										//Prints out the roles who can use the command.
										ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
										MessageBuilder roles = new MessageBuilder().append("You can only use the `~updateContract` command if you have at least one of the following roles: ");

										if(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										while(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles
													.append(", ")
													.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									}
								}
								case "removecontract" -> {
									//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
									Class.forName("com.mysql.cj.jdbc.Driver");
									Connection connection = DriverManager.getConnection("...");

									//Creates a 'SELECT' SQL statement.
									Statement statement = connection.createStatement();

									//Checks if the user issuing the command is in the correct channel.
									ResultSet resultSet = statement.executeQuery("SELECT channelId FROM Channels");
									boolean channel = false;
									while(resultSet.next() && !channel) {
										if(resultSet.getLong(1) == event.getChannel().asServerTextChannel().get().getId()) {
											channel = true;
										}
									}

									//Checks if the user issuing the command has at least one of the required roles.
									resultSet = statement.executeQuery("SELECT roleId FROM Roles");
									boolean role = false;
									while(resultSet.next() && !role) {
										if(server.getRoleById(resultSet.getLong(1)).isPresent()) {
											if(server.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
												role = true;
											}
										}
									}

									//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
									if((channel && role) || (channel && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner()))) {
										//Closes the connections.
										resultSet.close();
										statement.close();
										connection.close();

										cmd.removeContractFromDatabase(event, argument1);
									} else if(!channel) {
										event.getMessage().addReaction("👎");

										//Prints out the channels where the command can be used.
										ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
										MessageBuilder channels = new MessageBuilder().append("The command `~removeContract` can only be used in the following channels: ");

										if(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										while(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels
													.append(", ")
													.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									} else {
										event.getMessage().addReaction("👎");

										//Prints out the roles who can use the command.
										ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
										MessageBuilder roles = new MessageBuilder().append("You can only use the `~removeContract` command if you have at least one of the following roles: ");

										if(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										while(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles
													.append(", ")
													.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									}
								}
								case "showprioritycontracts" -> cmd.showPriorityContracts(event);
								case "addprioritycontract" -> {
									//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
									Class.forName("com.mysql.cj.jdbc.Driver");
									Connection connection = DriverManager.getConnection("...");

									//Creates a 'SELECT' SQL statement.
									Statement statement = connection.createStatement();

									//Checks if the user issuing the command is in the correct channel.
									ResultSet resultSet = statement.executeQuery("SELECT channelId FROM Channels");
									boolean channel = false;
									while(resultSet.next() && !channel) {
										if(resultSet.getLong(1) == event.getChannel().asServerTextChannel().get().getId()) {
											channel = true;
										}
									}

									//Checks if the user issuing the command has at least one of the required roles.
									resultSet = statement.executeQuery("SELECT roleId FROM Roles");
									boolean role = false;
									while(resultSet.next() && !role) {
										if(server.getRoleById(resultSet.getLong(1)).isPresent()) {
											if(server.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
												role = true;
											}
										}
									}

									//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
									if((channel && role) || (channel && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner()))) {
										//Closes the connections.
										resultSet.close();
										statement.close();
										connection.close();

										cmd.addPriorityContract(event, argument1);
									} else if(!channel) {
										event.getMessage().addReaction("👎");

										//Prints out the channels where the command can be used.
										ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
										MessageBuilder channels = new MessageBuilder().append("The command `~addPriorityContract` can only be used in the following channels: ");

										if(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										while(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels
													.append(", ")
													.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									} else {
										event.getMessage().addReaction("👎");

										//Prints out the roles who can use the command.
										ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
										MessageBuilder roles = new MessageBuilder().append("You can only use the `~addPriorityContract` command if you have at least one of the following roles: ");

										if(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										while(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles
													.append(", ")
													.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									}
								}
								case "removeprioritycontract" -> {
									//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
									Class.forName("com.mysql.cj.jdbc.Driver");
									Connection connection = DriverManager.getConnection("...");

									//Creates a 'SELECT' SQL statement.
									Statement statement = connection.createStatement();

									//Checks if the user issuing the command is in the correct channel.
									ResultSet resultSet = statement.executeQuery("SELECT channelId FROM Channels");
									boolean channel = false;
									while(resultSet.next() && !channel) {
										if(resultSet.getLong(1) == event.getChannel().asServerTextChannel().get().getId()) {
											channel = true;
										}
									}

									//Checks if the user issuing the command has at least one of the required roles.
									resultSet = statement.executeQuery("SELECT roleId FROM Roles");
									boolean role = false;
									while(resultSet.next() && !role) {
										if(server.getRoleById(resultSet.getLong(1)).isPresent()) {
											if(server.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
												role = true;
											}
										}
									}

									//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
									if((channel && role) || (channel && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner()))) {
										//Closes the connections.
										resultSet.close();
										statement.close();
										connection.close();

										cmd.removePriorityContract(event, argument1);
									} else if(!channel) {
										event.getMessage().addReaction("👎");

										//Prints out the channels where the command can be used.
										ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
										MessageBuilder channels = new MessageBuilder().append("The command `~removePriorityContract` can only be used in the following channels: ");

										if(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										while(resultSet0.next()) {
											if(server.getChannelById(resultSet0.getLong(1)).isPresent() && server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().isPresent()) {
												channels
													.append(", ")
													.append(server.getChannelById(resultSet0.getLong(1)).get().asServerTextChannel().get().getMentionTag());
											}
										}
										channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									} else {
										event.getMessage().addReaction("👎");

										//Prints out the roles who can use the command.
										ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
										MessageBuilder roles = new MessageBuilder().append("You can only use the `~removePriorityContract` command if you have at least one of the following roles: ");

										if(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										while(resultSet0.next()) {
											if(server.getRoleById(resultSet0.getLong(1)).isPresent()) {
												roles
													.append(", ")
													.append("`" + server.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
											}
										}
										roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

										//Closes the connections.
										resultSet0.close();
										statement.close();
										connection.close();
									}
								}
								case "showchannels" -> cmd.showChannels(event);
								case "addchannel" -> {
									User user = null;
									if(event.getMessageAuthor().asUser().isPresent()) {
										user = event.getMessageAuthor().asUser().get();
									}
									Role role1 = null;
									if(server.getRoleById(343680704502300672L).isPresent()) {
										role1 = server.getRoleById(343680704502300672L).get();
									}
									Role role2 = null;
									if(server.getRoleById(419232575261769730L).isPresent()) {
										role2 = server.getRoleById(419232575261769730L).get();
									}

									if(role1 != null && role2 != null) {
										if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || role1.hasUser(user) || role2.hasUser(user))) {
											cmd.addChannel(server, event, argument1);
										} else {
											event.getMessage().addReaction("👎");

											Role role3 = null;
											if(server.getRoleById(262784255816499201L).isPresent()) {
												role3 = server.getRoleById(262784255816499201L).get();
											}
											Role role4 = null;
											if(server.getRoleById(262802994574131200L).isPresent()) {
												role4 = server.getRoleById(262802994574131200L).get();
											}

											if(role3 != null && role4 != null) {
												new MessageBuilder()
													.append("You can only use the `~addChannel` command if you have at least one of the following roles: ")
													.append("`" + role1.getName() + "`").append(", ")
													.append("`" + role2.getName() + "`").append(", ")
													.append("`" + role3.getName() + "`").append(", ")
													.append("`" + role4.getName() + "`").append(".")
													.replyTo(event.getMessageId())
													.send(event.getChannel());
											}
										}
									}
								}
								case "removechannel" -> {
									User user = null;
									if(event.getMessageAuthor().asUser().isPresent()) {
										user = event.getMessageAuthor().asUser().get();
									}
									Role role1 = null;
									if(server.getRoleById(343680704502300672L).isPresent()) {
										role1 = server.getRoleById(343680704502300672L).get();
									}
									Role role2 = null;
									if(server.getRoleById(419232575261769730L).isPresent()) {
										role2 = server.getRoleById(419232575261769730L).get();
									}

									if(role1 != null && role2 != null) {
										if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || role1.hasUser(user) || role2.hasUser(user))) {
											cmd.removeChannel(server, event, argument1);
										} else {
											event.getMessage().addReaction("👎");

											Role role3 = null;
											if(server.getRoleById(262784255816499201L).isPresent()) {
												role3 = server.getRoleById(262784255816499201L).get();
											}
											Role role4 = null;
											if(server.getRoleById(262802994574131200L).isPresent()) {
												role4 = server.getRoleById(262802994574131200L).get();
											}

											if(role3 != null && role4 != null) {
												new MessageBuilder()
													.append("You can only use the `~removeChannel` command if you have at least one of the following roles: ")
													.append("`" + role1.getName() + "`").append(", ")
													.append("`" + role2.getName() + "`").append(", ")
													.append("`" + role3.getName() + "`").append(", ")
													.append("`" + role4.getName() + "`").append(".")
													.replyTo(event.getMessageId())
													.send(event.getChannel());
											}
										}
									}
								}
								case "showroles" -> cmd.showRoles(event);
								case "addrole" -> {
									User user = null;
									if(event.getMessageAuthor().asUser().isPresent()) {
										user = event.getMessageAuthor().asUser().get();
									}
									Role role1 = null;
									if(server.getRoleById(343680704502300672L).isPresent()) {
										role1 = server.getRoleById(343680704502300672L).get();
									}
									Role role2 = null;
									if(server.getRoleById(419232575261769730L).isPresent()) {
										role2 = server.getRoleById(419232575261769730L).get();
									}

									if(role1 != null && role2 != null) {
										if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || role1.hasUser(user) || role2.hasUser(user))) {
											cmd.addRole(server, event, argument1);
										} else {
											event.getMessage().addReaction("👎");

											Role role3 = null;
											if(server.getRoleById(262784255816499201L).isPresent()) {
												role3 = server.getRoleById(262784255816499201L).get();
											}
											Role role4 = null;
											if(server.getRoleById(262802994574131200L).isPresent()) {
												role4 = server.getRoleById(262802994574131200L).get();
											}

											if(role3 != null && role4 != null) {
												new MessageBuilder()
													.append("You can only use the `~addRole` command if you have at least one of the following roles: ")
													.append("`" + role1.getName() + "`").append(", ")
													.append("`" + role2.getName() + "`").append(", ")
													.append("`" + role3.getName() + "`").append(", ")
													.append("`" + role4.getName() + "`").append(".")
													.replyTo(event.getMessageId())
													.send(event.getChannel());
											}
										}
									}
								}
								case "removerole" -> {
									User user = null;
									if(event.getMessageAuthor().asUser().isPresent()) {
										user = event.getMessageAuthor().asUser().get();
									}
									Role role1 = null;
									if(server.getRoleById(343680704502300672L).isPresent()) {
										role1 = server.getRoleById(343680704502300672L).get();
									}
									Role role2 = null;
									if(server.getRoleById(419232575261769730L).isPresent()) {
										role2 = server.getRoleById(419232575261769730L).get();
									}

									if(role1 != null && role2 != null) {
										if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || role1.hasUser(user) || role2.hasUser(user))) {
											cmd.removeRole(server, event, argument1);
										} else {
											event.getMessage().addReaction("👎");

											Role role3 = null;
											if(server.getRoleById(262784255816499201L).isPresent()) {
												role3 = server.getRoleById(262784255816499201L).get();
											}
											Role role4 = null;
											if(server.getRoleById(262802994574131200L).isPresent()) {
												role4 = server.getRoleById(262802994574131200L).get();
											}

											if(role3 != null && role4 != null) {
												new MessageBuilder()
													.append("You can only use the `~removeRole` command if you have at least one of the following roles: ")
													.append("`" + role1.getName() + "`").append(", ")
													.append("`" + role2.getName() + "`").append(", ")
													.append("`" + role3.getName() + "`").append(", ")
													.append("`" + role4.getName() + "`").append(".")
													.replyTo(event.getMessageId())
													.send(event.getChannel());
											}
										}
									}
								}
								case "idhelp" -> cmd.idHelp(api, event);
								case "rpsplay" -> cmd.rockPaperScissorsGame(event, argument1);
								case "rpshighscores" -> cmd.rockPaperScissorsHighscores(api, event);
								case "contracts" -> {
									event.getMessage().addReaction("👀");
									new MessageBuilder().append("Did you mean `~showContracts`?").replyTo(event.getMessageId()).send(event.getChannel());

									event.getMessageAuthor().asUser().get().addMessageCreateListener(mEvent -> {
										if(mEvent.getMessageContent().equalsIgnoreCase("No")) {
											if(api.getCustomEmojiById(929549686682034196L).isPresent()) {
												new MessageBuilder().append("<:limed:929549686682034196>").replyTo(mEvent.getMessageId()).send(event.getChannel());
											} else {
												new MessageBuilder().append("🤡").replyTo(mEvent.getMessageId()).send(event.getChannel());
											}
										}
									}).removeAfter(5, TimeUnit.SECONDS);
								}
							}
						} else if(!Character.isAlphabetic(event.getMessageContent().charAt(0)) && !Character.isDigit(event.getMessageContent().charAt(0))) {
							int commandIndex = -1;
							if(event.getMessageContent().charAt(0) != '~') {
								String[] message = event.getMessageContent().split(" ", 2);
								for(String availableCommand: cmd.getAvailableCommands()) {
									commandIndex++;
									if(availableCommand.equalsIgnoreCase(message[0].substring(1))) {
										event.getMessage().addReaction("👀");
										if(event.getMessageContent().charAt(0) == '`') {
											new MessageBuilder().append("Did you mean `~" + cmd.getAvailableCommands().get(commandIndex) + "`?\nThe prefix for the commands is a `~` and not a `` `.")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										} else {
											new MessageBuilder().append("Did you mean `~" + cmd.getAvailableCommands().get(commandIndex) + "`?\nThe prefix for the commands is a `~` and not a `" + event.getMessageContent().charAt(0) + "`.")
												.replyTo(event.getMessageId())
												.send(event.getChannel());
										}
										break;
									}
								}
							}
						}
					}
				}
			} catch(ArrayIndexOutOfBoundsException | StringIndexOutOfBoundsException | NoSuchElementException e) {
       				logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
       			} catch(Exception e) {
				event.getMessage().addReaction("⚠");
       				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
       			}
		});

		//Gets triggered when someone renames a channel from the server.
       		api.addServerChannelChangeNameListener(event -> {
       			try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateDBChannelsOnChangeName(event);
				}
       			} catch(Exception e) {
       				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
       			}
       		});

        	//Gets triggered when someone deletes a channel from the server.
		 api.addServerChannelDeleteListener(event -> {
        		try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateDBChannelsOnDelete(event);
				}
        		} catch(Exception e) {
        			logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
        		}
        	});

		//Gets triggered when someone renames a role from the server.
       		api.addRoleChangeNameListener(event -> {
       			try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateDBRolesOnChangeName(event);
				}
       			} catch(Exception e) {
       				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
       			}
      	 	});

		//Gets triggered when someone deletes a role from the server.
		api.addRoleDeleteListener(event -> {
			try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateDBRolesOnDelete(event);
				}
			} catch(Exception e) {
				logger.warn("Fatal error occurred!");
				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e.printStackTrace();
			}
		});

   		//Gets triggered when someone joins the server and updates the members' Multimap.
   		api.addServerMemberJoinListener(event -> {
   			try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateMembersOnJoinAndWelcome(event);
				}
			} catch (Exception e) {
				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
       		});

        	//Gets triggered when someone has their nickname changed and updates the members' Multimap.
		api.addUserChangeNicknameListener(event -> {
        		try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateMembersOnNicknameChanged(event);
				}
			} catch (Exception e) {
				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
       		});

        	//Gets triggered when someone is given the 'Guest' role and updates the members' Multimap.
        	api.addUserRoleAddListener(event -> {
        		try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateMembersOnGuestRoleAdded(event);
				}
			} catch (Exception e) {
				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
		});

        	//Gets triggered when someone has the 'Guest' role removed and updates the members' Multimap.
        	api.addUserRoleRemoveListener(event -> {
			try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateMembersOnGuestRoleRemoved(event);
				}
			} catch (Exception e) {
				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
        	});

        	//Gets triggered when someone leaves the server and then notifies the current members about it and updates the members' Multimap.
		api.addServerMemberLeaveListener(event -> {
        		try {
				Server server = event.getServer(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateMembersOnLeave(event);
				}
			} catch (Exception e) {
				logger.warn("Fatal error occurred!");
       				logger.fatal("" + e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
        	});
	}
}
