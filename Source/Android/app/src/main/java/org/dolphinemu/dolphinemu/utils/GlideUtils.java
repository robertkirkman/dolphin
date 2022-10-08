// SPDX-License-Identifier: GPL-2.0-or-later

package org.dolphinemu.dolphinemu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import org.dolphinemu.dolphinemu.R;
import org.dolphinemu.dolphinemu.features.settings.model.BooleanSetting;
import org.dolphinemu.dolphinemu.model.GameFile;
import org.dolphinemu.dolphinemu.viewholders.GameViewHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlideUtils
{
  private static final ExecutorService executor = Executors.newSingleThreadExecutor();

  public static void loadGameBanner(ImageView imageView, GameFile gameFile)
  {
    Context context = imageView.getContext();
    int[] vector = gameFile.getBanner();
    int width = gameFile.getBannerWidth();
    int height = gameFile.getBannerHeight();
    if (width > 0 && height > 0)
    {
      Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      bitmap.setPixels(vector, 0, width, 0, 0, width, height);
      Glide.with(context)
              .load(bitmap)
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .centerCrop()
              .into(imageView);
    }
    else
    {
      Glide.with(context)
              .load(R.drawable.no_banner)
              .into(imageView);
    }
  }

  public static void loadGameCover(GameViewHolder gameViewHolder, ImageView imageView,
          GameFile gameFile)
  {
    if (BooleanSetting.MAIN_SHOW_GAME_TITLES.getBooleanGlobal() && gameViewHolder != null)
    {
      gameViewHolder.textGameTitle.setText(gameFile.getTitle());
      gameViewHolder.textGameTitle.setVisibility(View.VISIBLE);
      gameViewHolder.textGameTitleInner.setVisibility(View.GONE);
      gameViewHolder.textGameCaption.setVisibility(View.VISIBLE);
    }
    else if (gameViewHolder != null)
    {
      gameViewHolder.textGameTitleInner.setText(gameFile.getTitle());
      gameViewHolder.textGameTitle.setVisibility(View.GONE);
      gameViewHolder.textGameCaption.setVisibility(View.GONE);
    }

    String customCoverPath = gameFile.getCustomCoverPath();
    Uri customCoverUri = null;
    boolean customCoverExists = false;
    if (ContentHandler.isContentUri(customCoverPath))
    {
      try
      {
        customCoverUri = ContentHandler.unmangle(customCoverPath);
        customCoverExists = true;
      }
      catch (FileNotFoundException | SecurityException ignored)
      {
        // Let customCoverExists remain false
      }
    }
    else
    {
      customCoverUri = Uri.parse(customCoverPath);
      customCoverExists = new File(customCoverPath).exists();
    }

    Context context = imageView.getContext();
    File cover;
    if (customCoverExists)
    {
      Glide.with(context)
              .load(customCoverUri)
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .centerCrop()
              .listener(new RequestListener<Drawable>()
              {
                @Override public boolean onLoadFailed(@Nullable GlideException e, Object model,
                        Target<Drawable> target, boolean isFirstResource)
                {
                  GlideUtils.enableInnerTitle(gameViewHolder, imageView);
                  return false;
                }

                @Override public boolean onResourceReady(Drawable resource, Object model,
                        Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
                {
                  GlideUtils.disableInnerTitle(gameViewHolder);
                  return false;
                }
              })
              .into(imageView);
    }
    else if ((cover = new File(gameFile.getCoverPath(context))).exists())
    {
      Glide.with(context)
              .load(cover)
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .centerCrop()
              .listener(new RequestListener<Drawable>()
              {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                        Target<Drawable> target, boolean isFirstResource)
                {
                  GlideUtils.enableInnerTitle(gameViewHolder, imageView);
                  return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model,
                        Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
                {
                  GlideUtils.disableInnerTitle(gameViewHolder);
                  return false;
                }
              })
              .into(imageView);
    }
    else if (BooleanSetting.MAIN_USE_GAME_COVERS.getBooleanGlobal())
    {
      Glide.with(context)
              .load(CoverHelper.buildGameTDBUrl(gameFile, CoverHelper.getRegion(gameFile)))
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .centerCrop()
              .listener(new RequestListener<Drawable>()
              {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                        Target<Drawable> target, boolean isFirstResource)
                {
                  GlideUtils.enableInnerTitle(gameViewHolder, imageView);
                  return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model,
                        Target<Drawable> target, DataSource dataSource, boolean isFirstResource)
                {
                  GlideUtils.disableInnerTitle(gameViewHolder);
                  return false;
                }
              })
              .into(new CustomTarget<Drawable>()
              {
                @Override
                public void onResourceReady(@NonNull Drawable resource,
                        @Nullable Transition<? super Drawable> transition)
                {
                  Bitmap cover = ((BitmapDrawable) resource).getBitmap();
                  executor.execute(
                          () -> CoverHelper.saveCover(cover, gameFile.getCoverPath(context)));
                  imageView.setImageBitmap(cover);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder)
                {
                }
              });
    }
    else
    {
      enableInnerTitle(gameViewHolder, imageView);
    }
  }

  private static void enableInnerTitle(GameViewHolder gameViewHolder, ImageView imageView)
  {
    Glide.with(imageView.getContext())
            .load(R.drawable.no_banner)
            .into(imageView);

    if (gameViewHolder != null && !BooleanSetting.MAIN_SHOW_GAME_TITLES.getBooleanGlobal())
    {
      gameViewHolder.textGameTitleInner.setVisibility(View.VISIBLE);
    }
  }

  private static void disableInnerTitle(GameViewHolder gameViewHolder)
  {
    if (gameViewHolder != null && !BooleanSetting.MAIN_SHOW_GAME_TITLES.getBooleanGlobal())
    {
      gameViewHolder.textGameTitleInner.setVisibility(View.GONE);
    }
  }
}
