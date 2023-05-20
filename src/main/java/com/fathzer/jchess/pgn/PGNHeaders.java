package com.fathzer.jchess.pgn;

import java.time.LocalDate;

public class PGNHeaders {
	private String event = "?";
	private String site = "?";
	private LocalDate date = LocalDate.now();
	private Long round = null;
	private String whiteName = "?";
	private String blackName = "?";
	private String variant = null;
	
    private PGNHeaders() {
        // Private constructor to enforce the use of the builder
    }
    
    public String getEvent() {
		return event;
	}

	public String getSite() {
		return site;
	}

	public LocalDate getDate() {
		return date;
	}

	public Long getRound() {
		return round;
	}

	public String getWhiteName() {
		return whiteName;
	}

	public String getBlackName() {
		return blackName;
	}


	public String getVariant() {
		return variant;
	}


	public static class Builder {
        private PGNHeaders headers;
        
        public Builder() {
            headers = new PGNHeaders();
        }
        
        public Builder setEvent(String event) {
            headers.event = event;
            return this;
        }
        
        public Builder setSite(String site) {
            headers.site = site;
            return this;
        }
        
        public Builder setDate(LocalDate date) {
            headers.date = date;
            return this;
        }
        
        public Builder setRound(Long round) {
            headers.round = round;
            return this;
        }
        
        public Builder setWhiteName(String whiteName) {
            headers.whiteName = whiteName;
            return this;
        }
        
        public Builder setBlackName(String blackName) {
            headers.blackName = blackName;
            return this;
        }
        
        public Builder setVariant(String variant) {
            headers.variant = variant;
            return this;
        }
            
        public PGNHeaders build() {
            return headers;
        }
    }
 
}
