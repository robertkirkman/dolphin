package org.dolphinemu.dolphinemu.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.color.MaterialColors;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.ui.main.ThemeProvider;

public class ThemeHelper
{
  public static final String CURRENT_THEME = "current_theme";

  public static final int DEFAULT = 0;
  public static final int MONET = 1;
  public static final int MATERIAL_DEFAULT = 2;
  public static final int GREEN = 3;
  public static final int PINK = 4;

  public static final float NAV_BAR_ALPHA = 0.9f;

  public static void setTheme(@NonNull AppCompatActivity activity)
  {
    // We have to use shared preferences in addition to Dolphin's settings to guarantee that the
    // requested theme id is ready before the onCreate method of any given Activity.
    SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    switch (preferences.getInt(CURRENT_THEME, DEFAULT))
    {
      case DEFAULT:
        activity.setTheme(R.style.Theme_Dolphin_Main);
        break;

      case MONET:
        activity.setTheme(R.style.Theme_Dolphin_Main_MaterialYou);
        break;

      case MATERIAL_DEFAULT:
        activity.setTheme(R.style.Theme_Dolphin_Main_Material);
        break;

      case GREEN:
        activity.setTheme(R.style.Theme_Dolphin_Main_Green);
        break;

      case PINK:
        activity.setTheme(R.style.Theme_Dolphin_Main_Pink);
        break;
    }

    // Since the top app bar matches the color of the status bar, devices below API 23 have to get a
    // black status bar since their icons do not adapt based on background color
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
    {
      activity.getWindow().setStatusBarColor(
              ContextCompat.getColor(activity.getApplicationContext(), android.R.color.black));
    }
  }

  public static void saveTheme(@NonNull Activity activity, int themeValue)
  {
    SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    preferences.edit().putInt(CURRENT_THEME, themeValue).apply();
  }

  public static void deleteThemeKey(@NonNull Activity activity)
  {
    SharedPreferences preferences =
            PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
    preferences.edit().remove(CURRENT_THEME).apply();
    activity.setTheme(R.style.Theme_Dolphin_Main);
    activity.recreate();
  }

  public static void setCorrectTheme(AppCompatActivity activity)
  {
    int currentTheme = ((ThemeProvider) activity).getThemeId();
    setTheme(activity);

    if (currentTheme != ((ThemeProvider) activity).getThemeId())
    {
      activity.recreate();
    }
  }

  public static void setStatusBarColor(AppCompatActivity activity, @ColorInt int color)
  {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
    {
      activity.getWindow().setStatusBarColor(
              ContextCompat.getColor(activity.getApplicationContext(), android.R.color.black));
    }
    else
    {
      activity.getWindow().setStatusBarColor(color);
    }
  }

  public static void setNavigationBarColor(Activity activity, @ColorInt int color)
  {
    int gestureType = InsetsHelper.getSystemGestureType(activity.getApplicationContext());
    int orientation = activity.getResources().getConfiguration().orientation;

    // Use black if the Android version is too low to support changing button colors
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1)
    {
      activity.getWindow().setNavigationBarColor(
              ContextCompat.getColor(activity.getApplicationContext(), android.R.color.black));
    }
    // Use a solid color when the navigation bar is on the left/right edge of the screen
    else if ((gestureType == InsetsHelper.THREE_BUTTON_NAVIGATION ||
            gestureType == InsetsHelper.TWO_BUTTON_NAVIGATION) &&
            orientation == Configuration.ORIENTATION_LANDSCAPE)
    {
      activity.getWindow().setNavigationBarColor(color);
    }
    // Use semi-transparent color when in portrait mode with three/two button navigation to
    // partially see list items behind the navigation bar
    else if (gestureType == InsetsHelper.THREE_BUTTON_NAVIGATION ||
            gestureType == InsetsHelper.TWO_BUTTON_NAVIGATION)
    {
      activity.getWindow().setNavigationBarColor(getColorWithOpacity(color, NAV_BAR_ALPHA));
    }
    // Use transparent color when using gesture navigation
    else
    {
      activity.getWindow().setNavigationBarColor(
              ContextCompat.getColor(activity.getApplicationContext(),
                      android.R.color.transparent));
    }
  }

  public static void enableScrollTint(AppCompatActivity activity, MaterialToolbar toolbar,
          @NonNull AppBarLayout appBarLayout)
  {
    appBarLayout.addOnOffsetChangedListener((layout, verticalOffset) ->
    {
      if (-verticalOffset >= (layout.getTotalScrollRange() / 2))
      {
        @ColorInt int color = MaterialColors.getColor(toolbar, R.attr.colorSurfaceVariant);
        toolbar.setBackgroundColor(color);
        setStatusBarColor(activity, color);
      }
      else
      {
        @ColorInt int statusBarColor = ContextCompat.getColor(activity.getApplicationContext(),
                android.R.color.transparent);
        @ColorInt int appBarColor = MaterialColors.getColor(toolbar, R.attr.colorSurface);
        toolbar.setBackgroundColor(appBarColor);
        setStatusBarColor(activity, statusBarColor);
      }
    });
  }

  public static void enableStatusBarScrollTint(AppCompatActivity activity,
          @NonNull AppBarLayout appBarLayout)
  {
    appBarLayout.addOnOffsetChangedListener((layout, verticalOffset) ->
    {
      if (-verticalOffset > 0)
      {
        @ColorInt int color = MaterialColors.getColor(appBarLayout, R.attr.colorSurface);
        setStatusBarColor(activity, color);
      }
      else
      {
        @ColorInt int statusBarColor = ContextCompat.getColor(activity.getApplicationContext(),
                android.R.color.transparent);
        setStatusBarColor(activity, statusBarColor);
      }
    });
  }

  @RequiresApi(api = Build.VERSION_CODES.O_MR1) @ColorInt
  private static int getColorWithOpacity(@ColorInt int color, float alphaFactor)
  {
    return Color.argb(Math.round(alphaFactor * Color.alpha(color)), Color.red(color),
            Color.green(color), Color.blue(color));
  }
}
