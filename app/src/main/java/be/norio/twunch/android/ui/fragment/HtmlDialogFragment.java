package be.norio.twunch.android.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.webkit.WebView;

import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.Util;

public class HtmlDialogFragment extends DialogFragment {

    public static final String ARG_TITLE = "title";
    public static final String ARG_RESOURCE = "resource";
    public static final String ARG_PAGE= "page";

    public static HtmlDialogFragment newInstance(String title, int htmlResource, String page) {
        HtmlDialogFragment frag = new HtmlDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putInt(ARG_RESOURCE, htmlResource);
        args.putString(ARG_PAGE, page);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        int resource = getArguments().getInt(ARG_RESOURCE);
        String page = getArguments().getString(ARG_PAGE);

        AnalyticsUtils.trackPageView(page);
        final Activity context = getActivity();
        WebView webView = new WebView(context);
        webView.loadDataWithBaseURL(null, Util.readTextFromResource(context, resource), "text/html", "utf-8", null);
        return new AlertDialog.Builder(context).setTitle(title).setView(webView).create();
    }
}