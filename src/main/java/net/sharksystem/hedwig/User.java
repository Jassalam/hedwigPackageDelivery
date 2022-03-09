package net.sharksystem.hedwig;

import java.util.Objects;

public class User {
    String peerId;
    String longitunde;
    String latitude;

    public User(String peerId, String longitunde, String latitude) {
        this.peerId = peerId;
        this.longitunde = longitunde;
        this.latitude = latitude;
    }

    public User() {
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getLongitunde() {
        return longitunde;
    }

    public void setLongitunde(String longitunde) {
        this.longitunde = longitunde;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return peerId.equals(user.peerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerId);
    }
}
