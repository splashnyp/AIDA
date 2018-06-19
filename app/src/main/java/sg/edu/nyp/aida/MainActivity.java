package sg.edu.nyp.aida;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button activateButton, resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Activity Created");

        activateButton = findViewById(R.id.activateButton);
        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("MainActivity", "Go to RobotUIActivity");

                Intent i = new Intent (getApplicationContext(), RobotUIActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        resetButton = findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("MainActivity", "Reset Settings");

                SharedPreferences prefs = getSharedPreferences("sg.edu.nyp.aida", MODE_PRIVATE);
                prefs.edit().putString("user_name", "John").commit();
                prefs.edit().putString("user_age", "25").commit();
                prefs.edit().putString("user_gender", "Male").commit();

                Toast.makeText(getApplicationContext(),"App data initialized", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
