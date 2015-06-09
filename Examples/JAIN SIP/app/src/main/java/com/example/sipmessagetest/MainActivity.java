package com.example.sipmessagetest;

import org.mobicents.restcomm.android.sdk.IDevice;
import org.mobicents.restcomm.android.sdk.NotInitializedException;
import org.mobicents.restcomm.android.sdk.SipProfile;
import org.mobicents.restcomm.android.sdk.impl.DeviceImpl;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends ActionBarActivity implements OnClickListener,
		 OnSharedPreferenceChangeListener {
	SharedPreferences prefs;
	Button btnSubmit;
	EditText editTextUser;
	EditText editTextDomain;
	EditText editTextTo;
	EditText editTextMessage;
	TextView textViewChat;
	String chatText = "";
	SipProfile sipProfile;

	private class Prefs {
		static final String USER = "pref_sip_user";
		static final String DOMAIN = "pref_sip_domain";
		static final String AUTH_USER = "pref_sip_auth_user";
		static final String PASSWORD = "pref_sip_password";
		static final String PROXY_IP = "pref_proxy_ip";
		static final String PROXY_PORT = "pref_proxy_port";
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sipProfile = new SipProfile();
        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("customHeader1","customValue1");
        customHeaders.put("customHeader2","customValue2");

        DeviceImpl.GetInstance().Initialize(getApplicationContext(), sipProfile,customHeaders);
		
		Button btnRegister = (Button) findViewById(R.id.btnSubmit);
		btnRegister.setOnClickListener(this);
		Button btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(this);
		Button btnCall = (Button) findViewById(R.id.btnCall);
		btnCall.setOnClickListener(this);

		editTextTo = (EditText) findViewById(R.id.editTextTo);
		editTextMessage = (EditText) findViewById(R.id.editTextMessage);
		textViewChat = (TextView) findViewById(R.id.textViewChat);
		textViewChat.setMovementMethod(new ScrollingMovementMethod());
		// ////////////////////////////////////////////////////////////

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// register preference change listener
		prefs.registerOnSharedPreferenceChangeListener(this);
		initializeSipFromPreferences();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.btnSubmit):
			DeviceImpl.GetInstance().Register();
			break;
		case (R.id.btnCall):
		
			DeviceImpl.GetInstance().Call(editTextTo.getText().toString());
		
			break;
		case (R.id.btnSend):
			
			DeviceImpl.GetInstance().SendMessage(editTextTo.getText().toString(), editTextMessage.getText().toString() );
			
			break;
		}
	}





	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Prefs.PROXY_IP)) {
			sipProfile.setRemoteIp((prefs.getString(key, "")));
		} else if (key.equals(Prefs.PROXY_PORT)) {
			sipProfile.setRemotePort(Integer.parseInt(prefs.getString(key, "5060")));
		} else if (key.equals(Prefs.USER)) {
			sipProfile.setSipIdentity(prefs.getString(key, "alice"));
		} else if (key.equals(Prefs.DOMAIN)) {
			sipProfile.setSipDomain(prefs.getString(key, "SIP domain"));
		} else if (key.equals(Prefs.AUTH_USER)) {
			sipProfile.setSipUserName(prefs.getString(key, "alice"));
		} else if (key.equals(Prefs.PASSWORD)) {
			sipProfile.setSipPassword(prefs.getString(key, "1234"));
		}

	}

	@SuppressWarnings("static-access")
	private void initializeSipFromPreferences() {
		sipProfile.setRemoteIp((prefs.getString(Prefs.PROXY_IP, "")));
		sipProfile.setRemotePort(Integer.parseInt(prefs.getString(Prefs.PROXY_PORT, "5060")));
		sipProfile.setSipIdentity(prefs.getString(Prefs.USER, "alice"));
		sipProfile.setSipDomain(prefs.getString(Prefs.DOMAIN, "SIP domain"));
		sipProfile.setSipUserName(prefs.getString(Prefs.AUTH_USER, "alice"));
		sipProfile.setSipPassword(prefs.getString(Prefs.PASSWORD, "1234"));
	}

}
