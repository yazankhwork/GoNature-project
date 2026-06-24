package common;

import java.io.Serializable;

/**
 * Represents a visitor in the GoNature system.
 * <p>
 * A visitor can be either a regular visitor or an organized group guide. The
 * class stores identification information, contact information, and role
 * details.
 * </p>
 *
 * This class implements {@link Serializable} so visitor objects can be
 * transferred between the client and server.
 *
 * @author Group 4
 * @version 1.0
 */
public class Visitor implements Serializable {

	/**
	 * Serialization version identifier.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Unique identifier of the visitor.
	 */
	private String visitorId;

	/**
	 * Email address of the visitor.
	 */
	private String email;

	/**
	 * Indicates whether the visitor is registered as a guide.
	 */
	private boolean isGuide;

	/**
	 * Creates a new visitor.
	 *
	 * @param visitorId unique visitor identifier
	 * @param email     visitor email address
	 * @param isGuide   true if the visitor is a guide, otherwise false
	 */
	public Visitor(String visitorId, String email, boolean isGuide) {
		this.visitorId = visitorId;
		this.email = email;
		this.isGuide = isGuide;
	}

	/**
	 * Returns the visitor ID.
	 *
	 * @return visitor ID
	 */
	public String getVisitorId() {
		return visitorId;
	}

	/**
	 * Sets the visitor ID.
	 *
	 * @param visitorId visitor ID
	 */
	public void setVisitorId(String visitorId) {
		this.visitorId = visitorId;
	}

	/**
	 * Returns the visitor email address.
	 *
	 * @return visitor email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the visitor email address.
	 *
	 * @param email visitor email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Returns whether the visitor is a guide.
	 *
	 * @return true if the visitor is a guide, otherwise false
	 */
	public boolean isGuide() {
		return isGuide;
	}

	/**
	 * Sets the guide status of the visitor.
	 *
	 * @param guide true if the visitor is a guide, otherwise false
	 */
	public void setGuide(boolean guide) {
		isGuide = guide;
	}
}