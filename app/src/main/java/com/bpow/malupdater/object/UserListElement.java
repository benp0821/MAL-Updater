package com.bpow.malupdater.object;

import com.bpow.malupdater.activity.UserListActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserListElement {
    public static class NameID{
        private final int id;
        private final String name;

        NameID(int id, String name){
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class AltNames{
        private final ArrayList<String> synonyms;
        private final String enTitle;
        private final String jpTitle;

        public AltNames(ArrayList<String> synonyms, String enTitle, String jpTitle){
            this.synonyms = synonyms;
            this.enTitle = enTitle;
            this.jpTitle = jpTitle;
        }

        public ArrayList<String> getSynonyms() {
            return synonyms;
        }

        public String getEnTitle() {
            return enTitle;
        }

        public String getJpTitle() {
            return jpTitle;
        }
    }

    public static class Picture{
        private final String mediumPicture;
        private final String largePicture;

        Picture(String mediumPicture, String largePicture){
            this.mediumPicture = mediumPicture;
            this.largePicture = largePicture;
        }

        public String getMediumPicture() {
            return mediumPicture;
        }

        public String getLargePicture() {
            return largePicture;
        }
    }

    public static class Season{
        private final String year;
        private final String season;

        public Season(String year, String season){
            this.year = year;
            this.season = season;
        }

        public String getYear() {
            return year;
        }

        public String getSeason() {
            return season;
        }
    }

    public static class UserSpecific{
        private final int score;
        private final int numWatchedEpisodesOrReadVolumes;
        private final int numReadChapters;

        public UserSpecific(int score, int numWatchedEpisodesOrReadVolumes){
            this.score = score;
            this.numWatchedEpisodesOrReadVolumes = numWatchedEpisodesOrReadVolumes;
            this.numReadChapters = -1;
        }

        public UserSpecific(int score, int numWatchedEpisodesOrReadVolumes, int numReadChapters){
            this.score = score;
            this.numWatchedEpisodesOrReadVolumes = numWatchedEpisodesOrReadVolumes;
            this.numReadChapters = numReadChapters;
        }

        public int getScore(){
            return score;
        }

        public int getNumWatchedEpisodesOrReadVolumes(){
            return numWatchedEpisodesOrReadVolumes;
        }

        public int getNumReadChapters(){
            return numReadChapters;
        }
    }

    private final UserListActivity.ListType listType;

    private final NameID nameID;
    private final AltNames altNames;
    private final Picture picture;
    private NameID[] genres;
    private final String mediaType;
    private final String airingStatus;
    private final int numEpisodesOrVolumes;
    private final int numChapters;
    private final Season season;
    private String source;
    private int avgEpisodeLength;
    private final String ageRating;
    private final UserSpecific userListDetails;

    public UserListElement(JSONObject json, UserListActivity.ListType listType){
        this.listType = listType;

        int idAttr = 0;
        String titleAttr = "";
        String altTitleEnAttr = "";
        String altTitleJpAttr = "";
        ArrayList<String> synonymsAttr = new ArrayList<>();
        String mediumPicAttr = "";
        String largePicAttr = "";
        String mediaTypeAttr = "";
        String airingStatusAttr = "";
        int numEpisodesOrVolumesAttr = 0;
        int numChaptersAttr = 0;
        String seasonAttr = "";
        String seasonYearAttr = "";
        String ageRatingAttr = "";
        int myListScoreAttr = 0;
        int myListEpisodeWatchCntOrVolReadAttr = 0;
        int myListChaptersReadAttr = 0;

        try {
            JSONObject node = json.getJSONObject("node");

            idAttr = node.getInt("id");
            titleAttr = node.getString("title");
            JSONObject altTitlesAttr = node.getJSONObject("alternative_titles");
            altTitleEnAttr = altTitlesAttr.getString("en");
            altTitleJpAttr = altTitlesAttr.getString("ja");
            JSONArray synonymsJsonArrAttr = altTitlesAttr.getJSONArray("synonyms");
            for (int i = 0; i < synonymsJsonArrAttr.length(); i++){
                synonymsAttr.add(synonymsJsonArrAttr.getString(i));
            }
            JSONObject mainPicAttr = node.getJSONObject("main_picture");
            mediumPicAttr = mainPicAttr.getString("medium");
            largePicAttr = mainPicAttr.getString("large");
            mediaTypeAttr = node.getString("media_type");
            airingStatusAttr = node.getString("status");
            if (listType == UserListActivity.ListType.ANIME) {
                numEpisodesOrVolumesAttr = node.getInt("num_episodes");
                numChaptersAttr = -1;
            }else if (listType == UserListActivity.ListType.MANGA){
                numEpisodesOrVolumesAttr = node.getInt("num_volumes");
                numChaptersAttr = node.getInt("num_chapters");
            }

            JSONObject startSeasonAttr;
            seasonAttr = "";
            seasonYearAttr = "";
            if (node.has("start_season")) {
                startSeasonAttr = node.getJSONObject("start_season");
                if (startSeasonAttr.has("season")) {
                    seasonAttr = startSeasonAttr.getString("season");
                }
                if (startSeasonAttr.has("year")) {
                    seasonYearAttr = startSeasonAttr.getString("year");
                }
            }

            if (listType == UserListActivity.ListType.ANIME) {
                ageRatingAttr = node.getString("rating");
            }else{
                ageRatingAttr = "";
            }

            JSONObject myListStatusAttr = node.getJSONObject("my_list_status");
            myListScoreAttr = myListStatusAttr.getInt("score");
            if (listType == UserListActivity.ListType.ANIME) {
                myListEpisodeWatchCntOrVolReadAttr = myListStatusAttr.getInt("num_episodes_watched");
            }else if (listType == UserListActivity.ListType.MANGA){
                myListEpisodeWatchCntOrVolReadAttr = myListStatusAttr.getInt("num_volumes_read");
                myListChaptersReadAttr = myListStatusAttr.getInt("num_chapters_read");
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

        //nameID
        nameID = new NameID(idAttr, titleAttr);

        //altNames
        altNames = new AltNames(synonymsAttr, altTitleEnAttr, altTitleJpAttr);

        //picture
        picture = new Picture(mediumPicAttr, largePicAttr);

        //mediaType
        switch (mediaTypeAttr){
            case "tv":
                mediaType = "TV";
                break;
            case "ova":
                mediaType = "OVA";
                break;
            case "movie":
                mediaType = "Movie";
                break;
            case "special":
                mediaType = "Spec";
                break;
            case "ona":
                mediaType = "ONA";
                break;
            case "music":
                mediaType = "Music";
                break;
            case "unknown":
            default:
                mediaType = "-";
                break;
        }

        //airingStatus
        switch (airingStatusAttr){
            case "finished_airing":
                airingStatus = "Finished";
                break;
            case "currently_airing":
                airingStatus = "Airing";
                break;
            case "not_yet_aired":
            default:
                airingStatus = "Not Aired";
                break;
        }

        numEpisodesOrVolumes = numEpisodesOrVolumesAttr;
        numChapters = numChaptersAttr;

        //season
        String seasonsVal;
        switch (seasonAttr){
            case "winter":
                seasonsVal = "Winter";
                break;
            case "spring":
                seasonsVal = "Spring";
                break;
            case "summer":
                seasonsVal = "Summer";
                break;
            case "fall":
                seasonsVal = "Fall";
                break;
            default:
                seasonsVal = "";
                break;
        }
        season = new Season(seasonYearAttr, seasonsVal);

        switch (ageRatingAttr){
            case "g":
                ageRating = "G - All Ages";
                break;
            case "pg":
                ageRating = "PG - Children";
                break;
            case "pg_13":
                ageRating = "PG 13 - Teens";
                break;
            case "r":
                ageRating = "R 17+";
                break;
            case "r+":
                ageRating = "R+";
                break;
            case "rx":
                ageRating = "Rx - Hentai";
                break;
            default:
                ageRating = "";
                break;
        }

        //userListDetails
        if (listType == UserListActivity.ListType.ANIME) {
            userListDetails = new UserSpecific(myListScoreAttr, myListEpisodeWatchCntOrVolReadAttr);
        }else{
            userListDetails = new UserSpecific(myListScoreAttr, myListEpisodeWatchCntOrVolReadAttr, myListChaptersReadAttr);
        }
    }

    public UserListActivity.ListType getListType() {
        return listType;
    }

    public NameID getNameID() {
        return nameID;
    }

    public AltNames getAltNames() {
        return altNames;
    }

    public Picture getPicture() {
        return picture;
    }

    public NameID[] getGenres() {
        return genres;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getAiringStatus() {
        return airingStatus;
    }

    public int getNumEpisodesOrVolumes() {
        return numEpisodesOrVolumes;
    }

    public int getNumChapters() {
        return numChapters;
    }

    public Season getSeason() {
        return season;
    }

    public String getSource() {
        return source;
    }

    public int getAvgEpisodeLength() {
        return avgEpisodeLength;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public UserSpecific getUserListDetails() {
        return userListDetails;
    }

}
