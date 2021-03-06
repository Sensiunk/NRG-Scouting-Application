package com.competitionapp.nrgscouting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
//Read and Approved by Acchindra.
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ActivityUtility{
    Toolbar toolbar = null;
    FloatingActionButton fab;
    FloatingActionButton fab2;
    RefreshableFragment currentFragment;
    FragmentType currentType;

    public static String CURRENT_VERSION = "2.5_0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);

        //Set about frag initially when app opens
        final MatchFragment matchFragment = new MatchFragment();

        currentFragment = matchFragment;
        currentType = FragmentType.MATCH;

        final android.support.v4.app.FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, matchFragment, "mat");
        fragmentTransaction.commit();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab2.hide();
        /*MatchFragment mat = (MatchFragment) getSupportFragmentManager().findFragmentByTag("mat");
        mat.refreshEntryList();*/

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, TeamSearchPop.class), 0);
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, TeamSearchPopSpec.class), 0);
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setActionBarTitle("Match Scouting");
    }
    public static void cacheSaver(String fileString) throws FileNotFoundException{
        File cacheDir=new File("/storage/emulated/0/Hi");
        cacheDir.mkdirs();
        File cacheFile=new File(cacheDir.getPath()+File.separator+"CLASSIFIED.txt");
        try {
            if(!cacheFile.exists()) {
                cacheFile.createNewFile();
            }
            PrintStream printer=new PrintStream(cacheFile);
            printer.print(fileString);
        }
        catch(IOException IO){
            //oops
        }
    }
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_export:
                //Toast.makeText(MainActivity.this, (String) "help", Toast.LENGTH_SHORT).show();
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Export Entries")
                        .setMessage(RetrieveDataFromPrefs())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(R.drawable.ic_download_yellow)
                        .setNegativeButton("Copy to Clipboard", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                copyToClipboard(RetrieveDataFromPrefs());

                                Toast.makeText(MainActivity.this, "Entries copied to clipboard.", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                        .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                TextView textView = (TextView) dialog.findViewById(android.R.id.message);
                textView.setScroller(new Scroller(this));
                textView.setVerticalScrollBarEnabled(true);
                textView.setTextIsSelectable(true);
                textView.setMovementMethod(new ScrollingMovementMethod());
                return true;
            case R.id.action_settings:
                Toast.makeText(MainActivity.this, (String) "No settings yet.", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_clearMemory:
                if(currentType.equals(FragmentType.MATCH)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Delete ALL stored match entries?");
                    builder.setMessage("Warning: This cannot be undone!");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            if (sharedPreferences.contains("MatchEntryList") && sharedPreferences.getStringSet("MatchEntryList", null) != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putStringSet("MatchEntryList", null);
                                editor.commit();
                                Toast.makeText(MainActivity.this, (String) "Cleared all stored match entries.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, (String) "No stored match entries found.", Toast.LENGTH_LONG).show();
                            }

                            currentFragment.refreshFragment();
                        }
                    });
                    builder.show();
                    return true;
                } else if (currentType.equals(FragmentType.SPECIALIST)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Delete ALL stored specialist entries?");
                    builder.setMessage("Warning: This cannot be undone!");
                    builder.setCancelable(true);
                    builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            if (sharedPreferences.contains("SpecialistEntryList") && sharedPreferences.getStringSet("SpecialistEntryList", null) != null) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putStringSet("SpecialistEntryList", null);
                                editor.commit();
                                Toast.makeText(MainActivity.this, (String) "Cleared all stored specialist entries.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, (String) "No stored specialist entries found.", Toast.LENGTH_LONG).show();
                            }

                            currentFragment.refreshFragment();
                        }
                    });
                    builder.show();
                    return true;
                }

        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public String RetrieveDataFromPrefs() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(currentType.equals(FragmentType.MATCH)) {
            if (sharedPref.contains("MatchEntryList") && sharedPref.getStringSet("MatchEntryList", null) != null) {
                String printList = "";

                for (String x : sharedPref.getStringSet("MatchEntryList", null)) {
                    if (sharedPref.contains(x)) {
                        printList += sharedPref.getString(x, "");
                    }
                }

                return printList;
            }
        } else if (currentType.equals(FragmentType.SPECIALIST)){
            if (sharedPref.contains("SpecialistEntryList") && sharedPref.getStringSet("SpecialistEntryList", null) != null) {
                String printList = "";

                for (String x : sharedPref.getStringSet("SpecialistEntryList", null)) {
                    if (sharedPref.contains(x)) {
                        printList += sharedPref.getString(x, "");
                    }
                }

                return printList;
            }
        }

        return "(No data found.)";
    }

    public void copyToClipboard(String copy) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(copy);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("?", copy);
            clipboard.setPrimaryClip(clip);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(currentFragment != null) {
            currentFragment.refreshFragment();
        }

    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_match) {
            MatchFragment matchFragment = new MatchFragment();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();

            currentFragment = matchFragment;
            currentType = FragmentType.MATCH;

            fragmentTransaction.replace(R.id.fragment_container, matchFragment, "mat");
            fragmentTransaction.commit();
            toolbar = (Toolbar)findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            fab.show();
            fab2.hide();

            setActionBarTitle("Match Scouting");
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();


        } else if (id == R.id.nav_about) {

            About abfragment = new About();
            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, abfragment);
            fragmentTransaction.commit();
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            fab.hide();
            fab2.hide();

            setActionBarTitle("About");
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

        } else if(id == R.id.nav_spec) {
            SpecialistFragment specFragment = new SpecialistFragment();

            currentFragment = specFragment;
            currentType = FragmentType.SPECIALIST;

            android.support.v4.app.FragmentTransaction fragmentTransaction =
                    getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, specFragment, "spec");
            fragmentTransaction.commit();
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            fab2.show();
            fab.hide();

            setActionBarTitle("Specialist Scouting");
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
