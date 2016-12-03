package br.com.cauep.speechrecognizerapp;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements android.speech.RecognitionListener
{

    //private  static  final int VOICE_RECOGNITION_REQUEST_CODE = 1001;

    String TAG = "MainActivity";

    private TextView mTvPartialRecognition;
    private Button mbtStart;
    private Button mbtEnd;

    //Speech speechRecognizer
    private RecognitionListener listener = null;
    SpeechRecognizer speechRecognizer = null;
    Intent recognizerIntent;

    // Resultado da transcrição
    String resultText = "";
    StringBuilder transcriptionText = new StringBuilder();

    // Lista de transcrições
    ArrayList<String> listaTranscricoes = new ArrayList<>();

    // Controllers
    boolean isRecording = false;
    boolean isPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mbtStart = (Button) findViewById(R.id.btStart);
        mbtEnd = (Button) findViewById(R.id.btEnd);

        mTvPartialRecognition = (TextView) findViewById(R.id.tvPartialRecognition);

        setRecognizerIntent();

        isRecording = false;
        isPaused = false;

        CheckVoiceRecognition();
        Log.d("SPEECH", "speech recognition available: " + SpeechRecognizer.isRecognitionAvailable(this));


    }



    public  void  CheckVoiceRecognition(){
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH),0);
        if (activities.size()==0){
            mbtStart.setEnabled(false);
            Toast.makeText(this,"Voice speechRecognizer not present",Toast.LENGTH_LONG).show();
        }
    }

    // configura as especificacoes do Intent
    public void setRecognizerIntent(){
        Log.d(TAG, "Criação do recognizerIntent");
        Log.d("SPEECH", "speech recognition available: " + SpeechRecognizer.isRecognitionAvailable(this));
        //Intent recognizerIntent = RecognizerIntent.getVoiceDetailsIntent(getApplicationContext());
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);//LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);    // get partial results
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,
                getString(R.string.speech_length));
    }

    public void setSpeechRecognizer(){
        Log.d("setSpeechRecognizer", "setSpeechRecognizer Iniciou");
        if(speechRecognizer != null){ // verifica se o speechRecognizer nao é nulo, para para-lo antes de instanciar
            deleteSpeechRecognizer();
        } else
            Log.d("setSpeechRecognizer", "Listener is NULL");

        Log.d("setSpeechRecognizer", "criando um novo recognizer");

        // Criação do speechRecognizer speech
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);   // cria um novo Recognizer
        speechRecognizer.setRecognitionListener(this);  // seta o Listener
    }

    public void deleteSpeechRecognizer(){
        if(speechRecognizer != null){
            speechRecognizer.cancel();
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
        Log.d("setSpeechRecognizer", "destruindo recognizer existente");

    }

    // comeca a gravacao
    public void start(View view){

        if(isRecording){    // se estiver gravando, pausa
            deleteSpeechRecognizer();
            showToastMessage(getString(R.string.txt_paused));
            Log.d(TAG, "Listening Paused");
            mTvPartialRecognition.setText(R.string.txt_paused);
            isPaused = true;
            isRecording = false;
        } else if(isPaused){ // se estiver pausado, volta a gravar
            mTvPartialRecognition.setText(R.string.txt_listening);
            setSpeechRecognizer();
            speechRecognizer.startListening(recognizerIntent);
            showToastMessage(getString(R.string.txt_recording));
            Log.d(TAG, "Listening Resumed");
            isPaused = false;
            isRecording = true;
        } else {    // Condição se não está gravando nem pausado, ou seja, uma nova gravação
            // Reinicia a string que armazenará a transcrição
            mTvPartialRecognition.setText(R.string.txt_listening);
            setSpeechRecognizer();
            Log.d(TAG, "Listening started");
            speechRecognizer.startListening(recognizerIntent);
            showToastMessage(getString(R.string.txt_new_recording));
            isPaused = false;
            isRecording = true;
        }
    }

    public void pause(View view){

    }

    public void end(View view){
        Log.d("end", "Stop recording requested");
        deleteSpeechRecognizer();
        isPaused = false;
        isRecording = false;
        mTvPartialRecognition.setText(R.string.txt_ended);
        mTvPartialRecognition.append("\n" + transcriptionText);
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
            speechRecognizer.cancel();
        if(error == 7)
            Log.d("SPEECH", "Result Text final (onError): " + resultText);

        speechRecognizer.startListening(recognizerIntent);
    }



    @Override
    public void onResults(Bundle bundle) {
        Log.d("MainActivity", "onResults: ready ");

        speechRecognizer.startListening(recognizerIntent);
        Log.d("SPEECH", "Result Text final: " + resultText);
        mTvPartialRecognition.append(resultText +" | ");
        transcriptionText.append(resultText);
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

            //mTvPartialRecognition.append(resultText +" | ");
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteSpeechRecognizer();
    }
}
