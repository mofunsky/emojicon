/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockerhieu.emojicon;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rockerhieu.emojicon.emoji.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com).
 */
public class EmojiconsFragment extends Fragment implements ViewPager.OnPageChangeListener {
    private int mEmojiTabLastSelectedIndex = -1;
    private PagerAdapter mEmojisAdapter;
    private boolean mUseSystemDefault = false;
    private LinearLayout viewIndicator;
    private Bitmap selected_page_indicator;
    private Bitmap unselect_page_indicator;
    private List<ImageView> image_list;

    //fix by reason
    private final static int MAX_COUNT_PER_PAGE = 27;

    private static final String USE_SYSTEM_DEFAULT_KEY = "useSystemDefaults";

    public static EmojiconsFragment newInstance(boolean useSystemDefault) {
        EmojiconsFragment fragment = new EmojiconsFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(USE_SYSTEM_DEFAULT_KEY, useSystemDefault);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emojicons, container, false);
        final ViewPager emojisPager = (ViewPager) view.findViewById(R.id.emojis_pager);
        emojisPager.setOnPageChangeListener(this);

        viewIndicator = (LinearLayout) view.findViewById(R.id.viewIndicator);
        List<EmojiconGridFragment> fragments = new ArrayList<EmojiconGridFragment>();
        Emojicon[] data = new Emojicon[MAX_COUNT_PER_PAGE + 1];
        int i = 0, j = 0;

        image_list = new ArrayList<ImageView>();
        selected_page_indicator = getRoundedCornerBitmap(12, Color.parseColor("#999999"));
        unselect_page_indicator = getRoundedCornerBitmap(12, Color.parseColor("#e4e4e4"));
        LinearLayout.LayoutParams layout_params = new LinearLayout.LayoutParams(12, 12);
        layout_params.setMargins(10, 10, 10, 10);
        while (i < People.DATA.length) {
            if (i > 0 && i % MAX_COUNT_PER_PAGE == 0) {
                data[j] = Emojicon.fromCodePoint(0x1f001);
                fragments.add(EmojiconGridFragment.newInstance(data, null, mUseSystemDefault));
                ImageView img = new ImageView(this.getActivity());
                img.setImageBitmap(unselect_page_indicator);
                img.setLayoutParams(layout_params);
                viewIndicator.addView(img);
                image_list.add(img);
                data = new Emojicon[MAX_COUNT_PER_PAGE + 1];
                j = 0;
            }
            data[j] = People.DATA[i];
            i++;
            j++;
        }

        for (ImageView img : image_list) {
            img.setImageBitmap(unselect_page_indicator);
        }
        image_list.get(0).setImageBitmap(selected_page_indicator);
        emojisPager.setOffscreenPageLimit(image_list.size());
        mEmojisAdapter = new EmojisPagerAdapter(getFragmentManager(), fragments);
        emojisPager.setAdapter(mEmojisAdapter);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static void input(EditText editText, Emojicon emojicon) {
        if (editText == null || emojicon == null) {
            return;
        }

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < 0) {
            editText.append(emojicon.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojicon.getEmoji(), 0, emojicon.getEmoji().length());
        }
    }

    public static void backspace(EditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
        if (mEmojiTabLastSelectedIndex == i) {
            return;
        }
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                mEmojiTabLastSelectedIndex = i;
                for (ImageView img : image_list) {
                    img.setImageBitmap(unselect_page_indicator);
                }
                image_list.get(i).setImageBitmap(selected_page_indicator);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    private static class EmojisPagerAdapter extends FragmentStatePagerAdapter {
        private List<EmojiconGridFragment> fragments;

        public EmojisPagerAdapter(FragmentManager fm, List<EmojiconGridFragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUseSystemDefault = getArguments().getBoolean(USE_SYSTEM_DEFAULT_KEY);
        } else {
            mUseSystemDefault = false;
        }
    }

    public static Bitmap getRoundedCornerBitmap(int radius, int color) {
        try {
            Bitmap output = Bitmap.createBitmap(radius, radius, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final RectF rectF = new RectF(new Rect(0, 0, radius, radius));
            final float roundPx = 14;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
