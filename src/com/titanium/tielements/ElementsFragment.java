package com.titanium.tielements;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import android.content.Context;
import android.content.ContentResolver;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.content.Context;
import android.content.ContentResolver;
import android.content.SharedPreferences;


import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.nano.MetricsProto;

import com.titanium.tielements.fragments.About;
import com.titanium.tielements.fragments.Animations;
import com.titanium.tielements.fragments.Buttons;
import com.titanium.tielements.fragments.Lockscreen;
import com.titanium.tielements.fragments.Navigation;
import com.titanium.tielements.fragments.Notifications;
import com.titanium.tielements.fragments.QuickSettings;
import com.titanium.tielements.fragments.Statusbar;
import com.titanium.tielements.fragments.System;
import com.titanium.tielements.views.MenuViews;

public class ElementsFragment extends SettingsPreferenceFragment implements View.OnClickListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "TiElements";

    private MenuViews statusbar, navbar, qs, system, lockscreen, notications, buttons, about, animations;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;
    private Fragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.elements_fragment, container, false);
        mFragmentManager = getActivity().getSupportFragmentManager();
        initViews(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        getActivity().setTitle(R.string.tielements_title);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    private void initViews(final View view) {
        statusbar = (MenuViews) view.findViewById(R.id.statusbar_menu);
        navbar = (MenuViews) view.findViewById(R.id.navbar_menu);
        qs = (MenuViews) view.findViewById(R.id.quick_settings_menu);
        notications = (MenuViews) view.findViewById(R.id.notifications_menu);
        lockscreen = (MenuViews) view.findViewById(R.id.lockscreen_menu);
        system = (MenuViews) view.findViewById(R.id.system_menu);
        animations = (MenuViews) view.findViewById(R.id.animations_menu);
        buttons = (MenuViews) view.findViewById(R.id.buttons_menu);
        about = (MenuViews) view.findViewById(R.id.about_menu);

        initClick();
    }

    private void initClick() {
        statusbar.setOnClickListener(this);
        navbar.setOnClickListener(this);
        qs.setOnClickListener(this);
        notications.setOnClickListener(this);
        lockscreen.setOnClickListener(this);
        system.setOnClickListener(this);
        animations.setOnClickListener(this);
        buttons.setOnClickListener(this);
        about.setOnClickListener(this);
    }

    private void loadFragment(String tag, boolean addToStack, Bundle bundle, Fragment setFragment) {
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left);
        mFragment = setFragment;

        if (addToStack) {
            if (bundle != null)
                mFragment.setArguments(bundle);
            mFragmentTransaction.replace(R.id.fragment_container, mFragment, tag);
            mFragmentTransaction.addToBackStack(tag).commit();

        } else {
            if (bundle != null)
                mFragment.setArguments(bundle);
            mFragmentTransaction.replace(R.id.fragment_container, mFragment, tag);
            mFragmentTransaction.commit();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case  R.id.statusbar_menu:
                loadFragment(Constants.STATUS_BAR_MENU_FRAGMENT,true,null,new Statusbar());
                break;
            case  R.id.navbar_menu:
                loadFragment(Constants.NAV_BAR_MENU_FRAGMENT,true,null,new Navigation());
                break;
            case  R.id.quick_settings_menu:
                loadFragment(Constants.QUICK_SETTINGS_MENU_FRAGMENT,true,null,new QuickSettings());
                break;
            case  R.id.notifications_menu:
                loadFragment(Constants.RECENT_MENU_FRAGMENT,true,null,new Notifications());
                break;
            case  R.id.lockscreen_menu:
                loadFragment(Constants.LOCK_SCREEN_MENU_FRAGMENT,true,null,new Lockscreen());
                break;
            case  R.id.system_menu:
                loadFragment(Constants.SYSTEM_MENU_FRAGMENT,true,null,new System());
                break;
            case  R.id.animations_menu:
                loadFragment(Constants.SYSTEM_MENU_FRAGMENT,true,null,new Animations());
                break;
            case  R.id.buttons_menu:
                loadFragment(Constants.SYSTEM_MENU_FRAGMENT,true,null,new Buttons());
                break;
            case  R.id.about_menu:
                loadFragment(Constants.SYSTEM_MENU_FRAGMENT,true,null,new About());
                break;

        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.TIELEMENTS;
    }

     @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        return true;
    }

}
