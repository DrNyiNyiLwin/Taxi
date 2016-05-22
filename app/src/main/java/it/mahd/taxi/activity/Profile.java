package it.mahd.taxi.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.mahd.taxi.Main;
import it.mahd.taxi.R;
import it.mahd.taxi.util.Calculator;
import it.mahd.taxi.util.Controllers;
import it.mahd.taxi.util.Encrypt;
import it.mahd.taxi.util.ServerRequest;

/**
 * Created by salem on 2/13/16.
 */
public class Profile extends Fragment {
    SharedPreferences pref;
    ServerRequest sr = new ServerRequest();
    Controllers conf = new Controllers();
    Encrypt algo = new Encrypt();

    private TextView Username_txt, City_txt, Age_txt, Email_txt, Phone_txt;
    private TextView DateN_txt;
    private EditText Phone_etxt;
    private LinearLayout Age_ll, Phone_ll;
    private ImageView Picture_iv;
    private Button Logout_btn, Disable_btn;
    private FloatingActionButton Age_btn, Phone_btn;
    private static Dialog disableDialog;

    private String fname, lname, gender, dateN, country, city, email, phone, picture;


    public Profile() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile, container, false);
        ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.profile));

        pref = getActivity().getSharedPreferences(conf.app, Context.MODE_PRIVATE);

        Username_txt = (TextView) rootView.findViewById(R.id.username_txt);
        Username_txt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        Age_ll = (LinearLayout) rootView.findViewById(R.id.Age_ll);
        DateN_txt = (TextView) rootView.findViewById(R.id.DateN_txt);
        Age_txt = (TextView) rootView.findViewById(R.id.age_txt);
        Age_txt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Age_txt.setVisibility(View.GONE);
                DateN_txt.setText(dateN);
                Age_ll.setVisibility(View.VISIBLE);
            }
        });

        Age_btn = (FloatingActionButton) rootView.findViewById(R.id.Age_btn);
        Age_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editAge();
            }
        });

        DateN_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(getActivity(), R.style.MyMaterialDesignTheme, dateSetListener, Integer.parseInt(dateN.split("/")[0]), Integer.parseInt(dateN.split("/")[1]), Integer.parseInt(dateN.split("/")[2])).show();
            }
        });

        City_txt = (TextView) rootView.findViewById(R.id.city_txt);
        City_txt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        Email_txt = (TextView) rootView.findViewById(R.id.email_txt);
        Email_txt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            }
        });

        Phone_ll = (LinearLayout) rootView.findViewById(R.id.Phone_ll);
        Phone_etxt = (EditText) rootView.findViewById(R.id.Phone_etxt);
        Phone_txt = (TextView) rootView.findViewById(R.id.phone_txt);
        Phone_txt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Phone_txt.setVisibility(View.GONE);
                Phone_etxt.setText(phone);
                Phone_ll.setVisibility(View.VISIBLE);
            }
        });

        Phone_btn = (FloatingActionButton) rootView.findViewById(R.id.Phone_btn);
        Phone_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editPhone();
            }
        });

        Picture_iv = (ImageView) rootView.findViewById(R.id.picture_iv);

        Disable_btn = (Button) rootView.findViewById(R.id.Disable_btn);
        Disable_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                disableDialog = new Dialog(getActivity(), R.style.FullHeightDialog);
                disableDialog.setContentView(R.layout.profile_dialog);
                disableDialog.setCancelable(true);
                final EditText Password_etxt;
                Button Disablex_btn, Cancel_btn;
                Password_etxt = (EditText) disableDialog.findViewById(R.id.Password_etxt);
                Disablex_btn = (Button) disableDialog.findViewById(R.id.Disablex_btn);
                Cancel_btn = (Button) disableDialog.findViewById(R.id.Cancel_btn);
                disableDialog.show();
                Cancel_btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        disableDialog.dismiss();
                    }
                });
                Disablex_btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        disableDialog.dismiss();
                        if(conf.NetworkIsAvailable(getActivity())){
                            disableFunct(Password_etxt.getText().toString());
                        }else{
                            Toast.makeText(getActivity(), R.string.networkunvalid, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        Logout_btn = (Button) rootView.findViewById(R.id.logout_btn);
        Logout_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if(conf.NetworkIsAvailable(getActivity())){
                    logoutFunct();
                }else{
                    Toast.makeText(getActivity(), R.string.networkunvalid, Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(conf.NetworkIsAvailable(getActivity())){
            findUser();
        }else{
            Toast.makeText(getActivity(), R.string.networkunvalid, Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    public void disableFunct(String pwd) {
        Encrypt algo = new Encrypt();
        int x = algo.keyVirtual();
        String key = algo.key(x);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app", algo.dec2enc(conf.app, key)));
        params.add(new BasicNameValuePair(conf.tag_key, x + ""));
        params.add(new BasicNameValuePair(conf.tag_token, pref.getString(conf.tag_token, "")));
        JSONObject json = sr.getJSON(conf.url_disableAccount, params);
        if (json != null) {
            try {
                if(json.getBoolean("res")){
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString(conf.tag_token, "");
                    edit.putString(conf.tag_fname, "");
                    edit.putString(conf.tag_lname, "");
                    edit.putString(conf.tag_picture, "");
                    edit.commit();

                    RelativeLayout rl = (RelativeLayout) getActivity().findViewById(R.id.nav_header_container);
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View vi = inflater.inflate(R.layout.toolnav_drawer, null);
                    TextView tv = (TextView) vi.findViewById(R.id.usernameTool_txt);
                    tv.setText("");
                    rl.addView(vi);

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Home());
                    ft.commit();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    private void editAge() {
        Encrypt algo = new Encrypt();
        int x = algo.keyVirtual();
        String key = algo.key(x);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app", algo.dec2enc(conf.app, key)));
        params.add(new BasicNameValuePair(conf.tag_key, x + ""));
        params.add(new BasicNameValuePair(conf.tag_token, pref.getString(conf.tag_token, "")));
        params.add(new BasicNameValuePair(conf.tag_dateN, algo.dec2enc(DateN_txt.getText().toString(), key)));
        JSONObject json = sr.getJSON(conf.url_editAge, params);
        if (json != null) {
            try {
                if (json.getBoolean("res")) {
                    Age_ll.setVisibility(View.GONE);
                    int[] tab = new Calculator().getAge(DateN_txt.getText().toString());
                    Age_txt.setText(tab[0] + "years, " + tab[1] + "month, " + tab[2] + "day");
                    Age_txt.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(),"Age not changed",Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), R.string.serverunvalid, Toast.LENGTH_SHORT).show();
        }
    }

    private void editPhone() {
        Encrypt algo = new Encrypt();
        int x = algo.keyVirtual();
        String key = algo.key(x);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app", algo.dec2enc(conf.app, key)));
        params.add(new BasicNameValuePair(conf.tag_key, x + ""));
        params.add(new BasicNameValuePair(conf.tag_token, pref.getString(conf.tag_token, "")));
        params.add(new BasicNameValuePair(conf.tag_phone, algo.dec2enc(Phone_etxt.getText().toString(), key)));
        JSONObject json = sr.getJSON(conf.url_editPhone, params);
        if (json != null) {
            try {
                if (json.getBoolean("res")) {
                    Phone_ll.setVisibility(View.GONE);
                    Phone_txt.setText(Phone_etxt.getText().toString());
                    Phone_txt.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getActivity(),"Phone not changed",Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), R.string.serverunvalid, Toast.LENGTH_SHORT).show();
        }
    }

    public void findUser() {
        Encrypt algo = new Encrypt();
        int x = algo.keyVirtual();
        String key = algo.key(x);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app", algo.dec2enc(conf.app, key)));
        params.add(new BasicNameValuePair(conf.tag_key, x + ""));
        params.add(new BasicNameValuePair(conf.tag_token, pref.getString(conf.tag_token, "")));
        JSONObject json = sr.getJSON(conf.url_profile, params);
        if(json != null){
            try{
                if(json.getBoolean("res")) {
                    int keyVirtual = Integer.parseInt(json.getString(conf.tag_key));
                    String newKey = algo.key(keyVirtual);
                    fname = algo.enc2dec(json.getString(conf.tag_fname), newKey);
                    lname = algo.enc2dec(json.getString(conf.tag_lname), newKey);
                    gender = algo.enc2dec(json.getString(conf.tag_gender), newKey);
                    dateN = algo.enc2dec(json.getString(conf.tag_dateN), newKey);
                    country = algo.enc2dec(json.getString(conf.tag_country), newKey);
                    city = algo.enc2dec(json.getString(conf.tag_city), newKey);
                    email = algo.enc2dec(json.getString(conf.tag_email), newKey);
                    phone = algo.enc2dec(json.getString(conf.tag_phone), newKey);
                    picture = json.getString(conf.tag_picture);
                    Username_txt.setText(fname + " " + lname);
                    int[] tab = new Calculator().getAge(dateN);
                    Age_txt.setText(tab[0] + "years, " + tab[1] + "month, " + tab[2] + "day");
                    City_txt.setText(gender + " from " + country + ", lives in " + city);
                    Email_txt.setText(email);
                    Phone_txt.setText(phone);
                    if (picture.equals("")) {
                        Picture_iv.setBackgroundResource(R.mipmap.ic_profile);
                    } else {
                        byte[] imageAsBytes = Base64.decode(picture.getBytes(), Base64.DEFAULT);
                        Picture_iv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Logout_btn.setVisibility(View.GONE);
            Toast.makeText(getActivity(), R.string.serverunvalid, Toast.LENGTH_SHORT).show();
        }
    }

    public void logoutFunct() {
        Encrypt algo = new Encrypt();
        int x = algo.keyVirtual();
        String key = algo.key(x);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("app", algo.dec2enc(conf.app, key)));
        params.add(new BasicNameValuePair(conf.tag_key, x + ""));
        params.add(new BasicNameValuePair(conf.tag_token, pref.getString(conf.tag_token, "")));
        JSONObject json = sr.getJSON(conf.url_logout, params);
        if(json != null){
            try{
                if(json.getBoolean("res")){
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString(conf.tag_token, "");
                    edit.putString(conf.tag_fname, "");
                    edit.putString(conf.tag_lname, "");
                    edit.putString(conf.tag_picture, "");
                    edit.commit();

                    RelativeLayout rl = (RelativeLayout) getActivity().findViewById(R.id.nav_header_container);
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View vi = inflater.inflate(R.layout.toolnav_drawer, null);
                    TextView tv = (TextView) vi.findViewById(R.id.usernameTool_txt);
                    tv.setText("");
                    rl.addView(vi);

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.container_body, new Home());
                    ft.commit();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            DateN_txt.setText(new StringBuilder().append(selectedYear).append("/").append(selectedMonth + 1).append("/").append(selectedDay));
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container_body, new Home());
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
