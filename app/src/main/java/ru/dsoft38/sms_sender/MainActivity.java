package ru.dsoft38.sms_sender;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    private ImageButton btnBrowse;
    private ImageButton btnStart;
    private ImageButton btnPause;
    private ImageButton btnStop;
    private ImageButton btnClean;

    private static TextView tvPhoneNumberListFilePatch;
    private static TextView tvPhoneNumberCount;
    private TextView tvPhoneNumberPathFile;
    private TextView tvMessage;

    private EditText editMessageTest;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBrowse   = (ImageButton) findViewById(R.id.imgButtonBrowse);
        btnStart    = (ImageButton) findViewById(R.id.imgButtonSend);
        btnPause    = (ImageButton) findViewById(R.id.imgButtonPause);
        btnStop     = (ImageButton) findViewById(R.id.imgButtonStop);
        btnClean    = (ImageButton) findViewById(R.id.imgButtonClean);

        editMessageTest = (EditText) findViewById(R.id.editMessageText);

        tvPhoneNumberListFilePatch  = (TextView) findViewById(R.id.tvPhoneNumberListPath);
        tvPhoneNumberPathFile       = (TextView) findViewById(R.id.tvPhoneNumberPathFile);
        tvPhoneNumberCount          = (TextView) findViewById(R.id.tvPhoneNumCount);
        tvMessage                   = (TextView) findViewById(R.id.tvMessage);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Назначаем обработчик нажатия на кнопку Отправить СМС
        //btnStart.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View view) {
        //        //startService(new Intent(this, SendSMSService.class));
        //        //startService(new Intent(this, MyService.class));
        //    }
        //});
    }


    public void onClickSend(View v) {
        String[] numberList = null;
        numberList = new String[3];
        numberList[0] = "Cheese";
        numberList[1] = "Pepperoni";
        numberList[2] = "Black Olives";


        startService(new Intent(this, SendSMSService.class).putExtra("numberList", numberList));
    }

    public void onClickStop(View v) {
        stopService(new Intent(this, SendSMSService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
