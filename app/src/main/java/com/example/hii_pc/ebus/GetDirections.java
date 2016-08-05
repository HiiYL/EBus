package com.example.hii_pc.ebus;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

public class GetDirections {
    Listener listener;

    class DirectionsTask extends AsyncTask<String, Void, String> {
        DirectionsTask() {
        }

        protected String doInBackground(String... url) {
            String data = null;
            try {
                data = GetDataFromUrl.getDataFromUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(new String[]{result});
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        private ParserTask() {
        }

        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            List<List<HashMap<String, String>>> routes = null;
            try {
                routes = new RoutesJsonParser().parse(new JSONObject(jsonData[0]));
            } catch (Exception e) {
                GetDirections.this.listener.onFail();
                e.printStackTrace();
            }
            return routes;
        }

        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            super.onPostExecute(result);
            if (result == null || result.size() <= 0) {
                GetDirections.this.listener.onFail();
            } else {
                GetDirections.this.listener.onSuccessfulRouteFetch(result);
            }
        }
    }

    public GetDirections(Listener listener) {
        this.listener = listener;
    }

    public void startGettingDirections(String downloadUrl) {
        new DirectionsTask().execute(new String[]{downloadUrl});
    }
}