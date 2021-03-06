package ist261.chatbot.component;

import ist261.chatbot.infra.Chatbot;
import ist261.user.intent.AbstractUserIntent;
// Import random so we are able to choose a variety of responses
import java.util.Random;

// Import sql libraries to read project database
import java.sql.*;
// Import FileIOStream to convert BLOB back to pdf
import java.io.FileOutputStream;

// Import file class to write pbs script for user
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class ResponseGenerator {

	private Connection conn;

	private Chatbot chatbot;
	
	public ResponseGenerator(Chatbot chatbot) {
		this.chatbot = chatbot;
	}

	public String getResponse(AbstractUserIntent nowUserIntent, String nowConversationalAction) {
		Random randomInt = new Random();
		if(nowUserIntent!=null&&nowUserIntent.getIntentName().equals("UseCommand")) {
			String nowCommand = (String) nowUserIntent.getLastestSlotValue("command");
			if(nowConversationalAction.equals("ask-command")){
				int askCommandResponse = randomInt.nextInt(3);
				switch (askCommandResponse) {
					case 0:
						return "Sure, I can help you figure out how to use a " +
								"command. Just tell me which command you are trying " +
								"to use!";

					case 1:
						return "Could you please tell me what command you are " +
								"trying to use?";

					case 2:
						return "What command are you trying to use?";
				}

			}else if(nowConversationalAction.equals("answer-command")){
				try {
					// Connect to database
					conn = DriverManager.getConnection("jdbc:sqlite:/home/nucci/Documents/ist261_code/ist261_final_project/data/commands.db");
					conn.setAutoCommit(false);

					// Retrieve data from commands.db
					String commandBlurbStmt = "SELECT content FROM commandblurb WHERE name = ?";
					PreparedStatement selectCommand = conn.prepareStatement(commandBlurbStmt);
					selectCommand.setString(1, nowCommand);
					ResultSet resultSet = selectCommand.executeQuery();

					// Return content statement
					return resultSet.getString("content") +
							"\n\nIt looks like I have some more documentation available for " + nowCommand +
							". Would you like me to fetch it for you?";

				} catch ( SQLException e ) {
					System.out.println(e);

				}

			}else if(nowConversationalAction.equals("provide-doc")){
				try {
					// Connect to database
					conn = DriverManager.getConnection("jdbc:sqlite:/home/nucci/Documents/ist261_code/ist261_final_project/data/commands.db");
					conn.setAutoCommit(false);

					// Retrieve PDF file from database
					String fetchDocumentation = "SELECT doc FROM docs WHERE name = ?";
					PreparedStatement selectCommand = conn.prepareStatement(fetchDocumentation);
					selectCommand.setString(1, nowCommand);
					ResultSet resultSet = selectCommand.executeQuery();

					// Convert Blob into PDF
					byte[] byteArray = resultSet.getBytes("doc");
					File newFile = new File("/home/nucci/work/" + nowCommand + ".pdf");
					if(newFile.isFile()){
						newFile.delete();
					}
					newFile.createNewFile();
					FileOutputStream writePDF = new FileOutputStream("/home/nucci/work/" +
							nowCommand + ".pdf");
					writePDF.write(byteArray);

					return "Alright, I saved the documentation for " + nowCommand + " in your work directory " +
							"as " + nowCommand + ".pdf";


				} catch (SQLException | IOException e){
					System.out.println(e);

				}


			}else if(nowConversationalAction.equals("no-doc")){
				int fetchDocumentationResponse = randomInt.nextInt(2);
				switch (fetchDocumentationResponse){
					case 0:
						return "Sounds good! If you want more documentation for " +
								nowCommand + " in the future just ask me \"Fetch me the " +
								"documentation for " + nowCommand + "\"";

					case 1:
						return "You got it! If you ever want more documentation for " +
								nowCommand + " in the future just ask me \"Grab me the " +
								"documentation for " + nowCommand + "\"";
				}
			}
			else{
				return "I'm sorry, but I don't understand what you just said. " +
						"Try asking me something like \"How to use the 'cat' command!\"";
			}

		}else if(nowUserIntent!=null&&nowUserIntent.getIntentName().equals("FetchDocumentation")){
			String docCommand = (String) nowUserIntent.getLastestSlotValue("doc_command");
			if (nowConversationalAction.equals("get-doc")){
				try {
					// Connect to database
					conn = DriverManager.getConnection("jdbc:sqlite:/home/nucci/Documents/ist261_code/ist261_final_project/data/commands.db");
					conn.setAutoCommit(false);

					// Retrieve PDF file from database
					String fetchDocumentation = "SELECT doc FROM docs WHERE name = ?";
					PreparedStatement selectCommand = conn.prepareStatement(fetchDocumentation);
					selectCommand.setString(1, docCommand);
					ResultSet resultSet = selectCommand.executeQuery();

					// Convert Blob into PDF
					byte[] byteArray = resultSet.getBytes("doc");
					File newFile = new File("/home/nucci/work/" + docCommand + ".pdf");
					if(newFile.isFile()){
						newFile.delete();
					}
					newFile.createNewFile();
					FileOutputStream writePDF = new FileOutputStream("/home/nucci/work/" +
							docCommand + ".pdf");
					writePDF.write(byteArray);

					return "You got it! I saved the documentation for " + docCommand + " in your work directory " +
							"as " + docCommand + ".pdf";


				} catch (SQLException | IOException e){
					System.out.println(e);

				}
			}else if(nowConversationalAction.equals("ask-doc-command")){
				int docResponse = randomInt.nextInt(2);
				switch (docResponse) {
					case 0:
						return "Sure, I can help you look for some documentation. Just tell me the command " +
								"that you are looking for, and I'll see if I have any documentation for it.";

					case 1:
						return "Could you please tell me the command you're looking for? I'll see if I have " +
								"documentation for it stored in my database.";
				}
			}else{
				return "Hmm. I don't think I have documentation for the command you specified. Try asking " +
						"me \"Fetch me the docs for the alias command\"";
			}


		}else if(nowUserIntent!=null&&nowUserIntent.getIntentName().equals("WritePBS")){
			// Pull all the slot values from the slot table
			String shell = (String) nowUserIntent.getLastestSlotValue("shell");
			String allocName = (String) nowUserIntent.getLastestSlotValue("alloc");
			String nodeAmount = (String) nowUserIntent.getLastestSlotValue("node");
			String ppnAmount = (String) nowUserIntent.getLastestSlotValue("ppn");
			String pmemAmount = (String) nowUserIntent.getLastestSlotValue("pmem");
			String wallTime = (String) nowUserIntent.getLastestSlotValue("walltime");
			String emailYesNo = (String) nowUserIntent.getLastestSlotValue("email_yes_no");
			String email = (String) nowUserIntent.getLastestSlotValue("email");

			if (nowConversationalAction.equals("finish-script")) {
				// Write to pbs script
				try {
					File newFile = new File("/home/nucci/work/roarbot_script.pbs");
					if(newFile.isFile()){
						newFile.delete();
					}
					newFile.createNewFile();
					FileWriter pbsWriter = new FileWriter("/home/nucci/work/roarbot_script.pbs");
					pbsWriter.write("#!/bin/"+shell+"\n");
					pbsWriter.write("#PBS -A "+allocName+"\n");
					pbsWriter.write("#PBS -l nodes="+nodeAmount+":ppn="+ppnAmount+"\n");
					pbsWriter.write("#PBS -l pmem="+pmemAmount+"\n");
					pbsWriter.write("#PBS -l walltime="+wallTime+"\n");
					pbsWriter.write("#PBS -j oe\n");

					if (emailYesNo.equals("yes")){
						pbsWriter.write("#PBS -m bea\n");
						pbsWriter.write("#PBS -M "+email+"\n");
					}

					pbsWriter.write("\n#------ ^Autogenerated by ROARbot^ ------#");
					pbsWriter.close();

				} catch (IOException e) {
					System.out.println("Something went wrong writing to pbs file");
					e.printStackTrace();
				}

				return "Alright, I saved your PBS script to ~/work/roarbot_script.pbs! " +
						"You can open it and add your script to it now! If you need " +
						"anything else, let me know!";

			} else if (nowConversationalAction.equals("ask-email")) {
				return "Awesome! I just need your email";

			} else if (nowConversationalAction.equals("ask-if-email")) {
				return "Would you like to receive and email if your job either " +
						"(b) begins, (e) ends, or (a) aborts? Please enter your " +
						"response as either yes or no";

			} else if (nowConversationalAction.equals("ask-walltime")) {
				return "How long do you think your job needs to run? Please " +
						"enter your response in the following format: " +
						"HH:MM:SS";

			} else if (nowConversationalAction.equals("ask-pmem")) {
				return "How much memory per processor do you think you need? " +
						"1gb, 2gb, 5gb, ...?";

			} else if (nowConversationalAction.equals("ask-ppn")) {
				return "How many processors do you need for your job? " +
						"1, 2, 3, ...?";

			} else if (nowConversationalAction.equals("ask-node")) {
				return "How many nodes do you need for your job? " +
						"1, 2, 3, ...?";

			} else if (nowConversationalAction.equals("ask-alloc")) {
				return "What allocation are we submitting your job to?";

			} else if (nowConversationalAction.equals("ask-shell")) {
				return "What scripting language language would you like to " +
						"use for your pbs script? You can use one of the following:\n" +
						"    1. bash\n" +
						"    2. csh";
			} else {
				return "I'm sorry, but I don't think I can understand what you just " +
						"said. Try asking me \"I need help writing a pbs script!\"";
			}

		}else if(nowUserIntent!=null&&nowUserIntent.getIntentName().equals("TroubleShoot")){
			if (nowConversationalAction.equals("ask-problem")){
				int troubleShootResponse = randomInt.nextInt(3);
				switch (troubleShootResponse){
					case 0:
						return "I should be able to help you with the error " +
								"that you are receiving! Just copy and paste the " +
								"error message in my text box and I'll see " +
								"if I can help!";

					case 1:
						return "I'll help! Just copy and paste the error message that " +
								"you are receiving here and I'll see if I have a solution";

					case 2:
						return "Oh no! Copy and paste the error message here and I " +
								"will see if I have a viable solution for you";

				}
			}else if (nowConversationalAction.equals("return-solution")){
				String id = (String)nowUserIntent.getLastestSlotValue("problem");
				String table = (String)nowUserIntent.getLastestSlotValue("table");
				try {
					conn = DriverManager.getConnection("jdbc:sqlite:/home/nucci/Documents/ist261_code/ist261_final_project/data/troubleshoot.db");
					conn.setAutoCommit(false);

					if (table.equals("qsubproblem")) {
						// Retrieve viable solution from database
						String solStmt = "SELECT solution FROM qsubproblem WHERE id = ?";
						PreparedStatement solSelect = conn.prepareStatement(solStmt);
						solSelect.setInt(1, Integer.parseInt(id));
						ResultSet solResult = solSelect.executeQuery();

						// return what is retrieved from the database
						return solResult.getString("solution");
					}

				} catch (SQLException e) {
					System.out.println(e);
				}


			}else{
				int noSolResponse = randomInt.nextInt(2);
				switch (noSolResponse){
					case 0:
						return "Uh oh. Looks like I can't help you with this one. " +
								"I suggest that you go get some help from the " +
								"ICDS i-ASK center: https://www.icds.psu.edu/computing-services/support/";

					case 1:
						return "It looks like I don't have the answer for this one. " +
								"I recommend contacting the ICDS i-ASK center:" +
								"https://www.icds.psu.edu/computing-services/support/";
				}
			}

		}else{
			int otherResponse = randomInt.nextInt(3);
			switch (otherResponse) {
				case 0:
					return "I wasn't programmed to be a social bot :( " +
							"but you can ask me for help if you're writing a pbs script " +
							"Try asking me, \"Write a pbs script!\"";

				case 1:
					return "I'm sorry Dave. I can't do that. But what I can do is " +
							"teach you how to use common Linux commands. Try asking " +
							"me \"How to use the 'tar' command!\"";

				case 2:
					return "I'm not much for conversation, but I do love to help users " +
							"discover what is wrong with their job! " +
							"Try copy and pasting a error message you're receiving " +
							"into the text box!";
			}
		}
		return "";
	}
}
