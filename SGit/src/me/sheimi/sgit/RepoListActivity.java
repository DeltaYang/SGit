package me.sheimi.sgit;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.utils.Constants;
import me.sheimi.sgit.activities.explorer.ExploreFileActivity;
import me.sheimi.sgit.activities.explorer.ImportRepositoryActivity;
import me.sheimi.sgit.activities.explorer.PrivateKeyManageActivity;
import me.sheimi.sgit.adapters.RepoListAdapter;
import me.sheimi.sgit.database.models.RepoCloneMonitor.CloneObserver;
import me.sheimi.sgit.dialogs.CloneDialog;
import me.sheimi.sgit.dialogs.ImportLocalRepoDialog;
import me.sheimi.sgit.dialogs.ProfileDialog;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

public class RepoListActivity extends SheimiFragmentActivity implements
        CloneObserver {

    private ListView mRepoList;
    private RepoListAdapter mRepoListAdapter;

    private static final int REQUEST_IMPORT_REPO = 0;
    private Intent mImportRepoIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRepoList = (ListView) findViewById(R.id.repoList);
        mRepoListAdapter = new RepoListAdapter(this);
        mRepoList.setAdapter(mRepoListAdapter);
        mRepoListAdapter.queryAllRepo();
        mRepoList.setOnItemClickListener(mRepoListAdapter);
        mRepoList.setOnItemLongClickListener(mRepoListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        configSearchAction(searchItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_add_private_key:
                intent = new Intent(this, PrivateKeyManageActivity.class);
                finish();
                return true;
            case R.id.action_new:
                CloneDialog cloneDialog = new CloneDialog();
                cloneDialog.show(getSupportFragmentManager(), "clone-dialog");
                return true;
            case R.id.action_git_profile:
                ProfileDialog profileDialog = new ProfileDialog();
                profileDialog.show(getSupportFragmentManager(),
                        "profile-dialog");
                return true;
            case R.id.action_import_repo:
                intent = new Intent(this, ImportRepositoryActivity.class);
                startActivityForResult(intent, REQUEST_IMPORT_REPO);
                finish();
                return true;
            case R.id.action_feedback:
                Intent feedback = new Intent(Intent.ACTION_SEND);
                feedback.setType("text/email");
                feedback.putExtra(Intent.EXTRA_EMAIL,
                        new String[] { Constants.FEEDBACK_EMAIL });
                feedback.putExtra(Intent.EXTRA_SUBJECT,
                        getString(Constants.FEEDBACK_SUBJECT));
                startActivity(Intent.createChooser(feedback,
                        getString(R.string.label_send_feedback)));
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void configSearchAction(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null)
            return;
        SearchListener searchListener = new SearchListener();
        searchItem.setOnActionExpandListener(searchListener);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(searchListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mImportRepoIntent != null) {
            String path = mImportRepoIntent.getExtras().getString(
                    ExploreFileActivity.RESULT_PATH);
            ImportLocalRepoDialog rld = new ImportLocalRepoDialog(path);
            rld.show(getSupportFragmentManager(), "import-local-dialog");
            mImportRepoIntent = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_IMPORT_REPO:
                mImportRepoIntent = data;
                break;
        }
    }

    public class SearchListener implements SearchView.OnQueryTextListener,
            MenuItem.OnActionExpandListener {

        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            mRepoListAdapter.searchRepo(s);
            return false;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            mRepoListAdapter.queryAllRepo();
            return true;
        }

    }

    @Override
    public void cloneStateUpdated() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRepoListAdapter.nofityChanged();
            }
        });
    }

}
