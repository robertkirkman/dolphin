// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.databinding.ActivityConvertBinding;
import org.dolphinemu.dolphinemu.fragments.ConvertFragment;
import org.dolphinemu.dolphinemu.utils.InsetsHelper;
import org.dolphinemu.dolphinemu.utils.ThemeHelper;

public class ConvertActivity extends AppCompatActivity
{
  private static final String ARG_GAME_PATH = "game_path";

  public static void launch(Context context, String gamePath)
  {
    Intent launcher = new Intent(context, ConvertActivity.class);
    launcher.putExtra(ARG_GAME_PATH, gamePath);
    context.startActivity(launcher);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    ThemeHelper.setTheme(this);

    super.onCreate(savedInstanceState);

    ActivityConvertBinding binding = ActivityConvertBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    String path = getIntent().getStringExtra(ARG_GAME_PATH);

    ConvertFragment fragment = (ConvertFragment) getSupportFragmentManager()
            .findFragmentById(R.id.fragment_convert);
    if (fragment == null)
    {
      fragment = ConvertFragment.newInstance(path);
      getSupportFragmentManager().beginTransaction().add(R.id.fragment_convert, fragment).commit();
    }

    binding.toolbarConvertLayout.setTitle(getString(R.string.convert_convert));
    setSupportActionBar(binding.toolbarConvert);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    InsetsHelper.setUpAppBarWithScrollView(this, binding.appbarConvert, binding.scrollViewConvert,
            binding.workaroundView);
    ThemeHelper.enableScrollTint(this, binding.toolbarConvert, binding.appbarConvert);
  }

  @Override
  public boolean onSupportNavigateUp()
  {
    onBackPressed();
    return true;
  }
}
