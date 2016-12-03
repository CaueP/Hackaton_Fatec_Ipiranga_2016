package br.com.cauep.speechrecognizerapp;

import android.os.Environment;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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

    // Variaveis de controle para gravacao do arquivo
    private File diretorio;
    private String nomeDiretorio;
    private String diretorioApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mbtStart = (Button) findViewById(R.id.btStart);
        mbtEnd = (Button) findViewById(R.id.btEnd);

        mTvPartialRecognition = (TextView) findViewById(R.id.tvPartialRecognition);
        nomeDiretorio = getString(R.string.app_name);
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
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500);
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

            isPaused = true;
            isRecording = false;
        } else if(isPaused){ // se estiver pausado, volta a gravar
            mTvPartialRecognition.setText("");
            setSpeechRecognizer();
            speechRecognizer.startListening(recognizerIntent);
            showToastMessage(getString(R.string.txt_recording));
            Log.d(TAG, "Listening Resumed");
            isPaused = false;
            isRecording = true;
        } else {    // Condição se não está gravando nem pausado, ou seja, uma nova gravação
            // Reinicia a string que armazenará a transcrição
            mTvPartialRecognition.setText("");
            setSpeechRecognizer();
            Log.d(TAG, "Listening started");
            speechRecognizer.startListening(recognizerIntent);
            showToastMessage(getString(R.string.txt_new_recording));
            isPaused = false;
            isRecording = true;
        }
    }

    public void end(View view){
        Log.d("end", "Stop recording requested");
        deleteSpeechRecognizer();
        isPaused = false;
        isRecording = false;
        //mTvPartialRecognition.setText(R.string.txt_ended);
        mTvPartialRecognition.setText("");
        mTvPartialRecognition.append("\n" + transcriptionText);
        writeFile();
    }

    public void writeFile() {

        // criar o diretorio
        diretorioApp = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + "/"+nomeDiretorio+"/";
        diretorio = new File(diretorioApp);
        diretorio.mkdirs();

        //Quando o File() tem um parâmetro ele cria um diretório.
        //Quando tem dois ele cria um arquivo no diretório onde é informado.
            String nomeArquivo = String.valueOf(new Date().getTime()) + ".txt";

            File fileExt = new File(diretorioApp, nomeArquivo);


        try {
            //Cria o arquivo
            fileExt.getParentFile().mkdirs();

            //Abre o arquivo
            FileOutputStream fosExt = null;

            fosExt = new FileOutputStream(fileExt);

            Writer out = new BufferedWriter(new OutputStreamWriter(
                    fosExt, "UTF-8"));

            out.write(transcriptionText.toString());  //Escreve no arquivo
            Log.d("TAG","transcriptionText writing: " + transcriptionText.toString().getBytes());

            showToastMessage(getString(R.string.save_file_succeed));
            transcriptionText = new StringBuilder();
            out.close(); // fecha arquivo
        } catch (FileNotFoundException e) {
            showToastMessage(getString(R.string.save_file_failed));
            e.printStackTrace();
        } catch (IOException e) {
            showToastMessage(getString(R.string.save_file_failed));
            e.printStackTrace();
        }
    }

    void  showToastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_voice_recognition, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


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
        mTvPartialRecognition.append(resultText + "\n");
        transcriptionText.append("\n" + resultText);
        ArrayList<String> textMatchlist = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
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
