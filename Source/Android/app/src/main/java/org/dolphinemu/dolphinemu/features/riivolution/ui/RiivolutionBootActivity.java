// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.features.riivolution.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.activities.EmulationActivity;
import org.dolphinemu.dolphinemu.features.riivolution.model.RiivolutionPatches;
import org.dolphinemu.dolphinemu.features.settings.model.StringSetting;
import org.dolphinemu.dolphinemu.utils.DirectoryInitialization;
import org.dolphinemu.dolphinemu.utils.ThemeHelper;

public class RiivolutionBootActivity extends AppCompatActivity
{
  private static final String ARG_GAME_PATH = "game_path";
  private static final String ARG_GAME_ID = "game_id";
  private static final String ARG_REVISION = "revision";
  private static final String ARG_DISC_NUMBER = "disc_number";

  private RiivolutionPatches mPatches;

  public static void launch(Context context, String gamePath, String gameId, int revision,
          int discNumber)
  {
    Intent launcher = new Intent(context, RiivolutionBootActivity.class);
    launcher.putExtra(ARG_GAME_PATH, gamePath);
    launcher.putExtra(ARG_GAME_ID, gameId);
    launcher.putExtra(ARG_REVISION, revision);
    launcher.putExtra(ARG_DISC_NUMBER, discNumber);
    context.startActivity(launcher);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    ThemeHelper.setTheme(this);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_riivolution_boot);

    Intent intent = getIntent();

    String path = getIntent().getStringExtra(ARG_GAME_PATH);
    String gameId = intent.getStringExtra(ARG_GAME_ID);
    int revision = intent.getIntExtra(ARG_REVISION, -1);
    int discNumber = intent.getIntExtra(ARG_DISC_NUMBER, -1);

    String loadPath = StringSetting.MAIN_LOAD_PATH.getStringGlobal();
    if (loadPath.isEmpty())
      loadPath = DirectoryInitialization.getUserDirectory() + "/Load";

    TextView textSdRoot = findViewById(R.id.text_sd_root);
    textSdRoot.setText(getString(R.string.riivolution_sd_root, loadPath + "/Riivolution"));

    Button buttonStart = findViewById(R.id.button_start);
    buttonStart.setOnClickListener((v) ->
    {
      if (mPatches != null)
        mPatches.saveConfig();

      EmulationActivity.launch(this, path, true);
    });

    new Thread(() ->
    {
      RiivolutionPatches patches = new RiivolutionPatches(gameId, revision, discNumber);
      patches.loadConfig();
      runOnUiThread(() -> populateList(patches));
    }).start();

    MaterialToolbar tb = findViewById(R.id.toolbar_riivolution);
    CollapsingToolbarLayout ctb = findViewById(R.id.toolbar_riivolution_layout);
    ctb.setTitle(getString(R.string.riivolution_riivolution));
    setSupportActionBar(tb);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    AppBarLayout appBarLayout = findViewById(R.id.appbar_riivolution);
    ThemeHelper.enableScrollTint(tb, appBarLayout, this);
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    if (mPatches != null)
      mPatches.saveConfig();
  }

  @Override
  public boolean onSupportNavigateUp()
  {
    onBackPressed();
    return true;
  }

  private void populateList(RiivolutionPatches patches)
  {
    mPatches = patches;

    RecyclerView recyclerView = findViewById(R.id.recycler_view);

    recyclerView.setAdapter(new RiivolutionAdapter(this, patches));
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }
}
