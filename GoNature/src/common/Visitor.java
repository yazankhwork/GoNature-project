package common;

import java.io.Serializable;

/**
 * This class represents a Visitor in the GoNature system.
 * It holds the visitor's identification, contact info, and role.
 */
public class Visitor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String visitorId;
    private String email;
    private boolean isGuide;

    public Visitor(String visitorId, String email, boolean isGuide) {
        this.visitorId = visitorId;
        this.email = email;
        this.isGuide = isGuide;
    }

    // Getters and Setters
    public String getVisitorId() { return visitorId; }
    public void setVisitorId(String visitorId) { this.visitorId = visitorId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isGuide() { return isGuide; }
    public void setGuide(boolean guide) { isGuide = guide; }
}