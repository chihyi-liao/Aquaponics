package com.softsnail.aquaponics;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.StringEntity;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton;
import android.view.View;
import android.util.Patterns;
import org.apache.http.params.CoreConnectionPNames;
import android.os.Handler;
import android.view.View.OnClickListener;

public class MainActivity extends ActionBarActivity {
    private HttpSensorGetTask HttpSensorGet;
    private String myUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HttpSensorGet = new HttpSensorGetTask();
        final ToggleButton relay1ToggleButton = (ToggleButton) findViewById(R.id.relay1ToggleButton);
        final ToggleButton relay2ToggleButton = (ToggleButton) findViewById(R.id.relay2ToggleButton);
        final ToggleButton relay3ToggleButton = (ToggleButton) findViewById(R.id.relay3ToggleButton);
        final ToggleButton relay4ToggleButton = (ToggleButton) findViewById(R.id.relay4ToggleButton);

        relay1ToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (relay1ToggleButton.isChecked()) {
                    new HttpSensorSetTask().execute(myUrl, "relay1", "true");
                }
                else {
                    new HttpSensorSetTask().execute(myUrl, "relay1", "false");
                }
            }
        });

        relay2ToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (relay2ToggleButton.isChecked()) {
                    new HttpSensorSetTask().execute(myUrl, "relay2", "true");
                }
                else {
                    new HttpSensorSetTask().execute(myUrl, "relay2", "false");
                }
            }
        });
        relay3ToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (relay3ToggleButton.isChecked()) {
                    new HttpSensorSetTask().execute(myUrl, "relay3", "true");
                }
                else {
                    new HttpSensorSetTask().execute(myUrl, "relay3", "false");
                }
            }
        });
        relay4ToggleButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (relay4ToggleButton.isChecked()) {
                    new HttpSensorSetTask().execute(myUrl, "relay4", "true");
                }
                else {
                    new HttpSensorSetTask().execute(myUrl, "relay4", "false");
                }
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HttpSensorGet.cancel(true);
    }

    //
    public void onClickCheckBtn(View view) {
        TextView connStatus = (TextView) findViewById(R.id.connectStatus);
        EditText inputDomain = (EditText) findViewById(R.id.inputDomain);
        String url = inputDomain.getText().toString();
        myUrl = new String(url);

        // 檢查輸入是否合法
        if( Patterns.WEB_URL.matcher(url).matches()) {
            if (connStatus.getText() == "無法取得資料") {
                HttpSensorGet.cancel(true);
                HttpSensorGet = null;
                HttpSensorGet = new HttpSensorGetTask();
            }
            // 執行 HttpGet 到指定網址
            if(HttpSensorGet.getStatus() == AsyncTask.Status.PENDING) {
                HttpSensorGet.execute(url);
            }
        }
        else {
            connStatus.setText("無效的網址");
        }
    }

    // HttpSensorGetTask
    private class HttpSensorGetTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute (){
            TextView connStatus = (TextView) findViewById(R.id.connectStatus);
            connStatus.setText("連線中");
        }
        @Override
        protected String doInBackground(String... urls) {
            // 不可在 doInBackground 更新 UI 元件
            InputStream inputStream = null;
            String result = "";
            if (isCancelled())
                return null;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                //設定5secs沒連上即timeout
                httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);

                HttpResponse httpResponse = httpclient.execute(new HttpGet(urls[0]));
                inputStream = httpResponse.getEntity().getContent();
                if(inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                }
                else {
                    result = "無法取得資料";
                }
            } catch (Exception e) {
                result = "無法取得資料";
                Log.d("InputStream", e.getLocalizedMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            TextView connStatus = (TextView) findViewById(R.id.connectStatus);
            TextView tempStatus = (TextView) findViewById(R.id.tempStatus);
            TextView humidityStatus = (TextView) findViewById(R.id.humidityStatus);
            TextView relay1Status = (TextView) findViewById(R.id.relay1Status);
            TextView relay2Status = (TextView) findViewById(R.id.relay2Status);
            TextView relay3Status = (TextView) findViewById(R.id.relay3Status);
            TextView relay4Status = (TextView) findViewById(R.id.relay4Status);
            ToggleButton relay1Toggle = (ToggleButton) findViewById(R.id.relay1ToggleButton);
            ToggleButton relay2Toggle = (ToggleButton) findViewById(R.id.relay2ToggleButton);
            ToggleButton relay3Toggle = (ToggleButton) findViewById(R.id.relay3ToggleButton);
            ToggleButton relay4Toggle = (ToggleButton) findViewById(R.id.relay4ToggleButton);

            if (result == null || isCancelled() || result.equals("無法取得資料")){
                connStatus.setText("無法取得資料");
                return;
            }

            try {
                JSONObject json = new JSONObject(result);

                // 更新 UI
                connStatus.setText("連線成功");
                String sensor_value = null;
                sensor_value = json.getString("temperature");
                tempStatus.setText(sensor_value);
                sensor_value = json.getString("humidity");
                humidityStatus.setText(sensor_value);

                sensor_value = json.getString("relay1");
                relay1Status.setText(sensor_value);
                // 設定 Relay1 開關
                if(sensor_value == "true")
                    relay1Toggle.setChecked(true);
                else
                    relay1Toggle.setChecked(false);

                sensor_value = json.getString("relay2");
                relay2Status.setText(sensor_value);
                // 設定 Relay2 開關
                if(sensor_value == "true")
                    relay2Toggle.setChecked(true);
                else
                    relay2Toggle.setChecked(false);

                sensor_value = json.getString("relay3");
                relay3Status.setText(sensor_value);
                // 設定 Relay3 開關
                if(sensor_value == "true")
                    relay3Toggle.setChecked(true);
                else
                    relay3Toggle.setChecked(false);

                sensor_value = json.getString("relay4");
                relay4Status.setText(sensor_value);
                // 設定 Relay4 開關
                if(sensor_value == "true")
                    relay4Toggle.setChecked(true);
                else
                    relay4Toggle.setChecked(false);
            } catch (JSONException e) {
                // 例外處理
                connStatus.setText("無法取得資料");
                Log.d("json", "no data");
                e.printStackTrace();
                return;
            }
            // 當response後會等5秒, 再重複執行HttpGet.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    HttpSensorGet = new HttpSensorGetTask();
                    HttpSensorGet.execute(myUrl);
                }
            }, 5000);
        }
        private  String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }

    }
    private class HttpSensorSetTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            InputStream inputStream = null;
            String result = "";
            if(params.length!=3) {
                Log.d("HttpSensorSetTask", "Lengths error");
                return result;
            }
            String myUrl = params[0];
            String putKey = params[1];
            String tmpVal = params[2];
            Integer putVal = 1;
            if(tmpVal == "false") {
                putVal = 0;
            }

            try {
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                // 2. make POST request to the given URL
                HttpPut httpPUT = new HttpPut(myUrl);
                String json = "";
                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(putKey,putVal);

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();
                Log.d("json", json);
                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);
                // 6. set httpPost Entity
                httpPUT.setEntity(se);
                // 7. Set some headers to inform server about the type of the content
                httpPUT.setHeader("Accept", "application/json");
                httpPUT.setHeader("Content-type", "application/json");
                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httpPUT);
            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return "EXITO!";
        }
    }
}
