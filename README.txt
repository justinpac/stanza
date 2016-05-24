README

Hello, and welcome to Stanza, an application that allows you to easily make poetry by suggesting rhymes for your words. This application also includes a friends feature. You can add your friend’s accounts to your friendslist, meaning you can see their published poetry, and they can see yours if they add you as a friend. 

How to use: 


Put the project in its entirety in a directory of your choice. 

(opening the file in android studio)
In Android Studio, select “open file” under “file” at the top. Navigate to the project’s “Stanza” folder and import the project by selecting the “build.gradle” file. You should now have the Stanza project open in Android Studio.

(running the backend)
Next, navigate to the Stanza_Backend file in the project’s directory. Compile all java files and run the Backend.java file with Java. Remember to add the port number to be used for the backend as a command line argument. This port number should be 28414. 

(ensuring that the Android app will connect to the backend)
Connecting the backend is a matter of ensuring that the App has the correct information regarding which computer is hosting the backend. In AccountCommThread.java in Android Studio, there is a line that says something similar to “String host = "rns202-17.cs.stolaf.edu";”. Make sure that this is changed to the correct computer that is running the host. You have to repeat this process for the CommThread.java file. While checking that the host name is correct, you will see a port number listed in both of these files. The port number in CommThread.java should be identical to the one in AccountCommThread.java and this is the number that should be used as a command line argument when running Backend.java. 

(running the App)
Run the App through Android Studio. If all previous steps have been followed then the application should function as intended. If not, repeat the previous steps and attempt to run the app again.

(Starting the app)
When Stanza first opens, you will see a login screen with two fields: Email and Password. If you already have a Stanza account, simply input the email address associated with your account, and its password, and press the LOGIN button. If you are connected to the internet, and if the backend server is running, it should take you directly to the app’s main view. 
If you do not have a Stanza account, press the text labeled “No account yet? Create one”. Here, you will see three fields: Name, Email and Password. Name is the username for your account, and will be associated with any poems you publish, as well as how other users will be able to add you to their Friends list. It will be displayed publicly, so choose something tasteful! Email is for the email address you would like to associate with this account, and must be a valid email address. Passwords must be between 5 and 13 characters. After entering this information, press the CREATE ACCOUNT button to create your account and enter the app’s main view.
To re-iterate: If you do not have an internet connection or the backend server is not running, you will not be able to get past the login screen. Provided for you app-using pleasure is a sample username and password, which are already in the backend:
Username: cunniff@stolaf.edu (note the two “n”s and two “f”s!)
Password: ilovepoetry

Sample friend names: Justin, Brianna, Marie, Maggie, Sarah, and Catherine 

(Using the app’s functionality)
Stanza allows you to create, edit and publish poems. To create a poem, navigate to the “MY POEMS” tab and press the floating action button at the lower right-hand corner of the screen. This will take you to the “New Poem” screen. From here, you can input your poem’s title and text. To save this poem to your phone, press the back arrow at the top left of the screen, or your poem’s back button.
	To edit an existing poem, select it from the “MY POEMS” tab. This editor gives you additional options: You can delete the poem with the trashcan icon, or publish it to the server with the plus icon. (Note: Publishing the poem to the server requires an internet connection, and requires the backend server to be running.) 
	When creating or editing a poem, Stanza gives suggestions for words that rhyme with the current word your cursor is on. To use one of these suggestions, simple press the desired word on the rhyme bar that appears above your keyboard’s autocomplete bar. The word you pressed will replace the word your cursor is on. If you wish to find a rhyming word for one that appears on a previous line, we suggest typing that word again, and replacing it with a suggestion you like.
	Under the Friends Board tab, you can see the ten most recently published poems of anyone in your friend list. To refresh the Friend Board, swipe down. If the backend is running, and you are connected to the internet, then the Friend Board should refresh. If either of these things is not true, then there should be an error message saying the server is disconnected. 

(Regarding the settings options)
	Under the My Poems tab, the settings pull-down menu includes “Create Sample Data” and “Delete all poems”. Create Sample Data will add some well-known poems to your My Poems tab, complete with titles and text. But be careful of publishing these to the server. They will be registered under your username, so don’t plagiarize! Delete all poems will delete all the poems in your My Poems tab, but it will not delete any poems that you have published to the server from the server database, so your friends will still be able to see them. 
	Under the Friend Board tab, the settings pull-down menu includes “Add friend” and “Manage Friends.” The Add friend option will generate a dialog box. Follow the instructions to enter the username of your desired friend. If the username exists as a Stanza user, he or she will be added to your friends list. If the username does not exist, there will be an error message telling you that the friend does not exist. 
	The Manage Friends option will bring you to your Friends List. Here you will see a list of all your friends. Clicking the plus button in the menu will generate the same dialog box that will allow you to add friends. Clicking the trashcan will delete all friends from your Friends List. Finally, clicking on one of your friends in the list, will generate a dialog box that asks you if you would like to delete that friend from your friend list. You can either “Delete Friend” or “Cancel” depending on what you wish to do. The back button from ManageFriends will bring you back to the FriendBoard where you can see your friends’ poetry.