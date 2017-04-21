package com.example.hope.movieapp_stage1;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {
    View rootView;
    View header;
    List<String> movieTrailers = new ArrayList<String>();
    List<String> movieReviews = new ArrayList<String>();
    ArrayAdapter testAdapter;
    String movieID;
    ListView listView;
    ListView reviewList;
    public MovieDetailFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        listView = (ListView) rootView.findViewById(R.id.trailers_list);
        header = inflater.inflate(R.layout.detail_fragment_header, null, false);
        //View footer = inflater.inflate(R.layout.review_textview, null, false);
        reviewList = (ListView) rootView.findViewById(R.id.reviews_list);


        //Intent intent = getActivity().getIntent();
        Intent intent = getActivity().getIntent();
        Bundle b = intent.getExtras();
        //String s;
        if (b != null /*&& intent.hasExtra("xx")*/) {

            //b = intent.getExtras();
            final String[] movieData  = b.getStringArray("xx");
            //s = intent.getStringExtra(intent.EXTRA_TEXT);
            ImageView iview = (ImageView) header.findViewById(R.id.detail_imageView);
            Picasso.with(getActivity()).load(movieData[0]).into(iview);
            ((TextView) header.findViewById(R.id.detail_textView_title)).setText(movieData[1]);
            ((TextView) header.findViewById(R.id.detail_textView_date)).setText(movieData[2]);
            ((TextView) header.findViewById(R.id.detail_textView_overview)).setText(movieData[3]);
            ((TextView) header.findViewById(R.id.detail_textView_rate)).setText(movieData[4]);
            Button reviewBtn = ((Button) header.findViewById(R.id.reviews_btn));
            reviewBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent reviewIntent = new Intent(getActivity(), reviewActivity.class).putExtra("MyClass", (Serializable) movieReviews);
                    startActivity(reviewIntent);
                }
            });
            movieID = movieData[5];

            testAdapter = new ArrayAdapter<String>(
                    // The current context (this fragment's parent )
                    getActivity()
                    // ID of the list item layout
                    , R.layout.list_item_trailers
                    // ID of the textView populate
                    , R.id.trailers_list_textview
                    // Forecast data
                    , new ArrayList<String>());

            listView.addHeaderView(header);

            fetchVideoJson ff = new fetchVideoJson();
            ff.execute(movieID);
            fetchReviewsJson reviewFetch = new fetchReviewsJson();
            reviewFetch.execute(movieID);


            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(movieTrailers.get(position - 1)).buildUpon().build();
                    intent.setData(uri);
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                        startActivity(intent);
                }
            });
        }
        return rootView;
    }
    public class fetchVideoJson extends AsyncTask<String, Void, List<String>> {
        private final String LOG_TAG = fetchVideoJson.class.getSimpleName();
        String jsonStr = null;

        @Override
        protected List<String> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.

            String api_key = "INSERT API Key HERE";
            try {
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL + "/" + params[0] + "/" + "videos" + "?").buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "BUILT URI " + builtUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
                Log.v(LOG_TAG, "Movie JSON String: " + jsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                movieTrailers = parseVideos(jsonStr);
                return movieTrailers;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private List<String> parseVideos(String movieJsonStr) throws JSONException {
            List<String> movieTrailers = new ArrayList<String>();
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_VIDEO_KEY = "key";

            JSONObject videoJson = new JSONObject(movieJsonStr);
            JSONArray videoArray = videoJson.getJSONArray(OWM_RESULTS);

            for (int i = 0; i < videoArray.length(); i++) {

                String trailerKey;

                // Get the JSON object representing the day
                JSONObject videoObject = videoArray.getJSONObject(i);
                trailerKey = videoObject.getString(OWM_VIDEO_KEY);

                movieTrailers.add(i, "https://www.youtube.com/watch?v=" + trailerKey);
            }

            for (String s : movieTrailers) {
                Log.v(LOG_TAG, "Movie Trailers: " + s);
            }
            return movieTrailers;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (result != null) {
                super.onPostExecute(result);
                String s = "Trailer ";
                for (String trailer : result) {
                    testAdapter.add(s + (result.indexOf(trailer) + 1));
                }
            }
            listView.setAdapter(testAdapter);
        }
    }

    public class fetchReviewsJson extends AsyncTask<String, Void, List<String>> {
        private final String LOG_TAG = fetchReviewsJson.class.getSimpleName();
        String jsonStr = null;

        @Override
        protected List<String> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.

            String api_key = "INSERT API KEY";
            try {
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL + "/" + params[0] + "/" + "reviews" + "?").buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "BUILT URI " + builtUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
                Log.v(LOG_TAG, "Movie JSON String: " + jsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                movieReviews = parseReviews(jsonStr);
                return movieReviews;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private List<String> parseReviews(String movieJsonStr) throws JSONException {
            List<String> Reviews = new ArrayList<String>();
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_CONTENT = "content";
            final String OWM_AUTHOR = "author";

            JSONObject reviewJson = new JSONObject(movieJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(OWM_RESULTS);

            for (int i = 0; i < reviewArray.length(); i++) {

                String review, author;

                // Get the JSON object representing the day
                JSONObject reviewObject = reviewArray.getJSONObject(i);
                author = reviewObject.getString(OWM_AUTHOR);
                review = reviewObject.getString(OWM_CONTENT);

                Reviews.add(author + "\n\n" + review + "\n\n-------------------------------\n");
            }

            for (String s : Reviews) {
                Log.v(LOG_TAG, "Movie Reviews: " + s);
            }
            return Reviews;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (result != null) {
                super.onPostExecute(result);
            }
        }
    }

}
