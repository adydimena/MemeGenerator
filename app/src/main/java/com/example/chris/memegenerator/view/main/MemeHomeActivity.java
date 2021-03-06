package com.example.chris.memegenerator.view.main;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.chris.memegenerator.LoginActivity;
import com.example.chris.memegenerator.MemeApplication;
import com.example.chris.memegenerator.R;
import com.example.chris.memegenerator.category.MemeInterestActivity;
import com.example.chris.memegenerator.category.MemesCategory;
import com.example.chris.memegenerator.data.remote.FacebookMemeSearch;
import com.example.chris.memegenerator.data.remote.RemoteDataSource;
import com.example.chris.memegenerator.fragments.interestfragment.TrendingInterestFragment;
import com.example.chris.memegenerator.fragments.memesliderfrag.MemeSliderFragment;
import com.example.chris.memegenerator.fragments.searchfragment.SearchMemeFragment;
import com.example.chris.memegenerator.fragments.toptrendingfragment.TrendingFragment;
import com.example.chris.memegenerator.util.Constants;
import com.example.chris.memegenerator.util.FacebookHandler;
import com.example.chris.memegenerator.util.MainPagerViewAdapter;
import com.example.chris.memegenerator.util.ZoomOutPageTransformer;
import com.example.chris.memegenerator.util.pojo.keywordfinder.Keywords;
import com.example.chris.memegenerator.view.FavoriteMemesActivity;
import com.example.chris.memegenerator.view.Main2Activity;
import com.example.chris.memegenerator.view.createMeme.CreateMemeActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemeHomeActivity extends AppCompatActivity implements MainContract.View{
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    private static final String TAG = MemeHomeActivity.class.getSimpleName() + "_TAG";
    @Inject
    MainPresenter presenter;
    MainPagerViewAdapter mainViewPagerAdapter;
    TabLayout mainTabLayout;
    ViewPager viewPager;
    private Toolbar homeToolbar;
    FrameLayout frameLayout;
    List<String> interests;
    EditText etpost;
    List<List<String>> interestsMemes;
    List<String> parsingKeywods = new ArrayList<>();
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == 1){
                List<String> keywords =(List<String>) msg.obj;
                for (int i = 0; i <keywords.size() ; i++)
                {
                    Log.d(TAG, "handleMessage: "+keywords.get(i));
                    parsingKeywods.add(keywords.get(i));
                }
                makingbingcall(parsingKeywods);
            }
            
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_meme_activity
        );
       MemeApplication.get(this).getMainComponent().inject(this);
        homeToolbar = findViewById(R.id.home_toolbar);

        homeToolbar.setBackgroundColor(Color.parseColor("#19B5FE"));
        viewPager = findViewById(R.id.app_main_pager);
        mainTabLayout = findViewById(R.id.app_main_tabs);
        etpost = findViewById(R.id.etpost);
        mainTabLayout.setBackgroundColor(Color.parseColor("#1E8BC3"));
        mainViewPagerAdapter = new MainPagerViewAdapter(getSupportFragmentManager());
        presenter.attachView(this);
//        presenter.getBingSearch("trendingMemes", Constants.topTrending);
        presenter.getBingSearch("top memes", Constants.topTrending);


        presenter.getBingSearch("trendingMemes", Constants.topTrending);
        viewPager.setAdapter(mainViewPagerAdapter);

        mainTabLayout.setupWithViewPager(viewPager);
        setSupportActionBar(homeToolbar);
        mainViewPagerAdapter.notifyDataSetChanged();
viewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        FacebookMemeSearch.KeyWordrestCall("kobe dunked the ball");
        frameLayout = findViewById(R.id.searchFragmentFrame);
