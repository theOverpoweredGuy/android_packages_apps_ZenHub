/*
 * Copyright (C) 2018 ZENX-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zenx.zen.hub.fragments.lockscreenambient.tabs;

import android.app.Activity;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.net.Uri;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto; 

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.zenx.support.colorpicker.ColorPickerPreference;
import com.zenx.support.preferences.CustomSeekBarPreference;
import com.zenx.support.preferences.SystemSettingSwitchPreference;
import com.zenx.support.preferences.SystemSettingEditTextPreference;

public class Ambient extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

   public static final String TAG = "EdgeLight";
    private static final String AMBIENT_LIGHT_COLOR = "ambient_light_color";
    private static final String AMBIENT_LIGHT_CUSTOM_COLOR = "ambient_light_custom_color";
    private static final String AMBIENT_LIGHT_DURATION = "ambient_light_duration";
    private static final String AMBIENT_LIGHT_REPEAT_COUNT = "ambient_light_repeat_count";
    private static final String AMBIENT_LIGHT_ALWAYS = "ambient_light_pulse_for_all";
    private static final String SYNTHOS_AMBIENT_TEXT_STRING = "synthos_ambient_text_string";
    private static final String SYNTHOS_AMBIENT_TEXT_ALIGNMENT = "synthos_ambient_text_alignment";
    private static final String SYNTHOS_AMBIENT_TEXT_FONT = "synthos_ambient_text_font";
    private static final String SYNTHOS_AMBIENT_TEXT_TYPE_COLOR = "synthos_ambient_text_type_color";
    private static final String SYNTHOS_AMBIENT_TEXT_COLOR = "synthos_ambient_text_color";
    private static final String FILE_AMBIENT_SELECT = "file_ambient_select";
    private static final String FILE_AMBIENT_VIDEO_SELECT = "file_ambient_video_select";

    private static final int REQUEST_PICK_IMAGE = 0;
    private static final int REQUEST_PICK_VIDEO = 1;

    private SystemSettingSwitchPreference mEdgeLightAlways;
    private ListPreference mEdgeLightColorMode;
    private ColorPickerPreference mEdgeLightColor;
    private CustomSeekBarPreference mEdgeLightDuration;
    private CustomSeekBarPreference mEdgeLightRepeatCount;

    private SystemSettingEditTextPreference mAmbientText;
    private ListPreference mAmbientTextAlign;
    private ListPreference mAmbientTextFonts;
    private ListPreference mAmbientTextTypeColor;
    private ColorPickerPreference mAmbientTextColor;

    private Preference mAmbientImage;
     private Preference mAmbientVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.zen_hub_ambient);

        mEdgeLightAlways = (SystemSettingSwitchPreference) findPreference(AMBIENT_LIGHT_ALWAYS);

        mEdgeLightColorMode = (ListPreference) findPreference(AMBIENT_LIGHT_COLOR);
        int edgeLightColorMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_COLOR, 0, UserHandle.USER_CURRENT);
        mEdgeLightColorMode.setValue(String.valueOf(edgeLightColorMode));
        mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntry());
        mEdgeLightColorMode.setOnPreferenceChangeListener(this);

        updateColorPrefs(edgeLightColorMode);
        mEdgeLightColor = (ColorPickerPreference) findPreference(AMBIENT_LIGHT_CUSTOM_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_CUSTOM_COLOR, 0xFF3980FF);
        mEdgeLightColor.setNewPreviewColor(edgeLightColor);
        String edgeLightColorHex = String.format("#%08x", (0xFF3980FF & edgeLightColor));
        if (edgeLightColorHex.equals("#ff3980ff")) {
            mEdgeLightColor.setSummary(R.string.default_string);
        } else {
            mEdgeLightColor.setSummary(edgeLightColorHex);
        }
        mEdgeLightColor.setOnPreferenceChangeListener(this);

        mEdgeLightDuration = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_DURATION);
        int lightDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_DURATION, 2, UserHandle.USER_CURRENT);
        mEdgeLightDuration.setValue(lightDuration);
        mEdgeLightDuration.setOnPreferenceChangeListener(this);

        mEdgeLightRepeatCount = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_REPEAT_COUNT);
        int edgeLightRepeatCount = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, 0, UserHandle.USER_CURRENT);
        mEdgeLightRepeatCount.setValue(edgeLightRepeatCount);
        mEdgeLightRepeatCount.setOnPreferenceChangeListener(this);

         mAmbientText = (SystemSettingEditTextPreference) findPreference(SYNTHOS_AMBIENT_TEXT_STRING);
        mAmbientText.setOnPreferenceChangeListener(this);

        // set ambient text alignment
        mAmbientTextAlign = (ListPreference) findPreference(SYNTHOS_AMBIENT_TEXT_ALIGNMENT);
        int align = Settings.System.getInt(getContentResolver(),
                Settings.System.SYNTHOS_AMBIENT_TEXT_ALIGNMENT, 3);
        mAmbientTextAlign.setValue(String.valueOf(align));
        mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntry());
        mAmbientTextAlign.setOnPreferenceChangeListener(this);

        // ambient text Fonts
        mAmbientTextFonts = (ListPreference) findPreference(SYNTHOS_AMBIENT_TEXT_FONT);
        mAmbientTextFonts.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_FONT, 8)));
        mAmbientTextFonts.setSummary(mAmbientTextFonts.getEntry());
        mAmbientTextFonts.setOnPreferenceChangeListener(this);

        // ambient text color type
        mAmbientTextTypeColor = (ListPreference) findPreference(SYNTHOS_AMBIENT_TEXT_TYPE_COLOR);
        mAmbientTextTypeColor.setValue(String.valueOf(Settings.System.getInt(
                getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_TYPE_COLOR, 0)));
        mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntry());
        mAmbientTextTypeColor.setOnPreferenceChangeListener(this);

        mAmbientTextColor = (ColorPickerPreference) findPreference(SYNTHOS_AMBIENT_TEXT_COLOR);
        mAmbientTextColor.setOnPreferenceChangeListener(this);
        int ambientTextColor = Settings.System.getInt(getContentResolver(),
                Settings.System.SYNTHOS_AMBIENT_TEXT_COLOR, 0xFF3980FF);
        String ambientTextColorHex = String.format("#%08x", (0xFF3980FF & ambientTextColor));
        if (ambientTextColorHex.equals("#ff3980ff")) {
            mAmbientTextColor.setSummary(R.string.default_string);
        } else {
            mAmbientTextColor.setSummary(ambientTextColorHex);
        }
        mAmbientTextColor.setNewPreviewColor(ambientTextColor);

        mAmbientImage = findPreference(FILE_AMBIENT_SELECT);
        mAmbientVideo = findPreference(FILE_AMBIENT_VIDEO_SELECT);

    }

       @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mAmbientImage) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
         } else if (preference == mAmbientVideo) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(intent, REQUEST_PICK_VIDEO);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEdgeLightColorMode) {
            int edgeLightColorMode = Integer.valueOf((String) newValue);
            int index = mEdgeLightColorMode.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_COLOR, edgeLightColorMode, UserHandle.USER_CURRENT);
            mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntries()[index]);
            updateColorPrefs(edgeLightColorMode);
            return true;
        } else if (preference == mEdgeLightColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff3980ff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_CUSTOM_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightDuration) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_DURATION, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mEdgeLightRepeatCount) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.AMBIENT_LIGHT_REPEAT_COUNT, value, UserHandle.USER_CURRENT);
            return true;
         } else if (preference == mAmbientText) {
            String value = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.SYNTHOS_AMBIENT_TEXT_STRING, value);
            return true;
        } else if (preference == mAmbientTextAlign) {
            int align = Integer.valueOf((String) newValue);
            int index = mAmbientTextAlign.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYNTHOS_AMBIENT_TEXT_ALIGNMENT, align);
            mAmbientTextAlign.setSummary(mAmbientTextAlign.getEntries()[index]);
            return true;
        } else if (preference == mAmbientTextFonts) {
            Settings.System.putInt(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_FONT,
                    Integer.valueOf((String) newValue));
            mAmbientTextFonts.setValue(String.valueOf(newValue));
            mAmbientTextFonts.setSummary(mAmbientTextFonts.getEntry());
            return true;
        } else if (preference == mAmbientTextTypeColor) {
            Settings.System.putInt(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_TEXT_TYPE_COLOR,
                    Integer.valueOf((String) newValue));
            mAmbientTextTypeColor.setValue(String.valueOf(newValue));
            mAmbientTextTypeColor.setSummary(mAmbientTextTypeColor.getEntry());
            return true;
        } else if (preference == mAmbientTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ff3980ff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SYNTHOS_AMBIENT_TEXT_COLOR, intHex);
            return true;
          }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri imageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_CUSTOM_IMAGE, imageUri.toString());
        } else if (requestCode == REQUEST_PICK_VIDEO) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri videoUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.SYNTHOS_AMBIENT_CUSTOM_VIDEO, videoUri.toString());
        }
    }

    private void updateColorPrefs(int edgeLightColorMode) {
        if (mEdgeLightColor != null) {
            if (edgeLightColorMode == 3) {
                mEdgeLightColor.setVisible(true);
            } else {
                mEdgeLightColor.setVisible(false);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ZENX_SETTINGS;
    }
}