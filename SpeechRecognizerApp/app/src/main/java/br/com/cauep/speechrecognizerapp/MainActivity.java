package br.com.cauep.testespeech;

import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements android.speech.RecognitionListener
{

    //private  static  final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
    //private EditText metTextHint;
    //private ListView mlvTextMatches;
    private TextView mTvPartialRecognition;
    //private Spinner msTextMatches;
    private Button mbtSpeak;

    //Speech recognizer
    private RecognitionListener listener = null;
    SpeechRecognizer recognizer = null;
    String resultText = "";

    String TAG = "MainActivity";

    Intent recognizerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //metTextHint = (EditText) findViewById(R.id.etTextHint);
        //mlvTextMatches = (ListView) findViewById(R.id.lvTextMatches);
        //msTextMatches = (Spinner) findViewById(R.id.sNoOfMatches);
        mbtSpeak = (Button) findViewById(R.id.btSpeak);
        mTvPartialRecognition = (TextView) findViewById(R.id.tvPartialRecognition);
        CheckVoiceRecognition();
        mTvPartialRecognition.setText("Ouvindo: ");

        Log.d("SPEECH", "speech recognition available: " + SpeechRecognizer.isRecognitionAvailable(this));


        // criação do recognizer speech
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        if(listener == null) Log.d(TAG, "Listener NULL");
        recognizer.setRecognitionListener(this);

        comecarOuvir();

    }

    public  void  CheckVoiceRecognition(){
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if (activities.size()==0){
            mbtSpeak.setEnabled(false);
            Toast.makeText(this,"Voice recognizer not present",Toast.LENGTH_LONG).show();
        }
    }

    public void speak(View view){
        /*Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getClass().getPackage().getName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,metTextHint.getText().toString());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        if (msTextMatches.getSelectedItemPosition()== AdapterView.INVALID_POSITION){
            Toast.makeText(this,"Please select No. of Matches from Spinner",Toast.LENGTH_LONG).show();
            return;
        }
        int noOfMatches = Integer.parseInt(msTextMatches.getSelectedItem().toString());
        Log.d("MainActvity","noOfMatches: " + msTextMatches.getSelectedItem().toString());

        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,noOfMatches);
        startActivityForResult(recognizerIntent,VOICE_RECOGNITION_REQUEST_CODE);*/
        mTvPartialRecognition.setText("Ouvindo: ");
        //startActivityForResult(recognizerIntent,VOICE_RECOGNITION_REQUEST_CODE);
    }

    public void comecarOuvir(){
        Log.d(TAG, "Criação do recognizerIntent");
        Log.d("SPEECH", "speech recognition available: " + SpeechRecognizer.isRecognitionAvailable(this));
        //Intent recognizerIntent = RecognizerIntent.getVoiceDetailsIntent(getApplicationContext());
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        Log.d(TAG, "Criação do SpeechRecognizer");
        recognizer.startListening(recognizerIntent);
    }

/*
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        if (resultCode == RESULT_OK){
            ArrayList<String> textMatchlist = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (!textMatchlist.isEmpty()){
                Log.d("MainActvity","Tamanho da textMatchlist: " + textMatchlist.size());
                if (textMatchlist.get(0).contains("search")){
                    String searchQuery = textMatchlist.get(0).replace("search"," ");
                    Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
                    search.putExtra(SearchManager.QUERY,searchQuery);
                    startActivity(search);
                }
                else {
                    mlvTextMatches.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,textMatchlist));


                }
            }
        }
        else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR){
            showToastMessage("Audio Error");

        }
        else if ((resultCode == RecognizerIntent.RESULT_CLIENT_ERROR)){
            showToastMessage("Client Error");

        }
        else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR){
            showToastMessage("Network Error");
        }
        else if (resultCode == RecognizerIntent.RESULT_NO_MATCH){
            showToastMessage("No Match");
        }
        else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR){
            showToastMessage("Server Error");
        }
        super.onActivityResult(requestCode, resultCode, data);

    }*/


    void  showToastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_voice_recognition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Implementação dos métodos do RecognitionListener

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG, "Ready for speech " + bundle);
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.d("SPEECH", "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float v) {
        //Log.d("SPEECH", "onRmsChanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.d("SPEECH", "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("SPEECH", "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        Log.d("SPEECH", "onError: " + error);

        if(error == 9)
            recognizer.cancel();
        if(error == 7)
            Log.d("SPEECH", "Result Text final (onError): " + resultText);

        recognizer.startListening(recognizerIntent);
    }



    @Override
    public void onResults(Bundle bundle) {
        Log.d("MainActivity", "onResults: ready ");

        recognizer.startListening(recognizerIntent);
        Log.d("SPEECH", "Result Text final: " + resultText);
        mTvPartialRecognition.append(resultText +" | ");
        ArrayList<String> textMatchlist = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

/*
            if (!textMatchlist.isEmpty()){
                Log.d("MainActvity","Tamanho da textMatchlist: " + textMatchlist.size());
                if (textMatchlist.get(0).contains("search")){
                    String searchQuery = textMatchlist.get(0).replace("search"," ");
                }
                else {
                    mlvTextMatches.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,textMatchlist));

                }
            }*/
    }



    @Override
    public void onPartialResults(Bundle partialResults) {

        Log.d("SPEECH", "onPartialResults");
        //receiveResults(partialResults);

        if ((partialResults != null) && partialResults.containsKey(SpeechRecognizer.RESULTS_RECOGNITION))  {
            resultText = "";
            Log.d("SPEECH", "onPartialResults: size " + partialResults.size());
            List<String> heard = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            resultText = heard.get(0);
            Log.d("received Results: ", resultText);
        }

/*

        ArrayList<String> partialResultsList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d("MainActivity", "onPartialResults: partialResult Array: ");
        if (!partialResultsList.isEmpty()){
            for (String resultadoParcial: partialResultsList) {
                Log.d("MainActivity", resultadoParcial);
            }
        }
        else{
            Log.d("MainActivity", "onPartialResults: partialResult Array is Empty");
        }
*/


    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d("SPEECH", "onEvent");
    }
}