//        FacebookMemeSearch.KeyWordrestCall("kobe dunked the ball");
        interests = new ArrayList<>();
        interestsMemes = new ArrayList<>();
    
        try
        {
            InputStream instream = openFileInput("interests.txt");
            if (instream != null)
            {
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line,line1 = "";
                try
                {
                    while ((line = buffreader.readLine()) != null)
                        line1+=line;
                    Log.d("Chris", "onCreate: "+line1);

                    presenter.getBingSearch(line1,Constants.interestTrending);

                    presenter.getBingSearch(line1, Constants.interestTrending);
                    Log.d(TAG, "onCreate: "+line1);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            Log.d(TAG, "onCreate: "+e.toString());
        }
    }
    
//    public void btnRemoveFrag(View view){
//        Log.d("great", "btnRemoveFrag: ");
//        MemeSliderFragment memeSliderFragment = new MemeSliderFragment();
//        getSupportFragmentManager().beginTransaction().remove(memeSliderFragment).commit();
//        //fragmentManager.beginTransaction().remove(memeSliderFragment).commit();
//    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater =new MenuInflater(this);
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()){
        case R.id.itemFavorites:
            Intent intentFav = new Intent(this, FavoriteMemesActivity.class);
            startActivity(intentFav);
            break;
        case R.id.itemSettings:
            Intent intentSearch = new Intent(this, MemesCategory.class);
            startActivity(intentSearch);
        case R.id.itemCreate:
            Intent intentCreate = new Intent(this, CreateMemeActivity.class);
            startActivity(intentCreate);
            break;
        case R.id.itemLogOut:
            FacebookHandler facebookHandler = FacebookHandler.getInstance();
            facebookHandler.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            break;
    }
        
        switch (item.getItemId()){
            case R.id.itemFavorites:
                Intent intentFav = new Intent(this, FavoriteMemesActivity.class);
                if (frameLayout.getVisibility() == View.VISIBLE)
                    onBackPressed();
                startActivity(intentFav);
                break;
            case R.id.itemSettings:
                Intent intentSearch = new Intent(this, MemeInterestActivity.class);
                if (frameLayout.getVisibility() == View.VISIBLE)
                    onBackPressed();
                startActivity(intentSearch);
                break;
            case R.id.itemCreate:
                Intent intentCreate = new Intent(this, CreateMemeActivity.class);
                if (frameLayout.getVisibility() == View.VISIBLE)
                    onBackPressed();
                startActivity(intentCreate);
                break;
            case  R.id.itemLogOut:
                if (frameLayout.getVisibility() == View.VISIBLE)
                    onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void showError(String error) {
    
    }
    
    @Override
    public void setBingSearch(List<String> memes) {
        Log.d("Set", "setBingSearch: "+memes.size());
        TrendingFragment trendingFragment = TrendingFragment.newInstance(memes);
        Log.d(TAG, "setBingSearch: "+memes.size());
    }
    
    @Override
    public void setInterestBingSearch(List<String> memes)
    {
        Log.d("setInterests", "setInterestBingSearch: "+memes.size());
        TrendingInterestFragment trendingInterestFragment = TrendingInterestFragment.newInstance(memes);
    }



    public void setSearchmeme(List<String> memes)
    {
        ArrayList<String> Thekey = new ArrayList<>();
        Log.d(TAG, "setSearchmeme: size" +memes.size());
        for (int i = 0; i <memes.size() ; i++)
        {
            Thekey.add(memes.get(i));
            Log.d(TAG, "setSearchmeme: thissss" + memes.get(i));
        }
        
        
    
    }
    public void makingbingcall(List<String> key){
        ArrayList<String> Thekey = new ArrayList<>();
        Thekey.add("https://www.google.com/imgres?imgurl=https%3A%2F%2Ffm.cnbc.com%2Fapplications%2Fcnbc.com%2Fresources%2Fimg%2Feditorial%2F2016%2F08%2F22%2F103884430-GettyImages-139369178.1910x1000.jpg&imgrefurl=https%3A%2F%2Fwww.cnbc.com%2F2017%2F04%2F24%2Fkobe-bryant-on-what-it-really-takes-to-succeed.html&docid=jzPdTHU95PXCtM&tbnid=bn2koAavuRB0YM%3A&vet=10ahUKEwiUx_D3lsvYAhUMY98KHbfaDuAQMwiDAigBMAE..i&w=1910&h=1000&bih=607&biw=1228&q=kobe&ved=0ahUKEwiUx_D3lsvYAhUMY98KHbfaDuAQMwiDAigBMAE&iact=mrc&uact=8");
        
        for (int i = 0; i <key.size() ; i++)
        {
            Thekey.add(key.get(i));
            Log.d("TAG_POST", "makingbingcall: keyword "+ key.get(i));
        }
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putStringArrayListExtra("magic",Thekey);
        if (frameLayout.getVisibility() == View.VISIBLE)
            onBackPressed();
        startActivity(intent);
        
        
    }
    
    
    @Override
    public void showProgress(String progress) {
    
    }
    
    public void searchkeyword(View view)
    {
        final List<String> keywords = new ArrayList<>();

        Constants.whichCall(Constants.keyword);
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                RemoteDataSource.KeyWordResponse(etpost.getText().toString())
                        .enqueue(new Callback<Keywords>() {
                            @Override
                            public void onResponse(Call<Keywords> call, Response<Keywords> response) {
                                if (response.body() != null) {
                                    String tag = "";
                                    String word = "";
                                    for (int i = 0; i < response.body().getText().size(); i++) {
                                        // Log.d(TAG, "onResponse: this is i: " + i);
                                        for (int j = 0; j < response.body().getText().get(i).size(); j++) {
                                            // Log.d(TAG, "onResponse: this is j: " + j);
                                            for (int k = 0; k < response.body().getText().get(i).get(j).size(); k++) {
                                                // Log.d(TAG, "onResponse: this is k: " + k);
                                                tag = response.body().getText().get(i).get(j).get(k).getTag();
                                                if (tag.equals("NOUN") )
                                                {
                                                    Log.d("MYOWNTAG", "onResponse: word "+
                                                            response.body().getText().get(i).get(j).get(k).getWord()
                                                            + "\ntag is: " + tag);
                                        
                                                    if (tag.equals("NOUN") && word != null && word.length() >0 && Character.isUpperCase(word.charAt(0))
                                                            && Character.isUpperCase(response.body().getText().get(i).get(j).get(k).getWord().charAt(0)))
                                                    {
                                                        String proper_name = word + " " + response.body().getText().get(i).get(j).get(k).getWord();
                                                        keywords.remove(word);
                                                        keywords.add(proper_name);
                                                        Log.d(TAG, "onResponse: saving the proper_name "
                                                                + proper_name +
                                                                " tag: " + tag);
                                                    }
                                                    else
                                                    {
                                                        word = response.body().getText().get(i).get(j).get(k).getWord();
                                                        keywords.add(word);
                                                        Log.d(TAG, "onResponse: saving the word "
                                                                + word +
                                                                " tag: " + tag);
                                                    }
                                        
                                                }
                                    
                                                // Log.d(TAG, "KewyWordRestCall: word " + response.body().getText().get(i).get(j).get(k).getWord()
                                                //   + "\ntag: " + response.body().getText().get(i).get(j).get(k).getTag());
                                                //logd(MainActivity.this, " word: " +
                                                //   response.body().getText().get(i).get(j).get(k).getWord()
                                                //    + "\ntag: " + response.body().getText().get(i).get(j).get(k).getTag()
                                                // , Toast.LENGTH_LONG).show();
                                    
                                                // Log.d(TAG, "onResponse: get tag " + tag );
                                            }
                                        }
                                    }
                                    Message message = handler.obtainMessage();
                                    message.what = 1;
                                    message.obj = keywords;
                                    handler.sendMessage(message);
                                    
                                }
                            }
                            @Override
                            public void onFailure(Call<Keywords> call, Throwable t) {
                            }
                        });
        
            }
        }).start();
        
    }

    public void textToSpeach(View view) {

    }
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etpost.setText(result.get(0));
                }
                break;
            }

        }
    }


//    public void openSearchFragment(View view) {
//        Log.d("FAb", "openSearchFragment: ");
//        SearchMemeFragment searchMemeFragment = new SearchMemeFragment();
//        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.searchFragmentFrame, searchMemeFragment).commit();
//    }



}
