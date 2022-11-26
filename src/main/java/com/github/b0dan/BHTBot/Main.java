package com.github.b0dan.BHTBot;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.NoSuchElementException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;

public class Main {
	private static final Logger logger = LogManager.getLogger(Main.class); //Creates an instance of the 'Logger' class for 'Main.class'.

	public static void main(String[] args) throws IOException {
		DiscordApi api = new DiscordApiBuilder().setToken("...").setAllIntents().login().join(); //Creates an object from the Discord API.        
		System.out.println(api.getServers() + "\nBHT Bot is online!\n");
		api.updateActivity(ActivityType.LISTENING, "~commandsHelp"); //Updates the activity to "Listening to ~commandsHelp".

		Commands cmd = new Commands(); //Creates an object from the 'Commands' class.

		//Different commands and permissions on who can use them.
		api.addMessageCreateListener(event -> { //Head Shan Chu(262802994574131200L), Shan Chu(262784255816499201L), Tech Manager(343680704502300672L), Crew Leader(419232575261769730L), Luminous Path(336588534351790080L).
       			try {
       				Server server = api.getServerById(event.getServerTextChannel().get().getServer().getId()).get(); //Gets the server.

       				if(server.getId() == 262781891705307137L) {
       					if(event.getMessageContent().equalsIgnoreCase("~commandsHelp")) {
           					cmd.displayCommands(api, event);
					} else if(event.getMessageContent().equals("~updateMembers") && (event.getMessageAuthor().isBotOwner() || event.getMessageAuthor().isServerAdmin()) && event.getChannel().equals(api.getServerById(event.getServerTextChannel().get().getServer().getId()).get().getSystemChannel().get())) {
           					cmd.manuallyUpdateMembers(api, event);
           				} else if(event.getMessageContent().equals("~getAllMembers") && event.getMessageAuthor().isBotOwner() && event.getChannel().equals(api.getServerById(event.getServerTextChannel().get().getServer().getId()).get().getSystemChannel().get())) {
           					cmd.getAllMembers(api, event);
           				} else if(event.getMessageContent().equalsIgnoreCase("~rpsHelp")) {
           					cmd.rockPaperScissorsHelp(api, event);
           				} else if(event.getMessageContent().equalsIgnoreCase("~idHelp")) {
           					cmd.idHelp(api, event);
           				} else if(event.getMessageContent().substring(0, 8).equalsIgnoreCase("~rpsPlay")) {
           					cmd.rockPaperScissorsGame(api, event);
           				} else if(event.getMessageContent().equalsIgnoreCase("~rpsHighscores")) {
						cmd.rockPaperScissorsHighscores(api, event);
					} else if(event.getMessageContent().equalsIgnoreCase("~contractsHelp")) {
           					cmd.contractsHelp(api, event);
					} else if(event.getMessageContent().equalsIgnoreCase("~showContracts")) {
						cmd.showContracts(api, event, 1, 1, 0, 0, false);
					} else if(event.getMessageContent().equalsIgnoreCase("~showChannels")) {
						cmd.showChannels(api, event);
					} else if(event.getMessageContent().equalsIgnoreCase("~showRoles")) {
						cmd.showRoles(api, event);
           				} else if(event.getMessageContent().substring(0, 8).equalsIgnoreCase("~addRole")) {
           					if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || api.getRoleById(343680704502300672L).get().hasUser(event.getMessageAuthor().asUser().get()) || (api.getRoleById(419232575261769730L).get().hasUser(event.getMessageAuthor().asUser().get()) && api.getRoleById(336588534351790080L).get().hasUser(event.getMessageAuthor().asUser().get())))) {
           						cmd.addRole(api, event);
           					} else {
           						event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
           						new MessageBuilder()
           							.append("You can only use the `~addRole` command if you have at least one of the following roles: ")
           							.append("`" + api.getRoleById(262802994574131200L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(262784255816499201L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(343680704502300672L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(419232575261769730L).get().getName() + " + " + api.getRoleById(336588534351790080L).get().getName() + "`")
           							.append(".")
           							.replyTo(event.getMessageId())
    								.send(event.getChannel());
           					}
           				} else if(event.getMessageContent().substring(0, 11).equalsIgnoreCase("~removeRole")) {
           					if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || api.getRoleById(343680704502300672L).get().hasUser(event.getMessageAuthor().asUser().get()) || (api.getRoleById(419232575261769730L).get().hasUser(event.getMessageAuthor().asUser().get()) && api.getRoleById(336588534351790080L).get().hasUser(event.getMessageAuthor().asUser().get())))) {
           						cmd.removeRole(api, event);
           					} else {
           						event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
           						new MessageBuilder()
           							.append("You can only use the `~removeRole` command if you have at least one of the following roles: ")
           							.append("`" + api.getRoleById(262802994574131200L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(262784255816499201L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(343680704502300672L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(419232575261769730L).get().getName() + " + " + api.getRoleById(336588534351790080L).get().getName() + "`")
           							.append(".")
           							.replyTo(event.getMessageId())
    								.send(event.getChannel());
           					}
           				} else if(event.getMessageContent().substring(0, 11).equalsIgnoreCase("~addChannel")) {
           					if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || api.getRoleById(343680704502300672L).get().hasUser(event.getMessageAuthor().asUser().get()) || (api.getRoleById(419232575261769730L).get().hasUser(event.getMessageAuthor().asUser().get()) && api.getRoleById(336588534351790080L).get().hasUser(event.getMessageAuthor().asUser().get())))) {
           						cmd.addChannel(api, event);
           					} else {
           						event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
           						new MessageBuilder()
           							.append("You can only use the `~addChannel` command if you have at least one of the following roles: ")
           							.append("`" + api.getRoleById(262802994574131200L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(262784255816499201L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(343680704502300672L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(419232575261769730L).get().getName() + " + " + api.getRoleById(336588534351790080L).get().getName() + "`")
           							.append(".")
           							.replyTo(event.getMessageId())
    								.send(event.getChannel());
           					}
           				} else if(event.getMessageContent().substring(0, 12).equalsIgnoreCase("~addContract")) {
           					//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
        	        			Class.forName("com.mysql.cj.jdbc.Driver");
        	        			Connection connection = DriverManager.getConnection("...");

						//Creates a 'SELECT' SQL statement.
						Statement statement = connection.createStatement();

						//Checks if the user issuing the command is in the correct channel.
						ResultSet resultSet = statement.executeQuery("SELECT channelName FROM Channels");
						boolean channel = false;
						while(resultSet.next() && channel == false) {
							if(resultSet.getString(1).equals(event.getChannel().asServerTextChannel().get().getName())) {
								channel = true;
							}
						}

        					//Checks if the user issuing the command has at least one of the required roles.
        					resultSet = statement.executeQuery("SELECT roleId FROM Roles");
        					boolean role = false;
        					while(resultSet.next() && role == false) {
        						if(api.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
        							role = true;
        						}
        					}

        					//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
        	        			if(channel == true && role == true) {
        	        				//Closes the connections.
        	        				resultSet.close();
        	        				statement.close();
        	        				connection.close();

        	        				cmd.manuallyAddContractToDatabase(event);
        	        			} else if((channel == true && role == false) && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner())) {
        	        				//Closes the connections.
        	        				resultSet.close();
        	        				statement.close();
        	        				connection.close();

        	        				cmd.manuallyAddContractToDatabase(event);
        	        			} else if(channel == false) {
        	        				event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

        	        				//Prints out the channels where the command can be used.
        	        				ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
        	        				MessageBuilder channels = new MessageBuilder().append("The command `~addContract` can only be used in the following channels: ");

        	        				if(resultSet0.next()) {
        	        					channels.append(api.getServerTextChannelById(resultSet0.getLong(1)).get().getMentionTag());
        	        				}
        	        				while(resultSet0.next()) {
        	        					channels
        	    							.append(", ")
        	    							.append(api.getServerTextChannelById(resultSet0.getLong(1)).get().getMentionTag());
        	        				}
        	        				channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

        	        				//Closes the connections.
							resultSet0.close();
							statement.close();
							connection.close();
        	        			} else if(role == false) {
        	        				event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

        	        				//Prints out the roles who can use the command.
        	        				ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
        	        				MessageBuilder roles = new MessageBuilder().append("You can only use the `~addContract` command if you have at least one of the following roles: ");

        	        				if(resultSet0.next()) {
        	        					roles.append("`" + api.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
        	        				}
        	        				while(resultSet0.next()) {
        	        					roles
        	    							.append(", ")
        	    							.append("`" + api.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
        	        				}
        	        				roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

        	        				//Closes the connections.
							resultSet0.close();
							statement.close();
							connection.close();
        	        			}
           				} else if(event.getMessageContent().substring(0, 14).equalsIgnoreCase("~removeChannel")) {
           					if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner() || api.getRoleById(343680704502300672L).get().hasUser(event.getMessageAuthor().asUser().get()) || (api.getRoleById(419232575261769730L).get().hasUser(event.getMessageAuthor().asUser().get()) && api.getRoleById(336588534351790080L).get().hasUser(event.getMessageAuthor().asUser().get())))) {
           						cmd.removeChannel(api, event);
           					} else {
           						event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
           						new MessageBuilder()
           							.append("You can only use the `~removeChannel` command if you have at least one of the following roles: ")
           							.append("`" + api.getRoleById(262802994574131200L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(262784255816499201L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(343680704502300672L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(419232575261769730L).get().getName() + " + " + api.getRoleById(336588534351790080L).get().getName() + "`")
           							.append(".")
           							.replyTo(event.getMessageId())
    								.send(event.getChannel());
           					}
           				} else if(event.getMessageContent().substring(0, 15).equalsIgnoreCase("~updateContract")) {
           					//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
						Class.forName("com.mysql.cj.jdbc.Driver");
        	        			Connection connection = DriverManager.getConnection("...");

						//Creates a 'SELECT' SQL statement.
						Statement statement = connection.createStatement();

						//Checks if the user issuing the command is in the correct channel.
						ResultSet resultSet = statement.executeQuery("SELECT channelName FROM Channels");
						boolean channel = false;
						while(resultSet.next() && channel == false) {
							if(resultSet.getString(1).equals(event.getChannel().asServerTextChannel().get().getName())) {
								channel = true;
							}
						}

						//Checks if the user issuing the command has at least one of the required roles.
						resultSet = statement.executeQuery("SELECT roleId FROM Roles");
						boolean role = false;
						while(resultSet.next() && role == false) {
							if(api.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
								role = true;
							}
						}

        					//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
        	        			if(channel == true && role == true) {
        	        				//Closes the connections.
        	        				resultSet.close();
        	        				statement.close();
        	        				connection.close();

        	        				cmd.updateContractInDatabase(event);
        	        			} else if((channel == true && role == false) && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner())) {
        	        				//Closes the connections.
        	        				resultSet.close();
        	        				statement.close();
        	        				connection.close();

        	        				cmd.updateContractInDatabase(event);
        	        			} else if(channel == false) {
        	        				event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

        	        				//Prints out the channels where the command can be used.
        	        				ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
        	        				MessageBuilder channels = new MessageBuilder().append("The command `~updateContract` can only be used in the following channels: ");

        	        				if(resultSet0.next()) {
        	        					channels.append(api.getServerTextChannelById(resultSet0.getLong(1)).get().getMentionTag());
        	        				}
        	        				while(resultSet0.next()) {
        	        					channels
        	    							.append(", ")
        	    							.append(api.getServerTextChannelById(resultSet0.getLong(1)).get().getMentionTag());
        	        				}
        	        				channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

        	        				//Closes the connections.
							resultSet0.close();
							statement.close();
							connection.close();
        	        			} else if(role == false) {
        	        				event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

        	        				//Prints out the roles who can use the command.
							ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
							MessageBuilder roles = new MessageBuilder().append("You can only use the `~updateContract` command if you have at least one of the following roles: ");

							if(resultSet0.next()) {
								roles.append("`" + api.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
							}
							while(resultSet0.next()) {
								roles
									.append(", ")
									.append("`" + api.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
							}
							roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

        	        				//Closes the connections.
							resultSet0.close();
							statement.close();
							connection.close();
        	        			}
           				} else if(event.getMessageContent().substring(0, 15).equals("~removeContract")) {
						//Opens up a connection to the 'BHT' SQL database (Channels, Roles).
						Class.forName("com.mysql.cj.jdbc.Driver");
						Connection connection = DriverManager.getConnection("...");

						//Creates a 'SELECT' SQL statement.
						Statement statement = connection.createStatement();

						//Checks if the user issuing the command is in the correct channel.
						ResultSet resultSet = statement.executeQuery("SELECT channelName FROM Channels");
						boolean channel = false;
						while(resultSet.next() && channel == false) {
							if(resultSet.getString(1).equals(event.getChannel().asServerTextChannel().get().getName())) {
								channel = true;
							}
						}

						//Checks if the user issuing the command has at least one of the required roles.
						resultSet = statement.executeQuery("SELECT roleId FROM Roles");
						boolean role = false;
						while(resultSet.next() && role == false) {
							if(api.getRoleById(resultSet.getLong(1)).get().hasUser(event.getMessageAuthor().asUser().get())) {
								role = true;
							}
						}

        					//If both conditions are met, the commands gets issued. If not, it notifies the user what's missing.
        	        			if(channel == true && role == true) {
        	        				//Closes the connections.
        	        				resultSet.close();
        	        				statement.close();
        	        				connection.close();

        	        				cmd.removeContractFromDatabase(event);
        	        			} else if((channel == true && role == false) && (event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner())) {
        	        				//Closes the connections.
							resultSet.close();
							statement.close();
							connection.close();

        	        				cmd.removeContractFromDatabase(event);
        	        			} else if(channel == false) {
        	        				event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

							//Prints out the channels where the command can be used.
							ResultSet resultSet0 = statement.executeQuery("SELECT channelId FROM Channels");
							MessageBuilder channels = new MessageBuilder().append("The command `~removeContract` can only be used in the following channels: ");

							if(resultSet0.next()) {
								channels.append(api.getServerTextChannelById(resultSet0.getLong(1)).get().getMentionTag());
							}
							while(resultSet0.next()) {
								channels
									.append(", ")
									.append(api.getServerTextChannelById(resultSet0.getLong(1)).get().getMentionTag());
							}
							channels.append(".").replyTo(event.getMessageId()).send(event.getChannel());

        	        				//Closes the connections.
							resultSet0.close();
							statement.close();
							connection.close();
        	        			} else if(role == false) {
							event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

							//Prints out the roles who can use the command.
							ResultSet resultSet0 = statement.executeQuery("SELECT roleId FROM Roles");
							MessageBuilder roles = new MessageBuilder().append("You can only use the `~removeContract` command if you have at least one of the following roles: ");

							if(resultSet0.next()) {
								roles.append("`" + api.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
							}
							while(resultSet0.next()) {
								roles
									.append(", ")
									.append("`" + api.getRoleById(resultSet0.getLong(1)).get().getName() + "`");
							}
							roles.append(".").replyTo(event.getMessageId()).send(event.getChannel());

        	        				//Closes the connections.
							resultSet0.close();
							statement.close();
							connection.close();
        	        			}
           				} else if(event.getMessageContent().substring(0, 15).equalsIgnoreCase("~setOnLeaveRole")) {
           					if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner())) {
           						cmd.setOnLeaveRole(api, event);
           					} else {
							event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
           						new MessageBuilder()
           							.append("You can only use the `~setOnLeavePing` command if you have at least one of the following roles: ")
           							.append("`" + api.getRoleById(262784255816499201L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(262802994574131200L).get().getName() + "`")
           							.append(".")
           							.replyTo(event.getMessageId())
           							.send(event.getChannel());
						}
           				} else if(event.getMessageContent().substring(0, 15).equalsIgnoreCase("~setOnLeavePing")) {
           					if((event.getMessageAuthor().isServerAdmin() || event.getMessageAuthor().isBotOwner())) {
           						if(event.getChannel().equals(api.getServerById(event.getServerTextChannel().get().getServer().getId()).get().getSystemChannel().get())) {
           							cmd.setOnLeavePing(event);
           						} else {
           							event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
           							new MessageBuilder()
       									.append("The command `~setOnLeavePing` can only be used in: ")
       									.append(api.getServerById(event.getServerTextChannel().get().getServer().getId()).get().getSystemChannel().get())
       									.append(".")
       									.replyTo(event.getMessageId())
       									.send(event.getChannel());
           						}
           					} else {
           						event.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
           						new MessageBuilder()
           							.append("You can only use the `~setOnLeavePing` command if you have at least one of the following roles: ")
           							.append("`" + api.getRoleById(262784255816499201L).get().getName() + "`")
           							.append(", ")
           							.append("`" + api.getRoleById(262802994574131200L).get().getName() + "`")
           							.append(".")
           							.replyTo(event.getMessageId())
           							.send(event.getChannel());
           					}
           				}
       				}
       			} catch(StringIndexOutOfBoundsException e) {
       				logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
       			} catch(NoSuchElementException e) {
       				logger.error("Expected/Handled: " + e + " -> (" + e.getCause() + ")"); //Sends an error log about an expected/handled error.
       			} catch(Exception e) {
				logger.warn("Fatal error occured!");
       				logger.fatal("", e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
				e.printStackTrace();
       			}
		});

   		//Gets triggered when someone joins the server and updates the members' Multimap.
   		api.addServerMemberJoinListener(event -> {
			try {
   				Server server = api.getServerById(event.getServer().getId()).get(); //Gets the server.

   				if(server.getId() == 262781891705307137L) {
   					cmd.updateMembersOnJoinAndWelcome(api, event);
   				}
			} catch(Exception e) {
				logger.warn("Fatal error occured!");
       				logger.fatal("", e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
       		});

        	//Gets triggered when someone has their nickname changed and updates the members' Multimap.
       		api.addUserChangeNicknameListener(event -> {
			try {
        			Server server = api.getServerById(event.getServer().getId()).get(); //Gets the server.

   				if(server.getId() == 262781891705307137L) {
   					cmd.updateMembersOnNicknameChanged(api, event);
   				}
			} catch(Exception e) {
				logger.warn("Fatal error occured!");
       				logger.fatal("", e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
       		});

       		//Gets triggered when someone leaves the server and then notifies the current members about it and updates the members' Multimap.
        	api.addServerMemberLeaveListener(event -> {
			try {
        			Server server = api.getServerById(event.getServer().getId()).get(); //Gets the server.

   				if(server.getId() == 262781891705307137L) {
   					cmd.updateMembersOnLeave(api, event);
   				}
			} catch(Exception e) {
				logger.warn("Fatal error occured!");
       				logger.fatal("", e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
        	});

		//Gets triggered when someone is given the 'Guest' role and updates the members' Multimap.
		api.addUserRoleAddListener(event -> {
			try {
        			Server server = api.getServerById(event.getServer().getId()).get(); //Gets the server.

        			if(server.getId() == 262781891705307137L) {
					cmd.updateMembersOnGuestRoleAdded(api, event);
				}
			} catch(Exception e) {
				logger.warn("Fatal error occured!");
       				logger.fatal("", e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
		});

		//Gets triggered when someone has the 'Guest' role removed and updates the members' Multimap.
		api.addUserRoleRemoveListener(event -> {
			try {
				Server server = api.getServerById(event.getServer().getId()).get(); //Gets the server.

				if(server.getId() == 262781891705307137L) {
					cmd.updateMembersOnGuestRoleRemoved(api, event);
				}
			} catch(Exception e) {
				logger.warn("Fatal error occured!");
	       			logger.fatal("", e + " -> (" + e.getCause() + ")"); //Sends a fatal log about an unhandled error.
       				e.printStackTrace();
			}
		});
	}
}
