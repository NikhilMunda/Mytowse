package com.mytowse.mytowse;

import static com.mytowse.mytowse.R.color.teal_700;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class HomeActivity extends AppCompatActivity {

    private WebView webView;
    FirebaseRemoteConfig remoteConfig;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home);
            //Titlebar
            getSupportActionBar().hide();


            //Firebase Remote-config In-App Update
            int currentVersionCode;
            currentVersionCode = getCurrentVersionCode();
            Log.d("Mytowse", String.valueOf(currentVersionCode));

            remoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(5)
                    .build();
            remoteConfig.setConfigSettingsAsync(configSettings);

            remoteConfig.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                @Override
                public void onComplete(@NonNull Task<Boolean> task) {
                    if(task.isSuccessful()){
                        final String new_version_code = remoteConfig.getString("new_version_code");
                        if(Integer.parseInt(new_version_code) > getCurrentVersionCode()){
                            showUpdateDialog();
                        }
                    }
                }
            });



            //Change the colour of top bar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(teal_700));
            }

            webView = findViewById(R.id.webView);
            WebSettings webSettings = webView.getSettings();
            webView.setWebViewClient(new MyWebviewClient());
            webView.getSettings().setJavaScriptEnabled(true);

            //improve WebView performance
            webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            webSettings.setAppCacheEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
            webSettings.setUseWideViewPort(true);
            webSettings.setSavePassword(true);
            webSettings.setSaveFormData(true);
            webSettings.setEnableSmoothTransition(true);


            //Check Internet Connectivity
                if (isNetworkAvailable() == false) {
                    final AlertDialog dialog = new AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Internet Connection Alert")
                            .setMessage("Please Check Your Internet Connection")
                            .setPositiveButton("TRY AGAIN", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                    startActivity(getIntent());
                                }
                            }).show();
                    dialog.setCancelable(false);
                } else if (isNetworkAvailable() == true) {

                    //My web Url
                    webView.loadUrl("https://www.mytowse.com");
                }

            }

           //Android In-App Update part
    private void showUpdateDialog() {
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("New Update Available")
                    .setMessage("Update Now")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try{
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mytowse.com")));
                            }catch (Exception e){
                                Toast.makeText(getApplicationContext(),"Something went wrong. Try Again later", Toast.LENGTH_SHORT);
                            }
                        }
                    })
                    .show();
            dialog.setCancelable(false);
    }

    private int getCurrentVersionCode(){
        PackageInfo packageInfo = null;
        try{
            packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
        }catch (Exception e){
            Log.d("Mytowse", e.getMessage());
        }

        return packageInfo.versionCode;
    }


   //This Class is for Internet Connectivity
    public boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {

                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {

                        return true;
                    }
                }
            }
        }
        return false;

   }


    //To go back while surffing webpage
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && this.webView.canGoBack()){
            this.webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //This Class is Handling Links of the Website
    private class MyWebviewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("https://mytowse.com"))  //Open mytowse link in app only
            {
                return false;
            }
            else{
                //here open external links in external browser
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

        }
    }
}

