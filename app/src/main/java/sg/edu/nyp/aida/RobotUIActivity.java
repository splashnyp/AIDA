package sg.edu.nyp.aida;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class RobotUIActivity extends Activity implements SensorEventListener {

    private TextView mainText, subText;
    private Button speakButton;

    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    private PendingIntent pendingIntent;
    private AlarmManager manager;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private boolean mInitialized;
    private double mLastX, mLastY, mLastZ;
    private final double SIDE_NOISE = 3.0;
    private final double FORWARD_NOISE = 3.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot_ui);

        Log.d("RobotUIActivity", "Activity Created");

        mInitialized = false;

        setupAsr();
        setupTts();
        setQuestionInterval(10);
        setMotionDection();

        mainText = findViewById(R.id.mainText);
        subText = findViewById(R.id.subText);
        speakButton = findViewById(R.id.speakButton);

        mainText.setText("Hi, this is the chatbot at your service.");

        speakButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAsr();
            }
        });

        BroadcastReceiver broadcastReceiver =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getExtras();
                String randomChoice = b.getString("randomChoice");
                startTts(randomChoice);
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("randomQuestion"));
    }

    private void setupAsr() {

        Log.d("RobotUIActivity", "Analyzing speech");

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                Log.e("ASR", "Error: " + Integer.toString(error));
            }

            @Override
            public void onResults(Bundle results) {

                Log.e("ASR", "Analyzing Results");

                List<String> texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (texts == null || texts.isEmpty()) {
                    mainText.setText("Sorry, I don't get what you are saying");
                    startTts("Sorry, I don't get what you are saying");
                } else {
                    String text = texts.get(0);
                    subText.setText(text);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });
    }

    private void startAsr() {

        Log.d("RobotUIActivity", "Listening speech");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
                recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

                speechRecognizer.startListening(recognizerIntent);
            }
        };
        Threadings.runInMainThread(this, runnable);
    }

    private void setupTts() {

        Log.d("RobotUIActivity", "Prepare speech");

        textToSpeech = new TextToSpeech(this, null);
    }

    private void startTts(String text) {

        Log.d("RobotUIActivity", "Creating speech");

        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (textToSpeech.isSpeaking()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e("tts", e.getMessage(), e);
                    }
                }
            }
        };
        Threadings.runInBackgroundThread(runnable);
    }

    public void setQuestionInterval(int interval) {

        Log.d("RobotUIActivity", "Interval Set at "+ interval);

        Intent questionIntent = new Intent(this, QuestionReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, questionIntent, 0);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),interval, pendingIntent);
    }

    public void setMotionDection() {

        Log.d("RobotUIActivity", "Motion detection in progress");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {

        Log.d("RobotUIActivity", "Resuming alarm and motion detection");

        super.onResume();
        setQuestionInterval(1000);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {

        Log.d("RobotUIActivity", "Stopping alarm and motion detection");

        super.onPause();
        manager.cancel(pendingIntent);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        else {
            double deltaX = Math.abs(mLastX - x);
            double deltaY = Math.abs(mLastY - y);
            double deltaZ = Math.abs(mLastZ - z);

            if (deltaX < SIDE_NOISE) deltaX = (float)0.0;
            if (deltaY < SIDE_NOISE) deltaY = (float)0.0;
            if (deltaZ < FORWARD_NOISE) deltaZ = (float)0.0;

            mLastX = x;
            mLastY = y;
            mLastZ = z;

            if (deltaX > 0.0) {
                mainText.setText("Please remember to signal when you are changing lane");
                startTts("Please remember to signal when you are changing lane");

            }
            else if (deltaZ > 0.0) {
                mainText.setText("Please slow down and do not jam your brakes");
                startTts("Please slow down and do not jam your brakes");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
