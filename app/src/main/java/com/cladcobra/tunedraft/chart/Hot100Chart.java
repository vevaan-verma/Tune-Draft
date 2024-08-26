package com.cladcobra.tunedraft.chart;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Hot100Chart {

    private String date;
    private ArrayList<Hot100ChartElement> data;

    // getters and setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public ArrayList<Hot100ChartElement> getData() {
        return data;
    }

    public void setData(ArrayList<Hot100ChartElement> data) {
        this.data = data;
    }

    public static class Hot100ChartElement {

        @SerializedName("song")
        private String name;

        @SerializedName("artist")
        private String artist;

        @SerializedName("this_week")
        private int thisWeekRank;

        @SerializedName("last_week")
        private Integer lastWeekRank;

        @SerializedName("peak_position")
        private int peakPosition;

        @SerializedName("weeks_on_chart")
        private int weeksOnChart;

        // getters and setters
        public String getName() {
            return name;
        }

        public void setName(String tuneName) {
            this.name = tuneName;
        }

        public String getRawArtist() {
            return artist;
        }

        public String getArtist() {
            return artist.replace("Featuring", "ft.");
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public int getThisWeekRank() {
            return thisWeekRank;
        }

        public void setThisWeekRank(int thisWeekRank) {
            this.thisWeekRank = thisWeekRank;
        }

        public Integer getLastWeekRank() {
            return lastWeekRank;
        }

        public void setLastWeekRank(Integer lastWeekRank) {
            this.lastWeekRank = lastWeekRank;
        }

        public int getPeakPosition() {
            return peakPosition;
        }

        public void setPeakPosition(int peakPosition) {
            this.peakPosition = peakPosition;
        }

        public int getWeeksOnChart() {
            return weeksOnChart;
        }

        public void setWeeksOnChart(int weeksOnChart) {
            this.weeksOnChart = weeksOnChart;
        }

    }
}