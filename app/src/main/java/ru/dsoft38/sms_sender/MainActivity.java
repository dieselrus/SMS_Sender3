package ru.dsoft38.sms_sender;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;


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

    // Максимальная длина текста СМС
    private int maxSMSLen = 160;
    static List<String> strNumbers;

    // Текущее количество СМС
    private int smsCount = 1;

    private SharedPreferences sp;

    //
    private int iMount = 1;
    private int iDay = 1;
    private int iSMSCount = 0;
    private Calendar calendar;
    private int MaxSMSCountSend = 100;

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

        // Получаем текущую дату
        calendar = Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
        calendar.setTime(new java.util.Date());

        sp = PreferenceManager.getDefaultSharedPreferences(this);

        iDay = sp.getInt("DAY_OF_YEAR", calendar.get(java.util.Calendar.DAY_OF_YEAR));

        // Если текущий день года не равен записаному. сбрасываем счетчик СМС
        if(iDay != calendar.get(java.util.Calendar.DAY_OF_YEAR)){
            resetSMSCount();
        }

        iSMSCount = sp.getInt("iSMSCount", 0);

        //Обработка ввода символов в текстовое поле для текста СМС
        editMessageTest.addTextChangedListener(new TextWatcher()  {
            @Override
            public void afterTextChanged(Editable s) {
                if(editMessageTest.getText().toString().length() > 0) {
                    btnClean.setEnabled(true);
                    btnClean.setBackgroundResource(R.drawable.clean_up);
                } else {
                    btnClean.setEnabled(false);
                    btnClean.setBackgroundResource(R.drawable.clean_down);
                }

                //imgStatus.setVisibility(View.INVISIBLE);
                //tvMessageText.setText(getResources().getString(R.string.MessageText) +
                // " (" + String.valueOf(MAX_LENGTH_SMS - strMyName.length() - txtSMSText.length()) + ")");

                //String strCurrentSMS = "1";

                if(isCyrillic(editMessageTest.getText().toString())){
                    maxSMSLen = 70;
                } else {
                    maxSMSLen = 160;
                }

                smsCount = (int)(editMessageTest.getText().length() / maxSMSLen) + 1;
                String strCurrentSMS = String.valueOf(smsCount);

                String totalSMSLen = String.valueOf(editMessageTest.getText().length());

                // SMS Sender Pro
                //tvMessage.setText(getResources().getString(R.string.messageText) + " (" + totalSMSLen + "/" + strCurrentSMS + ")");

                //tvMessage.setText(getResources().getString(R.string.messageText) + " (" + totalSMSLen + "/" + maxSMSLen + ")");
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

        });
    }


    public void onClickSend(View v) {
        if (btnStart.isEnabled()) {
            // Выбран файл с номерами телефонов
            if (tvPhoneNumberListFilePatch.getText().length() == 0 || strNumbers.size() == 0) {
                Toast.makeText(getApplicationContext(),
                        "Выберите файл со списком номеров для отправки!",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // Проверим введен ли текст СМС
            if (editMessageTest.getText().length() < 2) {
                Toast.makeText(getApplicationContext(),
                        "Введите текст СМС!",
                        Toast.LENGTH_LONG).show();
                return;
            } else {

                //String[] numberList = new String[100];

                //for ( int i = 0; i < 100; i++ ){
                //    numberList[i] =
                //}

                //AcceptSendCount();

                String[] numberList = new String[AcceptSendCount(strNumbers.size())];

                /**
                if (strNumbers.size() <= 100){
                    numberList = strNumbers.toArray(new String[strNumbers.size()]);
                } else {
                    // Устанавливаем размер списка равный оставшимся на сегодня кол-ом не отправленных СМС
                    numberList = strNumbers.toArray(new String[100]);
                }
                **/

                // Если список номеров телефонов пустой, то выходим.
                if (numberList.length == 0)
                    return;

                // Передаем данные в сервис отправки СМС
                Intent sms = new Intent(this, SendSMSService.class);
                sms.putExtra("numberList", numberList);
                sms.putExtra("smsText", editMessageTest.getText().toString());

                //startService(sms);

                // Делаем кнопку не активной
                btnBrowse.setEnabled(false);
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
                btnPause.setEnabled(true);
                btnClean.setEnabled(false);
                editMessageTest.setEnabled(false);

                btnBrowse.setBackgroundResource(R.drawable.browse_down);
                btnStart.setBackgroundResource(R.drawable.play_down);
                btnStop.setBackgroundResource(R.drawable.stop_up);
                btnPause.setBackgroundResource(R.drawable.pausa_up);
                btnClean.setBackgroundResource(R.drawable.clean_down);

            }
        }
    }

    // Назначаем обработчик нажатия на кнопку остановки отправки
    public void onClickStop(View v) {
        if (btnStop.isEnabled()) {
            // Останавливаем отправку
            stopService(new Intent(this, SendSMSService.class));

            btnStart.setEnabled(true);
            btnPause.setEnabled(false);
            btnStop.setEnabled(false);
            btnBrowse.setEnabled(true);
            btnClean.setEnabled(true);
            editMessageTest.setEnabled(true);

            btnStart.setBackgroundResource(R.drawable.play_up);
            btnStop.setBackgroundResource(R.drawable.stop_down);
            btnPause.setBackgroundResource(R.drawable.pausa_down);
            btnBrowse.setBackgroundResource(R.drawable.browse_up);
            btnClean.setBackgroundResource(R.drawable.clean_up);
        }
    }

    // Назначаем обработчик нажатия на кнопку выбора файла
    public void onClickBrowse(View v) {
        if (btnBrowse.isEnabled()) {
            OpenFileDialog fd = new OpenFileDialog(MainActivity.this).setFilter(".*\\.txt");
            fd.show();

            // Чтение списка номеров из файла
            //strNumbers = readFile(tvPhoneNumberListFilePatch.getText().toString());

            //tvPhoneNumberListFilePatch.setText(fd.getContext().);
            //tvPhoneNumberPathFile.setText(getResources().getString(R.string.phoneNumberList) + " (" + String.valueOf(strNumbers.size()) + ")");

            btnStart.setEnabled(true);
            btnPause.setEnabled(false);
            btnStop.setEnabled(false);

            btnStart.setBackgroundResource(R.drawable.play_up);
            btnStop.setBackgroundResource(R.drawable.stop_down);
            btnPause.setBackgroundResource(R.drawable.pausa_down);
        }
    }

    // Очистка тескта СМС
    public void onClickClean(View v) {
        if (btnClean.isEnabled()) {
            editMessageTest.setText("");
        }
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

    // Чтение файла с номерами
    static List<String> readFile(String filePath){

        List<String> strNumbers = new Vector<String>();
        //File sdcard = Environment.getExternalStorageDirectory();

        //Создаём объект файла
        //File file = new File(sdcard, filePath);
        File file = new File(filePath);

        //Read text from file
        //StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                //text.append(line);
                //text.append('\n');
                strNumbers.add(line.trim());
            }
        }
        catch (IOException e) {
            Log.d("Data", e.getMessage().toString());
        }

        //Log.d("Data", text);

        return strNumbers;
    }

    // Определение языка (Кирилица или нет)
    boolean isCyrillic(String _str){
        for(int i = 0; i < _str.length(); i++){
            //String hexCode = Integer.toHexString(_str.codePointAt(i)).toUpperCase();
            int hexCode = _str.codePointAt(i);
            //Log.d("Data", String.valueOf(hexCode));

            if(hexCode > 1040 && hexCode < 1103){
                return true;
            }

        }
        return false;
    }

    static public void setFilePath(String path){
        // Чтение списка номеров из файла
        strNumbers = readFile(path);

        tvPhoneNumberCount.setText("(" + String.valueOf(strNumbers.size()) + ")");
        tvPhoneNumberListFilePatch.setText(path);
    }

    // Проверяем разрешена ли отправка и сколько еще осталось СМС из лиминта бесплатной версии
    private int AcceptSendCount( int count ){
        //java.util.Calendar calendar = java.util.Calendar.getInstance(java.util.TimeZone.getDefault(), java.util.Locale.getDefault());
        calendar.setTime(new java.util.Date());
        //int currentYear = calendar.get(java.util.Calendar.YEAR);
        int currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR);

        if(currentDay == iDay && iSMSCount < MaxSMSCountSend){
            SharedPreferences.Editor editor = sp.edit();

            int freeSMS = MaxSMSCountSend - iSMSCount;

            if ( (freeSMS < MaxSMSCountSend) && (count + iSMSCount > MaxSMSCountSend) ){
                Toast.makeText(getApplicationContext(), "Будет отправленно " + String.valueOf( freeSMS ) + "!\n Сегодня Вы уже отправили " + String.valueOf( iSMSCount ) + " СМС. \n Приобретите полную версию.", Toast.LENGTH_SHORT).show();
            }

            editor.putInt("DAY_OF_YEAR", currentDay);
            //sp.edit().apply();
            //editor.commit();
            iSMSCount = iSMSCount + freeSMS;
            editor.putInt("iSMSCount", iSMSCount);
            //sp.edit().apply();
            editor.commit();

            iSMSCount = sp.getInt("iSMSCount", 0);

            //txtCount.setText("Отправлено " + iSMSCount + " из 15.");



            return freeSMS;
        } else {
            Toast.makeText(getApplicationContext(), "Вы израсходовали лимит (" + String.valueOf( MaxSMSCountSend ) + " СМС) на сегодня!\n Приобретите полную версию.", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    // Сбрасываем счетчик СМС
    private void resetSMSCount(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("iSMSCount", 0);
        //editor.putInt("iMount", calendar.get(java.util.Calendar.MONTH));
        editor.putInt("DAY_OF_YEAR", calendar.get(Calendar.DAY_OF_YEAR));
        editor.commit();
    }
}
