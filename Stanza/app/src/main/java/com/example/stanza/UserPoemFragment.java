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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
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
 * A class representing the User Poem fragment, which contains a list of the user's poems.
 * Appears under the "My Poems" tab in the dashboard page.
 */
public class UserPoemFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ItemTouchHelperAdapter {

    /**
     * The unique code indicating that the <code>UserPoemFragment</code> is coming back from the <code>EditPoemActivity</code>.
     */
    private static final int EDITOR_REQUEST_CODE = 1001;

    /**
     * An instance of <code>PoemRecyclerAdapter</code>, which provides binding from a set of poems
     * (in this case the user's poems) to card views that are displayed in a
     * <code>RecyclerView</code>, which in this case is displayed in the User Poems Fragment.
     */
    private  PoemRecyclerAdapter poemRecyclerAdapter;

    /**
     * Called to have the User Poem fragment instantiate its user interface view.
     * @param inflater The <code>LayoutInflater</code> object that can be used to inflate any views in the fragment.
     * @param container The parent view that the User Poem fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return The <code>View</code> for the User Poem Fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_poem_fragment, container, false);

        RecyclerView userPoemRecycler = (RecyclerView) view.findViewById(R.id.userPoemRecycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        userPoemRecycler.setLayoutManager(linearLayoutManager);


        poemRecyclerAdapter = new PoemRecyclerAdapter(getActivity(), null);
        poemRecyclerAdapter.setOnItemClickListener(new PoemRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String pid) {
                Intent intent = new Intent(getActivity(), EditPoemActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + pid);
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });
        userPoemRecycler.setAdapter(poemRecyclerAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(this);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(userPoemRecycler);

        FloatingActionButton circleButton = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        circleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditPoemActivity.class);
                startActivityForResult(intent, EDITOR_REQUEST_CODE);
            }
        });

        setHasOptionsMenu(true);
        return view;
    }

    /**
     * Called when <code>MainActivity</code> has been created and the User Poem fragment's
     * view hierarchy has been instantiated.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Initialize the content of <code>MainActivity</code>'s standard options menu.
     * The menu items for the User Poem fragment are only displayed when the fragment is visible.
     * @param menu The options menu in which items are placed.
     * @param inflater A MenuInflater object used to instantiate menu_userpoemfragment.xml into Menu objects
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_userpoemfragment,menu);
    }

    /**
     * This hook is called whenever an item in the options menu is selected, and will perform
     * the proper actions based on whatever option was selected.
     * @param item The <code>MenuItem</code> that was selected.
     * @return Returns false to allow normal menu processing to proceed, true to override normal menu processing.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete_all:
                deleteAllPoems();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts a new or restarts an existing Loader in this manager, registers the callbacks implemented in this class,
     * and (if the fragment is currently started) starts loading it.
     */
    public void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    /**
     * Instantiate and return a new Loader for user's poems in the database.
     * @param id The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return  A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = DBOpenHelper.CREATOR + " LIKE 'self'";
        String sortOrder = "self";
        return new CursorLoader(getActivity(), NotesProvider.CONTENT_URI,
                null, selection, null, sortOrder);
    }

    /**
     * Called when a previously created loader has finished its load. Provides <code>PoemRecyclerAdapter</code>
     * with the data generated by the loader.
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        poemRecyclerAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * Removes references to the Loader's data within <code>PoemRecyclerAdapter</code>.
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        poemRecyclerAdapter.swapCursor(null);
    }

    /**
     * Receive the result from a previous call to <code>startActivityForResult(Intent, int)</code>.
     * Responds accordingly for the results from specific activities.
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing us to identify which activity this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An <code>Intent</code>, which can return result data to the caller.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == MainActivity.RESULT_OK) {
            restartLoader();
        }
    }

    /**
     * Deletes all user-created poems from the database.
     */
    private void deleteAllPoems() {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE) {
                            //Insert Data management code here
                            String selectionClause = DBOpenHelper.CREATOR + "='self'";
                            getActivity().getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, selectionClause, null
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

    /**
     * Called when the poem cards within the User Poem fragment are moved by dragging and dropping (this feature is not implemented yet).
     * @param fromPosition The initial position of the card within the <code>RecyclerView</code>.
     * @param toPosition The final position of the card within the <code>RecyclerView</code>.
     */
    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    /**
     * called when the user swipes a poem card to the left. This will delete the poem.
     * @param position The position of the card within the <code>RecyclerView</code>.
     */
    @Override
    public void onItemDismiss(int position) {
        //String[] selectionArgs = {""};
        //selectionArgs[0] = "LIMIT 1 OFFSET " + String.valueOf(position - 1);
        position = (poemRecyclerAdapter.getItemCount() - 1) - position; // The first position in recycler adapter adapter corresponds to the last record in the database
        String selectionClause = DBOpenHelper.POEM_ID + " in (SELECT " +
                DBOpenHelper.POEM_ID + " FROM " + DBOpenHelper.TABLE_POEMS + " WHERE " +
                DBOpenHelper.CREATOR + "='self' LIMIT 1 OFFSET " + String.valueOf(position) + ")";
        System.out.println("POSITION IS: " + String.valueOf(position));
        getActivity().getContentResolver().delete(NotesProvider.CONTENT_URI, selectionClause, null);
        Toast.makeText(getActivity(), getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();
        restartLoader();
    }
}