package bank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootVersion;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Vad maskinen faktiskt kör på — till uppstartsskärmens rader.
 *
 * <p><b>Varför läsas ur anslutningen och inte skrivas som text:</b> appen kör H2 lokalt och
 * PostgreSQL i drift (se {@code application-prod.properties}). En hårdkodad rad "PostgreSQL
 * online" hade alltså varit fel i halva livet, och — värre — den hade fortsatt stå kvar som
 * sanning den dagen databasen byts. Namnet och versionen kommer från {@link DatabaseMetaData},
 * alltså från den anslutning raden påstår något om.
 *
 * <p>Värdena hämtas EN gång och sparas: metadata kostar en tur till databasen, och menyn ska
 * inte betala för en rad som aldrig ändrar sig under en körning.
 *
 * <p>Fel svaldes med flit. Ett register som inte svarar ska ge en meny utan siffror, inte en
 * startsida som kraschar — och splashen visar raden utan text i stället för att hitta på.
 *
 * @author Robert Andersson Kopler
 */
@Component
public class Systeminfo {

    private static final Logger log = LoggerFactory.getLogger(Systeminfo.class);

    private final DataSource dataSource;
    private volatile String databas;
    private volatile String jdbc = "";
    private volatile boolean last;

    public Systeminfo(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** T.ex. "PostgreSQL 16.4" eller "H2 2.3.232" — tom sträng när anslutningen inte svarar. */
    public String databas() {
        if (!last) {
            synchronized (this) {
                if (!last) {
                    databas = lasDatabas();
                    last = true;
                }
            }
        }
        return databas;
    }

    /** Kort commit-hash Render byggde ("9a9e5d0") — tom lokalt, där Render inte sätter variabeln. */
    public String deployCommit() {
        String c = System.getenv("RENDER_GIT_COMMIT");
        return c == null || c.isBlank() ? "" : c.substring(0, Math.min(7, c.length()));
    }

    /** Grenen Render autodeployar från ("master") — tom lokalt. */
    public String deployBranch() {
        String b = System.getenv("RENDER_GIT_BRANCH");
        return b == null ? "" : b;
    }

    /** T.ex. "JDBC 42.7.7" — drivrutinen appen pratar med databasen genom. Tom när okänd. */
    public String jdbc() {
        databas();
        return jdbc;
    }

    private String lasDatabas() {
        try (Connection c = dataSource.getConnection()) {
            DatabaseMetaData md = c.getMetaData();
            // Drivrutinen läses i samma tur: "PostgreSQL JDBC Driver 42.7.7" -> "JDBC 42.7.7".
            String dv = md.getDriverVersion();
            jdbc = dv == null || dv.isBlank() ? "" : "JDBC " + dv.trim().split("\\s+")[0];
            String namn = md.getDatabaseProductName();
            String ver = md.getDatabaseProductVersion();
            if (namn == null || namn.isBlank()) return "";
            // Versionssträngen kan vara en hel mening ("15.4 (Debian 15.4-1.pgdg120+1)").
            // Första ordet räcker på en rad som ska läsas på en halv sekund.
            if (ver != null && !ver.isBlank()) namn += " " + ver.trim().split("\\s+")[0];
            return namn;
        } catch (Exception e) {
            log.warn("Kunde inte läsa databasens namn till uppstartsskärmen: {}", e.getMessage());
            return "";
        }
    }

    /** T.ex. "3.5.16". */
    public String springBoot() {
        String v = SpringBootVersion.getVersion();
        return v == null ? "" : v;
    }

    /** T.ex. "25". */
    public String java() {
        String v = System.getProperty("java.version");
        return v == null ? "" : v;
    }
}
