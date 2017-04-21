package com.example.hope.movieapp_stage1;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class reviewActivityFragment extends Fragment {

    View rootView;
    ListView reviewList;
    ArrayAdapter reviewAdapter;
    List<String> movieReviews = new ArrayList<String>();

    public reviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_review, container, false);
        reviewList = (ListView) rootView.findViewById(R.id.reviews_list);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("MyClass")) {
            movieReviews = (ArrayList) intent.getSerializableExtra("MyClass");
            reviewAdapter = new ArrayAdapter<String>(
                    getActivity()
                    , R.layout.review_textview
                    , R.id.reviews_list_textview
                    , new ArrayList<String>());
            for(String review : movieReviews){
                if(movieReviews != null){
                    reviewAdapter.add(review);
                }
                reviewList.setAdapter(reviewAdapter);
            }
        }

        return rootView;
    }
}
