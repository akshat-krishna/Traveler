package com.valuecomvikaskumar.searchplaces;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;


import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.valuecomvikaskumar.searchplaces.model.MyPlaceDetail;
import com.valuecomvikaskumar.searchplaces.model.Photos;
import com.valuecomvikaskumar.searchplaces.model.Results;
import com.valuecomvikaskumar.searchplaces.remote.IGoogleService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Button button;
    private ImageView imageView;
    private IGoogleService service;
    private TextView placeName;
    private TextView openingH;
    private TextView closingHr;
    private TextView place_address;
    private RatingBar ratingBar;
    private Toolbar toolbar;
    private TextView description;
    private FloatingActionButton texttospeech;
    private Button selLang;
    CharSequence []lang={"Hindi",
    "Urdu",
    "English",
    "Kannada",
    "Malayalam",
    "Tamil",
    "Telugu",
    "Bengali",
    "Bihari",
    "Gujarati",
    "Marathi",
    "Sanskrit",
    "Oriya",
    "Punjabi",
    "Assamese"};
    String[][] langName={{"Hindi","hi"},
            {"Urdu","ur"},
            {"English","en"},
            {"Kannada","kn"},
            {"Malayalam",	"ml"},
            {"Tamil","ta"},
            {"Telugu","te"},
            {"Bengali","bn"},
            {"Bihari","bh"},
            {"Gujarati","gu"},
            {"Marathi","mr"},
            {"Sanskrit","sa"},
            {"Oriya","or"},
            {"Punjabi","pa"},
            {"Assamese","as"}};

    String sel;

    String des;
    TextToSpeech tts;

    MyPlaceDetail detail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        service=Common.getGoogleService();
        placeName=findViewById(R.id.placeName);
        ratingBar=findViewById(R.id.ratingBar);
        openingH=findViewById(R.id.openingH);
        place_address=findViewById(R.id.place_detail);
        placeName=findViewById(R.id.placeName);
        description=findViewById(R.id.description);
        texttospeech=findViewById(R.id.tts);
        selLang=findViewById(R.id.SelLang);

        imageView=findViewById(R.id.imageView);

        tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {
                // TODO Auto-generated method stub
                if(status == TextToSpeech.SUCCESS){
                    int result;
                    Log.d("Selection", "onInit: "+sel);
                    if(sel.equals("")){
                         result=tts.setLanguage(Locale.US);
                    }else {
                         result = tts.setLanguage(new Locale(sel));
                    }
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.e("error", "This Language is not supported");
                    }
//
                }
                else
                    Log.e("error", "Initilization Failed!");
            }
        });

//            texttospeech.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });

//        tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
//
//            @Override
//            public void onInit(int status) {
//                // TODO Auto-generated method stub
//                if(status == TextToSpeech.SUCCESS){
//
//                    if(result==TextToSpeech.LANG_MISSING_DATA ||
//                            result==TextToSpeech.LANG_NOT_SUPPORTED){
//                        Log.e("error", "This Language is not supported");
//                    }
////                    else{
////                        ConvertTextToSpeech();
////                    }
//                }
//                else
//                    Log.e("error", "Initilization Failed!");
//            }
//        });






        ProgressDialog dialog=new ProgressDialog(MainActivity.this);
        dialog.setMessage("wait");
        dialog.show();

        if(Common.currentResult!=null&&!TextUtils.isEmpty(String.valueOf(Common.currentResult))&&Common.currentResult.getPhotos()!=null){
            Picasso.with(MainActivity.this).load(getUrl(Common.currentResult.getPhotos()[0].getPhoto_reference())).placeholder(R.drawable.ic_image_black_24dp).networkPolicy(NetworkPolicy.OFFLINE).into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(MainActivity.this).load(getUrl(Common.currentResult.getPhotos()[0].getPhoto_reference())).placeholder(R.drawable.ic_image_black_24dp).into(imageView);

                }
            });
        }

        if(Common.currentResult.getRating()!=null&&!TextUtils.isEmpty(Common.currentResult.getRating())){
            ratingBar.setRating(Float.parseFloat(Common.currentResult.getRating()));
        }
        else {
            ratingBar.setVisibility(View.INVISIBLE);
        }
        if(Common.currentResult.getOpening_hours()!=null){

            openingH.setText("open now: "+String.valueOf(Common.currentResult.getOpening_hours().getOpen_now()));
        }

        service.getPlaceDetail(getPlaceDetailUrl(Common.currentResult.getPlace_id())).enqueue(new Callback<MyPlaceDetail>() {
            @Override
            public void onResponse(Call<MyPlaceDetail> call, Response<MyPlaceDetail> response) {

                detail=response.body();
                placeName.setText(detail.getResult().getName());
                toolbar.setTitle(detail.getResult().getName());
                getSupportActionBar().setTitle(detail.getResult().getName());
                place_address.setText(detail.getResult().getFormatted_address());
                new doit().execute();
            }

            @Override
            public void onFailure(Call<MyPlaceDetail> call, Throwable t) {

            }
        });


        dialog.dismiss();
    }

    private String getPlaceDetailUrl(String place_id) {

        StringBuilder builder=new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        builder.append("placeid="+place_id);
        builder.append("&key=AIzaSyA9WThoNk-cu-Mj0PGboj4FVjif3zAAb4U");
        return builder.toString();
    }

    private String getUrl(String ref) {

        StringBuilder builder=new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        builder.append("maxwidth=1000");
        builder.append("&photoreference="+ref);
        builder.append("&key=AIzaSyA9WThoNk-cu-Mj0PGboj4FVjif3zAAb4U");
        return builder.toString();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    public void OnClick(View v) {
        ConvertToSpeech();



    }
    public void OnClick1(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
        builder.setItems(lang, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sel= (String) langName[which][1];
                Log.d("Selection", "OnClick1: "+sel);

            }
        });
        builder.show();

    }

    public void ConvertToSpeech(){
        if(des==null||"".equals(des)) {
            des = "Content not available";
            tts.speak(des, TextToSpeech.QUEUE_FLUSH, null);
        }else
            tts.speak(des, TextToSpeech.QUEUE_FLUSH, null);
    }


public class doit extends AsyncTask<Void,Void,Void>{

    @Override
    protected Void doInBackground(Void... voids) {
        Document doc= null;
        try {
            doc = Jsoup.connect("https://en.wikipedia.org/wiki/"+detail.getResult().getName()).get();
            Element link=doc.select("p").first();
            des=link.text();

            for(int i=1;i<=9;i++){
                des=des.replace("["+i+"]"," ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        description.setText(des);
    }
}

}
