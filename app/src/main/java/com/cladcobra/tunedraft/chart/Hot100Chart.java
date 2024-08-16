package com.cladcobra.tunedraft.chart;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Hot100Chart {

    private String date;
    private ArrayList<Hot100ChartData> data;

    // getters and setters
    public String getDate() {

        return date;

    }

    public void setDate(String date) {

        this.date = date;

    }

    public ArrayList<Hot100ChartData> getData() {

        return data;

    }

    public void setData(ArrayList<Hot100ChartData> data) {

        this.data = data;

    }

    public static class Hot100ChartData {

        private String song; // must be called "song" to match JSON key
        private String artist;

        @SerializedName("this_week")
        private int thisWeek;

        @SerializedName("last_week")
        private Integer lastWeek;

        @SerializedName("peak_position")
        private int peakPosition;

        @SerializedName("weeks_on_chart")
        private int weeksOnChart;

        // getters and setters
        public String getSongName() {

            return song;

        }

        public void setSongName(String songName) {

            this.song = songName;

        }

        public String getArtist() {

            return artist;

        }

        public void setArtist(String artist) {

            this.artist = artist;

        }

        public int getThisWeek() {

            return thisWeek;

        }

        public void setThisWeek(int thisWeek) {

            this.thisWeek = thisWeek;

        }

        public Integer getLastWeek() {

            return lastWeek;

        }

        public void setLastWeek(Integer lastWeek) {

            this.lastWeek = lastWeek;

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