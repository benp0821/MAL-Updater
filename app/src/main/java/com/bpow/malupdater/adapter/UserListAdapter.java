package com.bpow.malupdater.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bpow.malupdater.R;
import com.bpow.malupdater.activity.UserListActivity;
import com.bpow.malupdater.object.UserListElement;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

public class UserListAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<UserListAdapter.UserListViewHolder>{

    private final ArrayList<UserListElement> userListElementList = new ArrayList<>();

    public void add(UserListElement userListElement){
        userListElementList.add(userListElement);
        notifyItemInserted(userListElementList.size() - 1);
    }

    @SuppressLint("NotifyDataSetChanged") //The entire list should be invalidated, so this function call is needed
    public void clear(){
        userListElementList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_user_list, parent, false);

        return new UserListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        UserListElement userListElement = userListElementList.get(position);

        holder.animeImageView.setImageBitmap(null);
        holder.animeImageView.setTag(position); //sets the tag to position as a check to prevent multiple old images from loading
                                                //before being replaced with the correct image on fast scrolling
        new DownloadImageTask(holder.animeImageView, position).execute(userListElement.getPicture().getLargePicture());

        holder.animeNameView.setText(userListElement.getNameID().getName());

        if (userListElement.getAltNames().getSynonyms() != null && userListElement.getAltNames().getSynonyms().size() > 0){
            holder.animeSynonyms1View.setVisibility(View.VISIBLE);
            holder.animeSynonyms2View.setVisibility(View.VISIBLE);
            holder.animeSynonyms3View.setVisibility(View.VISIBLE);
            holder.synLabelView.setVisibility(View.VISIBLE);
            if (userListElement.getAltNames().getSynonyms().size() > 3){
                holder.animeSynonyms3View.setText("...");
                holder.animeSynonyms2View.setText(userListElement.getAltNames().getSynonyms().get(1));
                holder.animeSynonyms1View.setText(userListElement.getAltNames().getSynonyms().get(0));
            }else if (userListElement.getAltNames().getSynonyms().size() == 3){
                holder.animeSynonyms3View.setText(userListElement.getAltNames().getSynonyms().get(2));
                holder.animeSynonyms2View.setText(userListElement.getAltNames().getSynonyms().get(1));
                holder.animeSynonyms1View.setText(userListElement.getAltNames().getSynonyms().get(0));
            }else if (userListElement.getAltNames().getSynonyms().size() == 2){
                holder.animeSynonyms3View.setText(null);
                holder.animeSynonyms3View.setVisibility(View.GONE);
                holder.animeSynonyms2View.setText(userListElement.getAltNames().getSynonyms().get(1));
                holder.animeSynonyms1View.setText(userListElement.getAltNames().getSynonyms().get(0));
            }else{
                holder.animeSynonyms3View.setText(null);
                holder.animeSynonyms3View.setVisibility(View.GONE);
                holder.animeSynonyms2View.setText(null);
                holder.animeSynonyms2View.setVisibility(View.GONE);
                holder.animeSynonyms1View.setText(userListElement.getAltNames().getSynonyms().get(0));
            }
        }else{
            holder.animeSynonyms1View.setText(null);
            holder.animeSynonyms1View.setVisibility(View.GONE);
            holder.animeSynonyms2View.setText(null);
            holder.animeSynonyms2View.setVisibility(View.GONE);
            holder.animeSynonyms3View.setText(null);
            holder.animeSynonyms3View.setVisibility(View.GONE);
            holder.synLabelView.setVisibility(View.GONE);
        }

        if (userListElement.getAltNames().getEnTitle() != null && !userListElement.getAltNames().getEnTitle().equals("")){
            holder.animeEnTitleView.setText(userListElement.getAltNames().getEnTitle());
            holder.animeEnTitleView.setVisibility(View.VISIBLE);
            holder.enLabelView.setVisibility(View.VISIBLE);
        }else{
            holder.animeEnTitleView.setText(null);
            holder.animeEnTitleView.setVisibility(View.GONE);
            holder.enLabelView.setVisibility(View.GONE);
        }


        if (userListElement.getAltNames().getJpTitle() != null && !userListElement.getAltNames().getJpTitle().equals("")){
            holder.animeJpTitleView.setText(userListElement.getAltNames().getJpTitle());
            holder.animeJpTitleView.setVisibility(View.VISIBLE);
            holder.jpLabelView.setVisibility(View.VISIBLE);
        }else{
            holder.animeJpTitleView.setText(null);
            holder.animeJpTitleView.setVisibility(View.GONE);
            holder.jpLabelView.setVisibility(View.GONE);
        }

        holder.animeMediaTypeView.setText(userListElement.getMediaType());
        holder.animeAiringStatusView.setText(userListElement.getAiringStatus());

        StringBuilder seasonStr = new StringBuilder();
        seasonStr.append(userListElement.getSeason().getSeason());

        if (!userListElement.getSeason().getYear().equals("")) {
            seasonStr.append(" ");
            seasonStr.append(userListElement.getSeason().getYear());
        }

        holder.animeSeasonView.setText(seasonStr);

        int score = userListElement.getUserListDetails().getScore();
        StringBuilder scoreString = new StringBuilder();
        if (score != 0) {
            scoreString.append(String.format(Locale.US, "%d", score));
        }else{
            scoreString.append("-");
        }

