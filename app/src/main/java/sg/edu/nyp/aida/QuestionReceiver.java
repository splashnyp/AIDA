package sg.edu.nyp.aida;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import java.util.Random;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

public class QuestionReceiver extends BroadcastReceiver {

    private AIDataService aiDataService;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("QuestionReceiver", "Timer Triggered");

        Intent randomIntent = new Intent("randomQuestion");
        randomIntent.putExtra("randomChoice", randomQuestion(context,1,10));
        context.sendBroadcast(randomIntent);
    }

    public String randomQuestion(Context context, int min, int max) {

        Random r = new Random();
        int result = r.nextInt((max - min) + 1) + min;

        Log.d("QuestionReceiver", "Ask Random Question " + result);

        String questionAsked = "I am asking random question " + result;
        Toast.makeText(context, questionAsked , Toast.LENGTH_SHORT).show();

        return questionAsked;
    }

    private void setupNlu() {

        Log.d("RobotUIActivity", "Start DialogFlow");

        String clientAccessToken = "537088457d8844e68f006fab56aef034";
        AIConfiguration aiConfiguration = new AIConfiguration(clientAccessToken,
                AIConfiguration.SupportedLanguages.English);
        aiDataService = new AIDataService(aiConfiguration);
    }

    private void startNlu(final String text) {

        Log.d("RobotUIActivity", "Run DialogFlow");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    AIRequest aiRequest = new AIRequest();
                    aiRequest.setQuery(text);

                    AIResponse aiResponse = aiDataService.request(aiRequest);
                    Result result = aiResponse.getResult();
                    Fulfillment fulfillment = result.getFulfillment();
                    String speech = fulfillment.getSpeech();

                } catch (AIServiceException e) {
                    Log.e("nlu", e.getMessage(), e);
                }
            }
        };
        Threadings.runInBackgroundThread(runnable);
    }
}