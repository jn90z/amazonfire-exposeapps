package com.jn90z.fireexposeapps;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Launches apps already installed and enabled for the current Android user.
 * Does not elevate privileges, install packages, or change Amazon Kids controls.
 */
public class MainActivity extends Activity {
    private static final String DAYLIO = "net.daylio";
    private LinearLayout content;
    private PackageManager pm;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        pm = getPackageManager();
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        content.setPadding(pad, pad, pad, pad);
        scroll.addView(content);
        setContentView(scroll);
        showHome();
    }

    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String message, int size) {
        TextView v = new TextView(this);
        v.setText(message);
        v.setTextSize(size);
        v.setTextColor(Color.DKGRAY);
        v.setPadding(0, dp(8), 0, dp(12));
        content.addView(v);
        return v;
    }

    private void addButton(String label, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(click);
        content.addView(b);
    }

    private void showHome() {
        content.removeAllViews();
        text("Expose Apps", 24);
        text("Runs apps already installed for this profile. Parent controls and installation settings are unchanged.", 14);
        addButton("Open Daylio", v -> openApp(DAYLIO));
        addButton("Show installed launchable apps", v -> showApps());
    }

    private void openApp(String packageName) {
        try {
            Intent launch = pm.getLaunchIntentForPackage(packageName);
            if (launch == null && DAYLIO.equals(packageName)) {
                launch = new Intent(Intent.ACTION_MAIN);
                launch.setClassName("net.daylio", "net.daylio.activities.OverviewActivity");
            }
            if (launch == null) {
                Toast.makeText(this, "App unavailable in this profile: " + packageName, Toast.LENGTH_LONG).show();
                return;
            }
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        } catch (ActivityNotFoundException | SecurityException error) {
            Toast.makeText(this, "Cannot open app: " + error.getMessage(), Toast.LENGTH_LONG).show();
        } catch (RuntimeException error) {
            Toast.makeText(this, "Launch failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showApps() {
        content.removeAllViews();
        text("Installed apps", 24);
        addButton("Back", v -> showHome());
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> discovered = pm.queryIntentActivities(query, 0);
        ArrayList<ResolveInfo> apps = new ArrayList<>();
        for (ResolveInfo info : discovered) {
            if (info.activityInfo == null || info.activityInfo.packageName.equals(getPackageName())) continue;
            apps.add(info);
        }
        Collections.sort(apps, Comparator.comparing(
            info -> String.valueOf(info.loadLabel(pm)), String.CASE_INSENSITIVE_ORDER
        ));
        if (apps.isEmpty()) text("No launchable apps were returned for this profile.", 15);
        for (ResolveInfo info : apps) {
            String pkg = info.activityInfo.packageName;
            String label = String.valueOf(info.loadLabel(pm));
            addButton(label, v -> openApp(pkg));
        }
    }
}
