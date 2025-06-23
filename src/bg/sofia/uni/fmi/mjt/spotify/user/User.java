package bg.sofia.uni.fmi.mjt.spotify.user;

import java.util.Objects;

public record User(String email, String password) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
