/*
 * Copyright (C) 2020 ZenX-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zenx.zen.hub.fragments.screenanimation.tabs;

import static com.zenx.zen.hub.utils.Utils.handleOverlays;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.*;
import android.provider.Settings;
import com.android.internal.util.zenx.Utils;
import com.android.internal.util.zenx.ThemesUtils;

import android.content.om.IOverlayManager;
import android.os.ServiceManager;
import android.content.om.OverlayInfo;
import android.graphics.Color;
import android.os.SystemProperties;
import android.os.RemoteException;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.NightModePreferenceController;
import com.android.settings.display.ThemePreferenceController;
import com.android.settings.search.Indexable;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.search.SearchIndexable;
import com.android.internal.logging.nano.MetricsProto;

import com.zenx.support.preferences.SystemSettingListPreference;
import com.zenx.support.preferences.CustomSeekBarPreference;
import com.zenx.support.colorpicker.ColorPickerPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Ui extends DashboardFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "DisplaySettings";
    private static final String DUAL_STATUSBAR_ROW_MODE = "dual_statusbar_row_mode";
    private static final String DUAL_ROW_DATAUSAGE = "dual_row_datausage";
    private static final String ZENHUB_ICON_TYPE = "zenhub_icon_type";
    private static final String ZENHUB_ICON_SIZE = "zenhub_icon_size";
    private static final String CUSTOM_STATUSBAR_HEIGHT = "custom_statusbar_height";
    private static final String UI_STYLE = "ui_style";
    private static final String ACCENT_PRESET = "accent_preset";
    private static final String ACCENT_COLOR = "accent_color";
    private static final String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";

    private SystemSettingListPreference mStatusbarDualRowMode;
    private SystemSettingListPreference mDualRowDataUsageMode;
    private SystemSettingListPreference mZenHubIconType;
    private SystemSettingListPreference mZenHubIconSize;
    private CustomSeekBarPreference mCustomStatusbarHeight;
    private ListPreference mUIStyle;
    private ListPreference mAccentPreset;
    private ColorPickerPreference mThemeColor;

    private IOverlayManager mOverlayManager;


    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ZENX_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.zen_hub_ui;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        mStatusbarDualRowMode = (SystemSettingListPreference) findPreference(DUAL_STATUSBAR_ROW_MODE);
        int statusbarDualRowMode = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.DUAL_STATUSBAR_ROW_MODE, 0, UserHandle.USER_CURRENT);
        mStatusbarDualRowMode.setValue(String.valueOf(statusbarDualRowMode));
        mStatusbarDualRowMode.setSummary(mStatusbarDualRowMode.getEntry());
        mStatusbarDualRowMode.setOnPreferenceChangeListener(this);

        mZenHubIconType = (SystemSettingListPreference) findPreference(ZENHUB_ICON_TYPE);
        int zenHubIconType = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.ZENHUB_ICON_TYPE, 0, UserHandle.USER_CURRENT);
        mZenHubIconType.setValue(String.valueOf(zenHubIconType));
        mZenHubIconType.setSummary(mZenHubIconType.getEntry());
        mZenHubIconType.setOnPreferenceChangeListener(this);

        mCustomStatusbarHeight = (CustomSeekBarPreference) findPreference(CUSTOM_STATUSBAR_HEIGHT);
        int customStatusbarHeight = Settings.System.getIntForUser(getActivity().getContentResolver(),
                Settings.System.CUSTOM_STATUSBAR_HEIGHT, getResources().getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height), UserHandle.USER_CURRENT);
        mCustomStatusbarHeight.setValue(customStatusbarHeight);
        mCustomStatusbarHeight.setOnPreferenceChangeListener(this);

        mUIStyle = (ListPreference) findPreference(UI_STYLE);
        int UIStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.UI_STYLE, 0);
        int UIStyleValue = getOverlayPosition(ThemesUtils.UI_THEMES);
        if (UIStyleValue != 0) {
            mUIStyle.setValue(String.valueOf(UIStyle));
        }
        mUIStyle.setSummary(mUIStyle.getEntry());
        mUIStyle.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference == mUIStyle) {
                    String value = (String) newValue;
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.UI_STYLE, Integer.valueOf(value));
                    int valueIndex = mUIStyle.findIndexOfValue(value);
                    mUIStyle.setSummary(mUIStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.UI_THEMES);
                    if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayManager);
                    }
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.UI_THEMES[valueIndex],
                                true, mOverlayManager);
                    }
                    return true;
                }
                return false;
            }
       });

        mOverlayManager = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
        mThemeColor = (ColorPickerPreference) findPreference(ACCENT_COLOR);
        String colorVal = SystemProperties.get(ACCENT_COLOR_PROP, "-1");
        try {
            int color = "-1".equals(colorVal)
                    ? Color.WHITE
                    : Color.parseColor("#" + colorVal);
            mThemeColor.setNewPreviewColor(color);
        }
        catch (Exception e) {
            mThemeColor.setNewPreviewColor(Color.WHITE);
        }
        mThemeColor.setOnPreferenceChangeListener(this);

        handleDataUsePreferences();
        handleZenHubIconPreferences();
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getSettingsLifecycle());
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(
            Context context, Lifecycle lifecycle) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new NightModePreferenceController(context));
        controllers.add(new ThemePreferenceController(context));
        return controllers;
    }

    @Override
    public void onResume() {
        super.onResume();
        handleDataUsePreferences();
        handleZenHubIconPreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        handleDataUsePreferences();
        handleZenHubIconPreferences();
    }

    private void handleDataUsePreferences() {

        int dualRowMode = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DUAL_STATUSBAR_ROW_MODE, 0);

        if(dualRowMode == 3) {
            mDualRowDataUsageMode = (SystemSettingListPreference) findPreference(DUAL_ROW_DATAUSAGE);
            int dualRowDataUsageMode = Settings.System.getIntForUser(getActivity().getContentResolver(),
                    Settings.System.DUAL_ROW_DATAUSAGE, 0, UserHandle.USER_CURRENT);
            mDualRowDataUsageMode.setValue(String.valueOf(dualRowDataUsageMode));
            mDualRowDataUsageMode.setSummary(mDualRowDataUsageMode.getEntry());
            mDualRowDataUsageMode.setOnPreferenceChangeListener(this);
            mDualRowDataUsageMode.setVisible(true);
        } else {
            mDualRowDataUsageMode = (SystemSettingListPreference) findPreference(DUAL_ROW_DATAUSAGE);
            if(mDualRowDataUsageMode != null) {
                mDualRowDataUsageMode.setVisible(false);
            }
        }

    }
    private void handleZenHubIconPreferences() {

        int iconType = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ZENHUB_ICON_TYPE, 0);

        if(iconType == 1) {
            mZenHubIconSize = (SystemSettingListPreference) findPreference(ZENHUB_ICON_SIZE);
            int zenHubIconSize = Settings.System.getIntForUser(getActivity().getContentResolver(),
                    Settings.System.ZENHUB_ICON_SIZE, 0, UserHandle.USER_CURRENT);
            mZenHubIconSize.setValue(String.valueOf(zenHubIconSize));
            mZenHubIconSize.setSummary(mZenHubIconSize.getEntry());
            mZenHubIconSize.setOnPreferenceChangeListener(this);
            mZenHubIconSize.setVisible(true);
        } else {
            mZenHubIconSize = (SystemSettingListPreference) findPreference(ZENHUB_ICON_SIZE);
            if(mZenHubIconSize != null) {
                mZenHubIconSize.setVisible(false);
            }
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusbarDualRowMode) {
            int statusbarDualRowMode = Integer.parseInt((String) newValue);
            int statusbarDualRowModeIndex = mStatusbarDualRowMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.QS_BATTERY_MODE, statusbarDualRowMode);
            mStatusbarDualRowMode.setSummary(mStatusbarDualRowMode.getEntries()[statusbarDualRowModeIndex]);
            Utils.showSystemUiRestartDialog(getContext());
            handleDataUsePreferences();
            return true;
        } else if (preference == mDualRowDataUsageMode) {
            int dualRowDataUsageMode = Integer.parseInt((String) newValue);
            int dualRowDataUsageModeIndex = mDualRowDataUsageMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DUAL_ROW_DATAUSAGE, dualRowDataUsageMode);
            mDualRowDataUsageMode.setSummary(mDualRowDataUsageMode.getEntries()[dualRowDataUsageModeIndex]);
            return true;
        } else if (preference == mZenHubIconSize) {
            if(mZenHubIconSize != null) {
                int zenHubIconSize = Integer.parseInt((String) newValue);
                int zenHubIconSizeIndex = mZenHubIconSize.findIndexOfValue((String) newValue);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.ZENHUB_ICON_SIZE, zenHubIconSize);
                mZenHubIconSize.setSummary(mZenHubIconSize.getEntries()[zenHubIconSizeIndex]);
            }
            return true;
        } else if (preference == mZenHubIconType) {
            int zenHubIconType = Integer.parseInt((String) newValue);
            int zenHubIconTypeIndex = mZenHubIconType.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ZENHUB_ICON_TYPE, zenHubIconType);
            mZenHubIconType.setSummary(mZenHubIconType.getEntries()[zenHubIconTypeIndex]);
            handleZenHubIconPreferences();
            return true;
        } else if (preference == mCustomStatusbarHeight) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.CUSTOM_STATUSBAR_HEIGHT, value, UserHandle.USER_CURRENT);
            return true;
       } else if (preference == mThemeColor) {
            int color = (Integer) newValue;
            String hexColor = String.format("%08X", (0xFFFFFFFF & color));
            SystemProperties.set(ACCENT_COLOR_PROP, hexColor);
            checkColorPreset(hexColor);
            try {
                 mOverlayManager.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayManager.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayManager.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
            } catch (RemoteException ignored) {
            }
         } else if (preference == mAccentPreset) {
            String value = (String) newValue;
            int index = mAccentPreset.findIndexOfValue(value);
            mAccentPreset.setSummary(mAccentPreset.getEntries()[index]);
            SystemProperties.set(ACCENT_COLOR_PROP, value);
            try {
                 mOverlayManager.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayManager.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayManager.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    private void checkColorPreset(String colorValue) {
        List<String> colorPresets = Arrays.asList(
                getResources().getStringArray(R.array.accent_presets_values));
        if (colorPresets.contains(colorValue)) {
            mAccentPreset.setValue(colorValue);
            int index = mAccentPreset.findIndexOfValue(colorValue);
            mAccentPreset.setSummary(mAccentPreset.getEntries()[index]);
        }
        else {
            mAccentPreset.setSummary(
                    getResources().getString(R.string.custom_string));
        }
    }


    private String getOverlayName(String[] overlays) {
            String overlayName = null;
            for (int i = 0; i < overlays.length; i++) {
                String overlay = overlays[i];
                if (Utils.isThemeEnabled(overlay)) {
                    overlayName = overlay;
                }
            }
            return overlayName;
        }

    private int getOverlayPosition(String[] overlays) {
            int position = -1;
            for (int i = 0; i < overlays.length; i++) {
                String overlay = overlays[i];
                if (Utils.isThemeEnabled(overlay)) {
                    position = i;
                }
            }
            return position;
        }
}
