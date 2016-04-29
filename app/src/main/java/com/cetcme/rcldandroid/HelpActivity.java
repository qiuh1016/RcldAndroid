package com.cetcme.rcldandroid;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class HelpActivity extends AppCompatActivity {

    //TODO: help View
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setTitle("帮助");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem add = menu.add(0,0,0,"Add");
        MenuItem del = menu.add(0,0,0,"Del");
        MenuItem save = menu.add(0,0,0,"save");
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        del.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        save.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        add.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                System.out.println("add");
                return false;
            }
        });
        del.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                System.out.println("del");
                return false;
            }
        });
        save.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                System.out.println("save");
                return false;
            }
        });

        return true;
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu, menu);
//        return true;
    }

    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in_no_alpha,
                R.anim.push_right_out_no_alpha);
    }
}
