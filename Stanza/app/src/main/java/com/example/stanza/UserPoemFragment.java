package com.example.stanza;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import at.markushi.ui.CircleButton;

/**
 * Created by Justin on 4/26/2016.
 */


public class UserPoemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EDITOR_REQUEST_CODE = 1001;
    private NotesCursorAdapter cursorAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_poem_fragment, container, false);

        cursorAdapter = new NotesCursorAdapter(getActivity(), null, 0);

        ListView list = (ListView) view.findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EditPoemActivity.class);
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ID is:" + String.valueOf(id));
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });


        FloatingActionButton circleButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        circleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditPoemActivity.class);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        getLoaderManager().initLoader(0, null, this);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_userpoemfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_sample:
                insertSampleData();
                break;
            case R.id.action_delete_all:
                deleteAllNotes();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            restartLoader();
        }
    }

    private void deleteAllNotes() {

        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            getActivity().getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader();

                            Toast.makeText(getActivity(),
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();
    }
    private void insertNote(String noteText, String noteTitle) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.POEM_TEXT,noteText);
        values.put(DBOpenHelper.POEM_TITLE, noteTitle);
        getActivity().getContentResolver().insert(NotesProvider.CONTENT_URI, values);
    }

    private void insertSampleData() {
        insertNote("Some say the world will end in fire, \n" +
                "Some say in ice. \n" +
                "From what I’ve tasted of desire \n" +
                "I hold with those who favor fire. \n" +
                "But if it had to perish twice, \n" +
                "I think I know enough of hate \n" +
                "To say that for destruction ice \n" +
                "Is also great \n" +
                "And would suffice.", "Fire and Ice" );
        insertNote("Do not go gentle into that good night, \n" +
                "Old age should burn and rage at close of day; \n" +
                "Rage, rage against the dying of the light.\n" +
                "Though wise men at their end know dark is right, \n" +
                "Because their words had forked no lightning they \n" +
                "Do not go gentle into that good night.\n" +
                "\n" +
                "Good men, the last wave by, crying how bright \n" +
                "Their frail deeds might have danced in a green bay, \n" +
                "Rage, rage against the dying of the light.\n" +
                "\n" +
                "Wild men who caught and sang the sun in flight, \n" +
                "And learn, too late, they grieved it on its way, \n" +
                "Do not go gentle into that good night.\n" +
                "\n" +
                "Grave men, near death, who see with blinding sight \n" +
                "Blind eyes could blaze like meteors and be gay, \n" +
                "Rage, rage against the dying of the light.\n" +
                "\n" +
                "And you, my father, there on the sad height, \n" +
                "Curse, bless me now with your fierce tears, I pray. \n" +
                "Do not go gentle into that good night. \n" +
                "Rage, rage against the dying of the light.", "Do Not Go Gentle Into That Good Night");
        insertNote("You do not have to be good.\n" +
                "You do not have to walk on your knees\n" +
                "For a hundred miles through the desert, repenting.\n" +
                "You only have to let the soft animal of your body\n" +
                "love what it loves.\n" +
                "Tell me about your despair, yours, and I will tell you mine.\n" +
                "Meanwhile the world goes on.\n" +
                "Meanwhile the sun and the clear pebbles of the rain\n" +
                "are moving across the landscapes,\n" +
                "over the prairies and the deep trees,\n" +
                "the mountains and the rivers.\n" +
                "Meanwhile the wild geese, high in the clean blue air,\n" +
                "are heading home again.\n" +
                "Whoever you are, no matter how lonely,\n" +
                "the world offers itself to your imagination,\n" +
                "calls to you like the wild geese, harsh and exciting --\n" +
                "over and over announcing your place\n" +
                "in the family of things.", "Wild Geese");
        insertNote("What is this life if, full of care,\n" +
                "We have no time to stand and stare.\n" +
                "\n" +
                "No time to stand beneath the boughs\n" +
                "And stare as long as sheep or cows.\n" +
                "\n" +
                "No time to see, when woods we pass,\n" +
                "Where squirrels hide their nuts in grass.\n" +
                "\n" +
                "No time to see, in broad daylight,\n" +
                "Streams full of stars, like skies at night.\n" +
                "\n" +
                "No time to turn at Beauty's glance,\n" +
                "And watch her feet, how they can dance.\n" +
                "\n" +
                "No time to wait till her mouth can\n" +
                "Enrich that smile her eyes began.\n" +
                "\n" +
                "A poor life this is if, full of care,\n" +
                "We have no time to stand and stare. ", "Leisure");
        insertNote("Whose woods these are I think I know.   \n" +
                "His house is in the village though;   \n" +
                "He will not see me stopping here   \n" +
                "To watch his woods fill up with snow.   \n" +
                "\n" +
                "My little horse must think it queer   \n" +
                "To stop without a farmhouse near   \n" +
                "Between the woods and frozen lake   \n" +
                "The darkest evening of the year.   \n" +
                "\n" +
                "He gives his harness bells a shake   \n" +
                "To ask if there is some mistake.   \n" +
                "The only other sound’s the sweep   \n" +
                "Of easy wind and downy flake.   \n" +
                "\n" +
                "The woods are lovely, dark and deep,   \n" +
                "But I have promises to keep,   \n" +
                "And miles to go before I sleep,   \n" +
                "And miles to go before I sleep.", "Stopping By Woods on a Snowy Evening");
        insertNote("It was many and many a year ago,\n" +
                "In a kingdom by the sea,\n" +
                "That a maiden there lived whom you may know\n" +
                "By the name of ANNABEL LEE; \n" +
                "And this maiden she lived with no other thought\n" +
                "Than to love and be loved by me.\n" +
                "\n" +
                "I was a child and she was a child,\n" +
                "In this kingdom by the sea; \n" +
                "But we loved with a love that was more than love-\n" +
                "I and my Annabel Lee; \n" +
                "With a love that the winged seraphs of heaven\n" +
                "Coveted her and me.\n" +
                "\n" +
                "And this was the reason that, long ago,\n" +
                "In this kingdom by the sea,\n" +
                "A wind blew out of a cloud, chilling\n" +
                "My beautiful Annabel Lee; \n" +
                "So that her highborn kinsman came\n" +
                "And bore her away from me,\n" +
                "To shut her up in a sepulchre\n" +
                "In this kingdom by the sea.\n" +
                "\n" +
                "The angels, not half so happy in heaven,\n" +
                "Went envying her and me-\n" +
                "Yes! - that was the reason (as all men know,\n" +
                "In this kingdom by the sea) \n" +
                "That the wind came out of the cloud by night,\n" +
                "Chilling and killing my Annabel Lee.\n" +
                "\n" +
                "But our love it was stronger by far than the love\n" +
                "Of those who were older than we-\n" +
                "Of many far wiser than we-\n" +
                "And neither the angels in heaven above,\n" +
                "Nor the demons down under the sea,\n" +
                "Can ever dissever my soul from the soul\n" +
                "Of the beautiful Annabel Lee.\n" +
                "\n" +
                "For the moon never beams without bringing me dreams\n" +
                "Of the beautiful Annabel Lee; \n" +
                "And the stars never rise but I feel the bright eyes\n" +
                "Of the beautiful Annabel Lee; \n" +
                "And so, all the night-tide, I lie down by the side\n" +
                "Of my darling- my darling- my life and my bride,\n" +
                "In the sepulchre there by the sea,\n" +
                "In her tomb by the sounding sea. " , "Annabel Lee");
        insertNote("Two roads diverged in a yellow wood, \n" +
                "And sorry I could not travel both \n" +
                "And be one traveler, long I stood \n" +
                "And looked down one as far as I could \n" +
                "To where it bent in the undergrowth; \n" +
                "\n" +
                "Then took the other, as just as fair, \n" +
                "And having perhaps the better claim, \n" +
                "Because it was grassy and wanted wear; \n" +
                "Though as for that the passing there \n" +
                "Had worn them really about the same, \n" +
                "\n" +
                "And both that morning equally lay \n" +
                "In leaves no step had trodden black. \n" +
                "Oh, I kept the first for another day! \n" +
                "Yet knowing how way leads on to way, \n" +
                "I doubted if I should ever come back. \n" +
                "\n" +
                "I shall be telling this with a sigh \n" +
                "Somewhere ages and ages hence: \n" +
                "Two roads diverged in a wood, and I— \n" +
                "I took the one less traveled by, \n" +
                "And that has made all the difference.", "The Road Not Taken");
        insertNote("Then this ebony bird beguiling my sad fancy into smiling, \n" +
                "By the grave and stern decorum of the countenance it wore, \n" +
                "“Though thy crest be shorn and shaven, thou,” I said, “art sure no craven, \n" +
                "Ghastly grim and ancient Raven wandering from the Nightly shore— \n" +
                "Tell me what thy lordly name is on the Night’s Plutonian shore!” \n" +
                "            Quoth the Raven “Nevermore.” \n" +
                "\n" +
                "    Much I marvelled this ungainly fowl to hear discourse so plainly, \n" +
                "Though its answer little meaning—little relevancy bore; \n" +
                "    For we cannot help agreeing that no living human being \n" +
                "    Ever yet was blessed with seeing bird above his chamber door— \n" +
                "Bird or beast upon the sculptured bust above his chamber door, \n" +
                "            With such name as “Nevermore.” ", "Nevermore excerpt");
        insertNote("Nature's first green is gold,\n" +
                "Her hardest hue to hold.\nHer early leaf's a flower;\n" +
                "But only so an hour.\n" +
                "Then leaf subsides to leaf,\n" +
                "So Eden sank to grief,\n" +
                "So dawn goes down to day\n" +
                "Nothing gold can stay. ", "Nothing Gold Can Stay");
        insertNote("I have been one acquainted with the night.\n" +
                "I have walked out in rain - and back in rain.\n" +
                "I have outwalked the furthest city light.\n" +
                "\n" +
                "I have looked down the saddest city lane.\n" +
                "I have passed by the watchman on his beat\n" +
                "And dropped my eyes, unwilling to explain.\n" +
                "\n" +
                "I have stood still and stopped the sound of feet\n" +
                "When far away an interrupted cry\n" +
                "Came over houses from another street,\n" +
                "\n" +
                "But not to call me back or say good-bye; \n" +
                "And further still at an unearthly height,\n" +
                "One luminary clock against the sky\n" +
                "\n" +
                "Proclaimed the time was neither wrong nor right.\n" +
                "I have been one acquainted with the night. ", "Acquainted with the Night");
        insertNote("As virtuous men pass mildly away,\n" +
                "And whisper to their souls, to go,\n" +
                "Whilst some of their sad friends do say,\n" +
                "'The breath goes now,' and some say, 'No:'\n" +
                "\n" +
                "So let us melt, and make no noise,\n" +
                "No tear-floods, nor sigh-tempests move;\n" +
                "'Twere profanation of our joys\n" +
                "To tell the laity our love.\n" +
                "\n" +
                "Moving of th' earth brings harms and fears;\n" +
                "Men reckon what it did, and meant;\n" +
                "But trepidation of the spheres,\n" +
                "Though greater far, is innocent.\n" +
                "\n" +
                "Dull sublunary lovers' love\n" +
                "(Whose soul is sense) cannot admit\n" +
                "Absence, because it doth remove\n" +
                "Those things which elemented it.\n" +
                "\n" +
                "But we by a love so much refin'd,\n" +
                "That ourselves know not what it is,\n" +
                "Inter-assured of the mind,\n" +
                "Care less, eyes, lips, and hands to miss.\n" +
                "\n" +
                "Our two souls therefore, which are one,\n" +
                "Though I must go, endure not yet\n" +
                "A breach, but an expansion,\n" +
                "Like gold to airy thinness beat.\n" +
                "\n" +
                "If they be two, they are two so\n" +
                "As stiff twin compasses are two;\n" +
                "Thy soul, the fix'd foot, makes no show\n" +
                "To move, but doth, if the' other do.\n" +
                "\n" +
                "And though it in the centre sit,\n" +
                "Yet when the other far doth roam,\n" +
                "It leans, and hearkens after it,\n" +
                "And grows erect, as that comes home.\n" +
                "\n" +
                "Such wilt thou be to me, who must\n" +
                "Like th' other foot, obliquely run;\n" +
                "Thy firmness makes my circle just,\n" +
                "And makes me end, where I begun. ", "A Valediction: Forbidding Mourning");

        restartLoader();
    }
}