        holder.scoreView.setText(scoreString.append(" / 10").toString());

        int numWatchedEpisodesOrReadVolumes = userListElement.getUserListDetails().getNumWatchedEpisodesOrReadVolumes();
        String numWatchedEpisodesOrReadVolumesString;
        if (numWatchedEpisodesOrReadVolumes == 0){
            numWatchedEpisodesOrReadVolumesString = "-";
        }else{
            numWatchedEpisodesOrReadVolumesString = String.format(Locale.US, "%d", numWatchedEpisodesOrReadVolumes);
        }
        holder.numWatchedEpisodesOrReadVolumesView.setText(numWatchedEpisodesOrReadVolumesString);

        int numEpisodesOrVolumes = userListElement.getNumEpisodesOrVolumes();
        String numEpisodesOrVolumesString;
        if (numEpisodesOrVolumes == 0){
            numEpisodesOrVolumesString = "-";
        }else{
            numEpisodesOrVolumesString = String.format(Locale.US, "%d", numEpisodesOrVolumes);
        }
        holder.numEpisodesOrVolumesView.setText(numEpisodesOrVolumesString);

        if (userListElement.getListType() == UserListActivity.ListType.MANGA) {
            holder.spaceBetweenVolAndChapIndicators.setVisibility(View.VISIBLE);
            holder.chapterCountsLinearLayout.setVisibility(View.VISIBLE);

            int numReadChapters = userListElement.getUserListDetails().getNumReadChapters();
            String numReadChaptersString;
            if (numReadChapters == 0) {
                numReadChaptersString = "-";
            } else {
                numReadChaptersString = String.format(Locale.US, "%d", numReadChapters);
            }
            holder.numReadChaptersView.setText(numReadChaptersString);

            int numChapters = userListElement.getNumChapters();
            String numChaptersString;
            if (numChapters == 0) {
                numChaptersString = "-";
            } else {
                numChaptersString = String.format(Locale.US, "%d", numChapters);
            }
            holder.numChaptersView.setText(numChaptersString);
        }

        holder.ageRatingView.setText(userListElement.getAgeRating());
    }

    @Override
    public int getItemCount() {
        return userListElementList.size();
    }

    static class UserListViewHolder extends RecyclerView.ViewHolder {
        final ImageView animeImageView;
        final TextView animeNameView;
        final TextView animeSynonyms1View;
        final TextView animeSynonyms2View;
        final TextView animeSynonyms3View;
        final TextView animeEnTitleView;
        final TextView animeJpTitleView;
        final TextView animeMediaTypeView;
        final TextView animeAiringStatusView;
        final TextView animeSeasonView;
        final TextView synLabelView;
        final TextView enLabelView;
        final TextView jpLabelView;
        final TextView scoreView;
        final TextView numWatchedEpisodesOrReadVolumesView;
        final TextView numEpisodesOrVolumesView;
        final TextView numReadChaptersView;
        final TextView numChaptersView;
        final LinearLayout chapterCountsLinearLayout;
        final Space spaceBetweenVolAndChapIndicators;
        final TextView ageRatingView;

        UserListViewHolder(LinearLayout v) {
            super(v);
            animeImageView = v.findViewById(R.id.animeImage);
            animeNameView = v.findViewById(R.id.animeName);
            animeSynonyms1View = v.findViewById(R.id.animeSynonyms1);
            animeSynonyms2View = v.findViewById(R.id.animeSynonyms2);
            animeSynonyms3View = v.findViewById(R.id.animeSynonyms3);
            animeEnTitleView = v.findViewById(R.id.animeEnTitle);
            animeJpTitleView = v.findViewById(R.id.animeJpTitle);
            animeMediaTypeView = v.findViewById(R.id.mediaType);
            animeAiringStatusView = v.findViewById(R.id.airingStatus);
            animeSeasonView = v.findViewById(R.id.season);

            synLabelView = v.findViewById(R.id.synLabel);
            enLabelView = v.findViewById(R.id.enLabel);
            jpLabelView = v.findViewById(R.id.jpLabel);

            scoreView = v.findViewById(R.id.score);

            numWatchedEpisodesOrReadVolumesView = v.findViewById(R.id.numWatchedEpisodesOrReadVolumes);
            numEpisodesOrVolumesView = v.findViewById(R.id.numEpisodesOrVolumes);

            numReadChaptersView = v.findViewById(R.id.numReadChapters);
            numChaptersView = v.findViewById(R.id.numChapters);
            chapterCountsLinearLayout = v.findViewById(R.id.chapterCountsLinearLayout);
            spaceBetweenVolAndChapIndicators = v.findViewById(R.id.spaceBetweenVolAndChapIndicators);
            ageRatingView = v.findViewById(R.id.ageRating);
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewRef;
        private final int tag;

        DownloadImageTask(ImageView imageView, int tag) {
            this.imageViewRef = new WeakReference<>(imageView);
            this.tag = tag;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                bmp = BitmapFactory.decodeStream(in);
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            ImageView imageView = imageViewRef.get();

            if (imageView == null) { return; }

            if ((int)imageView.getTag() == tag) {
                imageView.setImageBitmap(result);
            }
        }
    }

}
