package com.wesleykerr.steam.domain.game;

import java.util.Calendar;

public class Game {

    private long   appid;
    private String title;
    private String appType;
    
    private int    owned;
    private int    notPlayed;
    
    private double totalPlaytime;
    private double totalQ25;
    private double totalQ75;
    private double totalMedian;
    
    private double recentPlaytime;
    private double recentQ25;
    private double recentQ75;
    private double recentMedian;
    
    private String metacritic;
    private int    giantbombId;

    private String steamURL;
    private String steamImgURL;
    
    private Calendar updatedDateTime;
    private Calendar lastChecked;
    
    private String recomms;

    public Game() { 
        
    }
    
    /**
     * @return the appid
     */
    public long getAppid() {
        return appid;
    }

    /**
     * @param appid the appid to set
     */
    public void setAppid(long appid) {
        this.appid = appid;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the appType
     */
    public String getAppType() {
        return appType;
    }

    /**
     * @param appType the appType to set
     */
    public void setAppType(String appType) {
        this.appType = appType;
    }

    /**
     * @return the owned
     */
    public int getOwned() {
        return owned;
    }

    /**
     * @param owned the owned to set
     */
    public void setOwned(int owned) {
        this.owned = owned;
    }

    /**
     * @return the notPlayed
     */
    public int getNotPlayed() {
        return notPlayed;
    }

    /**
     * @param notPlayed the notPlayed to set
     */
    public void setNotPlayed(int notPlayed) {
        this.notPlayed = notPlayed;
    }

    /**
     * @return the totalPlaytime
     */
    public double getTotalPlaytime() {
        return totalPlaytime;
    }

    /**
     * @param totalPlaytime the totalPlaytime to set
     */
    public void setTotalPlaytime(double totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }

    /**
     * @return the totalQ25
     */
    public double getTotalQ25() {
        return totalQ25;
    }

    /**
     * @param totalQ25 the totalQ25 to set
     */
    public void setTotalQ25(double totalQ25) {
        this.totalQ25 = totalQ25;
    }

    /**
     * @return the totalQ75
     */
    public double getTotalQ75() {
        return totalQ75;
    }

    /**
     * @param totalQ75 the totalQ75 to set
     */
    public void setTotalQ75(double totalQ75) {
        this.totalQ75 = totalQ75;
    }

    /**
     * @return the totalMedian
     */
    public double getTotalMedian() {
        return totalMedian;
    }

    /**
     * @param totalMedian the totalMedian to set
     */
    public void setTotalMedian(double totalMedian) {
        this.totalMedian = totalMedian;
    }

    /**
     * @return the recentPlaytime
     */
    public double getRecentPlaytime() {
        return recentPlaytime;
    }

    /**
     * @param recentPlaytime the recentPlaytime to set
     */
    public void setRecentPlaytime(double recentPlaytime) {
        this.recentPlaytime = recentPlaytime;
    }

    /**
     * @return the recentQ25
     */
    public double getRecentQ25() {
        return recentQ25;
    }

    /**
     * @param recentQ25 the recentQ25 to set
     */
    public void setRecentQ25(double recentQ25) {
        this.recentQ25 = recentQ25;
    }

    /**
     * @return the recentQ75
     */
    public double getRecentQ75() {
        return recentQ75;
    }

    /**
     * @param recentQ75 the recentQ75 to set
     */
    public void setRecentQ75(double recentQ75) {
        this.recentQ75 = recentQ75;
    }

    /**
     * @return the recentMedian
     */
    public double getRecentMedian() {
        return recentMedian;
    }

    /**
     * @param recentMedian the recentMedian to set
     */
    public void setRecentMedian(double recentMedian) {
        this.recentMedian = recentMedian;
    }

    /**
     * @return the metacritic
     */
    public String getMetacritic() {
        return metacritic;
    }

    /**
     * @param metacritic the metacritic to set
     */
    public void setMetacritic(String metacritic) {
        this.metacritic = metacritic;
    }

    /**
     * @return the giantbombId
     */
    public int getGiantbombId() {
        return giantbombId;
    }

    /**
     * @param giantbombId the giantbombId to set
     */
    public void setGiantbombId(int giantbombId) {
        this.giantbombId = giantbombId;
    }

    /**
     * @return the steamURL
     */
    public String getSteamURL() {
        return steamURL;
    }

    /**
     * @param steamURL the steamURL to set
     */
    public void setSteamURL(String steamURL) {
        this.steamURL = steamURL;
    }

    /**
     * @return the steamImgURL
     */
    public String getSteamImgURL() {
        return steamImgURL;
    }

    /**
     * @param steamImgURL the steamImgURL to set
     */
    public void setSteamImgURL(String steamImgURL) {
        this.steamImgURL = steamImgURL;
    }

    /**
     * @return the updatedDateTime
     */
    public Calendar getUpdatedDateTime() {
        return updatedDateTime;
    }

    /**
     * @param updatedDateTime the updatedDateTime to set
     */
    public void setUpdatedDateTime(Calendar updatedDateTime) {
        this.updatedDateTime = updatedDateTime;
    }

    /**
     * @return the lastChecked
     */
    public Calendar getLastChecked() {
        return lastChecked;
    }

    /**
     * @param lastChecked the lastChecked to set
     */
    public void setLastChecked(Calendar lastChecked) {
        this.lastChecked = lastChecked;
    }

    /**
     * @return the recomms
     */
    public String getRecomms() {
        return recomms;
    }

    /**
     * @param recomms the recomms to set
     */
    public void setRecomms(String recomms) {
        this.recomms = recomms;
    }
}
