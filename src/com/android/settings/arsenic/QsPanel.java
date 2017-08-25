
/*Copyright (C) 2015 The ResurrectionRemix Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
          http://www.apache.org/licenses/LICENSE-2.0
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.android.settings.arsenic;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.arsenic.SystemSettingSwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import cyanogenmod.providers.CMSettings;

public class QsPanel extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "QsPanel";

	private static final String PREF_SMART_PULLDOWN = "smart_pulldown";
	private static final String PREF_QUICK_PULLDOWN_FP = "quick_pulldown_fp";

	private ListPreference mSmartPulldown;
	private SystemSettingSwitchPreference mQuickPulldownFp;
	private FingerprintManager mFingerprintManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.arsenic_qs_panel);

        PreferenceScreen prefSet = getPreferenceScreen();
	ContentResolver resolver = getActivity().getContentResolver();

	mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        mSmartPulldown = (ListPreference) findPreference(PREF_SMART_PULLDOWN);
        mSmartPulldown.setOnPreferenceChangeListener(this);
        int smartPulldown = Settings.System.getInt(resolver,
                Settings.System.QS_SMART_PULLDOWN, 0);
        mSmartPulldown.setValue(String.valueOf(smartPulldown));
        updateSmartPulldownSummary(smartPulldown);

        mQuickPulldownFp = (SystemSettingSwitchPreference) findPreference(PREF_QUICK_PULLDOWN_FP);
        if (!mFingerprintManager.isHardwareDetected()) {
             getPreferenceScreen().removePreference(mQuickPulldownFp);
        } else {
             mQuickPulldownFp.setChecked((Settings.System.getInt(getContentResolver(),
                   Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN_FP, 0) == 1));
             mQuickPulldownFp.setOnPreferenceChangeListener(this);
        }

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
	ContentResolver resolver = getActivity().getContentResolver();
	 if (preference == mSmartPulldown) {
            int smartPulldown = Integer.valueOf((String) newValue);
            Settings.System.putInt(resolver, Settings.System.QS_SMART_PULLDOWN, smartPulldown);
            updateSmartPulldownSummary(smartPulldown);
            return true;
	} else if (preference == mQuickPulldownFp) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN_FP, value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }

     private void updateSmartPulldownSummary(int value) {
         Resources res = getResources();
 
         if (value == 0) {
             // Smart pulldown deactivated
             mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
         } else if (value == 3) {
             mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_none_summary));
         } else {
             String type = res.getString(value == 1
                     ? R.string.smart_pulldown_dismissable
                     : R.string.smart_pulldown_ongoing);
             mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
         }
     }
}
