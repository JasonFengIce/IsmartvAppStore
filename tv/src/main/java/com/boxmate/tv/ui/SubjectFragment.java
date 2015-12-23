package com.boxmate.tv.ui;


import com.boxmate.tv.R;
import com.boxmate.tv.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SubjectFragment extends Fragment {
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_subject, container,false);
	}
}
