# Stanza
An Android application for creating and sharing poetry with ease. Created as part of a team project for the Mobile Computing Applications Course at St. Olaf college.

## Find My Code
Stanza was created as a team project. Some of the files (and corresponding documentation) that I was responsible for writing are:
* [UserPoemFragment.java](./Stanza/app/src/main/java/com/example/stanza/UserPoemFragment.java)
* [PoemRecyclerAdapter.java](./Stanza/app/src/main/java/com/example/stanza/PoemRecyclerAdapter.java)
* [MainActivity.java](./Stanza/app/src/main/java/com/example/stanza/MainActivity.java)

## Just for Show
This repository was created to showcase a past project as part of my portfolio. Some features are not intended to work right out of the box, such as our backend server that was configured to work on the St. Olaf network. If you still wish to run the app, instructions on doing so are below.

### Using the App


Clone this repository into the directory of your choice.

#### Opening the File in Android Studio
In Android Studio, select “open file” under “file” at the top. Navigate to the project’s “Stanza” folder and import the project by selecting the “build.gradle” file. You should now have the Stanza project open in Android Studio.

#### Running the Backend
Next, navigate to the Stanza_Backend file in the project’s directory. Compile all java files and run the Backend.java file with Java. Remember to add the port number to be used for the backend as a command line argument. This port number should be 28414.

#### Ensuring that the Android App Will Connect to the Backend
Connecting the backend is a matter of ensuring that the App has the correct information regarding which computer is hosting the backend. In AccountCommThread.java in Android Studio, there is a line that says something similar to “String host = "rns202-17.cs.stolaf.edu";”. Make sure that this is changed to the correct computer that is running the host. You have to repeat this process for the CommThread.java file. While checking that the host name is correct, you will see a port number listed in both of these files. The port number in CommThread.java should be identical to the one in AccountCommThread.java and this is the number that should be used as a command line argument when running Backend.java.

#### Running the App
Run the App through Android Studio. If all previous steps have been followed then the application should function as intended. If not, repeat the previous steps and attempt to run the app again.

#### Starting the App
* When Stanza first opens, you will see a login screen with two fields: Email and Password. If you already have a Stanza account, simply input the email address associated with your account, and its password, and press the LOGIN button. If you are connected to the internet, and if the backend server is running, it should take you directly to the app’s main view.
* If you do not have a Stanza account, press the text labeled “No account yet? Create one”. Here, you will see three fields: Name, Email and Password. Name is the username for your account, and will be associated with any poems you publish, as well as how other users will be able to add you to their Friends list. It will be displayed publicly, so choose something tasteful! Email is for the email address you would like to associate with this account, and must be a valid email address. Passwords must be between 5 and 13 characters. After entering this information, press the CREATE ACCOUNT button to create your account and enter the app’s main view.
  * To re-iterate: If you do not have an internet connection or the backend server is not running, you will not be able to get past the login screen. Provided for you app-using pleasure is a sample username and password, which are already in the backend:
  * Username: cunniff@stolaf.edu
  * Password: ilovepoetry

#### Functionality
* Stanza allows you to create, edit and publish poems. To create a poem, navigate to the “MY POEMS” tab and press the floating action button at the lower right-hand corner of the screen. This will take you to the “New Poem” screen. From here, you can input your poem’s title and text. To save this poem to your phone, press the back arrow at the top left of the screen, or your poem’s back button.
* To edit an existing poem, select it from the “MY POEMS” tab. This editor gives you additional options: You can delete the poem with the trashcan icon, or publish it to the server with the plus icon. (Note: Publishing the poem to the server requires an internet connection, and requires the backend server to be running.)
When creating or editing a poem, Stanza gives suggestions for words that rhyme with the current word your cursor is on. To use one of these suggestions, simple press the desired word on the rhyme bar that appears above your keyboard’s autocomplete bar. The word you pressed will replace the word your cursor is on. If you wish to find a rhyming word for one that appears on a previous line, we suggest typing that word again, and replacing it with a suggestion you like.
* Under the Friends Board tab, you can see the ten most recently published poems of anyone in your friend list. To refresh the Friend Board, swipe down. If the backend is running, and you are connected to the internet, then the Friend Board should refresh. If either of these things is not true, then there should be an error message saying the server is disconnected.
