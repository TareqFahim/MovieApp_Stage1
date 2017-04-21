package com.example.hope.movieapp_stage1;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainMovieGridFragment extends Fragment {

    private View rootView;
    public MovieGridAdapter postersAdapter;
    private ArrayAdapter detailAdapter;
    MovieDataTask movieTask;
    GridView gridView;
    ListView detailList;
    List image = new ArrayList<>(); // Contain posters' URL
    List movieReleaseDate = new ArrayList();  // Contain movies' release date
    List movieOverview = new ArrayList(); //Contain movie overview
    List movieRate = new ArrayList();  // Movies' Rate
    List movieTitle = new ArrayList();  // Movies' titles
    List movieID = new ArrayList();
    MovieList movieListListner;
    //String movieJson;               // Full json string
    String [] intentExtra = new String[6] ;          // Extra of intent sent to MovieDetail
    public MainMovieGridFragment() {
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.moviefragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        if(id==R.id.action_refresh) {
            updateMovies();
            Log.v("RefreshX", "is clicked " );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateMovies(){
        movieTask = new MovieDataTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_popular));
        movieTask.execute(sort);
        Log.v("UpdateMovies", "has been entered ");
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main_movie_grid, container, false);
        gridView = (GridView) rootView.findViewById(R.id.movie_gridView);
//        ImageView imageview = (ImageView) rootView.findViewById(R.id.movie_grid_imageView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String forecast = postersAdapter.getItem(position);
                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                intentExtra[0] = (String) image.get(position);
                intentExtra[1] = (String) movieTitle.get(position);
                intentExtra[2] = (String) movieReleaseDate.get(position);
                intentExtra[3] = (String) movieOverview.get(position);
                intentExtra[4] = (String) movieRate.get(position);
                intentExtra[5] = (String) movieID.get(position);

                movieListListner.openSelectedmovie(intentExtra);

            }
        });

        return rootView;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        movieListListner = (MovieList) getActivity();

    }

    public void setMovieListListner(MovieList selectedMovie){
        movieListListner = selectedMovie;
    }
    private class detailView{
        public ImageView imageViewDetail;
        public TextView textViewDeatil;
        detailView(){
            imageViewDetail = (ImageView) rootView.findViewById(R.id.detail_imageView);
            textViewDeatil = (TextView) rootView.findViewById(R.id.detail_textView_date);
        }
    }
    public class MovieDataTask extends AsyncTask<String, Void, List<String>> {
        private final String LOG_TAG = MovieDataTask.class.getSimpleName();
        //public ImageCustomAdapter postersAdapter;
        String movieJsonStr = null;
        @Override
        protected List<String> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<String> postersUrls = new ArrayList<String>();
            // Will contain the raw JSON response as a string.

            String api_key = "INSERT API KEY";
            try {
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie";
                final String API_KEY_PARAM = "api_key";
                Uri builtUri = Uri.parse(MOVIE_BASE_URL+"/"+params[0]+"?").buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, api_key)
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "BUILT URI " + builtUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                //Log.v("111","1111111111111111111111");
                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Movie JSON String: " + movieJsonStr);
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
                postersUrls = parseJson(movieJsonStr);
                return postersUrls;
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> result) {

            if (result != null) {
                super.onPostExecute(result);
                image = (ArrayList) result;
                try {
                    getMovieData(movieJsonStr);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error in getting movies' overviews and titles");
                }
                postersAdapter = new MovieGridAdapter(getActivity(),(ArrayList) result);
                gridView.setAdapter(postersAdapter);
            }
        }

        public void getMovieData(String movieJsonStr) throws JSONException {
            //List<String> postersUrls = new ArrayList<String>();
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_TITLE = "original_title";
            final String OWM_OVERVIEW = "overview";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_RATE = "vote_average";
            final String OWM_ID = "id";
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {

                String overview, year, rate , mTitle , id;

                JSONObject movieObject = movieArray.getJSONObject(i);
                overview = movieObject.getString(OWM_OVERVIEW);
                movieOverview.add(i, overview);   // ADD movies' overview to movieOverview list

                year = movieObject.getString(OWM_RELEASE_DATE);
                year = year.substring(0, 4)+"\n120 min"; // Year of release
                movieReleaseDate.add(i, year);        // ADD movies' release date to movieTitle list

                rate = movieObject.getString(OWM_RATE);
                Double x = Double.parseDouble(rate);
                x = (Math.round(x*10))/10.0;
                rate = Double.toString(x);
                movieRate.add(i, rate+"/10");        // Add movies' rate to movieRate list

                mTitle = movieObject.getString(OWM_TITLE);
                movieTitle.add(i, mTitle);          // ADD movies' titles to movieTitle list

                id = movieObject.getString(OWM_ID);
                movieID.add(i ,id);
            }
        }

        private List<String> parseJson(String movieJsonStr) throws JSONException {
            List<String> postersUrls = new ArrayList<String>();
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_POSTER_PATH = "poster_path";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OWM_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {

                String posterUrl;

                // Get the JSON object representing the day
                JSONObject movieObject = movieArray.getJSONObject(i);
                posterUrl = movieObject.getString(OWM_POSTER_PATH);

                postersUrls.add(i, "http://image.tmdb.org/t/p/w185" + posterUrl + "?api_key=30e0eb4c9a04152e0620151e5d67943c");
            }

            for (String s : postersUrls) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return postersUrls;
        }
    }
}

