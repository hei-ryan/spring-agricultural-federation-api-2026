package edu.hei.school.agricultural.service;

import edu.hei.school.agricultural.api.ApiClient;
import edu.hei.school.agricultural.api.model.CollectivityLocalStatistics;
import edu.hei.school.agricultural.api.model.CollectivityOverallStatistics;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class OverallStatisticIT {

    final ApiClient apiClient = new ApiClient();

    // =========================================================
    // GET /collectivities/{id}/statistics — assiduityPercentage
    // =========================================================

    @Test
    void col3_assiduity_per_member_march_only() {
        // act-6 AG3 (toutes occupations), 1 seule occurrence : 06/03/2026 (Tableau 29)
        //   C3-M1..M6 → Présent  : 1/1 = 100 %
        //   C3-M7, C3-M8 → Absent : 0/1 = 0 %
        var from = LocalDate.of(2026, 3, 1);
        var to = LocalDate.of(2026, 3, 31);

        var stats = apiClient.getCollectivityStatistics("col-3", from, to);

        assertNotNull(stats);
        log.info("col-3 local statistics (assiduité) 03/2026 : " + stats);

        assertAssiduity(stats, "C3-M1", 100.0);
        assertAssiduity(stats, "C3-M2", 100.0);
        assertAssiduity(stats, "C3-M3", 100.0);
        assertAssiduity(stats, "C3-M4", 100.0);
        assertAssiduity(stats, "C3-M5", 100.0);
        assertAssiduity(stats, "C3-M6", 100.0);
        assertAssiduity(stats, "C3-M7", 0.0);
        assertAssiduity(stats, "C3-M8", 0.0);
    }

    @Test
    void col1_assiduity_per_member_march_to_april() {
        // act-1 AG1 (toutes occupations), 2 occurrences :
        //   Tableau 24 — 07/03/2026 : M1-M6 Présent, M7-M8 Absent
        //   Tableau 25 — 04/04/2026 : M1/M2/M5-M8 Présent, M3/M4 Absent
        //
        // M1, M2, M5, M6 : présents les 2 fois → 2/2 = 100 %
        // M3, M4         : 1 présence + 1 absence → 1/2 = 50 %
        // M7, M8         : 1 présence + 1 absence → 1/2 = 50 %
        var from = LocalDate.of(2026, 3, 1);
        var to = LocalDate.of(2026, 4, 30);

        var stats = apiClient.getCollectivityStatistics("col-1", from, to);

        assertNotNull(stats);
        log.info("col-1 local statistics (assiduité) 03-04/2026 : " + stats);

        assertAssiduity(stats, "C1-M1", 100.0);
        assertAssiduity(stats, "C1-M2", 100.0);
        assertAssiduity(stats, "C1-M3", 50.0);
        assertAssiduity(stats, "C1-M4", 50.0);
        assertAssiduity(stats, "C1-M5", 100.0);
        assertAssiduity(stats, "C1-M6", 100.0);
        assertAssiduity(stats, "C1-M7", 50.0);
        assertAssiduity(stats, "C1-M8", 50.0);
    }

    @Test
    void col3_assiduity_per_member_march_to_april() {
        // act-6 AG3 (toutes occupations), 2 occurrences :
        //   Tableau 29 — 06/03/2026 : M1-M6 Présent, M7-M8 Absent
        //   Tableau 30 — 03/04/2026 : M1/M2/M5/M6/M8 Présent, M3/M4/M7 Absent
        //                              (C1-M1 présent en externe, non compté)
        //
        // M1, M2, M5, M6 : 2/2 = 100 %
        // M3, M4, M8     : 1/2 = 50 %  (M8 absent mars, présent avril)
        // M7             : 0/2 = 0 %   (absent les deux fois)
        var from = LocalDate.of(2026, 3, 1);
        var to = LocalDate.of(2026, 4, 30);

        var stats = apiClient.getCollectivityStatistics("col-3", from, to);

        assertNotNull(stats);
        log.info("col-3 local statistics (assiduité) 03-04/2026 : " + stats);

        assertAssiduity(stats, "C3-M1", 100.0);
        assertAssiduity(stats, "C3-M2", 100.0);
        assertAssiduity(stats, "C3-M3", 50.0);
        assertAssiduity(stats, "C3-M4", 50.0);
        assertAssiduity(stats, "C3-M5", 100.0);
        assertAssiduity(stats, "C3-M6", 100.0);
        assertAssiduity(stats, "C3-M7", 0.0);
        assertAssiduity(stats, "C3-M8", 50.0);
    }

    // =========================================================
    // GET /collectivities/statistics — overallMemberAssiduityPercentage
    // =========================================================

    @Test
    void col3_overall_assiduity_march_only() {
        // act-6 mars (Tableau 29) : 6 présents / 8 membres requis = 75 %
        var from = LocalDate.of(2026, 3, 1);
        var to = LocalDate.of(2026, 3, 31);

        var overall = apiClient.getCollectivitiesOverallStatistics(from, to);
        assertNotNull(overall);

        var col3Stats = findCollectivityStats(overall, "col-3");
        assertNotNull(col3Stats, "Statistiques globales de col-3 introuvables");
        assertNotNull(col3Stats.overallMemberAssiduityPercentage);
        log.info("col-3 assiduité globale 03/2026 : " + col3Stats.overallMemberAssiduityPercentage);

        assertEquals(75.0, col3Stats.overallMemberAssiduityPercentage, 0.01,
                "col-3 mars : 6 présences / 8 membres requis = 75 %");
    }

    @Test
    void col2_overall_assiduity_march_only() {
        // act-3 AG2 mars (Tableau 26, 08/03/2026) :
        //   M1/M2/M5-M8 Présent, M3/M4 Absent → 6/8 = 75 %
        var from = LocalDate.of(2026, 3, 1);
        var to = LocalDate.of(2026, 3, 31);

        var overall = apiClient.getCollectivitiesOverallStatistics(from, to);
        assertNotNull(overall);

        var col2Stats = findCollectivityStats(overall, "col-2");
        assertNotNull(col2Stats, "Statistiques globales de col-2 introuvables");
        assertNotNull(col2Stats.overallMemberAssiduityPercentage);
        log.info("col-2 assiduité globale 03/2026 : " + col2Stats.overallMemberAssiduityPercentage);

        assertEquals(75.0, col2Stats.overallMemberAssiduityPercentage, 0.01,
                "col-2 mars : 6 présences / 8 membres requis = 75 %");
    }

    @Test
    void col1_overall_assiduity_march_to_april() {
        // act-1 sur 2 occurrences (Tableaux 24 + 25) :
        //   Mars  (07/03) : 6 présents / 8
        //   Avril (04/04) : 6 présents / 8
        //   Total : 12 présences / 16 occurrences (8 membres × 2) = 75 %
        var from = LocalDate.of(2026, 3, 1);
        var to = LocalDate.of(2026, 4, 30);

        var overall = apiClient.getCollectivitiesOverallStatistics(from, to);
        assertNotNull(overall);

        var col1Stats = findCollectivityStats(overall, "col-1");
        assertNotNull(col1Stats, "Statistiques globales de col-1 introuvables");
        assertNotNull(col1Stats.overallMemberAssiduityPercentage);
        log.info("col-1 assiduité globale 03-04/2026 : " + col1Stats.overallMemberAssiduityPercentage);

        assertEquals(75.0, col1Stats.overallMemberAssiduityPercentage, 0.01,
                "col-1 mars-avril : 12 présences / 16 total (8 membres × 2 activités) = 75 %");
    }

    @Test
    void col3_overall_assiduity_march_to_april() {
        // act-6 sur 2 occurrences (Tableaux 29 + 30) :
        //   Mars  (06/03) : 6 présents / 8  → M1-M6 ✓, M7-M8 ✗
        //   Avril (03/04) : 5 présents / 8  → M1/M2/M5/M6/M8 ✓, M3/M4/M7 ✗
        //                   (C1-M1 external non comptabilisé)
        //   Total : 11 présences / 16 = 68,75 %
        var from = LocalDate.of(2026, 3, 1);
        var to = LocalDate.of(2026, 4, 30);

        var overall = apiClient.getCollectivitiesOverallStatistics(from, to);
        assertNotNull(overall);

        var col3Stats = findCollectivityStats(overall, "col-3");
        assertNotNull(col3Stats, "Statistiques globales de col-3 introuvables");
        assertNotNull(col3Stats.overallMemberAssiduityPercentage);
        log.info("col-3 assiduité globale 03-04/2026 : " + col3Stats.overallMemberAssiduityPercentage);

        assertEquals(68.75, col3Stats.overallMemberAssiduityPercentage, 0.01,
                "col-3 mars-avril : 11 présences / 16 total = 68,75 %");
    }

    // =========================================================
    // Helpers
    // =========================================================

    private void assertAssiduity(List<CollectivityLocalStatistics> stats,
                                  String memberId, double expected) {
        var entry = stats.stream()
                .filter(s -> s.memberDescription != null && memberId.equals(s.memberDescription.id))
                .findFirst();
        assertTrue(entry.isPresent(), "Aucune statistique trouvée pour le membre " + memberId);
        assertNotNull(entry.get().assiduityPercentage,
                "assiduityPercentage null pour le membre " + memberId);
        assertEquals(expected, entry.get().assiduityPercentage, 0.01,
                "Taux d'assiduité inattendu pour " + memberId
                        + " : attendu=" + expected + " %, obtenu=" + entry.get().assiduityPercentage + " %");
    }

    private CollectivityOverallStatistics findCollectivityStats(
            List<CollectivityOverallStatistics> overall, String collectivityId) {
        var collectivity = apiClient.getCollectivity(collectivityId);
        return overall.stream()
                .filter(s -> s.collectivityInformation != null
                        && collectivity.name != null
                        && collectivity.name.equals(s.collectivityInformation.name))
                .findFirst()
                .orElse(null);
    }
}
