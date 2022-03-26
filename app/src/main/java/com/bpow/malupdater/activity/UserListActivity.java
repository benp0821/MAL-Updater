package com.bpow.malupdater.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bpow.malupdater.R;
import com.bpow.malupdater.adapter.UserListAdapter;
import com.bpow.malupdater.listener.UserListScrollListener;
import com.bpow.malupdater.object.UserListElement;
import com.bpow.malupdater.request.UserListRequest;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class UserListActivity extends AppCompatActivity {

    private UserListAdapter userListAdapter;
    private Pair<String, String> currentStatus;
    private Pair<String, String> currentSort;

    int pageNum;
    boolean isLoading;
    boolean isLastPage;
    boolean isInitialLoad;

    public enum ListType {
        ANIME,
        MANGA
    }

    ListType listType;

    private void reloadList(){
        pageNum = 0;
        isLoading = false;
        isLastPage = false;
        userListAdapter.clear();
        loadPage();
    }

    public void initSpinner(int spinnerId, HashMap<String, String> options,
                            AdapterView.OnItemSelectedListener listener){
        Spinner statusSpinner = findViewById(spinnerId);

        statusSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_layout, options.keySet().toArray()));
        statusSpinner.setOnItemSelectedListener(listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Add hamburger button to open navigation menu
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, findViewById(R.id.toolbar_user_list), R.string.nav_menu_open, R.string.nav_menu_close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();

        NavigationView navView = findViewById(R.id.navigation_view);

        if (listType == ListType.MANGA){
            Menu navigationMenu = navView.getMenu();
            navigationMenu.findItem(R.id.switch_list_type).setTitle(getString(R.string.view_anime_list));
        }

        //Navigation View Event Listener
        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.switch_list_type) {
                if (listType == ListType.ANIME) {
                    MainActivity.loadUserList(UserListActivity.this, ListType.MANGA);
                }else{
                    MainActivity.loadUserList(UserListActivity.this, ListType.ANIME);
                }
            }
            return false;
        });

        HashMap<String, String> sortByOptions;
        HashMap<String, String> statusOptions;

        statusOptions = new LinkedHashMap<>();
        sortByOptions = new LinkedHashMap<>();

        if (listType == ListType.ANIME) {
            statusOptions.put("Watching", "watching");
            statusOptions.put("Completed", "completed");
            statusOptions.put("On Hold", "on_hold");
            statusOptions.put("Dropped", "dropped");
            statusOptions.put("Plan to Watch", "plan_to_watch");
            statusOptions.put("All", null);

            sortByOptions.put("Title", "anime_title");
            sortByOptions.put("Score", "list_score");
            sortByOptions.put("Last Updated", "list_updated_at");
            sortByOptions.put("Start Date", "anime_start_date");
        }else if (listType == ListType.MANGA){
            statusOptions.put("Reading", "reading");
            statusOptions.put("Completed", "completed");
            statusOptions.put("On Hold", "on_hold");
            statusOptions.put("Dropped", "dropped");
            statusOptions.put("Plan to Read", "plan_to_read");
            statusOptions.put("All", null);

            sortByOptions.put("Title", "manga_title");
            sortByOptions.put("Score", "list_score");
            sortByOptions.put("Last Updated", "list_updated_at");
            sortByOptions.put("Start Date", "manga_start_date");
        }

        currentStatus = new Pair<>((String) statusOptions.keySet().toArray()[0],
                (String) statusOptions.values().toArray()[0]);

        currentSort = new Pair<>((String) sortByOptions.keySet().toArray()[0],
                (String) sortByOptions.values().toArray()[0]);

        TextView statusLabel = findViewById(R.id.statusLabel);

        UserListRequest.getUserName(this, response -> {
            try {
                JSONObject responseObject = new JSONObject(response);
                StringBuilder statusLabelTxt = new StringBuilder(responseObject.getString("name"));
                if (listType == ListType.ANIME) {
                    statusLabelTxt.append(getString(R.string.anime_list_label));
                }else if (listType == ListType.MANGA){
                    statusLabelTxt.append(getString(R.string.manga_list_label));
                }
                statusLabel.setText(statusLabelTxt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


        initSpinner(R.id.statusSpinner, statusOptions, new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentStatus = new Pair<>((String) statusOptions.keySet().toArray()[i],
                        (String) statusOptions.values().toArray()[i]);
                if (!isInitialLoad) {
                    reloadList();
                }
                isInitialLoad = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        initSpinner(R.id.sortSpinner, sortByOptions, new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSort = new Pair<>((String) sortByOptions.keySet().toArray()[i],
                        (String) sortByOptions.values().toArray()[i]);
                if (!isInitialLoad) {
                    reloadList();
                }
                isInitialLoad = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        isInitialLoad = true;

        Intent intent = getIntent();
        listType = (ListType)intent.getSerializableExtra("type");

        initActionBar();
        initRecyclerView();
    }

    private void loadPage(){
        isLoading = true;

        pageNum++;
        UserListRequest.getUserList(UserListActivity.this, "@me", listType, currentStatus, currentSort, pageNum, response -> {
            try {
                JSONObject responseObject = new JSONObject(response);

                isLastPage = !responseObject.getJSONObject("paging").has("next");

                JSONArray list = responseObject.getJSONArray("data");
                for (int i = 0; i < list.length(); i++){
                    JSONObject json = list.getJSONObject(i);
                    UserListElement userListElement = new UserListElement(json, listType);
                    userListAdapter.add(userListElement);
                }
                isLoading = false;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    private void initActionBar(){
        Toolbar toolbar = findViewById(R.id.toolbar_user_list);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    public void initRecyclerView(){
        GridLayoutManager layoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            layoutManager = new GridLayoutManager(this, 2);
        }else{
            layoutManager = new GridLayoutManager(this, 1);
        }

        userListAdapter = new UserListAdapter();
        RecyclerView recyclerView = findViewById(R.id.recycle_view);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemViewCacheSize(10);
        recyclerView.setAdapter(userListAdapter);
        recyclerView.addOnScrollListener(new UserListScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                loadPage();
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });
    }

}
