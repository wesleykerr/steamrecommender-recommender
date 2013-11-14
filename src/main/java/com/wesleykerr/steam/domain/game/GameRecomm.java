package com.wesleykerr.steam.domain.game;

import com.google.common.collect.Ordering;

public class GameRecomm {

    private Long appid;
    private String title;
    private String imgUrl;
    
    private Double score;

    public GameRecomm() { 
        
    }
    
    /**
     * @return the appid
     */
    public Long getAppid() {
        return appid;
    }

    /**
     * @param appid the appid to set
     */
    public void setAppid(Long appid) {
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
     * @return the imgUrl
     */
    public String getImgUrl() {
        return imgUrl;
    }

    /**
     * @param imgUrl the imgUrl to set
     */
    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    /**
     * @return the score
     */
    public Double getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(Double score) {
        this.score = score;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{appid=%d score=%1.4f}", appid, score);
    }

    /**
     * Create a barebones GameRecomm.
     * @param appId
     * @param score
     * @return
     */
    public static GameRecomm create(long appId, double score) { 
        GameRecomm recomm = new GameRecomm();
        recomm.appid = appId;
        recomm.score = score;
        return recomm;
    }
    
    public static final Ordering<GameRecomm> LARGEST_TO_SMALLEST = 
            new Ordering<GameRecomm>() {
                @Override
                public int compare(GameRecomm o1, GameRecomm o2) {
                    int compValue = Double.compare(o1.score, o2.score);
                    if (compValue == 0) { 
                        return Long.compare(o1.appid, o2.appid);
                    }
                    return -compValue;
                } 
                
            };
}
