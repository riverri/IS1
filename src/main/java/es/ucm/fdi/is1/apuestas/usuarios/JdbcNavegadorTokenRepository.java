package es.ucm.fdi.is1.apuestas.usuarios;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** JDBC-backed persistent login storage with per-browser listing and revocation. */
@Repository
public class JdbcNavegadorTokenRepository implements PersistentTokenRepository {

    private static final String INSERT_TOKEN = """
            insert into persistent_logins (username, series, token, last_used, created_at, user_agent)
            values (?, ?, ?, ?, ?, ?)
            """;
    private static final String TOKEN_BY_SERIES = """
            select username, series, token, last_used from persistent_logins where series = ?
            """;

    private final JdbcTemplate jdbc;

    public JdbcNavegadorTokenRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        Timestamp created = new Timestamp(token.getDate().getTime());
        jdbc.update(INSERT_TOKEN, token.getUsername(), token.getSeries(), token.getTokenValue(), created, created,
                userAgent());
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        jdbc.update("update persistent_logins set token = ?, last_used = ? where series = ?",
                tokenValue, new Timestamp(lastUsed.getTime()), series);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
        List<PersistentRememberMeToken> tokens = jdbc.query(TOKEN_BY_SERIES,
                (result, rowNumber) -> token(result), series);
        return tokens.isEmpty() ? null : tokens.getFirst();
    }

    @Override
    public void removeUserTokens(String username) {
        jdbc.update("delete from persistent_logins where username = ?", username);
    }

    public List<NavegadorRecordado> findByUsername(String username) {
        return jdbc.query("""
                        select series, user_agent, created_at, last_used
                        from persistent_logins where username = ? order by last_used desc, series
                        """,
                (result, rowNumber) -> new NavegadorRecordado(result.getString("series"),
                        result.getString("user_agent"), localDateTime(result, "created_at"),
                        localDateTime(result, "last_used")), username);
    }

    public boolean removeToken(String username, String series) {
        return jdbc.update("delete from persistent_logins where username = ? and series = ?", username, series) > 0;
    }

    public void removeTokenForSeries(String series) {
        jdbc.update("delete from persistent_logins where series = ?", series);
    }

    private static PersistentRememberMeToken token(ResultSet result) throws SQLException {
        return new PersistentRememberMeToken(result.getString("username"), result.getString("series"),
                result.getString("token"), result.getTimestamp("last_used"));
    }

    private static java.time.LocalDateTime localDateTime(ResultSet result, String column) throws SQLException {
        return result.getTimestamp(column).toLocalDateTime();
    }

    private static String userAgent() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String userAgent = attributes.getRequest().getHeader("User-Agent");
            if (userAgent != null && !userAgent.isBlank()) {
                return userAgent.substring(0, Math.min(userAgent.length(), 255));
            }
        }
        return "Unknown browser";
    }
}
