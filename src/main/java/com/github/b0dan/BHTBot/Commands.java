package com.github.b0dan.BHTBot;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.lang.ArrayIndexOutOfBoundsException;
import org.apache.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.message.mention.AllowedMentions;
import org.javacord.api.entity.message.mention.AllowedMentionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.event.user.UserChangeNicknameEvent;

public class Commands implements BotInterface {
	private long roleID = 336588534351790080L; //The role value for the 'onLeaveRole' from the `~setOnLeaveRole` command (default: Luminous Path -> 336588534351790080L).
	private int pingable = 1; //The ping value for the 'onLeaveMessage' from the `~setOnLeavePing` command: 0 = disabled, 1 = enabled (default).

	private List<Integer> firstPages = new ArrayList<Integer>(); //A list with all the "first pages" from the `~showContracts` command.
	private Map<String, String> allMembers = new HashMap<String, String>(); //A HashMap containing all the members' display names with their discord tag as a key.

	//A command that displays all available commands by typing `~commandsHelp` (not case-sensitive).
	public void displayCommands(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with all the commands.
			EmbedBuilder commands = new EmbedBuilder()
				.setTitle("Available Commands")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.addField("~commandsHelp", "Displays all available commands.")
				.addField("~rpsHelp", "Displays all the info needed to use the `~rpsPlay` (rock, paper, scissors) command.")
				.addField("~contractsHelp", "Displays all contract related commands.")
				.addField("~setOnLeaveRole *[Role ID]*", "Sets which role to be pinged when someone leaves the server.")
				.addInlineField("~setOnLeavePing *0*", "Disables the `onLeave` ping.")
				.addInlineField("~setOnLeavePing *1*", "Enables the `onLeave` ping.")
				.addField("~sourceCode", "Sends a link to the source code.")
				.setFooter("The commands are not case-sensitive!");
			mEvent.getChannel().sendMessage(commands);

			System.out.println("Command (~commandsHelp) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command to manually update the members' HashMap by typing `~updateMembers` (not case-sensitive).
	public void updateMembers(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			Server server = dApi.getServerById(mEvent.getServerTextChannel().get().getServer().getId()).get(); //Gets the server.
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//Clear the members' HashMap if the server is not empty.
			if(!allMembers.isEmpty()) {
				allMembers.clear();
			}

			//Fills out the members' HashMap.
			for(User user: server.getMembers()) {
				allMembers.put(user.getDiscriminatedName(), user.getDisplayName(server));
			}
			System.out.println("Members updated by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command to print out the member's HashMap by typing `~getAllMembers` (not case-sensitive). 
	public void getAllMembers(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//If the server is empty, prints a corresponding message. If not, it prints out all the members in the server.
			if(allMembers.isEmpty()) {
				System.out.println("The server has no members.\n");
			} else {
				System.out.println();
				for(Map.Entry<String, String> entry: allMembers.entrySet()) {
					System.out.println(entry);
				}
				System.out.println("Members: " + allMembers.size() + "\n");
				System.out.println("Command (~getAllMembers) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command to set what the `onLeave` role will be by typing `~setOnLeaveRole` (not case-sensitive).
	public void setOnLeaveRole(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			//Gets the number after the `~setOnLeaveRole` command and changes the role value if that role exists, also reacts on the message.
			if(dApi.getRoles().contains(dApi.getRoleById(Long.valueOf(mEvent.getMessageContent().substring(16, mEvent.getMessageContent().length()))).get())) {
				roleID = Long.valueOf(mEvent.getMessageContent().substring(16, mEvent.getMessageContent().length()));
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
				System.out.println("Command (~setOnLeaveRole) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + roleID + "."); //Sends a system message about who issued the command.
			}
		} catch(NumberFormatException | StringIndexOutOfBoundsException e1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The ID must be a digit of type `Long`. Use `~idHelp` for more information on how to get the ID.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e0_0) {
				e0_0.printStackTrace();
			}
		} catch(NoSuchElementException e) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! The Role ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e0_1) {
				e0_1.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command to enable or disable the `onLeave` ping by typing `~setOnLeavePing` (not case-sensitive).
	public void setOnLeavePing(MessageCreateEvent mEvent) {
		try {
			//Gets the number after the `~setOnLeavePing` command, gives an error if it isn't 0 or 1 or changes the ping value if it is, also reacts on the message.
			if(Integer.valueOf(mEvent.getMessageContent().substring(16, mEvent.getMessageContent().length())) != 0 && Integer.valueOf(mEvent.getMessageContent().substring(16, mEvent.getMessageContent().length())) != 1) {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The number should be either 0 or 1.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} else {
				pingable = Integer.valueOf(mEvent.getMessageContent().substring(16, mEvent.getMessageContent().length()));
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
				System.out.println("Command (~setOnLeavePing) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + pingable + "."); //Sends a system message about who issued the command.
			}
		} catch(NumberFormatException | StringIndexOutOfBoundsException e1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The number should be either 0 or 1.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} catch(Exception e1) {
			e1.printStackTrace();
		}
	}

	//A listener that automatically updates the members' HashMap when someone joins the server.
	public void updateMembersOnJoin(DiscordApi dApi, ServerMemberJoinEvent jEvent) {
		try {
			Server server = dApi.getServerById(jEvent.getServer().getId()).get(); //Gets the server.

			//Puts the user who joined the server in the members' HashMap.
			allMembers.put(jEvent.getUser().getDiscriminatedName(), jEvent.getUser().getDisplayName(server));
    		System.out.println("Members updated due to " + jEvent.getUser().getDiscriminatedName() + " joining the server."); //Sends a system message about what issued the listener.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the members' HashMap when someone has their nickname changed.
	public void updateMembersOnNicknameChanged(DiscordApi dApi, UserChangeNicknameEvent nEvent) {
		try {
			Server server = dApi.getServerById(nEvent.getServer().getId()).get(); //Gets the server.

			//Changes the name in the members' HashMap of the person that just had their nickname changed/updated.
			allMembers.replace(nEvent.getUser().getDiscriminatedName(), nEvent.getUser().getDisplayName(server));
			System.out.println("Members updated due to " + nEvent.getUser().getDiscriminatedName() + " having their nickname changed - value: " + allMembers.get(nEvent.getUser().getDiscriminatedName()) + "."); //Sends a system message about what issued the listener.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A listener that automatically updates the members' HashMap when someone leaves the server, sends a message about it and adds the person to the 'Contracts' SQL database.
	public void updateMembersOnLeave(DiscordApi dApi, ServerMemberLeaveEvent lEvent) {
		try {
			Server server = dApi.getServerById(lEvent.getServer().getId()).get(); //Gets the server.
			AllowedMentions allowedMentions = new AllowedMentionsBuilder().addRole(roleID).setMentionRoles(true).build(); //Allows the `onLeave` role to be mentioned.

			//Sends a message and notifies the current members when someone leaves the server.
			MessageBuilder onLeaveMessage = new MessageBuilder();
			onLeaveMessage
				.append(allMembers.get(lEvent.getUser().getDiscriminatedName()) + " (" + lEvent.getUser().getDiscriminatedName() + ") ", MessageDecoration.BOLD)
				.append("just left ")
				.append("The Black Hand Triads", MessageDecoration.BOLD)
				.append(". Looks like loyalty wasn't one of their virtues. Hunt that sssnake down! ");

			//Checks if the ping value is 1 and notifies the `onLeave` role if yes, then sends the message.
			if(pingable == 1) {
				onLeaveMessage
					.append(server.getRoleById(roleID).get().getMentionTag())
					.setAllowedMentions(allowedMentions);
			}
			onLeaveMessage.send((TextChannel)server.getSystemChannel().get());

			//Checks if the name of the person who had left the server looks like a real name and adds it to the 'Contracts' SQL database if yes.
			if(allMembers.get(lEvent.getUser().getDiscriminatedName()).matches("^[A-Z](?=.{2,19}$)[A-Za-z.]+(?:\\h+[A-Z][A-Za-z.]+)+$")) {
				//Opens up a connection to the 'BHT' SQL database (Contracts).
	        	Class.forName("com.mysql.cj.jdbc.Driver");
	        	Connection connection = DriverManager.getConnection("...");

				//Adds the person who had left the server to the 'BHT' SQL database (Contracts) if he's not there already.
				PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(contractName) FROM Contracts WHERE contractName = ?");
				preparedStatement0.setString(1, allMembers.get(lEvent.getUser().getDiscriminatedName()));
				ResultSet resultSet = preparedStatement0.executeQuery();
				resultSet.next();
	        	if(resultSet.getInt(1) == 0) {
	        		resultSet.close();
	        		preparedStatement0.close();

	        		PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Contracts(contractName) VALUES(?)");
					preparedStatement.setString(1, allMembers.get(lEvent.getUser().getDiscriminatedName()));
					int affectedRows = preparedStatement.executeUpdate();
					if(affectedRows > 0) {
						System.out.println(allMembers.get(lEvent.getUser().getDiscriminatedName()) + " has been successfuly added to the database.");
						server.getSystemChannel().get().getMessages(1).get().getNewestMessage().get().addReaction("🐍"); //Reacts with a snake emoji to the leaving message if the person was successfully added to the 'Contracts' SQL database.

						//Closes the connections.
						preparedStatement.close();
						connection.close();
					} else {
						System.out.println(allMembers.get(lEvent.getUser().getDiscriminatedName()) + " can not be added to the database!");
					}
				} else {
					resultSet.close();
					preparedStatement0.close();
					connection.close();
				}
	        }
			//Removes the person who had left the server from the members' HashMap.
			allMembers.remove(lEvent.getUser().getDiscriminatedName());
			System.out.println("Members updated due to " + lEvent.getUser().getDiscriminatedName() + " leaving the server."); //Sends a system message about what issued the listener.
		} catch(NullPointerException e) {
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that displays all the contract related commands by typing `~contractsHelp`(not case-sensitive).
	public void contractsHelp(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with all the contract related commands.
			EmbedBuilder contractsCommands = new EmbedBuilder()
				.setTitle("Contracts Commands")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.addField("~showContracts", "Shows a list of all active contracts.")
				.addField("~addContract *[Full Name]*", "Adds a new contract.")
				.addField("~updateContract *[ID]* *[Full Name]*", "Updates the contract with the specified ID.")
				.addField("~removeContract *[ID]*", "Removes the contract with the specified ID.")
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

			System.out.println("Command (~contractsHelp) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that displays all active contracts by typing `~showContracts` (not case-sensitive).
	public void showContracts(DiscordApi dApi, MessageCreateEvent mEvent, int currentPage, int currentContractNumber, int firstContractOnPage, int lastContractOnPage, boolean pageBack) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//Opens up a connection to the 'BHT' SQL database (Contracts).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

        	//Creates a 'SELECT' SQL statement.
        	Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        	ResultSet resultSet = statement.executeQuery("SELECT * FROM Contracts");

        	//An embed with all active contracts.
			EmbedBuilder contracts = new EmbedBuilder();
			contracts
				.setTitle("Active Contracts")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.setFooter("Make sure to remove the contract you've completed by typing `~removeContract [ID]`!");

			//If there are any contracts, finds the total amount of pages needed to group the contracts. If not, notifies the user.
			resultSet.last();
			if(!(resultSet.getRow() <= 0)) {
				final int totalContracts = resultSet.getRow();
				int tempTotalPages = (totalContracts / 25) + 1;
				if(totalContracts % 25 == 0) {
					tempTotalPages = tempTotalPages - 1;
				}
				final int totalPages = tempTotalPages;

				//Adds the contracts to the above mentioned embed.
				if(totalPages == 1 || currentPage == 1) {
					currentContractNumber = 1;
					resultSet.beforeFirst();
				} else if(pageBack == true) {
					resultSet.absolute(firstPages.get(currentPage - 1) - 1);
					currentContractNumber = resultSet.getRow() + 1;
				} else if(totalPages > 1 && currentPage > 1) {
					resultSet.absolute(currentContractNumber - 1);
				}
				boolean freshPage = true;
				while(resultSet.next()) {
					if(freshPage == true) {
						firstContractOnPage = resultSet.getRow();
						freshPage = false;
						if(!firstPages.contains(firstContractOnPage)) {
							firstPages.add(firstContractOnPage);
						}
					}
	       			contracts.addField(String.valueOf(resultSet.getInt("contractId") + ". " + resultSet.getString("contractName")), "*Contract #" + (currentContractNumber) + "*");

	       			currentContractNumber++;
	       			if(resultSet.getRow() % 25 == 0 || resultSet.getRow() == totalContracts) {
	       				lastContractOnPage = resultSet.getRow();
	       				break;
	       			}
	        	}
	        	mEvent.getChannel().sendMessage(contracts);

	        	//Adds the reactions needed to "flip a page".
	        	if(currentPage == 1 && totalPages > 1) {
	        		mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("➡");
	        	} else if(currentPage == totalPages && totalPages > 1) {
	        		mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("⬅");
	        	} else if(currentPage > 1 && currentPage < totalPages && totalPages > 1) {
	        		mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("⬅");
	        		mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("➡");
	        	}

	        	//Adds a listener that "flips a page" when the one who called the command reacts on the `~showContracts` message.
	        	if(totalPages > 1) {
	        		int ccn = currentContractNumber;
	        		int fcop = firstContractOnPage;
	        		int lcop = lastContractOnPage;
	        		mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReactionAddListener(event -> {
	        			if(event.getUserId() == mEvent.getMessageAuthor().getId()) {
	        				int cp = currentPage;
	        				boolean pressedBack = false;;
	            			if((currentPage < totalPages) && event.getEmoji().equalsEmoji("➡")) {
	            				cp = cp + 1;
	            			} else if((currentPage > 1) && event.getEmoji().equalsEmoji("⬅")) {
	            				pressedBack = true;
	            				cp = cp - 1;
	            			}
	            			event.getMessage().get().delete();
	            			showContracts(dApi, mEvent, cp, ccn, fcop, lcop, pressedBack);
	        			}
	            	}).removeAfter(30, TimeUnit.SECONDS);
	        	}
			} else {
				contracts.addField("Empty", "There are no active contracts.");
				mEvent.getChannel().sendMessage(contracts);
			}

			//Closes the connections.
			resultSet.close();
			statement.close();
			connection.close();

			System.out.println("Command/Page (~showContracts) called/flipped by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command/flipped a page.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command to manually add a contract to the 'Contracts' SQL database by typing `~addContract` (not case-sensitive).
	public void manuallyAddContractToDatabase(MessageCreateEvent mEvent) {
		try {
			//Checks if the name after the `~addContract` command resembles an actual name.
			if(mEvent.getMessageContent().substring(13).matches("^[A-Z](?=.{2,19}$)[A-Za-z.]+(?:\\h+[A-Z][A-Za-z.]+)+$")) {
				//Opens up a connection to the 'BHT' SQL database (Contracts).
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection("...");

				//Adds the person after the `~addContract` command to the 'BHT' SQL database (Contracts) if he's not there already.
				PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(contractName) FROM Contracts WHERE contractName = ?");
				preparedStatement0.setString(1, mEvent.getMessageContent().substring(13));
				ResultSet resultSet = preparedStatement0.executeQuery();
				resultSet.next();
				if(resultSet.getInt(1) == 0) {
					PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Contracts(contractName) VALUES(?)");
					preparedStatement.setString(1, mEvent.getMessageContent().substring(13));
					int affectedRows = preparedStatement.executeUpdate();
					if(affectedRows > 0) {
						mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
						new MessageBuilder().append("A contract has been successfully placed on `" + mEvent.getMessageContent().substring(13) + "`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
						System.out.println("Command (~addContract) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + mEvent.getMessageContent().substring(13) + "."); //Sends a system message about who issued the command.

						//Closes the connections.
						preparedStatement.close();
						connection.close();
					} else {
						mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

						//Closes the connections.
						preparedStatement.close();
						connection.close();
					}
				} else {
					mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
					new MessageBuilder().append("Error! There is already an active contract with that name.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					//Closes the connections.
					resultSet.close();
					preparedStatement0.close();
					connection.close();
				}
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! That is not a valid name.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
		} catch(StringIndexOutOfBoundsException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! That is not a valid name.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(NullPointerException e) {
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command to manually update a contract from the 'Contracts' SQL database by typing `~updateContract` (not case-sensitive).
	public void updateContractInDatabase(MessageCreateEvent mEvent) {
		try {
			String[] commandValues = mEvent.getMessage().getContent().split(" ", 3); //Splits the command in 3 parts(command, id, name).

			Integer.parseInt(commandValues[1]);
			if(commandValues[2].matches("^[A-Z](?=.{2,19}$)[A-Za-z.]+(?:\\h+[A-Z][A-Za-z.]+)+$")) {
				//Opens up a connection to the 'BHT' SQL database (Contracts).
				Class.forName("com.mysql.cj.jdbc.Driver");
				Connection connection = DriverManager.getConnection("...");

				//Updates the contract with the ID after the `~updateContract` command from the 'Contracts' SQL database if he's not there already.
				PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(contractName) FROM Contracts WHERE contractName = ?");
				preparedStatement0.setString(1, commandValues[2]);
				ResultSet resultSet = preparedStatement0.executeQuery();
				resultSet.next();
				if(resultSet.getInt(1) == 0) {
					PreparedStatement preparedStatement = connection.prepareStatement("UPDATE Contracts SET contractName = ? WHERE contractId = ?");
					preparedStatement.setString(1, commandValues[2]);
					preparedStatement.setInt(2, Integer.parseInt(commandValues[1]));
					int affectedRows = preparedStatement.executeUpdate();
					if(affectedRows > 0) {
						mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
						new MessageBuilder().append("A contract with an ID of `" + commandValues[1] + "` has been successfully updated.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
						System.out.println("Command (~updateContract) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + commandValues[1] + "(" + commandValues[2] + ")."); //Sends a system message about who issued the command.

						//Closes the connections.
						preparedStatement.close();
						connection.close();
					} else {
						mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
						new MessageBuilder().append("Error! The ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

						//Closes the connections.
						preparedStatement.close();
						connection.close();
					}
				} else {
					mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
					new MessageBuilder().append("Error! There is already an active contract with that name.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

					//Closes the connections.
					resultSet.close();
					preparedStatement0.close();
					connection.close();
				}
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! That is not a valid name.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
		} catch(ArrayIndexOutOfBoundsException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! Please, follow the format: `~updateContract [ID] [Full Name]`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(NumberFormatException e2_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~updateContract` command must be a digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2_2) {
				e2_2.printStackTrace();
			}
		} catch(NullPointerException e) {
		} catch(Exception e) {
			e.printStackTrace();
		}
	} 

	//A command to manually remove a contract from the 'Contracts' SQL database by typing `~removeContract` (not case-sensitive).
	public void removeContractFromDatabase(MessageCreateEvent mEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Contracts).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

			//Removes the contract with the ID after the `~removeContract` command from the 'Contracts' SQL database.
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Contracts WHERE contractId = ?");
			preparedStatement.setInt(1, Integer.valueOf(mEvent.getMessageContent().substring(16)));
			int affectedRows = preparedStatement.executeUpdate();
			if(affectedRows > 0) {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
				new MessageBuilder().append("A contract with an ID of `" + mEvent.getMessageContent().substring(16) + "` has been successfully removed.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
				System.out.println("Command (~removeContract) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + mEvent.getMessageContent().substring(16) + "."); //Sends a system message about who issued the command.

				//Closes the connections.
				preparedStatement.close();
				connection.close();
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! The ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				//Closes the connections.
				preparedStatement.close();
				connection.close();
			}
		} catch(StringIndexOutOfBoundsException e0_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeContract` command must be a digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e0_2) {
				e0_2.printStackTrace();
			}
		} catch(NumberFormatException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeContract` command must be a digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that displays all the channels where contract related commands can be used by typing `~showChannels` (not case-sensitive).
	public void showChannels(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

        	//An embed with the channels where contract related commands can be used.
			EmbedBuilder channels = new EmbedBuilder();
			channels
				.setTitle("Channels")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.setFooter("These are the channels where contract related commands can be used.");

			//Opens up a connection to the 'BHT' SQL database (Channels).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

			//Creates a 'SELECT' SQL statement.
			Statement statement = connection.createStatement();
        	ResultSet resultSet = statement.executeQuery("SELECT * FROM Channels");

			//If there are any channels, adds them to the above mentioned embed. If not, notifies the user.
        	resultSet.last();
        	if(!(resultSet.getRow() <= 0)) {
        		int k = 1;
        		resultSet.beforeFirst();
    			while(resultSet.next()) {
    				channels.addField(k + ". " + String.valueOf(resultSet.getString("channelName")), "**ID: **" + String.valueOf(resultSet.getLong("channelId")));
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

			System.out.println("Command (~showChannels) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that adds the channel with the specified ID to a database of channels in which the use of certain commands is allowed by typing `~addChannel` (not case-sensitive).
	public void addChannel(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Channels).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

        	//Adds the channel with the ID after the `~addChannel` command to the 'Channels' SQL database if it's not there already.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(channelId) FROM Channels WHERE channelId = ?");
			preparedStatement0.setLong(1, Long.parseLong(mEvent.getMessage().getContent().substring(12)));
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 0) {
				Server server = dApi.getServerById(mEvent.getServerTextChannel().get().getServer().getId()).get(); //Gets the server.

				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Channels(channelId, channelName) VALUES(?, ?)");
				preparedStatement.setLong(1, Long.parseLong(mEvent.getMessage().getContent().substring(12)));
				preparedStatement.setString(2, server.getChannelById(Long.parseLong(mEvent.getMessage().getContent().substring(12))).get().getName());
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
					new MessageBuilder().append("Contract related commands are now available in the following channel: " + dApi.getServerTextChannelById(Long.parseLong(mEvent.getMessage().getContent().substring(12))).get().getMentionTag() + ".").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
					System.out.println("Command (~addChannel) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + mEvent.getMessageContent().substring(12) + "."); //Sends a system message about who issued the command.

					//Closes the connections.
					preparedStatement.close();
					connection.close();
				} else {
					mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

					//Closes the connections.
					preparedStatement.close();
					connection.close();
				}
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! The channel is already in the database.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				//Closes the connections.
				resultSet.close();
				preparedStatement0.close();
				connection.close();
			}
			preparedStatement0.close();
			resultSet.close();
		} catch(StringIndexOutOfBoundsException e0_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~addChannel` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e0_2) {
				e0_2.printStackTrace();
			}
		} catch(NumberFormatException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~addChannel` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(NoSuchElementException e2_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! There is no channel with the specified ID in the server.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2_2) {
				e2_2.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that removes the channel with the specified ID from a database of channels in which the use of certain commands is allowed by typing `~removeChannel` (not case-sensitive).
	public void removeChannel(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Channels).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

        	//Removes the channel with the ID after the `~removeChannel` command from the 'Channels' SQL database.
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Channels WHERE channelId = ?");
			preparedStatement.setLong(1, Long.parseLong(mEvent.getMessage().getContent().substring(15)));
			int affectedRows = preparedStatement.executeUpdate();
			if(affectedRows > 0) {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
				new MessageBuilder().append("Contract related commands are no longer available in the following channel: " + dApi.getServerTextChannelById(Long.parseLong(mEvent.getMessage().getContent().substring(15))).get().getMentionTag() + ".").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
				System.out.println("Command (~removeChannel) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + mEvent.getMessageContent().substring(15) + "."); //Sends a system message about who issued the command.

				//Closes the connections.
				preparedStatement.close();
				connection.close();
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! The Channel ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				//Closes the connections.
				preparedStatement.close();
				connection.close();
			}
		} catch(StringIndexOutOfBoundsException e0_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeChannel` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e0_2) {
				e0_2.printStackTrace();
			}
		} catch(NumberFormatException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeChannel` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that displays all the roles who can use contract related commands by typing `~showRoles` (not case-sensitive).
	public void showRoles(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

        	//An embed with the roles who can use contract related commands.
			EmbedBuilder roles = new EmbedBuilder();
			roles
				.setTitle("Roles")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.setFooter("These are the roles who can use contract related commands.");

			//Opens up a connection to the 'BHT' SQL database (Roles).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

			//Creates a 'SELECT' SQL statement.
			Statement statement = connection.createStatement();
        	ResultSet resultSet = statement.executeQuery("SELECT * FROM Roles");

			//If there are any white-listed roles, adds them to the above mentioned embed. If not, notifies the user.
        	if(!(resultSet.getRow() <= 0)) {
        		int k = 1;
        		resultSet.beforeFirst();
    			while(resultSet.next()) {
    				roles.addField(k + ". " + String.valueOf(resultSet.getString("roleName")), "**ID: **" + String.valueOf(resultSet.getLong("roleId")));
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

			System.out.println("Command (~showRoles) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A commands that adds the role with the specified ID to a database of roles which allow the use of certain commands by typing `~addRole` (not case-sensitive).
	public void addRole(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Roles).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

        	//Adds the role with the ID after the `~addRole` command to the 'Roles' SQL database if it's not there already.
			PreparedStatement preparedStatement0 = connection.prepareStatement("SELECT COUNT(roleId) FROM Roles WHERE roleId = ?");
			preparedStatement0.setLong(1, Long.parseLong(mEvent.getMessage().getContent().substring(9)));
			ResultSet resultSet = preparedStatement0.executeQuery();
			resultSet.next();
			if(resultSet.getInt(1) == 0) {
				Server server = dApi.getServerById(mEvent.getServerTextChannel().get().getServer().getId()).get(); //Gets the server.

				PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Roles(roleId, roleName) VALUES(?, ?)");
				preparedStatement.setLong(1, Long.parseLong(mEvent.getMessage().getContent().substring(9)));
				preparedStatement.setString(2, server.getRoleById(Long.parseLong(mEvent.getMessage().getContent().substring(9))).get().getName());
				int affectedRows = preparedStatement.executeUpdate();
				if(affectedRows > 0) {
					mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
					new MessageBuilder().append("Contract related commands are now available to the following role: `" + dApi.getRoleById(Long.parseLong(mEvent.getMessage().getContent().substring(9))).get().getName() + "`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
					System.out.println("Command (~addRole) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + mEvent.getMessageContent().substring(9) + "."); //Sends a system message about who issued the command.

					//Closes the connections.
					preparedStatement.close();
					connection.close();
				} else {
					mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");

					//Closes the connections.
					preparedStatement.close();
					connection.close();
				}
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! The role is already in the database.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				//Closes the connections.
				resultSet.close();
				preparedStatement0.close();
				connection.close();
			}
			preparedStatement0.close();
			resultSet.close();
		} catch(StringIndexOutOfBoundsException e0_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~addRole` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e0_2) {
				e0_2.printStackTrace();
			}
		} catch(NumberFormatException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~addRole` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(NoSuchElementException e2_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! There is no role with the specified ID in the server.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e2_2) {
				e2_2.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A commands that removes the role with the specified ID from a database of roles which allow the use of certain commands by typing `~removeRole` (not case-sensitive).
	public void removeRole(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			//Opens up a connection to the 'BHT' SQL database (Roles).
        	Class.forName("com.mysql.cj.jdbc.Driver");
        	Connection connection = DriverManager.getConnection("...");

        	//Removes the channel with the ID after the `~removeRole` command from the 'Roles' SQL database.
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Roles WHERE roleId = ?");
			preparedStatement.setLong(1, Long.parseLong(mEvent.getMessage().getContent().substring(12)));
			int affectedRows = preparedStatement.executeUpdate();
			if(affectedRows > 0) {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👍");
				new MessageBuilder().append("Contract related commands are no longer available to the following role: `" + dApi.getRoleById(Long.parseLong(mEvent.getMessage().getContent().substring(12))).get().getName() + "`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
				System.out.println("Command (~removeRole) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ") - value: " + mEvent.getMessageContent().substring(12) + "."); //Sends a system message about who issued the command.

				//Closes the connections.
				preparedStatement.close();
				connection.close();
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Error! The Role ID doesn't exist.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

				//Closes the connections.
				preparedStatement.close();
				connection.close();
			}
		} catch(StringIndexOutOfBoundsException e0_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeRole` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e0_2) {
				e0_2.printStackTrace();
			}
		} catch(NumberFormatException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! The value after the `~removeRole` command must be a long digit.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that teaches you how to use the `~rpsPlay` command by typing `~rpsHelp` (not case-sensitive).
	public void rockPaperScissorsHelp(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//An embed with all the RPS related commands.
			EmbedBuilder rpsCommands = new EmbedBuilder()
				.setTitle("Rock, Paper or Scissors")
				.setThumbnail(dApi.getYourself().getAvatar())
				.setColor(Color.RED)
				.addField("Rules", "Here's what beats what if for whatever reason you don't know how the game works:\n**1.** Paper beats rock;\n**2.** Rock beats scissors;\n**3.** Scissors beats paper.")
				.addInlineField("~rpsPlay *[option]*", "Plays a game of \"Rock, Paper or Scissors\" against the bot.")
				.addInlineField("~rpsHighscores", "Shows the Top 10 users with the highest score.")
				.setFooter("The commands are not case-sensitive!");
			mEvent.getChannel().sendMessage(rpsCommands);

			System.out.println("Command (~rpsHelp) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command to play "Rock, Paper or Scissors" with the bot by typing `~rpsPlay` (not case-sensitive).
	public void rockPaperScissorsGame(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			Random rand = new Random(); //Creates an instance from the 'Random' class.

			//The bot randomly chooses an option.
			String[] botOptions = {"🪨", "📜", "✂"};
			int optionB = rand.nextInt(3);
			String optionBot = null;
			switch(optionB) {
				case 0:
					optionBot = "Rock";
					break;
				case 1:
					optionBot = "Paper";
					break;
				case 2:
					optionBot = "Scissors";
					break;
				default:
					System.err.println("RPS Error!");
			}

			//Checks the user's input and starts the game if it matches one of the three options.
			if(mEvent.getMessageContent().substring(9).equalsIgnoreCase("Rock") || mEvent.getMessageContent().substring(9).equalsIgnoreCase("Paper") || mEvent.getMessageContent().substring(9).equalsIgnoreCase("Scissors")) {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction(botOptions[optionB]);

	        	//Outputs the winner of the game and if it's the user, inserts his name to the database. Also, mentions the one who called the command.
	        	if(mEvent.getMessageContent().substring(9).equalsIgnoreCase(optionBot)) {
	        		new MessageBuilder().append("\nYou chose: **`" + mEvent.getMessageContent().substring(9) + "`**\nThe bot chose: **`" + optionBot + "`**\nIt's a **TIE**!").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
	        	} else if((mEvent.getMessageContent().substring(9).equalsIgnoreCase("Rock") && optionB == 1) || (mEvent.getMessageContent().substring(9).equalsIgnoreCase("Paper") && optionB == 2) || (mEvent.getMessageContent().substring(9).equalsIgnoreCase("Scissors") && optionB == 0)) {
	        		new MessageBuilder().append("\nYou chose: **`" + mEvent.getMessageContent().substring(9) + "`**\nThe bot chose: **`" + optionBot + "`**\nYou **LOSE**!").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
	        	} else {
	        		new MessageBuilder().append("\nYou chose: **`" + mEvent.getMessageContent().substring(9) + "`**\nThe bot chose: **`" + optionBot + "`**\nYou **WIN**!").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());

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
	        				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("🎉");

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
	        				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("🎉");

	        				//Closes the connections.
	        				resultSet.close();
	        				preparedStatement.close();
	        				connection.close();
	        			}
		        	}
	        	}
	        	System.out.println("Command (~rpsPlay) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + "). - value: " + mEvent.getMessageContent().substring(9) + "."); //Sends a system message about who issued the command.
			} else {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! Please choose either `Rock`, `Paper` or `Scissors`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			}
		} catch(StringIndexOutOfBoundsException e1_1) {
			try {
				mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("👎");
				new MessageBuilder().append("Wrong input! Please choose either `Rock`, `Paper` or `Scissors`.").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			} catch(Exception e1_2) {
				e1_2.printStackTrace();
			}
		} catch(NullPointerException e) {
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//A command that displays the top 10 users with the highest score in "Rock, Paper or Scissors" by typing `~rpsHighscores` (not case-sensitive).
	public void rockPaperScissorsHighscores(DiscordApi dApi, MessageCreateEvent mEvent) {
		try {
			mEvent.deleteMessage(); //Deletes the last message (the command).

			//Opens up a connection to the 'BHT' SQL database (Contracts).
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

			//Adds the actual users to the above mentioned embed.
			int k = 1;
			while(resultSet.next()) {
				highscores.addField(k + ". " + String.valueOf(resultSet.getString("highscoreUser")), "Score: **" + String.valueOf(resultSet.getInt("score")) + "**");
				k++;
			}
			mEvent.getChannel().sendMessage(highscores);

			//Closes the above connections.
			resultSet.close();
			statement.close();
			connection.close();

			System.out.println("Command (~rpsHighscores) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
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
				.addField("Commands", "You can also use one of the commands (`~showChannels`, `~showRoles`) to check the ID of an already whitelisted channel/role.")
				.setFooter("Use `~contractsHelp` for the commands where you can use the Channel/Role ID.");
			mEvent.getChannel().sendMessage(contractsCommands);

			System.out.println("Command (~idHelp) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	//Sends a link to the source code page on GitHub by typing `~sourceCode` (not case-sensitive).
	public void sourceCode(MessageCreateEvent mEvent) {
		try {
			mEvent.getChannel().getMessages(1).get().getNewestMessage().get().addReaction("🤖");
			new MessageBuilder().append("https://github.com/b0dan/BHTBot").replyTo(mEvent.getMessageId()).send(mEvent.getChannel());
			System.out.println("Command (~sourceCode) called by " + allMembers.get(mEvent.getMessageAuthor().getDiscriminatedName()) + " (" + mEvent.getMessageAuthor().getDiscriminatedName() + ")."); //Sends a system message about who issued the command.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
